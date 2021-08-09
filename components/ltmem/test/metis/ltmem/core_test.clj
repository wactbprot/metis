(ns metis.ltmem.core-test
  (:require [clojure.test :refer :all]
            [com.ashafa.clutch :as couch]
            [metis.ltmem.core :refer :all]))

(def conf
  (let [db "metis_test"
        usr (or (System/getenv "DB_ADMIN") (System/getenv "CAL_USR"))
        pwd (or (System/getenv "DB_PWD")  (System/getenv "CAL_PWD"))]
    {:ltmem-db db
     :ltmem-conn (str "http://"
                      (when (and usr pwd) (str usr ":" pwd "@"))
                      "127.0.0.1:5984/" db)}))

(deftest gen-db-i
  (testing "generation"
    (is (= (:ltmem-db conf) (:db_name (couch/get-database (:ltmem-conn conf))))
        "db or exist (needs env var DB_ADMIN and DB_PWD)")))

(deftest safe-map-i
  (testing "nil behaviour"
    (is (nil? (safe nil))))
  (testing "empty"
    (is (empty? (safe {}))))
  (testing "simple keep"
    (is (= {:a "@123"}  (safe {:a "@123"}))))
  (testing "simple change"
    (is (= {:a "%foo"}  (safe {:a "@foo"}))))
  (testing "cuts a string (i)"
    (is (= {:a "(@101,102)"} (safe {:a "(@101,102)"})) 
        "conserves @ infront of numbers")
    (is (= {:a "foo (@101,102) bar"} (safe {:a "foo (@101,102) bar"})) 
        "conserves @ infront of numbers")
    (is (= {:a "%foo (@101,102)%bar"} (safe {:a "@foo (@101,102)%bar"})) 
        "conserves @ infront of numbers")))
