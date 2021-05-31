(ns metis.worker.message
  ^{:author "wactbprot"
    :doc "message worker."}
  (:require [metis.config.interface :as c]
            [com.brunobonacci.mulog :as µ]
            [metis.stmem.interface :as stmem]
            [metis.utils.interface :as u]))

(defn message!
  "Writes a `:Message` to the message interface. Continues if message is replaced by
  something in the [[u/ok-set]].
  
  ```clojure
  (def m {:mp-id \"test\" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :resp})
  (message! {:Message \"go on?\"} m)
   ```"
  [{msg :Message} m]
  (stmem/set-state-working m)
  (let [msg-m (assoc m :func :msg :value msg :level 2)
        f     (fn [_]
                   (when (contains? u/ok-set (stmem/get-val msg-m))
                     (stmem/set-state-executed (assoc m :message "ready callback for message worker"))
                     (stmem/de-register msg-m)))]
    (stmem/set-val msg-m)
    (stmem/register msg-m f)))

(comment
  (def m {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :resp}))