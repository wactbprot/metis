(ns metis.worker.interface-test
  (:require [clojure.test :refer :all]
            [metis.worker.resp :refer :all]))

(deftest dispatch-i
  (testing "nil behaviour"
    (is (contains? (dispatch nil nil nil) :ok))))

(deftest do-retry-i
  (testing "nil behaviour"
    (is (contains? (do-retry nil) :ok))))

(deftest check-i
  (testing "nil behaviour"
    (is (nil? (check nil nil nil)))))
