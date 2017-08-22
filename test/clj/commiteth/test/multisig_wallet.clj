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

;; deploys a multisig and gets it's address. assumes test environment configured correctly (access to running geth, eth wallet, password etc)
(deftest test-deploy
  (testing "Deploying a multisig"
    (let [tx-id (multisig/deploy-multisig OWNER_ADDRESS)
          tx-receipt (wait-for (fn [] (eth/get-transaction-receipt tx-id)))
          addr (-> tx-receipt
                   (multisig/find-created-multisig-address))]
      (println "Created multisig address" addr)
      (is (not-empty addr)))))
