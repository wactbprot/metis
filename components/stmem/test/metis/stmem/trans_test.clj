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
    (is (= "tasks@*" (map->key {:mp-id "tasks" :task-name :*}))))
  (testing "exchange clear pat"
    (is (= "test@exchange@*" (map->key {:mp-id "test" :struct :exch :exchpath :*}))))
  (testing "id clear pat"
    (is (= "test@id@*" (map->key {:mp-id "test" :struct :id :doc-id :*}))))
  (testing "meta clear pat"
    (is (= "test@meta@*" (map->key {:mp-id "test" :struct :meta :metapath :*}))))
  (testing "seq-idx clear pat"
    (is (= "test@container@000@state@*@000" (map->key {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx :* :func :state}))))
  (testing "par-idx clear pat"
    (is (= "test@container@000@state@000@*" (map->key {:mp-id "test" :struct :cont :no-idx 0 :par-idx :* :seq-idx 0 :func :state}))))
  (testing "no-idx clear pat"
    (is (= "test@container@*@state@000@000" (map->key {:mp-id "test" :struct :cont :no-idx :* :par-idx 0 :seq-idx 0 :func :state})))))

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


(deftest key->map-i
  (testing "nil behaviour"
    (is (nil? (key->map nil))))
  (testing "empty mp-id"
    (is (= {:mp-id "",
            :struct nil,
            :no-idx nil,
            :func nil,
            :seq-idx nil,
            :par-idx nil} (key->map ""))))
  (testing "mp-id"
    (is (= {:mp-id "a",
            :struct nil,
            :no-idx nil,
            :func nil,
            :seq-idx nil,
            :par-idx nil} (key->map "a"))))
  (testing "mp-id no-idx is kept if string"
    (is (= {:mp-id "a",
            :struct :cont,
            :no-idx "c",
            :func nil,
            :seq-idx nil,
            :par-idx nil} (key->map "a@container@c"))))
  (testing "mp-id no-idx is turned to number"
    (is (= {:mp-id "a",
            :struct :cont,
            :no-idx 5,
            :func nil,
            :seq-idx nil,
            :par-idx nil} (key->map "a@container@00000005")))))
