(ns metis.worker.exchange-test
  (:require [clojure.test :refer :all]
            [metis.document.interface :as doc]
            [metis.ltmem.interface :as ltmem]
            [metis.stmem.interface :as stmem]
            [metis.exchange.interface :as exch]
            [metis.worker.exchange :refer :all]))

(def conf
  (let [db "metis_test"
        usr (or (System/getenv "DB_ADMIN") (System/getenv "CAL_USR"))
        pwd (or (System/getenv "DB_PWD")  (System/getenv "CAL_PWD"))]
    {:ltmem-db db
     :ltmem-conn (str "http://"
                      (when (and usr pwd) (str usr ":" pwd "@"))
                      "127.0.0.1:5984/" db)}))

(def m {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :state})

(def value {:Type "ind" :Unit "Pa" :Value 1})

(deftest write-i
  (testing "Write to stmem exchpath even with wrong path."
    (let [path "Test"]
      (write! {:ExchangePath path :Value value} m)
      (is (= value
             (stmem/get-val {:mp-id "test" :struct :exch :exchpath path})))
      (write! {:ExchangePath "Test.Unit" :Value "mbar"} {:mp-id "test"})
      (is (= (assoc value :Unit "mbar")
             (stmem/get-val {:mp-id "test" :struct :exch :exchpath path}))))))

(deftest read-i
  (testing "Read from stmem (with wrong path) and write to ltmem."
    (let [doc-id      "exch-read-test"
          exch-path   "ExchPath"
          doc-path    "Test"
          n           (rand-int 10000)]
      (ltmem/put-doc conf {:_id doc-id})
      (doc/renew conf m [])
      (is (empty? (first (doc/ids m))))
      (doc/add conf m doc-id)
      (is (= doc-id (first (doc/ids m))))
      (write! {:ExchangePath exch-path :Value (assoc value :Value n)} m)
      (is (=  n (:Value (get (exch/all m) exch-path))))
      (read! conf {:DocPath doc-path :ExchangePath exch-path} m)
      (is (= n (get-in (ltmem/get-doc conf doc-id) [:Test 0 :Value 0]))))))



