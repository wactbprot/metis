(ns metis.document.core-test
  (:require [clojure.test :refer :all]
            [metis.document.core :refer :all]))

(deftest add-i
  (testing "do not add document to stmem wo id"
    (is (contains? (add {:mp-id "test"} {}) :error)
        "")))

(deftest add-ii
  (testing "add document to stmem"
    (is (contains? (add {:mp-id "test"} {:_id "test"}) :ok)
        "")))
 

