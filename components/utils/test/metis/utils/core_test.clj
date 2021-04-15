(ns metis.utils.core-test
  (:require [clojure.test :refer :all]
            [metis.utils.core :refer :all]))


(deftest short-string-i
  (testing "cuts a string (i)"
    (is (= "aaa" (short-string "aaa")) 
        "cuts a short string")
    (is (nil? (short-string nil)) 
        "dont crash on nil")
    (is (= "" (short-string "")) 
        "cuts an empty string")
    (is (= "..." (short-string "a" 0)) 
        "adds dots if shorten")))

(deftest safe-map-i
  (testing "nil behaviour"
    (is (nil? (map->safe-map nil))))
  (testing "empty"
    (is (empty? (map->safe-map {}))))
  (testing "simple keep"
    (is (= {:a "@123"}  (map->safe-map {:a "@123"}))))
  (testing "simple change"
    (is (= {:a "%foo"}  (map->safe-map {:a "@foo"}))))
  (testing "cuts a string (i)"
    (is (= {:a "(@101,102)"} (map->safe-map {:a "(@101,102)"})) 
        "conserves @ infront of numbers")
    (is (= {:a "foo (@101,102) bar"} (map->safe-map {:a "foo (@101,102) bar"})) 
        "conserves @ infront of numbers")
    (is (= {:a "%foo (@101,102)%bar"} (map->safe-map {:a "@foo (@101,102)%bar"})) 
        "conserves @ infront of numbers")))

(deftest ensure-int-i
  (testing "nil behaviour"
    (is (nil? (ensure-int nil))))
  (testing "0"
    (is (= 0 (ensure-int "0"))))
  (testing "string"
    (is (= 0 (ensure-int "www"))))
  (testing "int already"
    (is (= 10 (ensure-int 10)))))
