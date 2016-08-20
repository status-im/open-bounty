(ns commiteth.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [commiteth.core-test]))

(doo-tests 'commiteth.core-test)

