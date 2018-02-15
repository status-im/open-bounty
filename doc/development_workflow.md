# Development process in Status Open Bounty

We have a continuously deployed version tracking the `develop` branch live at [test environment](https://openbounty.status.im:444). It uses the [Ropsten](https://ropsten.io/) Ethereum testnet. Any one is welcome to use it and contribute to Open Bounty.
 
Any help is greatly appreciated!
Currently we use two projects -  `Pipeline For Issues` and `Pipeline For Pull Requests`, issues and pull requests in our repository are passing through all or several stages described below.

## Pipeline For Issues

Issues are added to this project by product owner.

#### To define
This is backlog for all features/issues/enhancements which we want to include to development process. 

All issues here should be marked with:
* type - `bug`, `tech-debt`,` enhancement`, `proposal` labels
* priority - `Prio: high`, `Prio: med`, `Prio: low` labels

#### In Bounty
The store with issues which are open bounties. When we put funds to issue and it shows up in [status open bounty](https://openbounty.status.im), we move issue here.

#### Defining
The column is intended for issues not completely clear or for features, that should be splitted to smaller issues in order to go ahead.
After defining all issues that are intended to develop should have size label(`Size: S` - 2-3 hours, `Size: M` - 6-8 hours, `Size: L` - 8-24 hours)
#### To design
It uses for issues that are already defined and require designing process.
#### Designing
It shows up that issues are currently in designing process.
#### To develop
It stores issues which are ready for development.
They are explained, clear, designed and **small enough to create one pull request per issue.**
#### Developing
This is for issues that are currently developing, so pull requests assosiated with issues have to be placed in `Pipeline for Pull Request` project. 
#### Done
It stores issues with merged to `master` pull requests.
## Pipeline For Pull Requests

#### Developing
Contains all open pull requests (hereinafter PRs) that already assosiated with issues.
#### Contributor
It keeps all PRs from external contributors which should pass `Reviewing` stage.
#### Reviewing
The storage for all  PRs that pass reviewing process.
Review  process is discussed [here](https://github.com/status-im/open-bounty/issues/221)
Main points:
* PR is quick fix (from core contributor) - 1 approval from core contributor
* PR is complex (or from external contributor) - 2 or more approvals from core contributors
* PR need to be based on and opened against the `develop` branch.
* If a PR has undergone review and requires changes from author, move it back to `Contributor` column
#### To test
All PRs, that are already developed, reviewed, deployed to test environment and ready to be tested.
#### Testing
All PRs that are currently pass testing process.
After testing two scenarios possible:
* no issues assosiated with PR - `Tested: OK` label and it is moved to `Ready to merge`
* issues found - all of them are created as comments to current PR and PR is moved to `Developing`
#### Ready to merge
Keeps PRs that should be merged to `master` and deployed to [prod environment](https://openbounty.status.im/)
#### Done
Stores all merged and closed PRs.
