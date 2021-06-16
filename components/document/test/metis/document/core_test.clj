(ns metis.document.core-test
  (:require [clojure.test :refer :all]
            [metis.ltmem.interface :as ltmem]
            [metis.document.core :refer :all]))

(def conf
  (let [db "metis_test"
        usr (or (System/getenv "DB_ADMIN") (System/getenv "CAL_USR"))
        pwd (or (System/getenv "DB_PWD")  (System/getenv "CAL_PWD"))]
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
  (testing "rm document from stmem"
    (is (contains? (rm {:mp-id "test"} "test") :ok)
        "")
    (is (not (some #{"test"} (ids {:mp-id "test"})))
        "is there")))

(deftest renew-rm-i
  (testing "renew documents"
    (ltmem/put-doc conf {:_id "test_a"})
    (ltmem/put-doc conf {:_id "test_b"})
    (is (contains?
         (renew conf {:mp-id "test"} ["test_a" "test_b"]) :ok)) 
    (is (= #{"test_a" "test_b"} (set (ids {:mp-id "test"})))
        "contains a and b")
    (is (contains?
         (rm {:mp-id "test"} "test_a") :ok)) 
    (is (= #{"test_b"} (set (ids {:mp-id "test"})))
        "contains b")
    (is (contains?
         (rm {:mp-id "test"} "test_b") :ok)) 
    (is (empty? (ids {:mp-id "test"}))
        "contains nothing")))

(deftest store-results-i
  (testing "renew documents"
    (let [_       (add conf {:mp-id "test"} "test")
          old-rev (:_rev (ltmem/get-doc conf "test"))
          _       (store-results conf
                                 {:mp-id "test"}
                                 [{:Test "test"}]
                                 "test.path")
          new-rev (:_rev (ltmem/get-doc conf "test"))]
      (is (not= old-rev new-rev)))))

