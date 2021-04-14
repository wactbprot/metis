(ns metis.scheduler.proc-test
  (:require [clojure.test :refer :all]
            [metis.scheduler.proc :refer :all]))

(deftest next-map-i
  (testing " returns (i)"
    (is (nil? (next-map nil))
        "nil .")
    (is (nil? (next-map {}))
        "nil .")
    (is (= {:seq-idx 0, :par-idx 0, :value "ready"}
           (next-map
            [{:seq-idx 0, :par-idx 0, :value "ready"}
             {:seq-idx 0, :par-idx 1, :value "ready"}
             {:seq-idx 1, :par-idx 0, :value "ready"}
             {:seq-idx 2, :par-idx 0, :value "ready"}
             {:seq-idx 3, :par-idx 0, :value "ready"}
             {:seq-idx 3, :par-idx 1, :value "ready"}]))
        "starts first par step")
    (is (= {:seq-idx 0, :par-idx 1, :value "ready"}
           (next-map
            [{:seq-idx 0, :par-idx 0, :value "working"}
             {:seq-idx 0, :par-idx 1, :value "ready"}
             {:seq-idx 1, :par-idx 0, :value "ready"}
             {:seq-idx 2, :par-idx 0, :value "ready"}
             {:seq-idx 3, :par-idx 0, :value "ready"}
             {:seq-idx 3, :par-idx 1, :value "ready"}]))
        "second par step")
    (is (nil?
         (next-map
          [{:seq-idx 0, :par-idx 0, :value "working"}
           {:seq-idx 0, :par-idx 1, :value "working"}
           {:seq-idx 1, :par-idx 0, :value "ready"}
           {:seq-idx 2, :par-idx 0, :value "ready"}
           {:seq-idx 3, :par-idx 0, :value "ready"}
           {:seq-idx 3, :par-idx 1, :value "ready"}]))
        "nothing to do (nil is returned)")
    (is (= {:seq-idx 0, :par-idx 1, :value "ready"}
           (next-map
            [{:seq-idx 0, :par-idx 0, :value :error}
             {:seq-idx 0, :par-idx 1, :value "ready"}
             {:seq-idx 1, :par-idx 0, :value "ready"}
             {:seq-idx 2, :par-idx 0, :value "ready"}
             {:seq-idx 3, :par-idx 0, :value "ready"}
             {:seq-idx 3, :par-idx 1, :value "ready"}]))
        "second par step (error is filtered by start-next)")
    (is (nil?
         (next-map
          [{:seq-idx 0, :par-idx 0, :value "executed"}
           {:seq-idx 0, :par-idx 1, :value "working"}
           {:seq-idx 1, :par-idx 0, :value "ready"}
           {:seq-idx 2, :par-idx 0, :value "ready"}
           {:seq-idx 3, :par-idx 0, :value "ready"}
           {:seq-idx 3, :par-idx 1, :value "ready"}]))
        "")
    (is (= {:seq-idx 1, :par-idx 0, :value "ready"}
           (next-map
            [{:seq-idx 0, :par-idx 0, :value "executed"}
             {:seq-idx 0, :par-idx 1, :value "executed"}
             {:seq-idx 1, :par-idx 0, :value "ready"}
             {:seq-idx 2, :par-idx 0, :value "ready"}
             {:seq-idx 3, :par-idx 0, :value "ready"}
             {:seq-idx 3, :par-idx 1, :value "ready"}]))
        "")
    (is (= {:seq-idx 1, :par-idx 0, :value "ready"}
           (next-map
            [{:seq-idx 0, :par-idx 0, :value "executed"}
             {:seq-idx 0, :par-idx 1, :value "executed"}
             {:seq-idx 1, :par-idx 0, :value "ready"}
             {:seq-idx 2, :par-idx 0, :value "executed"}
             {:seq-idx 3, :par-idx 0, :value "ready"}
             {:seq-idx 3, :par-idx 1, :value "ready"}]))
        "")
    (is (= {:seq-idx 3, :par-idx 1, :value "ready"}
           (next-map
            [{:seq-idx 0, :par-idx 0, :value "executed"}
             {:seq-idx 0, :par-idx 1, :value "executed"}
             {:seq-idx 1, :par-idx 0, :value "executed"}
             {:seq-idx 2, :par-idx 0, :value "executed"}
             {:seq-idx 3, :par-idx 0, :value "working"}
             {:seq-idx 3, :par-idx 1, :value "ready"}]))
        "last step")
    (is (nil?
           (next-map
            [{:seq-idx 0, :par-idx 0, :value "executed"}
             {:seq-idx 0, :par-idx 1, :value "executed"}
             {:seq-idx 1, :par-idx 0, :value "executed"}
             {:seq-idx 2, :par-idx 0, :value "executed"}
             {:seq-idx 3, :par-idx 0, :value "executed"}
             {:seq-idx 3, :par-idx 1, :value "executed"}]))
        "last step")))
