#!/usr/bin/env bash

set -euxo pipefail

VARS_FILE=${VARS_FILE:-playbooks/vars.yml}
AWX_UPGRADE=${AWX_UPGRADE:-false}

TOWER_CONTAINER_IMAGE=${TOWER_CONTAINER_IMAGE:-''}
MESSAGING_CONTAINER_IMAGE=${MESSAGING_CONTAINER_IMAGE:-''}
MEMCACHED_CONTAINER_IMAGE=${MEMCACHED_CONTAINER_IMAGE:-''}

OPENSHIFT_PASS=${OPENSHIFT_PASS:-''}
OPENSHIFT_PROJECT=${OPENSHIFT_PROJECT:-"tower-qe-$(date +'%s')"}
OPENSHIFT_ROUTE=${OPENSHIFT_ROUTE:-"https://ansible-tower-web-svc-${OPENSHIFT_PROJECT}.openshift.ansible.eng.rdu2.redhat.com"}


if [[ -z "${OPENSHIFT_PASS}" ]]; then
    >&2 echo "openshift_install.sh: Environment variable OPENSHIFT_PASS must be specified"
    exit 1
fi

# -- Start
#
# shellcheck source=lib/common
source "$(dirname "${0}")"/lib/common

setup_python3_env

openshift_login

if [[ "${AWX_UPGRADE}" == false ]]; then
    openshift_bootstrap_project "${OPENSHIFT_PROJECT}"
    cat << EOF > pvc.yml
---
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: postgresql-${OPENSHIFT_PROJECT}
  annotations:
    volume.beta.kubernetes.io/storage-class: "managed-nfs-storage"
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 2Gi
EOF
    oc create -f pvc.yml

fi

mkdir -p artifacts
echo "${OPENSHIFT_PROJECT}" > artifacts/openshift_project
cp "${VARS_FILE}" artifacts/vars.yml

AW_REPO_URL="$(retrieve_value_from_vars_file "${VARS_FILE}" aw_repo_url)\
$(retrieve_value_from_vars_file "${VARS_FILE}" awx_setup_path)"
AWX_ADMIN_PASSWORD="$(retrieve_value_from_vars_file "${VARS_FILE}" admin_password)"
TOWER_VERSION="$(retrieve_value_from_vars_file "${VARS_FILE}" tower_version)"


if [[ "${TOWER_VERSION}" == "devel" ]]; then
    _TOWER_VERSION='latest'
    # NOTE: there are no images for the devel dependencies, so use the latest release.
    # Update this after every release
    _TOWER_NAMESPACE='ansible-tower-35'
else
    _TOWER_VERSION="${TOWER_VERSION}"
    _TOWER_NAMESPACE="ansible-tower-${TOWER_VERSION:0:1}${TOWER_VERSION:2:1}"
fi

MESSAGING_CONTAINER_IMAGE_VERSION='latest'
if [[ -z "${MESSAGING_CONTAINER_IMAGE}" ]]; then
    MESSAGING_CONTAINER_IMAGE="registry.access.redhat.com/${_TOWER_NAMESPACE}/ansible-tower-messaging"
else
    _MESSAGING_CONTAINER_IMAGE_VERSION=$(echo "${MESSAGING_CONTAINER_IMAGE}" | cut -d'/' -f2 | awk -F: '{ print $2 }')
    if [[ -n "${_MESSAGING_CONTAINER_IMAGE_VERSION}" ]]; then
        MESSAGING_CONTAINER_IMAGE_VERSION="${_MESSAGING_CONTAINER_IMAGE_VERSION}"
        MESSAGING_CONTAINER_IMAGE=${MESSAGING_CONTAINER_IMAGE//:$_MESSAGING_CONTAINER_IMAGE_VERSION/}
    fi
fi

MEMCACHED_CONTAINER_IMAGE_VERSION='latest'
if [[ -z "${MEMCACHED_CONTAINER_IMAGE}" ]]; then
    MEMCACHED_CONTAINER_IMAGE="registry.access.redhat.com/${_TOWER_NAMESPACE}/ansible-tower-memcached"
else
    _MEMCACHED_CONTAINER_IMAGE_VERSION=$(echo "${MEMCACHED_CONTAINER_IMAGE}" | cut -d'/' -f2 | awk -F: '{ print $2 }')
    if [[ -n "${_MEMCACHED_CONTAINER_IMAGE_VERSION}" ]]; then
        MEMCACHED_CONTAINER_IMAGE_VERSION="${_MEMCACHED_CONTAINER_IMAGE_VERSION}"
        MEMCACHED_CONTAINER_IMAGE=${MEMCACHED_CONTAINER_IMAGE//:$_MEMCACHED_CONTAINER_IMAGE_VERSION/}
    fi
fi

TOWER_CONTAINER_IMAGE_VERSION='latest'
if [[ -z "${TOWER_CONTAINER_IMAGE}" ]]; then
    if [[ "${AW_REPO_URL}" =~ "releases.ansible.com" ]]; then
        TOWER_CONTAINER_IMAGE="registry.access.redhat.com/${_TOWER_NAMESPACE}/ansible-tower"
        TOWER_CONTAINER_IMAGE_VERSION="${_TOWER_VERSION}"
    else
        oc tag tower-qe/ansible-tower:"${_TOWER_VERSION}" ansible-tower:"${_TOWER_VERSION}"
        oc set image-lookup --all -n "${OPENSHIFT_PROJECT}"
        TOWER_CONTAINER_IMAGE='ansible-tower'
        TOWER_CONTAINER_IMAGE_VERSION="${_TOWER_VERSION}"
    fi
else
    _TOWER_CONTAINER_IMAGE_VERSION=$(echo "${TOWER_CONTAINER_IMAGE}" | cut -d'/' -f2 | awk -F: '{ print $2 }')
    if [[ -n "${_TOWER_CONTAINER_IMAGE_VERSION}" ]]; then
        TOWER_CONTAINER_IMAGE_VERSION="${_TOWER_CONTAINER_IMAGE_VERSION}"
        TOWER_CONTAINER_IMAGE=${TOWER_CONTAINER_IMAGE//:$_TOWER_CONTAINER_IMAGE_VERSION/}
    fi
fi

if [[ "${AWX_UPGRADE}" == true ]]; then
    rm -rf setup
fi

curl -s "${AW_REPO_URL}" | tar -xzf - --transform "s|^[^/]*|setup|"
cd setup
cat << EOF > vars.yml
---
openshift_host: https://console.openshift.ansible.eng.rdu2.redhat.com:8443
openshift_project: ${OPENSHIFT_PROJECT}
openshift_user: jenkins
openshift_skip_tls_verify: true
admin_password: ${AWX_ADMIN_PASSWORD}
pg_username: tower
pg_password: towerpass
secret_key: 'towersecret'
rabbitmq_password: 'password'
rabbitmq_erlang_cookie: 'cookiemonster'
openshift_pg_pvc_name: postgresql-${OPENSHIFT_PROJECT}
web_cpu_request: 250  # we lower these to not hit our cluster capacity limit
task_cpu_request: 500
rabbitmq_cpu_request: 250
memcached_cpu_request: 250
kubernetes_web_image: ${TOWER_CONTAINER_IMAGE}
kubernetes_web_version: ${TOWER_CONTAINER_IMAGE_VERSION}
kubernetes_task_image: ${TOWER_CONTAINER_IMAGE}
kubernetes_task_version: ${TOWER_CONTAINER_IMAGE_VERSION}
kubernetes_rabbitmq_image: ${MESSAGING_CONTAINER_IMAGE}
kubernetes_rabbitmq_version: ${MESSAGING_CONTAINER_IMAGE_VERSION}
kubernetes_memcached_image: ${MEMCACHED_CONTAINER_IMAGE}
kubernetes_memcached_version: ${MEMCACHED_CONTAINER_IMAGE_VERSION}
EOF

cp vars.yml ../artifacts/openshift_installer_vars.yml

./setup_openshift.sh -e openshift_password="${OPENSHIFT_PASS}" -e @vars.yml
until is_tower_ready "${OPENSHIFT_ROUTE}"; do sleep 10; done
oc scale sts "ansible-tower" --replicas=5


_TOWER_VERSION=$(curl -ks "${OPENSHIFT_ROUTE}"/api/v2/ping/ | python -c 'import json,sys; print(json.loads(sys.stdin.read())["version"])' | cut -d . -f 1-3)

echo "${_TOWER_VERSION}"
echo "${OPENSHIFT_ROUTE}" > tower_url

cat << EOF > ../playbooks/inventory.log
[local]
127.0.0.1 ansible_connection=local ansible_python_interpreter="/usr/bin/env python"

[tower]
${OPENSHIFT_ROUTE:8} ansible_connection=docker ansible_user=awx
EOF

cp tower_url ../artifacts/tower_url
cp ../playbooks/inventory.log ../artifacts/inventory.log