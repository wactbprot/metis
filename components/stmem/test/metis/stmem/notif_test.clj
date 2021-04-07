(ns metis.stmem.notif-test
  (:require [clojure.test :refer :all]
            [metis.stmem.notif :refer :all]))

(deftest reg-key-i
  (testing "nil behaviour"
    (is (nil? (reg-key nil))))
  (testing "empty"
    (is (nil? (reg-key {})))))

(deftest reg-key-ii
  (testing "behaviour only mp-id"
    (is (= "a.*.*.*.00000"
           (reg-key {:stmem-key-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"}} {:mp-id "a"}))))
  (testing "behaviour :mp-id :struct"
    (is (= "a.b.*.*.00000"
           (reg-key {:stmem-key-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"}} {:mp-id "a" :struct :b}))))
  (testing "behaviour :mp-id :struct :no-idx"
    (is (= "a.b.00005.*.00000"
           (reg-key {:stmem-key-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"}} {:mp-id "a" :struct :b :no-idx 5}))))
  (testing "behaviour :mp-id :struct :no-idx :func"
    (is (= "a.b.00005.c.00000"
           (reg-key {:stmem-key-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"
                                   :c "c"}} {:mp-id "a" :struct :b :no-idx 5 :func :c})))))
