# Status Open Bounty
[![Riot Chat Badge](https://img.shields.io/badge/join%20%23openbounty-riot-green.svg)](https://chat.status.im/#/room/#openbounty:status.im)

Allows you to set bounties for GitHub issues, paid out in Ether or any ERC-20 token.

More information:
https://wiki.status.im/Status_Open_Bounty

Live production version:
https://openbounty.status.im
The `master` branch is automatically deployed here.

Live testnet (Ropsten) version:
https://openbounty.status.im:444
The `develop` branch is automatically deployed here.

## Table of contents
- [Prerequisites](#prerequisites)
- [Application config](#application-config)
- [GitHub integration](#github-integration)
- [Contracts](#contracts)
- [Running](#running)
- [Testing](#testing)
- [More info](#more-info)



## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed. Also, make sure that you have [wkhtmltoimage](https://wkhtmltopdf.org/downloads.html) available in your PATH. On macOS, it can be installed via `brew cask install wkhtmltopdf`.

### PostgreSQL

Install [PostgreSQL](https://www.postgresql.org/), and set it up properly:

```
psql postgres -c "CREATE USER commiteth WITH PASSWORD 'commiteth';"
psql postgres -c "CREATE DATABASE commiteth;"
```

## Application config

Create `/config-dev.edn`, and populate it correctly, according to `env/dev/resources/config.edn`. The config fields are described below:

Key | Description
--- | ---
dev | Currently specifies whether Swagger UI endpoints should be added to routes
port | HTTP port for the Ring web app
dev-login | Local development only. Set it to the GitHub name of your dev user in order to log in to the system and to bypass OAuth. Following this, set `server-address` according to your localhost address.
nrepl-port | nREPL port for development
jdbc-database-url | PostgreSQL database URL; for instance, the URL to the local db would be `jdbc:postgresql://localhost/commiteth?user=commiteth&password=commiteth`
server-address | The URL and port of the local server that can be resolved from public internet. It will be used as a redirect URI during GitHub OAuth authorization process.
eth-account | Ethereum account ID for the bot
eth-password | Ethereum account password for the bot
eth-rpc-url | RPC URL to the Ethereum node, e.g. Geth. Either local or remote
eth-wallet-file | Location of the wallet file. If Geth is run with the parameters specified below, it will reside under `$HOME/.ropsten/keystore`.
offline-signing | Specifies whether to sign transactions locally before sending. Default is true. Set to false when connecting to the local Geth node that unlocks accounts.
tokenreg-base-format | Should be set to `:status`
github-client-id | Related to OAuth. Copied from the GitHub account: Settings -> Developer settings -> OAuth Apps
github-client-secret | Related to OAuth. Copied from theGitHub account: Settings -> Developer settings -> OAuth Apps
github-user | GitHub username for a bot account. It is used for posting bounty comments.
github-password | GitHub password for a bot account.
webhook-secret | Secret string to be used when creating a GitHub App
user-whitelist | Set of GitHub user/org IDs to be whitelisted. E.g. `#{"status-im" "your_org"}`
testnet-token-data | Token data map; useful if there are Geth connectivity problems

## GitHub integration
Open Bounty uses both OAuth App and GitHub App integration.

### OAuth App
Follow the steps [here](https://developer.github.com/apps/building-oauth-apps/creating-an-oauth-app/). Specify the value of `:server-address` as "Homepage URL", and `:server-address` + `/callback` as "Authorization callback URL". Be sure to copy the Client ID and Client Secret values in the config file.

### GitHub App
Follow the steps [here](https://developer.github.com/apps/building-github-apps/creating-a-github-app/). Be sure to specify `:server-address` + `/webhook-app` as "Webhook URL", and `:webhook-secret` as "Webhook Secret".

## Contracts

All information related to development of OpenBounty smart contracts can be found in [`contracts/`](/contracts/).

## Running

### Ethereum node
There are two options for connecting to an Ethereum node: either run a local node with an unlocked account, or connect to a remote Geth node or Infura. We will be connecting to Ropsten, which is an Ethereum network used for testing.

#### Local

To launch a local geth node with the bot account unlocked, issue the following command:

```
#!/bin/bash
geth --fast --testnet --cache=1024 --datadir=$HOME/.ropsten --verbosity 4 --port 50100 --ipcpath ~/.ropsten/geth.ipc --rpc --rpcaddr 127.0.0.1 --rpcport 8545 --rpcapi db,eth,net,web3,personal --rpccorsdomain "https://wallet.ethereum.org" --unlock "0xYOUR_ADDR" --password <(echo "YOUR_PASSPHRASE")
```

#### Remote
Register at [Infura](https://infura.io/signup). You will receive an email with the provider's URLs. Paste a URL for the Ropsten network into `config.edn` under the `:eth-rpc-url` key, and set `:offline-signing` to true.


### CSS auto-compilation
Launch the following command in a separate shell:

```
lein less auto
```

### Solidity compilation
Compile Solidity files into Java classes with:
```
cd contracts && ./build.sh
```

### Clojure app without REPL
Launch the following commands, each in its own shell:

```
lein run
lein figwheel
```

### Clojure app with REPL

You'll have to start a REPL on the backend and frontend.

```
lein repl
```

Now you can start a CLJS REPL:

```
(use 'figwheel-sidecar.repl-api)
(start-figwheel!)
(cljs-repl)
```

(Alternatively, if you use emacs and CIDER, you can run cider-jack-in. Details [here](https://cider.readthedocs.io/en/latest/up_and_running/))

Next, start the application from the clojure REPL:

```
(user/start)
```

## Uberjar build

To create a standalone uberjar:

```
lein uberjar
```

This creates `target/uberjar/commiteth.jar`. You can run it with the following command from within the project's root:
```
java -Dconf=<path_to_config.edn> -jar target/uberjar/commiteth.jar
```


## Testing

### QA

Please refer to [doc/testing.md](https://github.com/status-im/commiteth/blob/develop/doc/testing.md).

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

### CircleCI

We use CircleCI to run unit tests. For this to work, set the following env vars:

> These env vars override the configuration parameters that are usually set using the `config.edn` file.

- `ETH_ACCOUNT` - as in `config.edn`
- `ETH_PASSWORD` - as in `config.edn`
- `ETH_RPC_URL` - as in `config.edn`
- `ETH_WALLET_FILE` - as in `config.edn`
- `ETH_WALLET_JSON` - contents of this will be written to `ETH_WALLET_FILE`

:bulb: Ideally, we'd create those parameters in a script. PR welcome.

## Update landing page

Landing page is static and different CSS and JS due to time constraints.

- Build CSS with Gulp (see `static_landing_page/README.md`
- Make changes and `./build-landing-page.sh`

This copies the necessary artifacts over to the `resources` dir.


## More info
Detailed information on code structure, troubleshooting, and more can be found [here](doc/README.md).

## License

Licensed under the [Affero General Public License v3.0](https://github.com/status-im/commiteth/blob/master/LICENSE.md)
