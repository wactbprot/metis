(ns metis.worker.select-definition
    ^{:author "wactbprot"
    :doc "Worker selects a definition from the same `mp-id` 
          by evaluating the related conditions."}
  (:require [metis.config.interface :as c]
            [metis.exchange.interface :as exch]
            [com.brunobonacci.mulog :as mu]
            [metis.stmem.interface :as stmem]
            [clojure.string :as string]
            [metis.utils.interface :as u]))

(defn gen-callback
  [match-m m]
  #(condp = (keyword (:value %))
     :run   (mu/log ::gen-callback :message "run callback for" :m m)
     :ready (do
              (mu/log ::gen-callback :message "ready callback for" :m m)
              (stmem/de-register match-m)
              (stmem/set-state-ready (assoc m :message "ready callback" :m match-m)) 
              )
     :error (stmem/set-state-error (assoc m :message "error callback" :m match-m))))

(defn start-defins!
  "Starts the matching `definitions` structure. `register`s a level 1
  callback. This callback sets the state of the calling task to
  `executed` if the `ctrl` of the matching `definitions` structure
  turns to `ready` (or `error` if `error`)."          
  [match-m m]
  (let [match-m (assoc match-m :struct :defins :func :ctrl :level 1)]
    (stmem/register match-m (gen-callback match-m m))
    (stmem/set-ctrl-run match-m)))

(defn cond-match?
  "Tests a single condition of the form defined in the `definitions`
  section.

  Example:
  ```clojure
  (cond-match? 10 :gt 1)
  ;; true
  ```"
  [l m r]
  (condp = (keyword m)
      :eq (= l r)
      :lt (< (read-string (str l)) (read-string (str r)))
      :gt (> (read-string (str l)) (read-string (str r)))))

(defn conds-match? [v] (every? true? (map :cond-match v)))

(defn filter-match [v] (when (conds-match? v) (first v)))

(defn match-class-fn [s] (fn [v] (filterv #(= s (:value %)) v)))

(defn get-classes [m] (stmem/get-maps (assoc m :func :cls :no-idx :*)))

(defn get-conds [m] (stmem/get-maps (assoc m :func :cond :seq-idx :*)))

(defn get-exch-value [a m] (exch/from-path a (get-in m [:value :ExchangePath])))

(defn resolve-cond [a m]
  (let [lft  (get-exch-value a m)
        meth (get-in m [:value :Methode]) ;; the blue one
        rgt  (get-in m [:value :Value])]
    (assoc m :cond-match (cond-match? lft meth rgt))))

(defn resolve-conds [v]
  (let [a (exch/all (first v))]
    (mapv #(resolve-cond a %) v)))
            
(defn select-definition!
  "Selects and runs a `Definition` from the `Definitions` section of the
  measurement program definition with the given `:mp.id`." 
  [{cls :DefinitionClass} {mp-id :mp-id :as m}]
  (stmem/set-state-working (assoc m :message "start select-definition"))
  (let [f (match-class-fn cls)
        v (-> {:mp-id mp-id :struct :defins} get-classes f)
        v (filterv #(-> % get-conds resolve-conds conds-match?) v)]
    (if-not (empty? v)
      (start-defins! (first v) m)
      (stmem/set-state-error (assoc m :message "no matching definition")))))

(comment
  (def cond-v (stmem/get-maps {:mp-id "mpd-ref" :struct :defins :func :cond :no-idx :* :seq-idx :*}))
  (def cls-v (stmem/get-maps {:mp-id "mpd-ref" :struct :defins :func :cls :no-idx :*}))
  (select-definition! {:DefinitionClass "wait"} {:mp-id "mpd-ref"}))
