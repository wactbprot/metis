(ns metis.stmem.notif-test
  (:require [clojure.test :refer :all]
            [metis.stmem.notif :refer :all]))

(deftest reg-key-i
  (testing "nil behaviour"
    (is (nil? (reg-key nil))))
  (testing "empty"
    (is (nil? (reg-key {})))))
