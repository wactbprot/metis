(ns metis.worker.resp-test
  (:require [clojure.test :refer :all]
            [metis.stmem.interface :as stmem]
            [metis.worker.resp :refer :all]))

(def m {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :state})

(def value {:Type "ind" :Unit "Pa" :Value 1})

(deftest dispatch-i
  (testing "nil behaviour"
    (is (contains? (dispatch nil nil nil) :ok))))

(deftest do-retry-i
  (testing "nil behaviour"
    (is (contains? (do-retry nil) :ok))))

(deftest check-i
  (testing "nil behaviour"
    (is (nil? (check nil nil nil)))))


#_(deftest write-to-exchange
  (testing "nil behaviour"
    (is (nil? (check nil nil nil)))))

