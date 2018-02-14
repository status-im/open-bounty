# Deployment flow

This briefly describes events that occur when an issue is labeled as bounty and a new contract has to be deployed.

1. Issue is labeled
2. Event is received via GitHub App webhook
3. Contract is deployed
4. GitHub issue comment "Deploying contract..." is posted
5. `transaction_hash` is stored in the `issues` table

The following items execute in scheduler threads that run each minute, so up to 60 sec delay can be expected.

6. `update-issue-contract-address` scheduler thread fetches transaction receipt, updates `contract_address` and updates GitHub comment with a new image and current balance
7. `deploy-pending-contracts` scheduler thread checks if there are issues that did not have corresponding contracts deployed and attempts to redeploy
8. `update-balances` scheduler thread checks balances and updates GitHub comment accordingly
9. `update-contract-internal-balances` scheduler threads updates internal ERC20 token balances for all deployed contracts. This is required by current contract code
