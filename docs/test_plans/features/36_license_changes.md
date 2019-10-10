# Subscription and Enforcement License Changes
## Owner

Primary: Caleb (@squidboylan)
Secondary: Elijah DeLee (@kdelee)

## Summary

For legal purposes, we must *not* terminate usage/restrict functionality after:
     - the license ends
     - the number of managed nodes provided by the license is exceeded
Provide a new _optional_ interaction in the UI _at license application time_ which allows you to specify Red Hat a Red Hat subscription username and password **instead of** uploading a license key file.

## Related Information

- [tower ticket](https://github.com/ansible/tower/issues/3550)
- test ticket TBD
- [initial awx PR](https://github.com/ansible/awx/pull/4611)
- [initial tower-license PR](https://github.com/ansible/tower-license/pull/10/)

## Verification Criteria

### License Generation
- [x] On the license application screen, users may *optionally* specify a username and password in lieu of a license file
    - [x] If invalid credentials are provided, a meaningful error message is printed in the Tower logs
    - [x] If the license server is unreachable, a meaningful error message is printed in the Tower logs
- [x] Tower license data generated via Red Hat's subscription APIs meets the following requirements:
    - [x] If there is *no* active Tower subscription (one of several predefined SKUs), a license is not generated
    - [x] If there is exactly *one* active Tower subscription, a license is generated which:
      - has a subscription name that matches the RH product name (i.e., Ansible Tower by Red Hat, Standard (100 Nodes))
      - has an expiration date that matches the *end* date of the RH subscription
      - has a total node count that is correct (based on the SKU and quantity provided by the Red Hat API)
    -  [x] If there are **multiple** active Tower subscriptions, we will generate a license based on the details of the subscription that ends **soonest**.
- [x] When a license is applied using Red Hat credentials, the Red Hat credentials are persisted and available at /api/v2/settings/system/
    - [x] If a user returns to the license screen /#/license _after_ applying a license with Red Hat credentials, their previously entered details will be pre-filled in the username/password field so they can re-submit them (the password will show as an "encrypted" widget).

### Enforcement
- If a *trial* license is provided:
  - [x] the API prevents you from creating hosts *past* your managed node count.  This includes:
    - [x] manually creating hosts via the API
    - [x] importing inventory via an inventory source
  - [x] the API drastically limits functionality when your license becomes expired
- If a *non*-trial is provided (included via the new RH login mechanism)
  - [x] the API **allows** you to create hosts *past* your managed node count, but warns you (loudly) in the Tower logs when you do so
  - [x] the API does *not* limit functionality in any way when your license becomes expired when you have an enterprise license

### SKU Scenarios to Test
- [x] Multiple subscriptions to the same SKU with the same end date should have their quantities added together
  - Unit test
- [x] Multiple subscriptions to different SKUs with the same end date should have their quantities added together
  - Unit test
- [x] Single active SKU
- [x] No SKUs
  - Unit test
- [x] An expired SKU
  - [x] Tower can still launch the following while displaying a warning about expired license
    - [x] Job
    - [x] Workflow Job
    - [x] Project Update
    - [x] Inventory Update
  - [x] Doesn't generate a license
    - [x] unit test
- [x] Accounts with 100+ SKUs work as expected (paging doesnt break us somehow)
- [x] Accounts with old ansible engine only SKUs don't work with tower
- [x] Account with only Trial license works as expected (start with SCR or SVC)
- [x] Verify we can import new hosts even when we exceed the host count
- [x] Verify we can import new hosts even when the license is expired
- [x] Verify we can import new hosts even when we exceed the host count and the license is expired