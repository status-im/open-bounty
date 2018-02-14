This document describes process guidelines to be followed when contributing to Status Open Bounty repo.

First, make sure to familiarize yourself with the [README](https://github.com/status-im/open-bounty/blob/develop/README.md) and [Testing](https://github.com/status-im/open-bounty/blob/develop/doc/testing.md) documents in order to setup the project properly.

# Issues
  - Issues should have type, priority and size (difficulty) assigned via corresponding labels
  - Issue descriptions should include the following fields:
    - **Summary**
    - **Type**
    - (*Features or enhancements only*) **User story**
    - (*Bugs only*) **Expected behavior**
    - (*Bugs only*) **Actual behavior**
    - **Additional information**

# Pull requests
  - Branch names should include:
    - prefixes indicating issue type (`bug`, `feature`, `doc`, `test`)
    - short description in lisp-case
    - and include associated issue number
    
    For instance, `bug/messy-problem-#1234`
  - Start the title of the PR with [FIX #NNN], where #NNN is the issue number
  - Always include `Status:` in the PR description to indicate whether PR is `WIP` or `Finished`.
  - PR description should include the following sections:
    - **Summary**
    - **Notes**
    - **Status**
  - Merges into `develop` branch should be approved by at least 1 person, into `master` - by 2.
