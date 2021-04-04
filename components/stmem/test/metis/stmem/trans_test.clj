(ns metis.stmem.trans-test
  (:require [clojure.test :refer :all]
            [metis.stmem.trans :as trans]))

(deftest map->key-i
  (testing "nil behaviour"
    (is (nil? (trans/map->key nil))))
  (testing "empty"
    (is (nil? (trans/map->key {})))))

(deftest lpad-i
  "  
  Example:
  ```clojure
   (u/lp 2)
  ;; \"002\"
  (u/lp \"02\")
  ;; \"002\"
  (u/lp 2)
  ;; \"002\"
  (u/lp true)
  ;; \"000\"
  (u/lp \"003\")
  ;; \"003\"
  (u/lp \"000003\")
  ;; \"003\"
  ```"
  (testing "nil behaviour"
    (is (nil? nil))))

(deftest lpad-i
  "  
  Example:
  ```clojure
  (u/ensure-int 100)
  ;; 100
  (u/ensure-int \"w\")
  ;; 0
  (u/ensure-int \"00\")
  ;; 0
  (u/ensure-int \"10\")
  ;; 10
  (u/ensure-int true)
  ;; 0
  ```"
  (testing "nil behaviour"
    (is (nil? nil))))