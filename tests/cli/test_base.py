import os

import pytest
from awxkit import config

from tests.cli.utils import format_error


@pytest.mark.yolo
class TestCLIBasics(object):

    def test_no_credentials(self, cli):
        # by default, awxkit will use localhost:8043,
        # which shouldn't be reachable in our CI environments
        result = cli(['awx'])
        assert result.returncode == 1, format_error(result)
        assert 'usage: awx' in result.stdout

    @pytest.mark.parametrize('args', [
        ['awx', '-h'],
        ['awx', '--help'],
    ])
    def test_plain_help_argument(self, cli, args):
        # by default, awxkit will use localhost:8043,
        # which shouldn't be reachable in our CI environments
        result = cli(args)
        assert result.returncode == 0, format_error(result)
        assert 'authenticate and retrieve an OAuth2 token' in result.stdout

    def test_anonymous_user(self, cli):
        result = cli(['awx', '-k', '--conf.host', config.base_url])

        # you can *see* endpoints without logging in
        for endpoint in ('login', 'config', 'ping', 'organizations'):
            assert endpoint in result.stdout

    def test_credentials_required(self, cli):
        result = cli(['awx', 'me', '-k', '--conf.host', config.base_url])
        assert result.returncode == 1, format_error(result)
        assert 'Valid credentials were not provided.' in result.stdout
        assert '$ awx login --help' in result.stdout

    def test_valid_credentials_from_env_vars(self, cli):
        username = config.credentials.users.admin.username
        result = cli(['awx', 'me'], env={
            'PATH': os.environ['PATH'],
            'TOWER_HOST': config.base_url,
            'TOWER_USERNAME': username,
            'TOWER_PASSWORD': config.credentials.users.admin.password,
            'TOWER_VERIFY_SSL': 'f',
        })
        assert result.returncode == 0, format_error(result)
        assert result.json['count'] == 1
        assert result.json['results'][0]['username'] == username

    def test_valid_credentials_from_cli_arguments(self, cli):
        username = config.credentials.users.admin.username
        result = cli([
            'awx', 'me',
            '-k', # SSL verify false
            '--conf.host', config.base_url,
            '--conf.username', username,
            '--conf.password', config.credentials.users.admin.password,
        ])
        assert result.returncode == 0, format_error(result)
        assert result.json['count'] == 1
        assert result.json['results'][0]['username'] == username

    def test_yaml_format(self, cli):
        result = cli(['awx', 'me', '-f', 'yaml'], auth=True)
        assert result.returncode == 0, format_error(result)
        assert result.yaml['count'] == 1
        assert result.yaml['results'][0]['username'] == config.credentials.users.admin.username  # noqa

    def test_human_format(self, cli):
        result = cli(['awx', 'me', '-f', 'human', '--filter', 'username'], auth=True)
        assert result.returncode == 0, format_error(result)
        assert result.stdout == 'username \n======== \nadmin    \n'

        # test https://github.com/ansible/awx/issues/4567
        result = cli(['awx', 'me', '-f', 'human', '--filter', 'summary_fields'], auth=True)
        assert result.returncode == 0, format_error(result)
        assert '{"user_capabilities": {"edit": true, "delete": false}}' in result.stdout

    def test_jq_custom_formatting(self, cli):
        result = cli(
            ['awx', 'me', '-f', 'jq', '--filter', '.results[].username'],
            auth=True
        )
        assert result.returncode == 0, format_error(result)
        assert result.stdout == config.credentials.users.admin.username + '\n'

    def test_verbose_requests(self, cli):
        # -v should print raw HTTP requests
        result = cli(['awx', 'users', 'list', '-v'], auth=True)
        assert result.returncode == 0, format_error(result)
        assert '"GET /api/v2/users/ HTTP/1.1" 200' in result.stdout

    def test_invalid_resource(self, cli):
        result = cli(['awx', 'taters'], auth=True)
        assert result.returncode == 2, format_error(result)
        assert "resource: invalid choice: 'taters'" in result.stdout

    def test_invalid_action(self, cli):
        result = cli(['awx', 'users', 'bodyslam'], auth=True)
        assert result.returncode == 2, format_error(result)
        assert "argument action: invalid choice: 'bodyslam'" in result.stdout

    def test_api_error(self, cli):
        """Assert there is a newline at end of error returned by API."""
        result = cli(['awx', 'settings', 'modify', 'MAX_UI_JOB_EVENTS', 'aardvark'], auth=True)
        assert result.returncode == 1, format_error(result)
        assert result.stdout == '{"MAX_UI_JOB_EVENTS": ["A valid integer is required."]}\n'

    def test_ping(self, cli):
        result = cli(['awx', 'ping'], auth=True)
        assert 'instances' in result.json

    def test_metrics(self, cli):
        result = cli(['awx', 'metrics'], auth=True)
        assert 'awx_system_info' in result.json