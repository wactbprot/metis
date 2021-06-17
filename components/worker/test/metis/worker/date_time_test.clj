(ns metis.worker.date-time-test
  (:require [clojure.test :refer :all]
            [metis.document.interface :as doc]
            [metis.ltmem.interface :as ltmem]
            [metis.stmem.interface :as stmem]
            [metis.exchange.interface :as exch]
            [metis.worker.date-time :refer :all]))

(def conf
  (let [db "metis_test"
        usr (or (System/getenv "DB_ADMIN") (System/getenv "CAL_USR"))
        pwd (or (System/getenv "DB_PWD")  (System/getenv "CAL_PWD"))]
    {:ltmem-db db
     :ltmem-conn (str "http://"
                      (when (and usr pwd) (str usr ":" pwd "@"))
                      "127.0.0.1:5984/" db)}))

(def m {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :state})

(deftest store-date-i
  (testing "date is written to doc and exchange."
    (let [doc-id "date-time-test"] 
      (doc/renew conf m [])
      (doc/add conf m doc-id)
      (is (= doc-id (first (doc/ids m))))
      (ltmem/put-doc conf {:_id doc-id})
      (store-date! conf {:Type "test" :DocPath "Test" :ExchangePath "Test"} m)
      (is (= "test"
             (get-in (ltmem/get-doc conf doc-id) [:Test 0 :Type])))
      (is (= "test"
             (:Type  (stmem/get-val {:mp-id "test" :struct :exch :exchpath "Test"})))))))
