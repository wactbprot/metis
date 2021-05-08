(ns metis.document.core-test
  (:require [clojure.test :refer :all]
            [metis.document.core :refer :all]))

(deftest add-i
  (testing "add document to stmem"
    (is (contains? (add {:mp-id "test"} {}) :error)
        "")))
 

