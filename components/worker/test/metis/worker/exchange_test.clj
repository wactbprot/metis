
  (:require [clojure.test :refer :all]
            [metis.document.interface :as doc]
            [metis.ltmem.interface :as ltmem]
            [metis.stmem.interface :as stmem]
            [metis.worker.exchange :refer :all]))

(def conf
  (let [db "metis_test"
        usr (or (System/getenv "DB_ADMIN") (System/getenv "CAL_USR"))
        pwd (or (System/getenv "DB_PWD")  (System/getenv "CAL_PWD"))]
    {:ltmem-db db
     :ltmem-conn (str "http://"
                      (when (and usr pwd) (str usr ":" pwd "@"))
                      "127.0.0.1:5984/" db)}))

(def m {:mp-id "test"
        :struct :cont
        :no-idx 0
        :par-idx 0
        :seq-idx 0
        :func :state})

(deftest read-i
(let [doc-id "exch-read-test"]
  (ltmem/put-doc conf {:_id doc-id})
  (doc/renew conf [])
  (doc/add conf m doc-id)
  (testing ""
    (is (= doc-id (first (doc/ids m))))
    (write! {:ExchangePath "ExchPath"
             :Value {:Type "ind" :Unit "Pa" :Value 10}} m)
    (read! conf {:DocPath "Test" :ExchangePath "ExchPath"} m))))
