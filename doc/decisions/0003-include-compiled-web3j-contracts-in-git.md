# 0003. Include Compiled Web3j Contracts in Git

| Date | Tags |
|---|---|
| 2018-03-20 | contracts, tooling |


## Status

Accepted

## Context

The OpenBounty project utilizes smart contracts to make certain core
aspects of it's product work without centralized trust.

In order to develop the platform a Java interface to these contracts needs to
be built beforehand. To create those interfaces various tools are required
(`web3j` & `solc`), often in specific versions that are not easily available
via widespread package managers.

This hurdle also applies to any other situations where the application is set
up from scratch, e.g. continuous integration.

## Decision

Instead of forcing every contributor to install those tools we will include
the compiled Java interfaces in our Git repository. This removes a significant
setup cost and hopefully allows people to get going much faster.

Installing `web3j` and `solc` will only be required when hacking on the
contracts itself which are much more stable than the majority of the code.

An alternative would be implementing scripts that install those tools in a
platform independent manner but this would require more work. Once we have
the time or someone wants to work on creating those scripts we can easily
revert the decision outlined in this document.

## Consequences

- The compiled Java interfaces may get out of date.  
  This could perhaps be addressed by some clever use of checksums.
- By having changes to the contract interfaces be part of a changeset it may
  be easier to spot what changes are required/how APIs are changing.
