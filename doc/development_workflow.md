# Development process in Status Open Bounty

We have a continuously deployed version tracking the `develop` branch live at [test environment](https://openbounty.status.im:444). It uses the [Ropsten](https://ropsten.io/) Ethereum testnet. Any one is welcome to use it and contribute to Open Bounty.
 
Any help is greatly appreciated!
Currently we use two projects -  `Pipeline For Issues` and `Pipeline For Pull Requests`, issues and pull requests in our repository are passing through all or several stages described below.
Whole team is responsible to keep the projects with accurate information.

**If issue or pull request marked with `Blocked` label, it means that it is blocked on some stage, reason of blocking have to be in comment.** 

## Pipeline For Issues

Team is working only on issues that included in this project.
Issues can be added to this project by any core team member.

#### To define
This is backlog for all features/issues/enhancements which we want to include to development process. 

All issues here should be marked with:
* type - `bug`, `tech-debt`,` enhancement` labels. Issues with `proposal` label should be converted to `bug`, `tech-debt`,` enhancement` before addidng to project.
* priority - `Prio: high`, `Prio: med`, `Prio: low` labels. On the board inside issues with same priority sorting from higher to lower priority is applied.

#### Defining
The column is intended for issues not completely clear or for features, that should be splitted to smaller issues in order to go ahead.
After defining all issues that are intended to develop should have size label
*`Size: XS` - 1-2 hours, 
*`Size: S` - 2-4 hours,
*`Size: M` - 4-8 hours, 
*`Size: L` - 8-20 hours,
*`Size: XL` - 20-40 hours,
*`Size: XXL` - 40-60 hours.
#### To design
It is used for issues that are already defined and require designing process.
#### Designing
It shows up that issues are currently in designing process.
#### To develop
It stores issues which are ready for development.
They are explained, clear, designed and **small enough to create one pull request per issue.**
#### In Bounty
The store with issues which are open bounties. When we put funds to issue and it shows up in [status open bounty](https://openbounty.status.im), we move issue here.
#### Developing
This is for issues that are currently developing, so pull requests assosiated with issues have to be placed in `Pipeline for Pull Request` project. 
#### Done
It stores issues with merged to `master` pull requests.
## Pipeline For Pull Requests

#### Developing
Contains all open pull requests (hereinafter PRs) that already assosiated with issues.
#### Reviewing, waiting for contributor
It keeps all PRs from external contributors which should pass `Reviewing` stage.
#### Reviewing
The storage for all  PRs that pass reviewing process.
Review  process is discussed [here](https://github.com/status-im/open-bounty/issues/221)
The number of reviewers should be proportional to the complexity of the change and may vary from PR to PR.
Recommended:
* PR is trivial (from core contributor) - 1 approval from core contributor
* PR is normal (or from external contributor) - 2 or more approvals from core contributors
* PR need to be based on and opened against the `develop` branch.
* If a PR has undergone review and requires changes from author, move it back to `Contributor` column
#### To test
All PRs, that are already developed, reviewed, and haven't conflicts with `develop` branch, so ready to be tested.
In case if PR has conflict - it is moved to `Reviewing, waiting for contributor` for external contributors or to `Developing` for core contributors.
#### Testing
Contains all PRs that are currently should pass through testing process, which is described in `core_testing_workflow.md`.
After testing two scenarios possible:
* no issues assosiated with PR - `Tested: OK` label, merge to develop (using `Merge` button) to `Merged to develop`
* issues found - all of them are created as comments to current PR and PR is moved to `Developing`
#### Merged to develop
Keeps PRs that should be merged to `master` and deployed to [prod environment](https://openbounty.status.im/)
#### Done
Stores all merged and closed PRs.
