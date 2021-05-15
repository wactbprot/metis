(ns metis.worker.wait
  ^{:author "wactbprot"
    :doc "wait worker."}
  (:require [com.brunobonacci.mulog  :as mu]
            [metis.stmem.interface :as stmem]
            [metis.utils.interface :as u]))

(defn wait!
  "Delays the `mp` for the time given with `:WaitTime`.

  Example:
  ```clojure
  (wait! {:WaitTime 1000 :mp-id \"test\" :struct :cont :func :defin :no-idx 0 :par-idx 0 :seq-idx 0})
  ```"
  [{wait-time :WaitTime :as task} m]
  (stmem/set-state-working  (assoc m :message "start waittime"))
  (Thread/sleep (u/ensure-int wait-time))
  (stmem/set-state-executed  (assoc m :message "wait time over")))

(comment
  (wait! {:WaitTime 1000} {:mp-id "test" :struct "test" :no-idx 0 :par-idx 0 :seq-idx 0}))
