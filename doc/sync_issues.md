# Common sync issues

- Repo rename. Related issue: https://github.com/status-im/open-bounty/issues/219.
- Transaction callback not executed, hence payout-hash not set.
- App downtime, multiple GitHub App deliveries failing. These can be afterwards replayed from GitHub App's Advanced tab.
- Geth issue. Not solvable on app end, Geth restart usually fixes these.
- Bot out of gas. Relevant issue: https://github.com/status-im/open-bounty/issues/195.
- Sometimes repos are disabled in DB (state=0), probably only happens to repos that were added earlier through OAuth App.
- PRs might end up in state=0 (opened) instead of state=1(merged), if PR did not have a proper text ("Fixes #..."). This one needs more investigation and more cases to reproduce.
- Sometimes contract_address cannot be fetched for a long period of time.

