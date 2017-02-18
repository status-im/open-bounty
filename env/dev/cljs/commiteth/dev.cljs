(ns ^:figwheel-no-load commiteth.app
  (:require [commiteth.core :as core]
            [devtools.core :as devtools]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :on-jsload core/on-js-load)

(devtools/install!)

(core/init!)
