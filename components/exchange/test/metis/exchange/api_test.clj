(ns metis.exchange.api-test
  (:require [clojure.test :refer :all]
            [metis.exchange.api :refer :all]))


(def a {"A" {:Type "ref" :Unit "Pa" :Value 100.0}
        "B" "token"
        "C" "true"
        "D" "false"
        "E" {:Ready "true"}
        "F" {:Ready "false"}
        "foo.bar" 100
        "Ref_gas" {:Selected "N2"
                   :Select
                   [{:value "N2" :display "Stickstoff"}
                    {:value "Ar" :display "Argon"}]
                   :Ready false}
        "Target_pressure" {:Selected 1 :Unit "Pa" :Ready true}})

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

