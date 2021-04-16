(ns metis.exchange.core-test
  (:require [clojure.test :refer :all]
            [metis.exchange.core :refer :all]))


(def a {"A" {:Type "ref" :Unit "Pa" :Value 100.0}
        "B" "token"
        "foo.bar" 100
        "Ref_gas" {:Selected "N2"
                   :Select
                   [{:value "N2" :display "Stickstoff"}
                    {:value "Ar" :display "Argon"}]
                   :Ready false}
        "Target_pressure" {:Selected 1 :Unit "Pa"}})


(deftest from-i
  (testing "nil behaviour"
    (is (nil? (from nil nil))))
  (testing "empty"
    (is (empty? (from {} {}))))
  (testing "simple"
    (is (= {:%check (get a "A")}
           (from a {:%check "A"}))))
  (testing "simple ."
    (is (= {:%check (get a "foo.bar")}
           (from a {:%check "foo.bar"}))))
  (testing "."
    (is (= {:%check (:Type (get a "A"))}
           (from a {:%check "A.Type"})))))
