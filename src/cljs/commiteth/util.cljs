(ns commiteth.util
  (:require [clojure.string :as string]))

(defn os-windows? []
  (string/includes? (-> js/navigator .-platform) "Win"))
