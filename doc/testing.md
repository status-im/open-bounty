# Testing Open Bounty

We have a continuously deployed version tracking the `develop` branch live at https://openbounty.status.im:444. It uses the [Ropsten](https://ropsten.io/) Ethereum testnet. Any one is welcome to use it and your help with testing Open Bounty is greatly appreciated!

### General

For testing you will need:
* a web browser (Chrome is known to work, testing with others appreciated)
* an Ethereum account on the Ropsten testnet
* a Github account with administrative access to one or more repositories
* for approving bounty payouts you will additionally need access to an Ethereum wallet. So far, Mist and [MetaMask](https://metamask.io/) have been used, but anything that provides the web3 javascript interface should work.

The developers can be reached on the `#commiteth` channel in the [Status slack](http://slack.status.im/).

### Signing up

* point your browser to https://openbounty.status.im:444 and click `Login`
* Authorise status-open-bounty to have read access to your public GitHub profile.

You should now see `Bounties`, `Activity`, `Repositories` and `Manage Payouts` tabs. In the upper right hand corner, there should be a dropdown with your GitHub username and options `My Payment Details` and `Sign Out`.


### Connecting your wallet

(instructions for Metamask)
* install Metamask and configure your account
* select `My Payment Details` from the top-right dropdown, select the account you want to use from the selection list and click `Update`


### Creating bounty issues

Before you can create bounties, you need to have administrative access to one or more GitHub repositories. These can be either in the scope of your personal user account or in the scope of a Github orgnazation.

* click the `Repositories` tab
* click on the button `Enable Github Account`
* If you have 1 or more Organisation repositories then grant Organisation access to each of them by clicking on the button `Grant` 
* grant Status Open Bounty the needed addtional permissions for managing repository webhooks, adding and modifying comments by clicking on the button `Authorize status-open-bounty`
* now you should see your repositories on the `Repositories` tab, click `Add` on one. This should cause the `bounty` label to be available in the repository's labels and a new webhook should now exist for the repository.
* now, add the `bounty` label to a new or an existing issue. This should cause Status Open Bounty to post a new comment for the issue containing an image with text `Deploying contract, please wait`
* once the contract has been mined, the comment will be updated to contain the bounty contract's address and a QR code


### Funding bounties

The Github comment has a QR code as an image containing the bounty contract address. The address is also on the comment as text. Use any ethereum wallet to send ETH and/or supported ERC20 tokens to this address. After a small delay (max 5 minutes), the activity feed should show that the related bounty issue's balance increased and comment should be updated.

### Submitting claims

To get bounties you need to provide an Ethereum address in you Payment details on the https://openbounty.status.im:444 that will be used to send bounties to.

Open a pull request against the target repository with `Fixes: #NN` in the comment where `NN` is the issue number of the bountied Github issue. After the PR has been opened, the activity feed should show an item indicating that your username has opened a claim for the related bounty issue. The repository admin should also see the claim under `Open claims` in the `Manage payouts` view.

### Managing payouts

Repository admins see a listing of all open claims and bounties that have already been paid out on the `Manage Payouts` tab. The `open claims` listing includes unmerged claim pull requests and merged pull requests. Once a claim pull request has been merged, it is selected as the winning claim. The repository admin will still need to sign off the payout with his connected Ethereum wallet. This is done with the `Confirm` button. Once the payout transaction has been mined, the `Activity` feed view will show that the claimer received the bounty funds. All tokens and ETH will be transferred to the claimer's Ethereum address.

### Removing bounties

To remove issue from the Bounties list you can close it in GitHub.  

### Reporting bugs

All bugs should be reported as issues in the [CommitETH Github repository](https://github.com/status-im/commiteth/issues).

Please first check that there is not already a duplicate issue. Issues should contain exact and minimal step-by-step instructions for reproducing the problem.
