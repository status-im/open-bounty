# Testing pull requests in Open Bounty

All new functionality and features both are being delivered by pull requests (hereinafter PRs).
How to test PR? Steps below could help a bit!

### Prerequisites
Requirements for PRs to be tested:
* should be in `To test` column in `Pipeline For Pull Requests` project
* shouldn't have conflicts with `develop` branch
* should have a successful build in Jenkins [status-openbounty-app](https://jenkins.status.im/job/status-openbounty/job/status-openbounty-app/view/change-requests/)


### Deployment
In order to deploy feature to [testing env](https://testing.openbounty.status.im/) you should **rebuild** PR you are about to test.  

Only one PR can be deployed on [testing env](https://testing.openbounty.status.im/)

Fresh develop branch with last changes is deployed automatically on [staging env](https://openbounty.status.im:444) 

### Testing
1)  Move appropriate PR card to IN TESTING on the [Board](https://github.com/status-im/open-bounty/projects/3) and let people know you are on it - assign it to yourself! :)
2)  Сheck the functionality current PR fixes / delivers (positive/negative tests related to the feature). In curtain cases it's worth to look in 'Files changed' tab in GitHub to check the list of what was changed to get understanding of the test coverage or "weak" places that have to be covered. Ask PR-author in #openbounty channel in slack what was changed if it's not clear from the notes in PR.
3)  Check reasonable regression using [SOB-general test suite](https://ethstatus.testrail.net/index.php?/suites/view/27&group_by=cases:section_id&group_order=asc)
4)  No issues? Perfect! Put appropriate label to the PR (`Tested - OK`), merge it to develop and move the PR instance to `Merged to develop`. 
5)  Found issues? Check for duplicates before adding one. Hint: make sure the issue is really introduced by current PR - check latest `develop` branch on [staging env](https://openbounty.status.im:444) . Issue exists in develop? Check existing issues list and make sure you are not adding duplicates before creating your own bug :)  **All PR-specific issues should be added as comments to tested PR.** 
Once all issues are logged put label `Tested-issues` to the PR and notify developer that there are several problems that are preventing the PR to merge. Move the PR to `Reviewing, waiting for contributor` on the board if PR is developed by external contributor, and to `Developing` - if it is presented by core contributor.

