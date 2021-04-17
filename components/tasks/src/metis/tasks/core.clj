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
  (->> s
       name
       (re-matches #"^(\w*)(s)$")
       second
       keyword))

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
  `meta-task` in a special order:

  * merge Use
  * replace from Replace
  * replace from Defaults
  * replace from Globals
  
  `assoc`s the structs afterwards.

  TODO:
  clarify :PreInput
  
  ```clojure
  (def proto {:TaskName \"Common-wait\"
                    :Replace {\"%waittime\" 10}})
  (assemble
    (gen-meta-task proto \"ref\" \"ref@container@0@state@0@0\"))
  ;; {:Action  \"wait\",
  ;;  :Comment  \"Ready in  10 ms\",
  ;;  :TaskName \"Common-wait\",
  ;;  :WaitTime \"10\",
  ;;  :MpName   \"ref\"
  ;;  :StateKey \"ref@container@0@state@0@0\"
  ;;  ...
  ;; }
  ```
  "
  [m]
  (let [db-task (:Task m)
        use-map (:Use m)
        rep-map (:Replace m)
        def-map (:Defaults m)
        glo-map (:Globals m)
        exch-map (:FromExchange db-task)
        from-map (exch/from m exch-map)]
    (assoc 
     (->> (dissoc db-task :Use :Replace)
          (merge-use-map use-map)
          (inner-replace-map from-map)
          (outer-replace-map rep-map)
          (outer-replace-map def-map)
          (outer-replace-map glo-map)
          (outer-replace-map from-map))
     :Use use-map
     :Replace rep-map)))


(defn gen-meta-task
  "Gathers all information for the given `proto-task` (map).
  The `proto-task` should be a map containing the `:TaskName`
  `keyword` at least. String version makes a `map` out of `s`
  and calls related method.

  ```clojure
  (gen-meta-task {:TaskName \"Common-wait\" :Replace {\"%waittime\" 10}})
  ```"
  [{task-name :TaskName :as pre-task}]
  (let [task (stmem/get-val {:task-name task-name})]
    {:Task (dissoc task :Defaults) 
     :Use (:Use pre-task)
     :Globals (utils/date-map)
     :Defaults (:Defaults task)
     :Replace (:Replace pre-task)}))

(defn build
  "Builds and returns the assembled `task` for the given position map
  `m`." 
  [m]
  (try 
    (merge (assemble (gen-meta-task (stmem/get-val (assoc m :struct :defin)))) m)
    (catch Exception e
      (stmem/set-state (assoc m :value :error :message (.getMessage e))))))
