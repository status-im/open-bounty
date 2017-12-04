 (ns macchiato-web3-example.app
  (:require
    [macchiato-web3-example.core :as core]
    [cljs.nodejs]
    [mount.core :as mount]))

(mount/in-cljc-mode)

(cljs.nodejs/enable-util-print!)

(set! *main-cli-fn* core/server)
