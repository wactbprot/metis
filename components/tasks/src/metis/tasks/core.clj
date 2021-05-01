(ns metis.tasks.core
  (:require [cheshire.core :as che]
            [metis.exchange.interface :as exch]
            [metis.stmem.interface :as stmem]
            [clojure.string :as string]
            [metis.utils.interface :as utils]))

(defn outer-replace-map
  "Replaces tokens (given in the m) in the task.
  This kind of replacement is used during the
  task build up at the beginning of its life cycle.
  
  Example:
  ```clojure
  (outer-replace-map (globals) {:TaskName \"foo\" :Value \"%time\"})
  ;; {:TaskName \"foo\", :Value \"1580652820247\"}
  (outer-replace-map nil {:TaskName \"foo\" :Value \"%time\"})
  ;; {:TaskName \"foo\", :Value \"%time\"}
  ```"
  [m task]
  (if (map? m)
    (che/decode
     (reduce
      (fn [s [k v]] (string/replace s (re-pattern (name k)) (str v)))
      (che/encode task) m) true)
    task))


(defn inner-replace-map
  "Applies the generated function  `f` to the
  values `v` of the `task` map. `f`s input is `v`.
  If `m` has a key `v` the value of this key is returned.
  If `m` has no key `v` the `v` returned.
  This kind of replacement is used during the
  runtime.
  "
  [m task]
  (let [nm (utils/apply-to-map-keys name m)
        f (fn [v]
            (if-let [r (get nm  v)]
              (if (map? r) (utils/apply-to-map-keys keyword r) r)
              v))]
    (utils/apply-to-map-values f task)))

(defn extract-use-value [task m k] ((keyword (m k)) (task k)))

(defn str->singular-kw
  "Takes a keyword or string and removes the tailing
  letter (most likely a s). Turns the result
  to a keyword.
  
  ```clojure
  (str->singular-kw :Values)
  ;; :Value
  (str->singular-kw \"Values\")
  ;; :Value
  ```
  "
  [s]
  (->> s name (re-matches #"^(\w*)(s)$") second keyword))

(defn merge-use-map
  "The use keyword enables a replace mechanism.
  It works like this:
  proto-task:
  
  ```clojure
  Use: {Values: med_range}
  ;; should lead to:
  task: { Value: rangeX.1}
  ```"
  [m task]
  (if (map? m)
    (merge task (into {} (map
                          (fn [k] (hash-map
                                   (str->singular-kw k)
                                   (extract-use-value task m k)))
                          (keys m))))
    task))

(defn assemble
  "Assembles the `task` from the given
  `meta-m`aps in a special order:

  * merge Use
  * replace from Replace
  * replace from Defaults
  * replace from Globals
   ```"
  [{from-m :FromExchange globals-m :Globals def-m :Defaults use-m :Use rep-m :Replace task :Task}]
   (assoc 
    (->> task
         (merge-use-map use-m)
         (inner-replace-map from-m)
         (outer-replace-map rep-m)
         (outer-replace-map def-m)
         (outer-replace-map globals-m)
         (outer-replace-map from-m))
    :Use use-m
    :Replace rep-m))

(defn prepair
  "Prepairs the task for assemble step."
  [{rep-m :Replace use-m :Use} raw-task from-m globals-m m]
  {:Task (merge (dissoc raw-task :Defaults :Use :Replace) m) 
   :Replace rep-m
   :Use use-m
   :Defaults (:Defaults raw-task)
   :FromExchange from-m
   :Globals globals-m})

(defn get-task
  "Trys to gather all information belonging to `m`. Calls `prepair` and
  `assemble` function.`" 
  [m]
  (try
    (let [pre-task    (stmem/get-val (assoc m :func :defin))
          raw-task    (stmem/get-val {:task-name (:TaskName pre-task)})
          from-map    (exch/from (exch/all m) (:FromExchange raw-task))
          globals-map (utils/date-map)]
      (assemble (prepair pre-task raw-task from-map globals-map m)))
    (catch Exception e
      (stmem/set-state (assoc m :value :error :message (.getMessage e))))))

(comment
  ;; get-task takes all side effects
  ;; prepair and assemple and the rest is pure
  (let [m        {:mp-id "mpd-ref" :struct :cont :func :defin :no-idx 0 :par-idx 0 :seq-idx 0}
        pre-task {:Replace {"%waittime" 200}}
        _        (stmem/get-val m)
        raw-task {:Action "wait",
                  :Comment "%waitfor  %waittime ms",
                  :TaskName "Common-wait",
                  :WaitTime "%waittime",
                  :FromExchange {:%check "A"}
                  :Defaults {:%waittime 1000}}
        _         (stmem/get-val {:task-name "Common-wait"})
        from-map {:%check {:Type "ref", :Unit "Pa", :Value 100.0}}
        _        (exch/from (exch/all m) (:FromExchange raw-task))
        glob-map {"%hour" "09",
                  "%minute" "06",
                  "%second" "22",
                  "%year" "2021",
                   "%month" "05",
                  "%day" "01",
                  "%time" "1619859982354"}
        _         (utils/date-map)
        ]
    (assemble (prepair pre-task raw-task from-map glob-map m))))