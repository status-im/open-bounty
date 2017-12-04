(ns macchiato-web3-example.app
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [macchiato-web3-example.core-test]))

(doo-tests 'macchiato-web3-example.core-test)


