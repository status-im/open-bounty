# Status Open Bounty

Allows you to set bounties for Github issues, paid out in Ether or any ERC-20 token.

More information:
https://wiki.status.im/Status_Open_Bounty

Live production version:
https://openbounty.status.im
The `master` branch is automatically deployed here.

Live testnet (Ropsten) version:
https://openbounty.status.im:444
The `develop` branch is automatically deployed here.


## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed. Also, make sure that you have [wkhtmltoimage](https://wkhtmltopdf.org/downloads.html) available in your PATH. On macOS, it can be installed via `brew cask install wkhtmltopdf`.

### PostgreSQL

Make sure you install [PostgreSQL](https://www.postgresql.org/) and properly set it up:

```
psql postgres -c "CREATE USER commiteth WITH PASSWORD 'commiteth';"
psql postgres -c "CREATE DATABASE commiteth;"
```

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

## Running

Launch following commands each in its own shell:

```
lein run
lein figwheel
lein less auto
```

## Application config

Make sure to create `/config-dev.edn` and populate it correctly, which is based on `env/dev/resources/config.edn`. Description of config fields is given below:

Key | Description
--- | ---
dev | Currently specifies whether Swagger UI endpoints should be added to routes
port | HTTP port for the Ring web app
nrepl-port | nREPL port for development
jdbc-database-url | PostgreSQL database URL. For instance, URL to local db would be `jdbc:postgresql://localhost/commiteth?user=commiteth&password=commiteth`
server-address | URL and port of local server that can be resolved from public internet. It will be used as a redirect URI during GitHub OAuth authorization process
eth-account | Ethereum account ID for the bot
eth-password | Ethereum account password for the bot
eth-rpc-url | RPC URL to Ethereum node, e.g. Geth. Either local or remote
eth-wallet-file | Location of wallet file. If Geth is run with the parameters as given below, it will reside under `$HOME/.ropsten/keystore`
tokenreg-base-format | Should be set to `:status`
github-client-id | Related to OAuth. Copied from GitHub account Settings->Developer settings->OAuth Apps
github-client-secret | Related to OAuth. Copied from GitHub account Settings->Developer settings->OAuth Apps
github-user | GitHub username for bot account. It is used for posting bounty comments
github-password | GitHub password for bot account
webhook-secret | Secret string to be used when creating a GitHub App
user-whitelist | Set of GitHub user/org IDs to be whitelisted. E.g. `#{"status-im" "your_org"}`
testnet-token-data | Token data map, useful if there are Geth connectivity problems

## GitHub integration
Open Bounty uses both OAuth App and GitHub App integration.

### OAuth App
Follow the steps [here](https://developer.github.com/apps/building-oauth-apps/creating-an-oauth-app/). Specify the value of `:server-address` as "Homepage URL", and `:server-address` + `/callback` as "Authorization callback URL". Be sure to copy Client ID and Client Secret values in the config file.

### GitHub App
Follow the steps [here](https://developer.github.com/apps/building-github-apps/creating-a-github-app/). Be sure to specify `:server-address` + `/webhook-app` as "Webhook URL", and `:webhook-secret` as "Webhook Secret".

## Running

Lauch a local geth node with the bot account unlocked:

```
#!/bin/bash
geth --fast --testnet --cache=1024 --datadir=$HOME/.ropsten --verbosity 4 --port 50100 --ipcpath ~/.ropsten/geth.ipc --rpc --rpcaddr 127.0.0.1 --rpcport 8545 --rpcapi db,eth,net,web3,personal --rpccorsdomain "https://wallet.ethereum.org" --unlock "0xYOUR_ADDR" --password <(echo "YOUR_PASSPHRASE")
```

Launch the following command in a separate shell:

```
lein less auto
```

Next you want to start a REPL on the backend and the frontend.

```
lein repl
```

Now you can start a CLJS repl with:

```
(use 'figwheel-sidecar.repl-api)
(start-figwheel!)
(cljs-repl)
```

(Alternatively, if you use emacs and CIDER, you can run cider-jack-in. Details [here](https://cider.readthedocs.io/en/latest/up_and_running/))

Next start the application from the clojure REPL with:

```
(user/start)
```

## Uberjar build

To create a standalone uberjar:

```
lein uberjar
```

This creates `target/uberjar/commiteth-<git-sha>.jar`


## Testing

### QA

Please refer to [doc/testing.md](https://github.com/status-im/commiteth/blob/develop/doc/testing.md)

### Clojure tests

```
lein test
```

### ClojureScript tests

```
lein with-profile test doo phantom test
```

### Reagent component devcards

```
lein with-profile test figwheel devcards
```

Open http://localhost:3449/cards.html

### Update landing page

Landing page is static and different CSS and JS due to time constraints.

- Build CSS with Gulp (see `static_landing_page/README.md`
- Make changes and `./build-landing-page.sh`

This copies over necessary artifacts to `resources` dir.


### Troubleshooting
See the [Cookbook](doc/cookbook.md).

## License

Licensed under the [Affero General Public License v3.0](https://github.com/status-im/commiteth/blob/master/LICENSE.md)
