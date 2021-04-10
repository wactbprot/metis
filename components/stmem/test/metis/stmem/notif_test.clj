(ns metis.stmem.notif-test
  (:require [clojure.test :refer :all]
            [metis.stmem.trans :as trans]
            [metis.stmem.notif :refer :all]))

(deftest reg-key-i
  (testing "nil behaviour"
    (is (nil? (reg-key nil))))
  (testing "empty"
    (is (nil? (reg-key {})))))

(deftest reg-key-ii
  (testing "behaviour only mp-id"
    (is (= "a.*.*.*.00000"
           (reg-key {:stmem-notif-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"}} {:mp-id "a"}))))
  (testing "behaviour :mp-id :struct"
    (is (= "a.b.*.*.00000"
           (reg-key {:stmem-notif-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"}} {:mp-id "a" :struct :b}))))
  (testing "behaviour :mp-id :struct :no-idx"
    (is (= "a.b.00005.*.00000"
           (reg-key {:stmem-notif-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"}} {:mp-id "a" :struct :b :no-idx 5}))))
  (testing "behaviour :mp-id :struct :no-idx :func"
    (is (= "a.b.00005.c.00000"
           (reg-key {:stmem-notif-sep "."
                     :stmem-key-pad-length 5
                     :stmem-trans {:* "*"
                                   :b "b"
                                   :c "c"}} {:mp-id "a" :struct :b :no-idx 5 :func :c})))))

(def reg-test (atom nil))

(deftest register-de-register-i
  (testing "re-de-reg works"
    (let [x (rand-int 1000)
          _ (reset! reg-test x)
          m {:mp-id "test" :struct :cont :no-idx 0 :func :state :seq-idx 0 :value x}
          f (fn [v] (swap! reg-test dec))]
      (is (= x  @reg-test))
      (register (assoc m :seq-idx :*) f)
      ;; (Thread/sleep 1)
      (is (= x @reg-test)
          "register event triggers but callback is not executed")
      (trans/set-val (assoc m :value (dec @reg-test)))
      ;; (Thread/sleep 1)
      (is (= (dec x)
             @reg-test))
      (trans/set-val (assoc m :value (dec @reg-test)))
      ;; (Thread/sleep 1)
      (is (= (dec (dec x))
             @reg-test))
      (trans/set-val (assoc m :value (dec @reg-test)))
      ;; (Thread/sleep 1)
      (is (= (dec (dec (dec x)))
             @reg-test))
      (trans/set-val (assoc m :value (dec @reg-test)))
      ;; (Thread/sleep 1)
      (is (= (dec (dec (dec (dec  x))))
             @reg-test))
      (de-register (assoc m :seq-idx :*))
      (trans/set-val (assoc m :value (dec @reg-test)))
      ;; (Thread/sleep 1)
      (is (= (dec (dec (dec (dec  x))))
             @reg-test)))))
