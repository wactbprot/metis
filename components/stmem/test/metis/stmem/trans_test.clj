(ns metis.stmem.trans-test
  (:require [clojure.test :refer :all]
            [metis.stmem.trans :as trans]))

(deftest map->key-i
  (testing "nil behaviour"
    (is (nil? (trans/map->key nil))))
  (testing "empty"
    (is (nil? (trans/map->key {})))))
