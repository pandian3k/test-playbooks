# Tower 3.4.0 Release Test Plan

## Resources
* 6 full-time engineers (yanis, mat, john, jim, elijah, danny)
* Initial planning document for whole engineering org (api + ui + qe) [here](https://docs.google.com/spreadsheets/d/1Dc287lghj1CYR24s853671l-P5RXtpwZNcir0olt5Zc/edit#gid=161330338)

## Features Tested

### Auto-splitting of jobs (mat + john)
* [Feature]()
* [API Test Plan](https://github.com/ansible/tower-qa/blob/devel/docs/test_plans/features/34_job_slicing.md)
* [UI Test Plan](https://github.com/ansible/tower-qa/blob/devel/docs/test_plans/features/34_job_slicing_ui.md)
- [x] Testing complete

### Workflow Convergence Node (jim + elijah + danny)
* [Feature]()
* [API Test Plan](https://github.com/ansible/tower-qa/blob/devel/docs/test_plans/features/34_workflow_convergence.md)
* [UI Test Plan](https://docs.google.com/document/d/1U9VgxNoTw6CPpWbqKPomglmAK50xW3IaCif_BKZNc2o)
- [x] Testing complete

### Workflow-Level Inventory (elijah + john)
* [Feature]()
* [API + UI Test Plan](https://github.com/ansible/tower-qa/blob/devel/docs/test_plans/features/34_workflow_level_inventory.md)
- [x] Testing complete

### Workflows within Workflows (yanis + john)
* [Feature]()
* [API + UI Test Plan](https://github.com/ansible/tower-qa/blob/devel/docs/test_plans/features/34_workflow_in_workflow.md)
- [x] Testing complete

### Workflows Always Nodes Allowed in Conjunction With Other Nodes (elijah + danny)
* [Feature]()
* [API + UI Test Plan](https://github.com/ansible/tower-qa/blob/devel/docs/test_plans/features/34_always_nodes_allowed_with_other_nodes.md)
- [x] Testing complete

### Source all Content from releases.ansible.com (yanis) (packaging only)
* [Feature]()
* [Test Plan](https://github.com/ansible/tower-qa/blob/devel/docs/test_plans/packaging/34-ensure-no-third-party-packages.md)
- [x] Testing complete


### Finish Organization Permission Views (danny) (ui only)
* [Feature]()
* [UI Test Plan](https://docs.google.com/document/d/18azadvf-9dqC39Ri-By6IiE_eUt2bu9rPX6WjRBjgic)
- [x] Testing complete

### Support FIPS mode on RHEL 7 and CentOS 7 (yanis) (api only)
* [Feature]()
* [API Test Plan](https://github.com/ansible/tower-qa/blob/devel/docs/test_plans/features/34_fips_compliant.md)
- [x] Testing complete

### Replace Celery with Dispatcher (jim) (api only)
* [Feature]()
* [API Test Plan](https://github.com/ansible/tower-qa/blob/devel/docs/test_plans/features/34_celery_replacement.md)
- [x] Testing complete

### Settings Menu Reorganization (john) (ui only)
* [Feature]()
* [UI Test Plan](https://docs.google.com/document/d/1bZEUe6FW-gKY4y5tfcDdUwbRH2UdxxutMqZwYtww4lw)
- [x] Testing complete

## Regression
1. [x] [UI regression completed](https://docs.google.com/document/d/153nKe65KhYnCmqoZAE6f3kAPW3762v8O8sly6QwdaJM/)
1. [x] [API regression completed - standalone](http://jenkins.ansible.eng.rdu2.redhat.com/job/Test_Tower_Integration/ANSIBLE_NIGHTLY_BRANCH=stable-2.7,PLATFORM=rhel-7.6-x86_64,label=jenkins-jnlp-agent/4605/)
1. [x] [API regression completed - traditional cluster](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Integration_Cluster/ANSIBLE_NIGHTLY_BRANCH=stable-2.7,PLATFORM=rhel-7.6-x86_64,label=jenkins-jnlp-agent/1087/)
1. [x] [API regression completed - OpenShift](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_OpenShift_Integration/373/)
1. [x] Tower social authentication regression completed (vm)
   - [x] Google OAuth
   - [x] GitHub
   - [x] GitHub Org
   - [x] GitHub Team
   - [x] Azure OAuth
   - [x] Radius
1. [x] Tower SAML integration regression completed (vm)
1. [x] Tower social authentication regression completed (OpenShift)
   - [x] Google OAuth
   - [x] GitHub
   - [x] GitHub Org
   - [x] GitHub Team
   - [x] Azure OAuth
   - [x] Radius
1. [x] Tower SAML integration regression completed (OpenShift)
1. [x] Logging regression completed - standalone
1. [x] Logging regression completed - cluster
1. [x] [Backup/restore successful - standalone](http://jenkins.ansible.eng.rdu2.redhat.com/job/Test_Tower_Backup_and_Restore/628/)
1. [x] [Backup/restore successful - traditional cluster (RHEL-7.5, Ubuntu-16)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Backup_and_Restore/645/ANSIBLE_BRANCH=stable-2.6,PLATFORM=rhel-7.6-x86_64,label=jenkins-jnlp-agent/)
1. [x] Backup/restore successful - OpenShift (elijahd, auto-manual, still working on job for future releases)

### Installation
Install/Deploy tower in the following configurations and validate functionality with automated tests

1. [Installation completes successfully on all supported platforms (automated)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Install/1205/)
    * [x] centos-7.latest
    * [x] ubuntu-16.04
    * [x] ol-7.latest
    * [x] rhel-7.4
    * [x] rhel-7.5
    * [x] rhel-7.6
1. [Installation completes successfully using supported ansible releases (automated)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Install/1205/)
    * [x] devel
    * [x] ansible-2.7
    * [x] ansible-2.6
1. [Cluster installation completes successfully on all supported platforms (automated)](http://jenkins.ansible.eng.rdu2.redhat.com/job/Test_Tower_Install_Cluster/1569/)
    * [x] centos-7.latest
    * [x] ubuntu-16.04
    * [x] ol-7.latest
    * [x] rhel-7.4
    * [x] rhel-7.5
    * [x] rhel-7.6
1. [Cluster installation completes successfully using supported ansible releases (automated)](http://jenkins.ansible.eng.rdu2.redhat.com/job/Test_Tower_Install_Cluster/1569/)
    * [x] devel
    * [x] ansible-2.7
    * [x] ansible-2.6
1. [Bundled installation completes successfully on all supported platforms (automated)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Bundle_Install/1753/)
    * [x] centos-7.latest
    * [x] ol-7.latest
    * [x] rhel-7.4
    * [x] rhel-7.5
    * [x] rhel-7.6
1. [x] [Bundled installation completes successfully for clustered deployments](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Install_Cluster/1577/)
1. [x] Deploy tower with [HTTPS+Load Balancer+Let's Encrypt](https://github.com/ansible/tower-qa/issues/1985)
1. [x] Deploy tower in [OpenShift with an external DB](http://jenkins.ansible.eng.adu2.redhat.com/job/Test_Tower_OpenShift_Deploy_External_DB/10/)
    - [Test Run against instance](http://jenkins.ansible.eng.rdu2.redhat.com/job/Test_Tower_OpenShift_Integration/375/)

### Upgrades for all supported platforms
1. [x] [Successful upgrades (and migrations) from `3.1.8` - `3.4.0` (standalone)](Manually tested)
1. [x] [Successful upgrades (and migrations) from `3.2.7` - `3.4.0` (standalone)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3486/)
1. [x] [Successful upgrades (and migrations) from `3.2.8` - `3.4.0` (standalone)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3485/)
1. [x] [Successful upgrades (and migrations) from `3.3.0` - `3.4.0` (standalone)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3481/)
1. [x] [Successful upgrades (and migrations) from `3.3.1` - `3.4.0` (standalone)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3482/)
1. [x] [Successful upgrades (and migrations) from `3.3.2` - `3.4.0` (standalone)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3483/)
1. [x] [Successful upgrades (and migrations) from `3.1.8` - `3.4.0` (traditional cluster)](Manually tested)
1. [x] [Successful upgrades (and migrations) from `3.2.7` - `3.4.0` (traditional cluster)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3501/)
1. [x] [Successful upgrades (and migrations) from `3.2.8` - `3.4.0` (traditional cluster)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3496/)
1. [x] [Successful upgrades (and migrations) from `3.3.1` - `3.4.0` (traditional cluster)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3497/)
1. [x] [Successful upgrades (and migrations) from `3.3.2` - `3.4.0` (traditional cluster)](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3493/)
1. [x] [Successful upgrades (and migrations) from `3.3.2` - `3.4.0` (OpenShift)](http://jenkins.ansible.eng.rdu2.redhat.com/job/Test_Tower_OpenShift_Upgrade/25/)
* Verify the following functionality after upgrade
    * Resource migrations
    * Launch project_updates for existing projects
    * Launch inventory_updates for existing inventory_source
    * Launch, and relaunch, existing job_templates

### Artifacts

  * [x] [AMI (unlicensed)](http://jenkins.ansible.eng.rdu2.redhat.com/job/qe-sandbox/job/Build_Tower_Image_Plain/5/)
  * [x] [Vagrant](http://jenkins.ansible.eng.rdu2.redhat.com/job/Build_Tower_Vagrant_Box/48/)
  * [x] [Documentation](http://jenkins.ansible.eng.rdu2.redhat.com/job/Build_Tower_Docs/3067/)

### Misc

1. [ ] Archive result from the sign-off run and attach them in here.

### Re-run for asgi-amqp change

  * [x] [Test Flake Investigation](https://docs.google.com/document/d/17Bc9YoC4CCDaAMjRsIpTgyUkQthBaem4Ryp2ykuOzEY/edit)
  * [x] [Slowyo with full integration](http://jenkins.ansible.eng.rdu2.redhat.com/job/qe-sandbox/job/Test_Tower_Integration_Plain/190/#showFailuresLink)
  * [x] [Openshift Integration](http://jenkins.ansible.eng.rdu2.redhat.com/job/Test_Tower_OpenShift_Integration/383/console)
  * [x] [Bundled Upgrade](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Upgrade/3528/console)
  * [x] [Cluster Upgrade](http://jenkins.ansible.eng.rdu2.redhat.com/view/Tower/job/Test_Tower_Install_Cluster/1588/console) and [Cluster Upgrade2](http://jenkins.ansible.eng.rdu2.redhat.com/job/Test_Tower_Install_Cluster/1589/console)
  * [x] [Manual UI Verification](https://github.com/ansible/tower/issues/3202#issuecomment-451168033)
  * [x] [e2e UI Testrun](http://jenkins.ansible.eng.rdu2.redhat.com/job/Test_Tower_E2E/1272/testReport/(root)/)