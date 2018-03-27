# OpenBounty Contracts

This directory contains all the underlying smart contracts used by the OpenBounty platform.

- A script `contracts/build.sh` is part of this repository and can be used to
compile the contracts and copy Java interfaces into `src/java/`.

In order to run the script the following dependencies have to be met:

- [solc](#solc)
- [web3j](#web3j)

### solc

Solidity compiler [0.4.15](https://github.com/ethereum/solidity/releases/tag/v0.4.15) is required and needs to be in $PATH.
Detailed [installation instructions for various platforms](https://solidity.readthedocs.io/en/develop/installing-solidity.html) can be found in the official Solidity documentation.

```
brew install https://raw.githubusercontent.com/ethereum/homebrew-ethereum/de1da16f7972a899fc8dd1f3f04299eced6f4312/solidity.rb
brew pin solidity
```

### web3j

Web3j [2.3.0](https://github.com/web3j/web3j/releases/tag/v2.3.0) is required and the command line tools need to be in $PATH.
Installation instructions for the command line tools can be found in the [Web3j Command Line Tools documentation](https://docs.web3j.io/command_line.html).

```
brew install https://raw.githubusercontent.com/web3j/homebrew-web3j/881cf369b551a5f2557bd8fb02fa8b7b970256ca/web3j.rb
brew pin web3j
```