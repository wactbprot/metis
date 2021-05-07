(ns metis.ltmem.core-test
  (:require [clojure.test :refer :all]
            [com.ashafa.clutch :as couch]
            [metis.ltmem.core :refer :all]))

(def conf
  (let [db "metis_test"
        usr (System/getenv "DB_ADMIN")
        pwd (System/getenv "DB_PWD")]
    {:ltmem-db db
     :ltmem-conn (str "http://"
                      (when (and usr pwd) (str usr ":" pwd "@"))
                      "127.0.0.1:5984/" db)}))

(deftest gen-db-i
  (testing "generation"
    (is (= (:ltmem-db conf) (:db_name (couch/get-database (:ltmem-conn conf))))
        "db or exist (needs env var DB_ADMIN and DB_PWD)")))

