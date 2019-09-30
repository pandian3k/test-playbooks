#!/usr/bin/env bash
set -euxo pipefail

# HACK: jnlp container installs requests using yum. Remove yum installation so that it doesn't overlap with the following pip install
yum remove -y python-requests

# shellcheck source=lib/common
source "$(dirname "${0}")"/lib/common
setup_python3_env

pip install -Ur scripts/requirements.install
pip install -Ur requirements.txt

## default ssh public key
mkdir -p ~/.ssh/
cp $PUBLIC_KEY ~/.ssh/id_rsa.pub


ansible-vault decrypt --vault-password-file="${VAULT_FILE}" config/credentials.vault --output=config/credentials.yml


export ANSIBLE_HOST_KEY_CHECKING=False
export ANSIBLE_TIMEOUT=30
export ANSIBLE_PACKAGE_NAME="ansible"
export PYTHONUNBUFFERED=1


if [[ "${VERBOSE}" == true ]]; then
    VERBOSITY="-vvvv"
else
    VERBOSITY="-v"
fi


env


cat << EOF > install_vars.yml
---
admin_password: ${AWX_ADMIN_PASSWORD}
ansible_install_method: ${ANSIBLE_INSTALL_METHOD}
ansible_nightly_repo: ${ANSIBLE_NIGHTLY_REPO}/${ANSIBLE_BRANCH}
ansible_package_name: ${ANSIBLE_PACKAGE_NAME}
aw_repo_url: ${AW_REPO_URL}
awx_setup_path: ${AWX_SETUP_PATH}
create_ec2_wait_upon_creation: false
create_ec2_assign_public_ip: true
create_ec2_vpc_id: vpc-552da032
create_ec2_vpc_subnet_id: subnet-9cdddbb0
ec2_images: `scripts/image_deploy_vars.py --cloud_provider ${CLOUD_PROVIDER} --platform ${PLATFORM} --ansible_version ${ANSIBLE_BRANCH} --groups tower`
instance_name_prefix: ${INSTANCE_NAME_PREFIX}-ansible-${ANSIBLE_BRANCH}
minimum_var_space: 0
pg_password: ${AWX_PG_PASSWORD}
terminate_ec2_wait_upon_creation: false
EOF

cat install_vars.yml > initial_install_vars.yml && echo "delete_on_start: ${DELETE_ON_INSTALL}" >> initial_install_vars.yml

# Define a method to cleanup any running instances
function teardown {
   echo "### DESTROY ###"
   ansible-playbook ${VERBOSITY} -i playbooks/inventory.log -e @initial_install_vars.yml playbooks/reap-tower-ec2.yml || true
}
trap teardown EXIT


echo "### Install ###"
ansible-playbook ${VERBOSITY} -i playbooks/inventory -e @initial_install_vars.yml playbooks/deploy-tower.yml | tee 01-install.log


if [ "${LOAD_RESOURCES}" = true ]; then
    echo "### Load Tower ###"
    scripts/resource_loading/load_tower.py ${LOADING_ARGS} | tee 02-resource-loading.log
fi

echo "### BACKUP TOWER ###"
ansible tower -i playbooks/inventory.log -a "chdir=/tmp/setup ./setup.sh -b -e @vars.yml" -e ansible_become=true | tee 03-backup.log


echo "### TRANSFER BACKUP TO AGENT ###"
INSTALL_HOSTNAME=$(ansible tower -i playbooks/inventory.log --list-hosts | tail -n 1 | awk 'NR==1{print $1}')
INSTALL_USER=$(ansible tower -i playbooks/inventory.log -m debug -a "msg={{ ansible_ssh_user }}" | tail -n 2 | awk 'NR==1{print $2}' | sed -e 's/"//g')
rsync -e 'ssh -o StrictHostKeyChecking=no' -L ${INSTALL_USER}@${INSTALL_HOSTNAME}:/tmp/setup/tower-backup-latest.tar.gz .


echo "### INSTALL ON NEW INSTANCE ###"
ansible-playbook ${VERBOSITY} -i playbooks/inventory -e @install_vars.yml playbooks/deploy-tower.yml | tee 05-install.log


echo "### TRANSFER BACKUP TO INSTANCE ###"
INSTALL_HOSTNAME=$(ansible tower -i playbooks/inventory.log --list-hosts | tail -n 1 | awk 'NR==1{print $1}')
INSTALL_USER=$(ansible tower -i playbooks/inventory.log -m debug -a "msg={{ ansible_ssh_user }}" | tail -n 2 | awk 'NR==1{print $2}' | sed -e 's/"//g')
ansible tower -i playbooks/inventory.log -a "chmod a+w /tmp /tmp/setup" -e ansible_become=true
rsync -e 'ssh -o StrictHostKeyChecking=no' tower-backup-latest.tar.gz ${INSTALL_USER}@${INSTALL_HOSTNAME}:/tmp/setup/


echo "### RUN RESTORE ###"
ansible tower -i playbooks/inventory.log -a "chdir=/tmp/setup ./setup.sh -r -e @vars.yml" -e ansible_become=true | tee 06-restore.log


if [ "${VERIFY_RESOURCES}" = true ]; then
    echo "### VERIFY RESTORED RESOURCES ###"
    scripts/resource_loading/verify_loaded_tower.py ${VERIFICATION_ARGS} | tee 07-restored-resource-verification.log
fi