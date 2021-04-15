(ns metis.stmem.trans-test
  (:require [clojure.test :refer :all]
            [metis.stmem.trans :refer :all]))

(deftest map->key-i
  (testing "nil behaviour"
    (is (nil? (map->key nil))))
  (testing "empty"
    (is (nil? (map->key {})))))

(deftest map->key-ii
  (let [config {:stmem-key-sep "@"
                :stmem-trans {:tasks "tasks"
                              :cont "container"
                              :defin "definition"
                              :defins "definitions"
                              :title "title"
                              :nc "ncont"
                              :nd "ndefins"
                              :state "state"
                              :ctrl "ctrl"
                              :exch "exchange"}}]
    (testing "task"
      (is (= "tasks@foo" (map->key config {:task-name "foo"}))))
    (testing "..."
      (is (nil? nil)))))

(deftest map->key-iii
  (testing "tasks clear pat"
    (is (= "tasks@*" (map->key {:mp-id "tasks" :task-name :*})))))

(deftest lpad-i
  (testing "longer string"
    (is (= (lpad {:stmem-key-pad-length 3} "000003")
           "003" )))
  (testing "shorter string"
    (is (= (lpad {:stmem-key-pad-length 3} "03")
           "003" ))))

(deftest lpad-ii
  (testing "integer"
    (is (= (lpad {:stmem-key-pad-length 3} 3)
           "003" )))
  (testing "integer"
    (is (= (lpad {:stmem-key-pad-length 5} 3)
           "00003" ))))

(deftest lpad-iii
  (testing "0"
    (is (= (lpad {:stmem-key-pad-length 3} 0)
           "000" )))
  (testing "nil"
    (is (nil? (lpad {:stmem-key-pad-length 5} nil)))))
