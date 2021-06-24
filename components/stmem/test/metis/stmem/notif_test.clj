(ns metis.stmem.notif-test
  (:require [metis.stmem.api :as api]
            [clojure.test :refer :all]
            [metis.stmem.notif :refer :all]))

(deftest reg-key-i
  (testing "nil behaviour"
    (is (nil? (reg-key nil))))
  (testing "empty"
    (is (nil? (reg-key {})))))

(deftest reg-key-ii
  (testing "behaviour only mp-id"
    (is (= "a.*.*.**.00000"
           (reg-key {:stmem-notif-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"}} {:mp-id "a"}))))
  (testing "behaviour :mp-id :struct"
    (is (= "a.b.*.**.00000"
           (reg-key {:stmem-notif-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"}} {:mp-id "a" :struct :b}))))
  (testing "behaviour :mp-id :struct :no-idx"
    (is (= "a.b.00005.**.00000"
           (reg-key {:stmem-notif-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"}} {:mp-id "a" :struct :b :no-idx 5}))))
  (testing "behaviour :mp-id :struct :no-idx :func"
    (is (= "a.b.00005.c*.00000"
           (reg-key {:stmem-notif-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"
                                   :c "c"}} {:mp-id "a" :struct :b :no-idx 5 :func :c}))))
  (testing "behaviour :mp-id :struct :no-idx :func :level"
    (is (= "a.b.00005.c*.00002"
           (reg-key {:stmem-notif-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"
                                   :c "c"}} {:mp-id "a" :struct :b :no-idx 5 :func :c :level 2})))))

(deftest register-de-register-i
  (testing "re-de-reg works"
    (let [r (atom (rand-int 1000))
          x @r 
          m {:mp-id "test" :struct :cont :no-idx 0 :func :state :seq-idx 0 :par-idx 0 :value x}
          f (fn [m] (swap! r dec))]
      (is (= x  @r))
      (register m f)
      (is (= x @r)
          "The register event triggers but callback is not executed.")
      (api/set-val (assoc m :value (dec @r)))
      (is (= (dec x)
             @r))
      (api/set-val (assoc m :value (dec @r)))
      (is (= (dec (dec x))
             @r))
      (api/set-val (assoc m :value (dec @r)))
      (is (= (dec (dec (dec x)))
             @r))
      (api/set-val (assoc m :value (dec @r)))
      (is (= (dec (dec (dec (dec  x))))
             @r))
      (de-register (assoc m :seq-idx :*))
      (api/set-val (assoc m :value (dec @r)))
      (is (= (dec (dec (dec (dec  x))))
             @r)
          "The set-val function does not trigger after de-register."))))
