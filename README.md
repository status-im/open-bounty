# Commiteth

Allows you to set bounties for Github issues, paid out in Ether.

More information:
http://wiki.status.im/proposals/commiteth/

Live alpha version (uses Ropsten testnet):
https://commiteth.com


## Prerequisites

* You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

    lein run
    lein figwheel
    lein less auto

## Testing

### Clojure tests

    lein test

### ClojureScript tests


    lein with-profile test phantom test

### Reagent component devcards

    lein with-profile test figwheel devcards

Open http://localhost:3449/cards.html


## License

Licensed under the [Affero General Public License v3.0](https://github.com/status-im/commiteth/blob/master/LICENSE.md)
