(ns metis.worker.devhub-test
  (:require [clojure.test :refer :all]
            [metis.worker.devhub :refer :all]
            [metis.stmem.interface :as stmem]
            [metis.worker.resp :as resp]))


(def conf
  {:json-post-header {:content-type :json
                      :accept :json}
   :dev-hub-url "http://localhost:9009/stub"})

(def task {:TaskName "IM540-read_out"})

(def m {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :state})

(deftest devhub-i
  (testing "stub communication"
    (is (= {:ok true} (devhub! conf task m)))
    (is (= "executed" (stmem/get-val m)))))
