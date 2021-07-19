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

(deftest ensure-int-i
  (testing "nil behaviour"
    (is (nil? (ensure-int nil))))
  (testing "0"
    (is (= 0 (ensure-int "0"))))
  (testing "string"
    (is (= 0 (ensure-int "www"))))
  (testing "int already"
    (is (= 10 (ensure-int 10)))))
