# Commiteth

Allows you to set bounties for Github issues, paid out in Ether.

More information:
http://wiki.status.im/proposals/commiteth/

Live alpha version:
https://commiteth.com
The `master` branch is automatically deployed here.


Live testnet (Rinkeby) version:
https://commiteth.com:444
The `develop` branch is automatically deployed here.


## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed.

### PostgreSQL

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

## Uberjar build

To create a standalone uberjar:

```
lein with-profile uberjar cljsbuild once min
lein uberjar
```

This creates `target/uberjar/commiteth-<git-sha>.jar`


## Testing

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


## License

Licensed under the [Affero General Public License v3.0](https://github.com/status-im/commiteth/blob/master/LICENSE.md)
