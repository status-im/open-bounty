# Payout flow

This describes the sequence of events happening once a PR for an issue with a bounty was merged by repo maintainer.

### Quick info on transaction hashes
In the sequence described below, several types of hashes are used. SOB checks presence of different hashes on records in the `issues` table to decide which action to take on an issue and associated contract. These hashes are:
- `transaction_hash`: set when contract is deployed to the labeled issue
- `execute_hash`: set when PR for an issue with a bounty was merged
- `confirm_hash`: fetched from receipt from transaction invoked in previous step
- `payout_hash`: set when payout was confirmed via `Manage Payouts`

The event flow is given below. For the bounty to be paid, each issue has to go through the steps in given order.

### 1. PR closed and merged
- app receives notification via GitHub App webhook (endpoint: `/webhook-app`)
- `handle-claim` fn is invoked which will:
- save PR in the `pull_requests` DB table, where state=1 for merged PRs
- update issue in the DB with commit SHA, if PR was merged

Afterwards two interleaving sequences of actions come into play: scheduler threads and manual user interaction in the `Manage Payouts` tab.

### 2. `self-sign-bounty` scheduler thread
- input query name: `pending-bounties`. This selects pending bounties (where `execute_hash` is nil and `commit_sha` is set)
- execute payout transactions
- store `execute_hash` and `winner_login` in `issues` DB table
- update GitHub comment to "Pending maintainer confirmation"
### 3. `update-confirm-hash` scheduler thread
- input query name: `pending-payouts`. This selects bounties with `execute_hash` set and no `confirm_hash`
- fetch `confirm_hash` from transaction receipt
- store `confirm_hash` in `issues` DB table

### 4. Manage Payouts view
In order to confirm a payout, following conditions have to be met for an issue:
- it is merged
- not paid yet (meaning its `payout_hash` is nil)
- not being confirmed at the moment (`:confirming?` flag is true)
  OR
- already confirmed by a scheduler thread(`confirm_hash` is not nil)

Note that `confirm_hash` issue field and confirmation action in the UI are different things, albeit identically named. In order to confirm a payout from the UI, `confirm_hash` has to be already set by scheduler thread (see above).

Payout confirmation action results in a `:confirm-payout` event. Its handler will
- use `confirm_hash` to construct transaction payload
- set `:confirming?` flag to `true`
- execute `confirmTransaction()` call
- pass transaction callback to `confirmTransaction()`. Once invoked, the callback will:
  - get `payout_hash` passed as an argument
  - dispatch `:save-payout-hash` event. Its handler will:
    - POST to /api/user/bounty/%s/payout
      - This will update `payout_hash` in `issues` DB table
    - if POST is successful, dispatch `:payout-confirmed`
      - `:payout-confirmed` will update  `:confirmed?` to `true` and remove `:confirming?` flag

### 5. `update-payout-receipt` scheduler thread
- input query name: `confirmed-payouts`. This selects confirmed payouts (the ones that have `payout_hash` and do not have `payout_receipt` set)
- store `payout_receipt` in `issues` DB table
- and update GitHub comment to "Paid to:..."
