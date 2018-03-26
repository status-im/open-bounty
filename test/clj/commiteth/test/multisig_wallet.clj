(ns commiteth.test.multisig-wallet
  (:require [clojure.test :refer :all]
            [commiteth.eth.core :as eth]
            [commiteth.eth.multisig-wallet :as multisig]))


(def OWNER_ADDRESS "0xa1cab91b36bea34487c5670bbd00a1aa8196aed8")

(defn wait-for [predicate]
  (let [timeout-secs 30
        end-time (+ (System/currentTimeMillis) (* timeout-secs 1000))]
    (loop []
      (if-let [result (predicate)]
        result
        (do
          (Thread/sleep 1000)
          (if (< (System/currentTimeMillis) end-time)
            (recur)))))))

;; deploys a multisig and gets it's address
;; assumes test environment configured correctly
;; (eth wallet with some eth, password etc)
(deftest test-deploy
  (testing "Deploying a multisig"
    (println "WARN multisig deployment test currently disabled as it requires gas")
    #_(let [tx-id (multisig/deploy-multisig OWNER_ADDRESS)
            tx-receipt (wait-for (fn [] (eth/get-transaction-receipt tx-id)))
            addr (multisig/find-created-multisig-address tx-receipt)]
        (println "Created multisig address" addr)
        (is (not-empty addr)))))
