(ns metis.exchange.core-test
  (:require [clojure.test :refer :all]
            [metis.exchange.core :refer :all]))


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

(deftest stop-if-i
  (testing "StopIf"
    (is (= true (stop-if a {:StopIf "C"}))
        "returns true on ok")
    (is (= false (stop-if a {:StopIf "D"}))
        "returns false if not exist")
    (is (= true (stop-if a {:StopIf "E.Ready"}))
        "returns true on ok")
    (is (= false (stop-if a {:StopIf "F.Ready"}))
        "returns false if not exist")
    (is (= false (stop-if a {:StopIf "D"}))
        "returns false if not exist")
    (is (= true (stop-if a {}))
        "returns false if kw not exist")))

(deftest run-if-i
  (testing "RunIf"
    (is (= true (run-if a {:RunIf "C"}))
        "returns true on ok")
    (is (= false (run-if a {:RunIf "D"}))
        "returns false if not exist")
    (is (= true (run-if a {:RunIf "E.Ready"}))
        "returns true on ok")
    (is (= false (run-if a {:RunIf "F.Ready"}))
        "returns false if not exist")
    (is (= false (run-if a {:RunIf "D"}))
        "returns false if not exist")
    (is (= true (run-if a {}))
        "returns false if kw not exist")))

(deftest only-if-not-i
  (testing "RunIf"
    (is (= false (only-if-not a {:OnlyIfNot "C"}))
        "returns false on ok")
    (is (= true (only-if-not a {:OnlyIfNot "D"}))
        "returns true on not ok")
    (is (= true (only-if-not a {}))
        "pass if not pressent")
    (is (= false (only-if-not a {:OnlyIfNot "X"}))
        "p does not exist")
    (is (= false (only-if-not a {:OnlyIfNot "X.Y"}))
        "p does not exist")))

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

