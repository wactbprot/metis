(ns metis.model.core-test
  (:require [clojure.test :refer :all]
            [metis.model.core :refer :all]))


(deftest safe-map-i
  (testing "nil behaviour"
    (is (nil? (safe nil))))
  (testing "empty"
    (is (empty? (safe {}))))
  (testing "simple keep"
    (is (= {:a "@123"}  (safe {:a "@123"}))))
  (testing "simple change"
    (is (= {:a "%foo"}  (safe {:a "@foo"}))))
  (testing "cuts a string (i)"
    (is (= {:a "(@101,102)"} (safe {:a "(@101,102)"})) 
        "conserves @ infront of numbers")
    (is (= {:a "foo (@101,102) bar"} (safe {:a "foo (@101,102) bar"})) 
        "conserves @ infront of numbers")
    (is (= {:a "%foo (@101,102)%bar"} (safe {:a "@foo (@101,102)%bar"})) 
        "conserves @ infront of numbers")))
