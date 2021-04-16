(ns metis.exchange.core
  ^{:author "wactbprot"
    :doc "Handles the access to the exchange interface."}
  (:require [com.brunobonacci.mulog :as mu]
            [clojure.string :as string]
            [metis.stmem.interface :as stmem]))

(comment
    
(defn exch-key
  "Returns the base key for the exchange path.

  ```clojure
  (exch-key  \"foo\" \"bar.baz\")
  ;; \"foo@exchange@bar\"
  (exch-key \"foo\" \"bar\")
  ;; \"foo@exchange@bar\"
  ```
  "
  [mp-id s]
  {:pre [(not (nil? s))]}
  (stu/exch-key mp-id (first (string/split s #"\."))))

(defn path->first-kw
  "Returns the keyword or nil.

  ```clojure
  (key->second-kw \"foo\" )
  ;; nil
  (key->second-kw \"foo.bar\" )
  ;; :bar
  ```"  
  [s]
  (when-let [x (first (string/split s #"\."))] (keyword x)))

(defn enclose-map
  "Encloses the given map `m` with respect to the key `k`.

  Example:
  ```clojure
  (enclose-map {:gg \"ff\"} \"mm.ll\")
  ;; gives:
  ;; {:mm {:ll {:gg \"ff\"}}}

  (enclose-map {:gg \"ff\"} \"mm\")
  ;; gives:
  ;; {:mm {:gg \"ff\"}}

  (enclose-map {:gg \"ff\"} nil)
  ;; gives:
  ;; {:gg \"ff\"}
  ```"
  [m k]
  (if-not k m
          (let [a (path->first-kw k)]
            (if-let [b (path->second-kw k)]
              {a {b m}}
              {a m}))))

(defn to!
  "Writes `m` to the exchange interface.  The first level keys of `m`
  are used for the key. The return value of the storing
  process (e.g. \"OK\") is converted to a `keyword`. After storing the
  amounts of `:OK` is compared to `(count m)`.

  Example:
  ```clojure
  {:A 1
   :B 2}
  ```
  Stores the value `1` under the key
  `<mp-id>@exchange@A` and a `2` under
  `<mp-id>@exchange@B`.

  If a path `p` is given the `enclose-map` function
  respects `p`.
  "
  ([mp-id m p]
   (to! mp-id (enclose-map m p)))
  ([mp-id m]
   (if (string? mp-id)
    (if (map? m)
      (let [res     (map (fn [[k new-val]]
                           (let [exch-key  (stu/exch-key mp-id (name k))
                                 curr-val  (st/key->val exch-key)
                                 both-map? (and (map? new-val) (map? curr-val))]
                             (st/set-val! exch-key (if both-map? (merge curr-val new-val) new-val))))
                         m)
            res-kw  (map keyword res)
            res-ok? (= (count m) (:OK (frequencies res-kw)))]
        (if res-ok? {:ok true} {:error "not all write processes succeed"}))
      {:ok true :warn "nothing to write"})
    {:error "mp-id must be a string"})))

(defn ok?
  "Checks a certain exchange endpoint to evaluate
  to true"
  [mp-id k]
  (contains? u/ok-set (read! mp-id k)))

(defn exists? [mp-id k] (some? (read! mp-id k)))

(defn stop-if
  "Checks if the exchange path given with `:MpName` and `:StopIf`
  evaluates to true."
  [{mp-id :MpName k :StopIf}]
  (if k
    (ok? mp-id k)
    true))

(defn run-if
  "Checks if the exchange path given with `:MpName` and `:RunIf`
  evaluates to true."
  [{mp-id :MpName k :RunIf}]
  (if k
    (ok? mp-id k)
    true))

(defn only-if-not
  "Runs the task `only-if-not` the exchange path given with `:MpName`
  and `:OnlyIfNot` evaluates to true."
  [{mp-id :MpName k :OnlyIfNot}]
  (cond
    (nil? k)                true
    (not (exists? mp-id k)) false
    (not (ok? mp-id k))     true))
    

)


(defn path->second-kw
  "Returns the keyword or nil.

  ```clojure
  (key->second-kw \"foo\" )
  ;; nil
  (key->second-kw \"foo.bar\" )
  ;; :bar
  ```"  
  [s]
  (when (string? s)
    (when-let [x (second (string/split s #"\."))] (keyword x))))

(defn path->first-string
  "Returns the keyword or nil.

  ```clojure
  (key->second-kw \"foo\" )
  ;; nil
  (key->second-kw \"foo.bar\" )
  ;; :bar
  ```"  
  [s]
  (when (string? s)
    (first (string/split s #"\."))))

(defn from
  "Builds a map by replacing the values of the input map `m`.
  The replacements are gathered from `a` the complete exchange interface
  
  Example:
  ```clojure
  (from {\"A\" {:Type \"ref\", :Unit \"Pa\", :Value 100.0},
         \"B\" \"token\",
         \"Target_pressure\" {:Selected 1, :Unit \"Pa\"}} {:%check A})
  ;; =>
  ;; {:%check {:Type \"ref\" :Unit \"Pa\" :Value 100.0}}
  ```"
  [a m]
  (when (and (map? a) (map? m))
    (into {} (map (fn [[k p]] (if-let [v (get a p)]
                                {k v}
                                (when-let [kw (path->second-kw p)]
                                  {k (kw (get a (path->first-string p)))})))
                  m))))

(defn all [{mp-id :mp-id}]
  (into {} (map
            (fn [m] {(:no-idx m) (:value m)})
            (stmem/get-maps {:mp-id mp-id :struct :exch :no-idx :*})))) 
