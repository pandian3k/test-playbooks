pipeline {

    agent { label 'jenkins-jnlp-agent' }

    parameters {
        choice(
            name: 'PRODUCT',
            description: 'Product to deploy',
            choices: ['awx', 'tower']
        )
        choice(
            name: 'SCENARIO',
            description: 'Deployment scenario for Tower',
            choices: ['standalone', 'cluster']
        )
        choice(
            name: 'AWX_USE_TLS',
            description: 'Should the network services (postgresql, rabbitmq and nginx) use TLS (with custom CA issued certificates)',
            choices: ['no', 'yes']
        )
        string(
            name: 'TOWER_FORK',
            description: 'Fork of tower to deploy',
            defaultValue: 'ansible'
        )
        string(
            name: 'TOWER_BRANCH',
            description: 'Branch to use for Tower',
            defaultValue: 'devel'
        )
        string(
            name: 'TOWER_PACKAGING_FORK',
            description: 'Fork of tower-packaging to deploy',
            defaultValue: 'ansible'
        )
        string(
            name: 'TOWER_PACKAGING_BRANCH',
            description: 'Branch to use for tower-packaging',
            defaultValue: 'devel'
        )
        string(
            name: 'TOWER_LICENSE_FORK',
            description: 'Fork of tower-license to deploy',
            defaultValue: 'ansible'
        )
        string(
            name: 'TOWER_LICENSE_BRANCH',
            description: 'Branch to use for tower-license',
            defaultValue: 'master'
        )
        string(
            name: 'TOWER_QA_FORK',
            description: 'Fork of tower-qa. Useful for testing changes to this pipeline.',
            defaultValue: 'ansible'
        )
        string(
            name: 'TOWER_QA_BRANCH',
            description: 'Branch to use for tower-qa',
            defaultValue: 'devel'
        )
        string(
            name: 'TOWERKIT_FORK',
            description: 'Fork to use for awxkit, or towerkit on older versions (depends on test.sh in you branch). Blank will default to TOWER_FORK',
            defaultValue: ''
        )
        string(
            name: 'TOWERKIT_BRANCH',
            description: 'Branch to use for awxkit, or  towerkit on older versions (depends on test.sh in your branch). Blank will default to TOWER_BRANCH',
            defaultValue: ''
        )
        string(
            name: 'AWXKIT_REPO',
            description: 'Repo to use for awxkit, when left blank, defaults to PRODUCT. No-op for versions prior to 3.6.0 where there was no awxkit.',
            defaultValue: ''
        )
        string(
            name: 'RUNNER_FORK',
            description: 'Fork of ansible-runner to deploy (Leave empty to rely on latest RPM)',
            defaultValue: ''
        )
        string(
            name: 'RUNNER_BRANCH',
            description: 'Branch to use for ansible-runner (Leave empty to rely on latest RPM)',
            defaultValue: ''
        )
        booleanParam(
            name: 'BUILD_INSTALLER_AND_PACKAGE',
            description: 'Should the installer and the packages be built as part of this pipeline ?',
            defaultValue: true
        )
        booleanParam(
            name: 'BUILD_OFFLINE',
            description: 'Simulate an internal (Brew) build.',
            defaultValue: false
        )
        booleanParam(
            name: 'RUN_INSTALLER',
            description: 'Should the installer be run as part of this pipeline ?',
            defaultValue: true
        )
        booleanParam(
            name: 'RUN_TESTS',
            description: 'Should the integration test suite be run as part of this pipeline ?',
            defaultValue: true
        )
        booleanParam(
            name: 'RUN_E2E',
            description: 'Should the e2e test suite be run as part of this pipeline ?',
            defaultValue: true
        )
        booleanParam(
            name: 'RUN_CLI_TESTS',
            description: 'Should the CLI test suite be run as part of this pipeline ?',
            defaultValue: true
        )
        booleanParam(
            name: 'RETRY_FAILED_TESTS',
            description: 'Should e2e tests be retried on failure ?',
            defaultValue: true
        )
        string(
            name: 'TESTEXPR',
            description: """\
            Test expression to pass to pytest to select which tests will be run.
            Use "yolo or ansible_integration" (default) to run YOLO.
            Use "test" to run SLOWYO (note that this will use a bigger EC2 instance to run tests quickly if SCENARIO is standalone and RUN_TESTS is checked. Because of that, the deployed instance will always be torn down).
            """,
            defaultValue: 'yolo or ansible_integration'
        )
        string(
            name: 'E2E_TESTEXPR',
            description: 'Specify the --filter flag for UI E2E tests if necessary',
            defaultValue: '*'
        )
        booleanParam(
            name: 'E2E_RUN_EXTERNAL_GRID',
            description: 'Select to run e2e tests against an external selenium grid',
            defaultValue: false
        )
        choice(
            name: 'PLATFORM',
            description: 'The OS to install the Tower instance on',
            choices: ['rhel-7.7-x86_64', 'rhel-7.6-x86_64', 'rhel-7.5-x86_64', 'rhel-7.4-x86_64',
                      'rhel-8.0-x86_64', 'ol-7.6-x86_64', 'centos-7.latest-x86_64',
                      'ubuntu-16.04-x86_64', 'ubuntu-14.04-x86_64']
        )
        choice(
            name: 'ANSIBLE_NIGHTLY_BRANCH',
            description: 'The Ansible version to install the Tower instance with',
            choices: ['devel', 'stable-2.9', 'stable-2.8', 'stable-2.7', 'stable-2.6', 'stable-2.5', 'stable-2.4', 'stable-2.3']
        )
        string(
            name: 'SLACK_USERNAME',
            description: """\
            Send slack DM on completion. Use space separate list to sent to multiple people, for example: @johill @elyezer.
            The message sent will follow the parrot code: party_parrot (green, pipeline succeed and if tests were run everthing is passing), confusedparrot (yellow, tests were run and had failures) and sad_parrot (red, something went wrong with the pipeline).
            """,
            defaultValue: '#jenkins'
        )
        booleanParam(
            name: 'TEARDOWN_INSTANCE_ON_SUCCESS',
            description: """\
            Will teardown the Tower instance if the pipeline succeeds.
            This will only happen when RUN_TESTS and/or RUN_E2E are selected.
            Note: the EC2 instance that runs pytest is cleaned up immediately after yolo completes.
            """,
            defaultValue: true
        )
        string(
            name: 'COMMENTS',
            description: 'Custom message summarizing the purpose of this yolo job for the Slack Message',
            defaultValue: 'yolo'
        )
    }

    options {
        timestamps()
        buildDiscarder(logRotator(daysToKeepStr: '30'))
        timeout(time: 18, unit: 'HOURS')
    }

    stages {
        stage('Build Information') {
            steps {
                script {
                    if (params.TOWER_BRANCH == 'devel' && params.TOWER_PACKAGING_BRANCH == 'devel') {
                        NIGHTLY_REPO_DIR = 'devel'
                    } else {
                        NIGHTLY_REPO_DIR = "tower_${params.TOWER_BRANCH}_packaging_${params.TOWER_PACKAGING_BRANCH}"
                    }

                    if (params.PLATFORM == 'rhel-8.0-x86_64') {
                        target_dist = 'epel-8-x86_64'
                        mock_cfg = 'rhel-8-x86_64'
                    } else {
                        target_dist = 'epel-7-x86_64'
                        mock_cfg = 'rhel-7-x86_64'
                    }
                }
            }
        }

        stage('Build Tower and deploy test-runner') {
            failFast true
            parallel {
                stage('Build Installer') {
                    when {
                        expression {
                            return params.BUILD_INSTALLER_AND_PACKAGE
                        }
                    }

                    steps {
                        build(
                            job: 'Build_Tower_TAR',
                            parameters: [
                                string(
                                    name: 'TOWER_PACKAGING_REPO',
                                    value: "git@github.com:${params.TOWER_PACKAGING_FORK}/tower-packaging.git"
                                ),
                                string(
                                    name: 'TOWER_PACKAGING_BRANCH',
                                    value: "origin/${params.TOWER_PACKAGING_BRANCH}"
                                ),
                                string(
                                    name: 'TOWER_REPO',
                                    value: "git@github.com:${params.TOWER_FORK}/${params.PRODUCT}.git"
                                ),
                                string(
                                    name: 'TOWER_BRANCH',
                                    value: "origin/${params.TOWER_BRANCH}"
                                ),
                                string(
                                    name: 'NIGHTLY_REPO_DIR',
                                    value: NIGHTLY_REPO_DIR
                                )
                            ]
                        )
                    }
                }

                stage('Build Package') {
                    when {
                        expression {
                            return params.BUILD_INSTALLER_AND_PACKAGE
                        }
                    }

                    steps {
                        script {
                            if ( params.PLATFORM.contains('ubuntu') ) {
                                PACKAGE_JOB_NAME = 'Build_Tower_DEB'
                            } else {
                                PACKAGE_JOB_NAME = 'Build_Tower_RPM'
                            }

                            build(
                                job: PACKAGE_JOB_NAME,
                                parameters: [
                                    string(
                                        name: 'TOWER_PACKAGING_REPO',
                                        value: "git@github.com:${params.TOWER_PACKAGING_FORK}/tower-packaging.git"
                                    ),
                                    string(
                                        name: 'TOWER_PACKAGING_BRANCH',
                                        value: "origin/${params.TOWER_PACKAGING_BRANCH}"
                                    ),
                                    string(
                                        name: 'TOWER_REPO',
                                        value: "git@github.com:${params.TOWER_FORK}/${params.PRODUCT}.git"
                                    ),
                                    string(
                                        name: 'TOWER_BRANCH',
                                        value: "origin/${params.TOWER_BRANCH}"
                                    ),
                                    string(
                                        name: 'NIGHTLY_REPO_DIR',
                                        value: NIGHTLY_REPO_DIR
                                    ),
                                    string(
                                        name: 'TARGET_DIST',
                                        value: target_dist
                                    ),
                                    string(
                                        name: 'MOCK_CFG',
                                        value: mock_cfg
                                    ),
                                    booleanParam(
                                        name: 'TRIGGER',
                                        value: false
                                    ),
                                    booleanParam(
                                        name: 'BUILD_OFFLINE',
                                        value: params.BUILD_OFFLINE
                                    ),
                                    string(
                                        name: 'TOWER_LICENSE_REPO',
                                        value: "git@github.com:${params.TOWER_LICENSE_FORK}/tower-license.git"
                                    ),
                                    string(
                                        name: 'TOWER_LICENSE_BRANCH',
                                        value: params.TOWER_LICENSE_BRANCH
                                    )
                                ]
                            )
                        }
                    }
                }

                stage('Deploy test-runner node') {
                    when {
                        expression {
                            return params.RUN_INSTALLER
                        }
                    }

                    steps {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: "*/${params.TOWER_QA_BRANCH}" ]],
                            userRemoteConfigs: [
                                [
                                    credentialsId: 'd2d4d16b-dc9a-461b-bceb-601f9515c98a',
                                    url: "git@github.com:${params.TOWER_QA_FORK}/tower-qa.git"
                                ]
                            ]
                        ])

                        script {
                            if (params.RUNNER_FORK != '' && params.RUNNER_BRANCH != '') {
                                AWX_ANSIBLE_RUNNER_URL = "https://github.com/${params.RUNNER_FORK}/ansible-runner.git@${params.RUNNER_BRANCH}"
                            } else {
                                AWX_ANSIBLE_RUNNER_URL = ''
                            }
                        }

                        withCredentials([file(credentialsId: '171764d8-e57c-4332-bff8-453670d0d99f', variable: 'PUBLIC_KEY'),
                                        file(credentialsId: 'abcd0260-fb83-404e-860f-f9697911a0bc', variable: 'VAULT_FILE'),
                                        file(credentialsId: '86ed99e9-dad9-49e9-b0db-9257fb563bad', variable: 'JSON_KEY_FILE'),
                                        string(credentialsId: 'aws_access_key', variable: 'AWS_ACCESS_KEY'),
                                        string(credentialsId: 'aws_secret_key', variable: 'AWS_SECRET_KEY')]) {
                            withEnv(["AWS_SECRET_KEY=${AWS_SECRET_KEY}",
                                     "AWS_ACCESS_KEY=${AWS_ACCESS_KEY}",
                                     "AWX_ANSIBLE_RUNNER_URL=${AWX_ANSIBLE_RUNNER_URL}",
                                     "AWX_USE_TLS=${AWX_USE_TLS}",
                                     "SCENARIO=${SCENARIO}",
                                     "PLATFORM=${PLATFORM}",
                                     "ANSIBLE_VERSION=${ANSIBLE_NIGHTLY_BRANCH}",
                                     "DEPLOYMENT_NAME=yolo-build-${env.BUILD_ID}",
                                     "AW_REPO_URL=http://nightlies.testing.ansible.com/ansible-tower_nightlies_m8u16fz56qr6q7/${NIGHTLY_REPO_DIR}"]) {
                                sshagent(credentials : ['d2d4d16b-dc9a-461b-bceb-601f9515c98a']) {
                                    sh 'mkdir -p ~/.ssh && cp ${PUBLIC_KEY} ~/.ssh/id_rsa.pub'
                                    sh 'cp ${JSON_KEY_FILE} json_key_file'
                                    sh 'ansible-vault decrypt --vault-password-file="${VAULT_FILE}" config/credentials.vault --output=config/credentials.yml'
                                    sh 'ansible-vault decrypt --vault-password-file="${VAULT_FILE}" config/credentials-pkcs8.vault --output=config/credentials-pkcs8.yml || true'

                                    // Generate variable file for test runner
                                    sh 'SCENARIO=test_runner ./tools/jenkins/scripts/generate_vars.sh'

                                    // Generate variable file for tower deployment
                                    sh './tools/jenkins/scripts/generate_vars.sh'

                                    // Archive the admin_password and show to the user
                                    sh 'mkdir -p artifacts'
                                    sh 'grep "admin_password" "playbooks/vars.yml" | awk \'{ print $2 }\' | tee artifacts/admin_password'

                                    // Update the credentials files before deploying the test runner so that the credentials files are copied with the random generated admin password in place
                                    sh 'sed -i "s/default: &id001 {password: fo0m4nchU,/default: \\&id001 {password: $(cat artifacts/admin_password),/" config/credentials.yml'
                                    sh 'sed -i "s/default: &id001 {password: fo0m4nchU,/default: \\&id001 {password: $(cat artifacts/admin_password),/" config/credentials-pkcs8.vault'

                                    sh 'ansible-playbook -v -i playbooks/inventory -e @playbooks/test_runner_vars.yml playbooks/deploy-test-runner.yml'

                                    // Archive test runner inventory file and show it to user so they can optionally shell in
                                    sh 'cat playbooks/inventory.test_runner | tee artifacts/inventory.test_runner'
                                    sh 'grep -A 1 test-runner playbooks/inventory.test_runner | tail -n 1 | cut -d" " -f1 > artifacts/test_runner_host'

                                    sh "ansible test-runner -i playbooks/inventory.test_runner -m git -a 'repo=git@github.com:${params.TOWER_QA_FORK}/tower-qa version=${params.TOWER_QA_BRANCH} dest=tower-qa ssh_opts=\"-o StrictHostKeyChecking=no\" force=yes'"
                                }
                            }
                        }

                        script {
                            ADMIN_PASSWORD = readFile('artifacts/admin_password').trim()
                            SSH_OPTS = '-o ForwardAgent=yes -o StrictHostKeyChecking=no'
                            TEST_RUNNER_HOST = readFile('artifacts/test_runner_host').trim()
                        }
                    }
                }
            }
        }

        stage('Install Tower') {
            when {
                expression {
                    return params.RUN_INSTALLER
                }
            }

            steps {
                sshagent(credentials : ['d2d4d16b-dc9a-461b-bceb-601f9515c98a']) {
                    sh "ssh ${SSH_OPTS} ec2-user@${TEST_RUNNER_HOST} 'cd tower-qa && ./tools/jenkins/scripts/install.sh'"
                    sh 'ansible-playbook -v -i playbooks/inventory.test_runner playbooks/test_runner/run_fetch_artifacts.yml'
                }
            }
        }

        stage('Run Integration Tests') {
            when {
                expression {
                    return params.RUN_TESTS
                }
            }

            steps {
                sshagent(credentials : ['d2d4d16b-dc9a-461b-bceb-601f9515c98a']) {
                    sh "ssh ${SSH_OPTS} ec2-user@${TEST_RUNNER_HOST} 'cd tower-qa && TESTEXPR=\"${params.TESTEXPR}\" TOWERKIT_FORK=\"${params.TOWERKIT_FORK}\" TOWERKIT_BRANCH=\"${params.TOWERKIT_BRANCH}\" PRODUCT=\"${params.PRODUCT}\" AWXKIT_REPO=\"${params.AWXKIT_REPO}\" TOWER_FORK=\"${params.TOWER_FORK}\" TOWER_BRANCH=\"${params.TOWER_BRANCH}\" ./tools/jenkins/scripts/test.sh'"
                    sh 'ansible-playbook -v -i playbooks/inventory.test_runner playbooks/test_runner/run_fetch_artifacts_test.yml'
                    junit allowEmptyResults: true, testResults: 'artifacts/results.xml'
                }
            }

        }
        stage('Run CLI Tests') {
            when {
                expression {
                    return params.RUN_CLI_TESTS
                }
            }

            steps {
                sshagent(credentials : ['d2d4d16b-dc9a-461b-bceb-601f9515c98a']) {
                    sh "ssh ${SSH_OPTS} ec2-user@${TEST_RUNNER_HOST} 'cd tower-qa && TESTEXPR=\"${params.TESTEXPR}\" TOWERKIT_FORK=\"${params.TOWERKIT_FORK}\" TOWERKIT_BRANCH=\"${params.TOWERKIT_BRANCH}\" PRODUCT=\"${params.PRODUCT}\" AWXKIT_REPO=\"${params.AWXKIT_REPO}\" TOWER_FORK=\"${params.TOWER_FORK}\" TOWER_BRANCH=\"${params.TOWER_BRANCH}\" ./tools/jenkins/scripts/test-cli.sh'"
                    sh 'ansible-playbook -v -i playbooks/inventory.test_runner playbooks/test_runner/run_fetch_artifacts_test_cli.yml'
                    junit allowEmptyResults: true, testResults: 'artifacts/results-cli.xml'
                }
            }
        }

        stage('Run E2E Tests') {
            when {
                expression {
                    return params.RUN_E2E
                }
            }

            steps {
                script {
                    AWX_E2E_URL = readFile('artifacts/tower_url').trim()

                    if  (params.RETRY_FAILED_TESTS) {
                        env.RETRY_E2E_STAGE_COUNT = "2"
                        env.E2E_RETRIES = "2"
                    }
                    else {
                        env.RETRY_E2E_COUNT = "0"
                        env.E2E_RETRIES = "0"
                    }

                    retry(env.RETRY_E2E_STAGE_COUNT) {
                        build(
                            job: 'Test_Tower_E2E',
                            parameters: [
                                string(
                                    name: 'E2E_URL',
                                    value: "${AWX_E2E_URL}"
                                ),
                                string(
                                    name: 'DEPLOYMENT_TYPE',
                                    value: "${params.PRODUCT}"
                                ),
                                string(
                                    name: 'E2E_FORK',
                                    value: "${params.TOWER_FORK}"
                                ),
                                string(
                                    name: 'E2E_BRANCH',
                                    value: "${params.TOWER_BRANCH}"
                                ),
                                string(
                                    name: 'E2E_SCRIPT_BRANCH',
                                    value: "${params.TOWER_QA_BRANCH}"
                                ),
                                password(
                                    name: 'E2E_PASSWORD',
                                    value: "${ADMIN_PASSWORD}",
                                ),
                                string(
                                    name: 'E2E_TEST_SELECTION',
                                    value: "${params.E2E_TESTEXPR}"
                                ),
                                string(
                                    name: 'E2E_RETRIES',
                                    value: "${env.E2E_RETRIES}"
                                ),
                                booleanParam(
                                    name: 'E2E_RUN_EXTERNAL_GRID',
                                    value: "${params.E2E_RUN_EXTERNAL_GRID}"
                                ),
                            ],
                            propagate: true
                        )
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                if (params.RUN_INSTALLER || params.RUN_TESTS || params.RUN_E2E) {
                    sshagent(credentials : ['d2d4d16b-dc9a-461b-bceb-601f9515c98a']) {
                        sh "ssh ${SSH_OPTS} ec2-user@${TEST_RUNNER_HOST} 'cd tower-qa && ./tools/jenkins/scripts/collect.sh' || true" // Continue on even if this fails so we can archive anything available
                        sh 'ansible-playbook -v -i playbooks/inventory.test_runner playbooks/test_runner/run_fetch_artifacts_tower.yml || true' // Continue on even if this fails so we can archive anything available
                    }
                }
            }

            archiveArtifacts allowEmptyArchive: true, artifacts: 'artifacts/*'
        }
        success {
            slackSend(
                botUser: false,
                color: "good",
                teamDomain: "ansible",
                channel: "${SLACK_USERNAME}",
                message: genSlackMessage('party_parrot')
            )

            script {
                if (params.TEARDOWN_INSTANCE_ON_SUCCESS && (params.RUN_TESTS || params.RUN_E2E) && params.TESTEXPR != 'test') {
                    sshagent(credentials : ['d2d4d16b-dc9a-461b-bceb-601f9515c98a']) {
                        sh "ssh ${SSH_OPTS} ec2-user@${TEST_RUNNER_HOST} 'cd tower-qa && ./tools/jenkins/scripts/cleanup.sh'"
                    }
                }
            }
        }
        failure {
            slackSend(
                botUser: false,
                color: "danger",
                teamDomain: "ansible",
                channel: "${SLACK_USERNAME}",
                message: genSlackMessage('sad_parrot')
            )
        }
        unstable {
            slackSend(
                botUser: false,
                color: "warning",
                teamDomain: "ansible",
                channel: "${SLACK_USERNAME}",
                message: genSlackMessage('confusedparrot')
            )
        }
        cleanup {
            script {
                if (params.TESTEXPR == 'test' && params.SCENARIO == 'standalone'  && params.RUN_TESTS) {
                    sshagent(credentials : ['d2d4d16b-dc9a-461b-bceb-601f9515c98a']) {
                        sh "ssh ${SSH_OPTS} ec2-user@${TEST_RUNNER_HOST} 'cd tower-qa && ./tools/jenkins/scripts/cleanup.sh'"
                    }
                }

                if (params.RUN_INSTALLER) {
                    sshagent(credentials : ['d2d4d16b-dc9a-461b-bceb-601f9515c98a']) {
                        sh 'ansible-playbook -v -i playbooks/inventory -e @playbooks/test_runner_vars.yml playbooks/reap-tower-ec2.yml'
                    }
                }
            }
        }
    }
}

def genSlackMessage (parrot) {
    message = "<${env.RUN_DISPLAY_URL}|yolo #${env.BUILD_ID}> took ${currentBuild.durationString.replace(' and counting', '')} and is :${parrot}:\n"

    if (fileExists('artifacts/tower_url')) {
        message += "Instance URL - ${readFile('artifacts/tower_url').trim()}\n"
    } else {
        message += 'Instance URL - N/A\n'
    }

    if (fileExists('artifacts/admin_password')) {
        message += "Admin password - <${env.BUILD_URL}artifact/artifacts/admin_password/*view*/|admin_password>\n"
    }

    message += """\
        Platform - ${params.PLATFORM}
        Product - ${params.PRODUCT} - ${params.TOWER_FORK}/${params.TOWER_BRANCH}
        Scenario - ${params.SCENARIO}
        Tower-Packaging - ${params.TOWER_PACKAGING_FORK}/${params.TOWER_PACKAGING_BRANCH}
        Tower-QA - ${params.TOWER_QA_FORK}/${params.TOWER_QA_BRANCH}
        Ansible Version - ${params.ANSIBLE_NIGHTLY_BRANCH}
    """.stripIndent()

    if (params.RUN_TESTS) {
        message += "Test Expression - ${params.TESTEXPR}\n"
    }
    if (params.RUN_E2E) {
        message += "E2E Test Expression - ${params.E2E_TESTEXPR}\n"
    }

    message += "Comments - ${params.COMMENTS}\n"
    return message
}