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
                :stmem-trans {:cont "container"
                              :defin "definition"
                              :defins "definitions"
                              :title "title"
                              :nc "ncont"
                              :nd "ndefins"
                              :state "state"
                              :msg "message"
                              :ctrl "ctrl"
                              :exch "exchange"}}]))

(deftest map->key-iii
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

(deftest map->key-iv
  (testing "exchange key"
    (is (= "test@exchange@foo" (map->key {:mp-id "test" :struct :exch :exchpath "foo"}))))
  (testing "id clear pat"
    (is (= "test@id@foo" (map->key {:mp-id "test" :struct :id :doc-id "foo"}))))
  (testing "meta clear pat"
    (is (= "test@meta@ncont" (map->key {:mp-id "test" :struct :meta :metapath :nc}))))
  (testing "seq-idx key"
    (is (= "test@container@000@state@000@000" (map->key {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :state}))))
  (testing "par-idx key"
    (is (= "test@container@000@state@000@000" (map->key {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :state}))))
  (testing "no-idx key"
    (is (= "test@container@000@state@000@000" (map->key {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :state}))))
  (testing "message key wit seq-idx and par-idx"
    (is (= "test@container@000@message" (map->key {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :msg})))))

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
    (is (= {:mp-id ""} (key->map ""))))
  (testing "mp-id"
    (is (= {:mp-id "a"} (key->map "a"))))
  (testing "mp-id no-idx is kept if string"
    (is (= {:mp-id "a"
            :struct :cont
            :no-idx "c"} (key->map "a@container@c"))))
  (testing "mp-id no-idx is turned to number"
    (is (= {:mp-id "a"
            :struct :cont
            :no-idx 5} (key->map "a@container@00000005"))))
  (testing "exchange is cared"
    (is (= {:mp-id "a"
            :struct :exch
            :exchpath "A"} (key->map "a@exchange@A")))))
