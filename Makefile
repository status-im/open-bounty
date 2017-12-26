
all: uberjar

.PHONY: contracts
contracts:
	./build_contracts.sh

less:
	lein less once

less-auto:
	lein less auto

repl:
	lein repl

uberjar:
	lein uberjar

yarn:
	yarn install

cljsbuild: yarn
	lein cljsbuild once

figwheel: yarn
	rlwrap lein figwheel

.PHONY: test
test:
	lein test

migrate:
	lein migratus migrate

create-migration:
	@read -p "Enter migration name: " migration \
	&& lein migratus create $$migration

orig:
	find . -name '*.orig' -delete
