import json
import yaml

import pytest
import fauxfactory

import awxkit.exceptions as exc
from awxkit.config import config
from tests.api import APITest


class TestContainerGroups(APITest):

    @pytest.fixture(scope='class')
    def container_group_and_client(self, request, gke_client_cscope, class_factories, v2_class):
        client = gke_client_cscope(config.credentials)
        namespace = fauxfactory.gen_alphanumeric().lower()
        client.setup_container_group_namespace(namespace=namespace)
        request.addfinalizer(client.destroy_container_group_namespace)
        cred_type = v2_class.credential_types.get(namespace='kubernetes_bearer_token').results.pop()
        cred = class_factories.credential(
            credential_type=cred_type,
            inputs={
                'host': client.client.configuration.host,
                'verify_ssl': True,
                'ssl_ca_cert': client.cacrt,
                'bearer_token': client.serviceaccount_token
                }
            )
        ig = class_factories.instance_group(name=f'ContainerGroup - {namespace}')
        ig.credential = cred.id
        ig_options = v2_class.instance_groups.options()
        pod_spec = dict(ig_options.actions['POST']['pod_spec_override']['default'])
        pod_spec['metadata']['namespace'] = namespace
        ig.pod_spec_override = json.dumps(pod_spec)

        return ig, client

    @pytest.fixture(scope='function', params=['cacrt', 'token', 'host', 'image', 'role', 'entry_point'])
    def bad_container_group_and_client(self, request, gke_client_fscope, factories, v2):
        problem = request.param
        client = gke_client_fscope(config.credentials)
        namespace = f'bad-{fauxfactory.gen_alphanumeric().lower()}'
        if problem == 'role':
            client.setup_container_group_namespace(namespace=namespace, corrupt_role=True)
        else:
            client.setup_container_group_namespace(namespace=namespace)
        request.addfinalizer(client.destroy_container_group_namespace)
        cred_type = v2.credential_types.get(namespace='kubernetes_bearer_token').results.pop()
        cacrt = 'blah' if problem == 'cacrt' else client.cacrt
        host = 'broken.example.com' if problem == 'host' else client.client.configuration.host
        token = '230420934823broken928340983240' if problem == 'token' else client.serviceaccount_token
        cred = factories.credential(credential_type=cred_type, inputs={
            'host': host, 'verify_ssl': True, 'ssl_ca_cert': cacrt, 'bearer_token': token}
            )
        ig = factories.instance_group(name=f'ContainerGroup - {namespace}')
        ig.credential = cred.id
        ig_options = v2.instance_groups.options()
        pod_spec = dict(ig_options.actions['POST']['pod_spec_override']['default'])
        pod_spec['metadata']['namespace'] = namespace
        if problem == 'image':
            pod_spec['spec']['containers'][0]['image'] = 'nginx'
        if problem == 'entry_point':
            pod_spec['spec']['containers'][0]['args'] = ['sleep', '10']
        ig.pod_spec_override = json.dumps(pod_spec)

        return ig, client, problem

    def test_failed_job(self, bad_container_group_and_client, factories):
        container_group, client, problem = bad_container_group_and_client
        jt = factories.job_template(playbook='sleep.yml', extra_vars='{"sleep_interval": 45}')
        jt.add_instance_group(container_group)
        job = jt.launch().wait_until_completed()
        if problem == 'cacrt':
            assert 'OpenSSL.SSL.Error' in job.result_traceback
        elif problem == 'token':
            assert 'Unauthorized' in job.result_traceback
        elif problem == 'host':
            assert 'Name or service not known' in job.result_traceback
        elif problem == 'role':
            assert 'cannot create resource' in job.result_traceback
            assert 'forbidden' in job.result_traceback.lower()
        elif problem == 'image':
            assert 'rsync error' in job.result_traceback
        elif problem == 'entry_point':
            assert 'container not found' in job.result_traceback
        job.assert_status('error')
        assert job.instance_group == container_group.id, "Container group is not indicated that the job tried to run on"

    def test_launch_job(self, container_group_and_client, factories):
        container_group, client = container_group_and_client
        inventory = factories.inventory()
        host = inventory.add_host()
        jt = factories.job_template(inventory=inventory)
        jt.add_instance_group(container_group)
        # TODO use sleep playbook and confirm a pod spins up on gke using client
        job = jt.launch().wait_until_completed()
        job.assert_successful()
        assert job.instance_group == container_group.id
        assert host.name in job.result_stdout

    def test_workflow_job(self, container_group_and_client, factories):
        container_group, client = container_group_and_client
        org = factories.organization()
        org.add_instance_group(container_group)
        inventory = factories.inventory(organization=org)
        host = inventory.add_host()
        jt = factories.job_template(inventory=inventory)
        wfjt = factories.workflow_job_template(organization=org)
        n1 = factories.workflow_job_template_node(workflow_job_template=wfjt, unified_job_template=jt)
        # TODO use sleep playbook and confirm a pod spins up on gke using client
        wf_job = wfjt.launch().wait_until_completed()
        wf_job.assert_successful()
        n1_job_node = wf_job.related.workflow_nodes.get(unified_job_template=jt.id).results.pop()
        n1_job = n1_job_node.wait_for_job().related.job.get()
        assert n1_job.instance_group == container_group.id

    def test_launch_project_update(self, container_group_and_client, factories):
        container_group, client = container_group_and_client
        org = factories.organization()
        org.add_instance_group(container_group)
        proj = factories.project(organization=org)
        update = proj.update().wait_until_completed()
        # TODO use sleep playbook and confirm a pod spins up on gke using client
        update.assert_successful()
        update.summary_fields.instance_group.id == container_group.id

    def test_launch_adhoc(self, container_group_and_client, factories):
        container_group, client = container_group_and_client
        organization = factories.organization()
        organization.add_instance_group(container_group)
        inv = factories.inventory(organization=organization)
        host = inv.add_host()
        adhoc = factories.ad_hoc_command(inventory=inv)
        # TODO use sleep and confirm a pod spins up on gke using client
        adhoc.wait_until_completed(timeout=90)
        adhoc.assert_successful()
        adhoc.summary_fields.instance_group.id == container_group.id
        assert host.name in adhoc.result_stdout


    def test_launch_cloud_inventory_update(self, container_group_and_client, aws_inventory_source):
        container_group, client = container_group_and_client
        organization = aws_inventory_source.related.inventory.get().related.organization.get()
        organization.add_instance_group(container_group)
        # TODO use sleep and confirm a pod spins up on gke using client
        update = aws_inventory_source.update().wait_until_completed()
        update.assert_successful()
        update.summary_fields.instance_group.id == container_group.id
