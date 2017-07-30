# Commiteth

Allows you to set bounties for Github issues, paid out in Ether.

More information:
http://wiki.status.im/proposals/commiteth/

Live alpha version:
https://commiteth.com


## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed.

### PostgreSQL

Make sure you install [PostgreSQL](https://www.postgresql.org/) and properly set it up:

```
sudo -u postgres psql -c "CREATE USER commiteth WITH PASSWORD 'commiteth';"
sudo -u postgres createdb commiteth
```

### GitHub App installation

* Browse https://github.com/settings/applications/new
* Fill details

```
Application name: a_unique_id
Homepage URL: http://_your_.ddns.net:3000/
Authorization URL: http://_your_.ddns.net:3000/callback
```


* Copy OAuth credentials to `env/dev/resources/config.edn`

## Running

Launch following commands each in its own shell:

```
lein run
lein figwheel
lein less auto

```

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
