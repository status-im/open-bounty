(ns commiteth.test.webhooks
    (:require [clojure.test :refer :all]
              [commiteth.routes.webhooks :as webhooks]))


(deftest test-issue-number-extraction
  (testing "Basic fixes case from PR body"
    (let [title "foo"
          body "fixes #123"]
      (is (= '(123) (webhooks/extract-issue-number body title)))))
  (testing "Basic fixes case from PR title"
    (let [title "My title (fixes: #123)"
          body "no use for a body"]
      (is (= '(123) (webhooks/extract-issue-number body title)))))
  (testing "Commented issue number ignored in PR body"
    (let [title "foo"
          body "
fixes #123
[comment]: # (To auto-close issue on merge, please insert the related issue number after # i.e fixes #566)
"]
      (is (= '(123) (webhooks/extract-issue-number body title))))))
