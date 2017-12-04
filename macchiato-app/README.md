# macchiato-web3-example
Example app built with Macchiato framework and cljs-web3 library

## Run

1. Start figwheel: `lein build`
2. Run node:  `node target/out/macchiato-web3-example.js`
3. Start test rpc: `testrpc -p 8545`


## Available endpoints

* `curl http://localhost:3000/accounts` eth accounts
* `curl http://localhost:3000/balance` eth balance of the first account
