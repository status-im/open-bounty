# 2. Sign Commits With GPG

| Date       | Tags              |
|------------|-------------------|
| 2018-02-16 | process, security |


## Status

Proposed

## Context

OpenBounty is a system which has value flowing through it.
Naturally security is a concern that should be taken into consideration.

Currently an attacker might get access to an account of a team member
and pose as that developer, merging PRs and pushing changes.

## Decision

In order to verify that commits in the repository are actually authored by the specified
author we adopt [GPG signing of Git commits](https://git-scm.com/book/id/v2/Git-Tools-Signing-Your-Work).

This will allow us to verify authenticity of the author information saved in
a Git commit and make workflows like deploying on push safer.

It also introduces some complexity because contributors who want to sign
their commits need to set up the appropriate tooling. Due to that we will
not require outside contributors to sign their commits for now.

## Consequences

GPG signing is only making things safer if we have a trusted way of
exchanging public keys. In the scenario outlined above a user who got access
to GitHub could simply upload an additional key.

This is currently a work-in-progress within the wider Status organization
and we'll have to wait to see what comes out of that.

## Appendix

- [GitHub's instructions for setting up GPG signing](https://help.github.com/articles/signing-commits-using-gpg/)
- More discussion around the usefulness of GPG signing in [issue #285](https://github.com/status-im/open-bounty/issues/285)