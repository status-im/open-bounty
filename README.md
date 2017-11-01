# Commiteth

Allows you to set bounties for Github issues, paid out in Ether.

More information:
http://wiki.status.im/proposals/commiteth/

Live beta version:
https://openbounty.status.im
The `master` branch is automatically deployed here.


Live testnet (Ropsten) version:
https://openbounty.status.im:444
The `develop` branch is automatically deployed here.


## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed.

### PostgreSQL
<<<<<<< HEAD

Make sure you install [PostgreSQL](https://www.postgresql.org/) and properly set it up:

```
sudo -u postgres psql -c "CREATE USER commiteth WITH PASSWORD 'commiteth';"
sudo -u postgres createdb commiteth
```

## Running

Launch following commands each in its own shell:

```
lein run
lein figwheel
lein less auto
```

=======

Make sure you install [PostgreSQL](https://www.postgresql.org/) and properly set it up:

```
sudo -u postgres psql -c "CREATE USER commiteth WITH PASSWORD 'commiteth';"
sudo -u postgres createdb commiteth
```

### solc

Solidity compiler [0.4.15](https://github.com/ethereum/solidity/releases/tag/v0.4.15) is required and needs to be in $PATH.

### web3j

Web3j [2.3.0](https://github.com/web3j/web3j/releases/tag/v2.3.0) is required and the command line tools need to be in $PATH.

## Running

Make sure `env/dev/resources/config.edn` is correctly populated.

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

## License

Licensed under the [Affero General Public License v3.0](https://github.com/status-im/commiteth/blob/master/LICENSE.md)
