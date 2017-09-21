# Testing CommitETH

We have a continuously deployed version tracking the `development` branch live at https://commiteth.com:444. It uses the [Ropsten](https://ropsten.io/) Ethereum testnet. Any one is welcome to use it and your help with testing CommitETH is greatly appreciated!

### General

For testing you will need:
* a web browser (Chrome is known to work, testing with others appreciated)
* an Ethereum account on the Ropsten testnet
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

The Github comment has a QR code as an image containing the bounty contract address. The address is also on the comment as text. Use any ethereum wallet to send ETH and/or supported ERC20 tokens to this address. After a small delay (max 5 minutes), the activity feed should show that the related bounty issue's balance increased and comment should be updated.

### Submitting claims

Open a pull request against the target repository with `Fixes: #NN` in the comment where `NN` is the issue number of the bountied Github issue. After the PR has been opened, the activity feed should show an item indicating the your username has opened a claim for the related bounty issue. The repository admin should also see the claim under `Open claims` in the `Manage payouts` view.

### Managing payouts

Repository admins see a listing of all open claims and bounties that have already been paid out. The `open claims` listing includes unmerged claim pull requests and merged pull requests. Once a claim pull request has been merged, it is selected as the winning claim. The repository admin will still need to sign off the payout with his connected Ethereum wallet. This is done with the `Sign off` button. Once the payout transaction has been mined, the activity feed view will show that the claimer received the bounty funds. All tokens and ETH will be transferred to the claimer's Ethereum address.

### Reporting bugs

All bugs should be reported as issues in the [CommitETH Github repository](https://github.com/status-im/commiteth/issues).

Please first check that there is not already a duplicate issue. Issues should contain exact and minimal step-by-step instructions for reproducing the problem.
