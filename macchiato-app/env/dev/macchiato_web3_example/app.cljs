 (ns ^:figwheel-always macchiato-web3-example.app
  (:require
    [macchiato-web3-example.core :as core]
    [cljs.nodejs :as node]
    [mount.core :as mount]))

(mount/in-cljc-mode)

(cljs.nodejs/enable-util-print!)

(.on js/process "uncaughtException" #(js/console.error %))

(set! *main-cli-fn* core/server)
