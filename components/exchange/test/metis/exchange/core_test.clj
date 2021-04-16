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

(def t {:ToExchange {:Filling_Pressure_current {:Value 100 
                                                :Unit "mbar"}
                     :Filling_Pressure_Dev {:Value 0.5 
                                            :Unit "1"}
                     :Filling_Pressure_Ok {:Ready false}}}) 
  
(deftest to-vec-i
  (testing "to ."
    (is (= [{:no-idx "B" :value {:A 1}}]  (to-vec a {:no-idx "B" :value {:A 1}})))
    "stores string")
  (testing "to .."
    (is (= [{:no-idx "E" :value {:Ready "true" :A 1}}]  (to-vec a {:no-idx "E" :value {:A 1}})))
    "stores string")
  (testing "to ..."
    (is (= [{:no-idx "B" :value {:A 1}}]  (to-vec a {:no-idx nil :value {:B {:A 1}}})))
    "stores string")
  (testing "to ..."
    (is (= [{:no-idx "B" :value {:C {:A 1}}}]  (to-vec a {:no-idx "B.C" :value {:A 1}})))
        "stores string"))


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


(deftest enclose-map-i
  (testing "nil behaviour"
    (is (nil? (enclose-map nil nil))))
  (testing "."
    (is (= {} (enclose-map {} nil))))
  (testing ".."
    (is (= {"k" {:a 1}} (enclose-map {:a 1} "k"))))
  (testing "..."
    (is (= {"k" {:b {:a 1}}} (enclose-map {:a 1} "k.b")))))


(comment
(deftest to-i
  (testing "to!"
    (to! "test" {:B "aaa"})
    (is (= "aaa" (st/key->val (stu/exch-key "test" "B")))
        "stores string")))

(deftest to-ii
  (testing "to! with simple path"
    (to! "test" {:B "aaa"} "dd")
    (is (=  "aaa"  (:B (st/key->val (stu/exch-key "test" "dd"))))
        "stores string under path ")))

(deftest to-iii
  (testing "to! with double path"
    (to! "test" {:C "aaa"} "ee.ff")
    (is (= {:ff {:C "aaa"}} (st/key->val (stu/exch-key "test" "ee")))
        "stores string under path ")))

(deftest to-iv
  (testing "to! with nil path"
    (to! "test" {:B "aaa"} nil)
    (is (= "aaa"  (st/key->val (stu/exch-key "test" "B")))
        "don't crash ")))

(deftest to-v
  (testing "to! with map"
    (to! "test" {:C {:D "aaa"}})
    (is (= {:D "aaa"} (st/key->val (stu/exch-key "test" "C")))
        "stores map")))
)
