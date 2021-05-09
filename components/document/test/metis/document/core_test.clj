(ns metis.document.core-test
  (:require [clojure.test :refer :all]
            [metis.ltmem.interface :as ltmem]
            [metis.document.core :refer :all]))

(def conf
  (let [db "metis_test"
        usr (System/getenv "DB_ADMIN")
        pwd (System/getenv "DB_PWD")]
    {:ltmem-db db
     :ltmem-conn (str "http://"
                      (when (and usr pwd) (str usr ":" pwd "@"))
                      "127.0.0.1:5984/" db)}))

(deftest add-i
  (testing "do not add document to stmem wo id"
    (is (contains? (add {:mp-id "test"} "not-at-ltmem") :error)
        "")))

(deftest add-ii
  (testing "add document to stmem"
    (ltmem/put-doc conf {:_id "test"}) 
    (is (contains? (add conf {:mp-id "test"}  "test") :ok)
        "")
    (is (some #{"test"} (ids {:mp-id "test"}))
        "is there")))

(deftest rm-ii
  (testing "add document to stmem"
    (is (contains? (rm {:mp-id "test"} "test") :ok)
        "")
    (is (not (some #{"test"} (ids {:mp-id "test"})))
        "is there")))


(deftest renew-i
  (testing "renew documents"
    (ltmem/put-doc conf {:_id "test_a"})
    (ltmem/put-doc conf {:_id "test_b"})
    (renew conf {:mp-id "test"} ["test_a" "test_b"])
    (is (= #{"test_a" "test_b"} (set (ids {:mp-id "test"})))
        "")))


