# Testing CommitETH

We have a continuously deployed version tracking the `development` branch live at https://commiteth.com:444. It uses the [Rinkeby](https://rinkeby.io/) Ethereum testnet. Any one is welcome to use it and your help with testing CommitETH is greatly appreciated!

### General

For testing you will need:
* a web browser (Chrome is known to work, testing with others appreciated)
* an Ethereum account on the Rinkeby testnet
* a Github account with administrative access to one or more repositories
* for approving bounty payouts you will additionally need access to an Ethereum wallet. So far, Mist and [MetaMask](https://metamask.io/) have been used, but anything that provides the web3 javascript interface should work.

The developers can be reached on the `#commiteth` channel in the [Status slack](http://slack.status.im/).

### Signing up

* point your browser to https://commiteth.com:444 and click `Sign in`
* grant CommitETH read access to your public profile

You should now see `Activity`, `Open bounties` and `Manage payouts` tabs. In the upper right hand corner, there should be a dropdown with `Update address` and `Logout`.


### Connecting your wallet

(instructions for Metamask)
* install Metamask + configure your account
* select `Update address` from the top-right dropdown and click `Update`


### Creating bounty issues

Before you can create bounties, you need to have administrative access to one or more repositories. These can be either in the scope of your personal user account or in the scope of a Github orgnazation.

* click the `Repositories tab`
* grant CommitETH the needed addtional permissions for managing repository webhooks, adding and modifying comments
* now you should see your repositories, click `Add` on one. This should cause the `bounty` label to available in the repository's labels and a new webhook should now exist for the repository.
* now, add the bounty label to a new or an existing issue. This should cause CommitETH to post a new comment for the issue containing an image with text `Deploying contract, please wait`
* once the contract has been mined, the comment will be updated to contain the bounty contract's address and a QR code


### Funding bounties
TODO

### Submitting claims
TODO

### Managing payouts
TODO

### Reporting bugs

All bugs should be reported as issues in the [CommitETH Github repository](https://github.com/status-im/commiteth/issues).

Please first check that there is not already a duplicate issue. Issues should contain exact and minimal step-by-step instructions for reproducing the problem.
