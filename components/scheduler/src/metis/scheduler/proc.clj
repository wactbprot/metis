(ns metis.scheduler.proc
  ^{:author "wactbprot"
    :doc "Finds and starts the up comming tasks of a certain container."}
  (:require [com.brunobonacci.mulog :as mu]
            ))
  
(defn filter-state
  "Returns a vector of maps where state is `s`."
  [v s]
  (filterv (fn [m] (= s (:value m))) v))

(defn filter-seq-idx
  "Returns a vector of maps where state is `s`."
  [v i]
  (filterv (fn [m] (= i (:seq-idx m))) v))

(defn all-error
  "Returns all steps with the state `:error` for a given state vector
  `v`"
  [v]
  (filter-state v "error"))

(defn all-ready
  "Returns all steps with the state `:ready` for a given state vector
  `v`"
  [v]
  (filter-state v "ready"))

(defn all-executed
  "Returns all-executed entrys of the given vector `v`.

  Example:
  ```clojure
    (all-executed (stu/seq-idx->all-par
                      [{:seq-idx 0 :par-idx 0 :state :executed}
                       {:seq-idx 0 :par-idx 0 :state :ready}]
                     0))
  ;; =>
  ;; [{:seq-idx 0 :par-idx 0 :state :executed}]
  ```"
  [v]
  (filter-state v "executed"))

(defn all-executed?
  "Checks if all entries of map `m` are executed"
  [v]
  (= (count v) (count (all-executed v))))

(defn errors?
  "Checks if there are any errors in the
  vector of maps `v`."
  [v]
  (not (empty? (all-error v))))

(defn next-ready
  "Returns a map (or `nil`) with the next step with state `:ready`."
  [v]
  (first (all-ready v)))

(defn predecessors-executed?
  "Checks if the steps of `v` before `i` are `all-executed?`. Returns
  `true` for `i` equal to zero (or negative `i`)"
  [v i]
  (if (pos? i)
    (every? true? (map
                   (fn [j] (all-executed? (filter-seq-idx v j)))
                   (range i)))
      true))


;;------------------------------
;; choose and start next task
;;------------------------------
(defn next-map
  "The `next-map` function returns a map containing the next step to
  start. See `cmp.state-test/next-map-i` for examples how `next-map`
  should work.

  Example:
  ```clojure
   (next-map [{:seq-idx 0 :par-idx 0 :state :executed}
              {:seq-idx 0 :par-idx 1 :state :executed}
              {:seq-idx 1 :par-idx 0 :state :executed}
              {:seq-idx 2 :par-idx 0 :state :executed}
              {:seq-idx 3 :par-idx 0 :state :working}
              {:seq-idx 3 :par-idx 1 :state :ready}])
  ;; =>
  ;; {:seq-idx 3 :par-idx 1 :state :ready}
  ```"
  [v]
  (when-let [next-m (next-ready v)]
    (when-let [i (:seq-idx next-m)]
      (when (or (zero? i) (predecessors-executed? v i))
        next-m))))
