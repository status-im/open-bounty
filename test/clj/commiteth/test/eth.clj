(ns commiteth.test.eth
  (:require [clojure.test :refer :all]
            [commiteth.eth.core :as eth]))

(deftest test-address-validation
  (testing "Short address is invalid"
    (let [addr "0x34264362346364326423"]
      (is (false? (eth/valid-address? addr)))))
  (testing "Valid address is valid"
    (let [addr "0xA1cab91b36bea34487c5670Bbd00a1Aa8196aeD8"]
      (is (true? (eth/valid-address? addr)))))
  (testing "Case sensitivity matters"
    (let [addr "0xa1cab91b36bea34487c5670bbd00a1aa8196aed8"]
      (is (false? (eth/valid-address? addr))))))
