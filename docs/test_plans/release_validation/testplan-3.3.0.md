# Tower 3.3.0 Release Test Plan

## Resources
* 5 full-time automation engineers - cwang/jladd/one-t/jhill/guozhang.
* Left company halfway through release - rfitzpatrick.
* Delegation of responsibilities detailed [here](https://docs.google.com/document/d/1hhTGiWETu2tvQPEYojn1ZTpxDQvPseeJB4sI_b6zNZM/edit).
* Historical [record](https://docs.google.com/document/d/11-nOFQdq0DYK9oKSljIDJqvscQ61bhmQfEryEeZ5iSo/edit) of failing integration tests at time of release.

## Features Not Tested

## Features Tested

### Parameters for Scheduled and Workflow Jobs (cwang)
* [Feature](https://github.com/ansible/tower/blob/devel/docs/prompting.md)
* [API Test Plan](https://docs.google.com/document/d/1M_W0g5gPo4kuuMwIcA5Z7BookbPID8PqxBwCuIIwS2Y/edit)
* [UI Test Plan](https://docs.google.com/document/d/13cjewq7eat2vMT-QuwGMtCl94aj9O_ljkFNncVDaXO8/edit)

### Revise capacity algorithm (cwang)
* [Feature](https://github.com/ansible/tower/blob/release_3.3.0/docs/capacity.md)
* [API Test Plan](https://docs.google.com/document/d/1wr1IiwsP8pnZ-b-SiiQnh_jL_q4gtzOZQQN8oCvDi5s/edit)
* [UI Test Plan](https://docs.google.com/document/d/1v-OGERQSQZJTyzDa1gYlLp74qm-YwNtf0dst2OfdG9s/edit)

### More granular permissions (mwilson)
* [Feature]()
* [UI Test Plan](https://docs.google.com/document/d/153--RdyViwK1g2_qNEyo78q28i1X8C9MdwNHeA7sOjM/edit#)

### SAML/LDAP/etc hitlist catchall (mwilson)
* [Feature (saml)](https://github.com/ansible/tower/blob/release_3.3.0/docs/auth/saml.md)
* [Feature (ldap)](https://github.com/ansible/tower/blob/release_3.3.0/docs/auth/ldap.md)
* [UI Testplan](https://docs.google.com/document/d/1uMSSYQdItLt9GKpq54LtBcbFKXE47qEIyJVTjpiiX_A/edit#heading=h.8i4wtrisc5i0)

### Update GCP credentials
* [Feature]()

### API-backed item copying (guozhang)
* [Feature](https://github.com/ansible/tower/blob/release_3.3.0/docs/resource_copy.md)
* [Test Plan](https://docs.google.com/document/d/1EXi_w2JWkZ_PzhHIK4hJ1GOJAVT8jjYwMsFz59OkC9E/edit?usp=sharing)
* [UI Test Plan](https://docs.google.com/document/d/1-foEZGWngVQY5NVkctq-AHqMdl70MWnh5517Q29P1Rk/edit)

### Multi-file support for Credential Types (cwang)
* [Feature](https://github.com/ansible/tower/blob/release_3.3.0/docs/multi_credential_assignment.md)
* [Test Plan](https://docs.google.com/document/d/1haYuCGYGqPbiUqig4rJoDYyKfLHgCXm_yzXCeSxoiEo/edit)

### Pendo Opt-Out in License Screen (johill)
* [Feature](https://github.com/ansible/ansible-tower/issues/7812)
* [UI Test Plan](https://docs.google.com/document/d/19haH5aTRhIdAsmpjKx9chOQI1po_gMRaVYoQf681aJQ/edit)

### Fact caching for isolated jobs (cwang)
* [Feature](https://github.com/ansible/awx/issues/198)
* [Test Plan](https://docs.google.com/document/d/1iddRSaS88L2bz10K1511PfKcDmVnm5M9LhqMh1ty6hE/edit)

### Containerized Tower as a product deliverable (cwang, jladd)
* [Feature](https://github.com/ansible/tower/blob/release_3.3.0/docs/clustering.md)
* [Test Plan](https://docs.google.com/document/d/1qaLCCXoGEcAIW0Be-JTvBOM-KoC1AOGBju1nM9b3WbU/edit)

### ~Inventory & Network Visualization~
* This feature never made Tower-3.3.0.

### Token-based authentication to Tower (mwilson, rpetrello)
* [Feature (sessions)](https://github.com/ansible/tower/blob/release_3.3.0/docs/auth/session.md)
* [Feature (oauth)](https://github.com/ansible/tower/blob/release_3.3.0/docs/auth/oauth.md)
* [UI Test Plan](https://docs.google.com/document/d/1lgkSJ9DkcWqKnMGixH5lzsrZcbmzHZ5VnJqxPFXMx_E/edit#)

### Retry on Failed Hosts (guozhang)
* [Feature](https://github.com/ansible/tower/blob/release_3.3.0/docs/retry_by_status.md)
* [Test Plan](https://docs.google.com/document/d/113A8f4j1_fUIZda2XArZ3kwHZVsOvIe8lcWrozd3oz4/edit?usp=sharing)

### Multiple venvs for ansible (jladd)
* [Feature](https://github.com/ansible/tower/blob/release_3.3.0/docs/custom_virtualenvs.md)
* [Test Plan](https://docs.google.com/document/d/1TKkdj1pdeh9p048_S-sIMxSpAUKZuX3T65kcVKT8odM/edit)
* [UI Test Plan](https://docs.google.com/document/d/1TKkdj1pdeh9p048_S-sIMxSpAUKZuX3T65kcVKT8odM/edit#bookmark=id.ouul149hfpe6)

### Multi-Credential (+ Multi-Vault) Assignment (jladd)
* [Feature](https://docs.google.com/document/d/1H5cphm39UFqV91nRiipNJxjlshpoBMwAIkx2n5cg2lQ/edit)
* [Test Plan](https://docs.google.com/document/d/1H5cphm39UFqV91nRiipNJxjlshpoBMwAIkx2n5cg2lQ/edit)

### Fix #823 (schedules timezone) (rfitzpatrick)
* [Feature](https://github.com/ansible/tower/blob/release_3.3.0/docs/schedules.md)

### Event-ize all stdout + optimized multi-MB+ stdout + handle free/serial (johill)
* [Feature](https://github.com/ansible/tower/blob/release_3.3.0/docs/job_events.md)
* [Test Plan](https://docs.google.com/document/d/1Fju6pAJ5kbK8xEeCSsse3RHwhhYpdMRMnMYxscs1B8Q/edit)

## Regression
1. [x] UI regression completed
1. [x] API regression completed - standalone
1. [x] API regression completed - traditional cluster
1. [x] API regression completed - OpenShift
1. [x] Tower social authentication regression completed (vm)
1. [x] Tower SAML integration regression completed (vm)
1. [x] Tower social authentication regression completed (OpenShift)
1. [x] Tower SAML integration regression completed (OpenShift)
1. [x] Logging regression completed - standalone
1. [x] Logging regression completed - cluster
1. [x] Backup/restore successful - standalone
1. [x] Backup/restore successful - traditional cluster (RHEL-7.4, Ubuntu-16)
1. [x] Backup/restore successful - OpenShift

### Installation
1. Installation completes successfully on all supported platforms (automated)
    * [x] ubuntu-14.04
    * [x] ubuntu-16.04
    * [x] rhel-7.5
    * [x] centos-7.latest
    * [x] ol-7.latest
1. Installation completes successfully using supported ansible releases (automated)
    * [ ] ~devel~ (Ansible builds are failing)
    * [x] ansible-2.7
    * [x] ansible-2.6
    * [x] ansible-2.5
    * [x] ansible-2.4
    * [x] ansible-2.3
1. Cluster installation completes successfully on all supported platforms (automated)
    * [x] ubuntu-16.04
    * [x] rhel-7.5
1. Cluster installation completes successfully using supported ansible releases (automated)
    * [ ] ~devel~ (Ansible builds are failing)
    * [x] ansible-2.7
    * [x] ansible-2.6
    * [x] ansible-2.5
    * [x] ansible-2.4
    * [x] ansible-2.3
1. Bundled installation completes successfully on all supported platforms (automated)
    * [x] rhel-7.4
    * [x] rhel-7.5
    * [x] centos-7.latest
    * [x] ol-7.latest
1. [x] Bundled installation completes successfully for clustered deployment

### Upgrades
1. [x] Successful migrations from `3.1.0` - `3.1.8`
1. [x] Successful migrations from `3.2.0` - `3.2.6`
1. [x] Successful upgrades on all supported platforms from `3.1.0` - `3.1.8` - standalone
1. [x] Successful upgrades on all supported platforms from `3.2.0` - `3.2.6` - standalone
* Successful upgrades from `3.2.0` - `3.2.6` - cluster
    * [x] RHEL-7.4
    * [x] Ubuntu 16
1. [x] Verify upgrades between two different image IDs in OpenShift
1. [x] Verify migration path from 330 VM cluster to 330 cluster in OpenShift
* Verify the following functionality after upgrade
    * Resource migrations
    * Launch project_updates for existing projects
    * Launch inventory_updates for existing inventory_source
    * Launch, and relaunch, existing job_templates

### Provided Images
1. Installation completes successfully on supported images
    * [x] AMI (unlicensed)
    * [x] Vagrant