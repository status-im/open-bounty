(ns commiteth.test.github
  (:require [clojure.test :refer :all]
            [commiteth.routes.webhooks :refer [extract-issue-number]]))

(deftest test-github-keywords
  (testing "Several keywords in mixed case"
    (let [res (set (extract-issue-number
                     "Fixes #12 and cloSes       #000028 and also resolved \n#32"))]
      (is (= #{12 28 32} res))))
  (testing "Ignoring big numbers and zeroes"
    (let [res (set (extract-issue-number
                     "Fixes #298374298229087345 and closes #0xFFEF"))]
      (is (= #{} res)))))
