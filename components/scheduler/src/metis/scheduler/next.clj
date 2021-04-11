(ns metis.scheduler.next
  ^{:author "wactbprot"
    :doc "Finds and starts the up comming tasks of a certain container."}
  (:require [com.brunobonacci.mulog :as mu]
            ))

(defn state-key->state-map  
  "Builds a `state-map` by means of the `info-map`.
  The state value is `assoc`ed afet getting it with `st/key->val`. "
  [state-key]
  (assoc (stu/key->info-map state-key)
         :state (keyword (st/key->val state-key))))

(defn ks->state-vec
  "Builds the state map `m` belonging to a key set `ks`.
  `m` is introduced in order to keep the functions testable.

  Example:
  ```clojure
  (ks->state-vec (k->state-ks \"ref@container@0\"))
  ```" 
  [ks]
  (when ks (mapv state-key->state-map ks)))


(defn k->state-ks
  "Returns the state keys for a given path.

  ```clojure
  (k->state-ks \"wait@container@0\")
  ```" 
  [k]
  (when k
    (sort
     (st/key->keys
      (stu/struct-state-key (stu/key->mp-id k) (stu/key->struct k) (stu/key->no-idx k))))))
  
(defn filter-state
  "Returns a vector of maps where state is `s`."
  [v s]
  (filterv (fn [m] (= s (:state m))) v))
  
(defn all-error
  "Returns all steps with the state `:error` for a given state vector
  `v`"
  [v]
  (filter-state v :error))

(defn all-ready
  "Returns all steps with the state `:ready` for a given state vector
  `v`"
  [v]
  (filter-state v :ready))

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
  (filter-state v :executed))

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
  (let [i (u/ensure-int i)]
    (if (pos? i)
      (every? true? (map
                     (fn [j] (all-executed? (stu/seq-idx->all-par v j)))
                     (range i)))
      true)))
