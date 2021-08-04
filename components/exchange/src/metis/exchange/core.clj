(ns metis.exchange.core
  ^{:author "wactbprot"
    :doc "Handles the access to the exchange interface."}
  (:require [com.brunobonacci.mulog :as µ]
            [clojure.string :as string]))

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

(defn get-val [a p]
  (or (get a p)
      (when-let [kw (path->second-kw p)]
        (kw (get a (path->first-string p))))))

(defn ok?
  "Checks a certain exchange endpoint to evaluate to true"
  [a p]
  (contains? #{"ok" :ok "true" true "yo!"} (get-val a p)))

(defn exists? [a p] (some? (get-val a p)))

(defn stop-if
  "Checks if the exchange path given with `:StopIf` evaluates to true."
  [a {p :StopIf}]
  (if p
    (ok? a p)
    true))

(defn run-if
  "Checks if the exchange path given with `:RunIf` evaluates to true."
  [a {p :RunIf}]
  (if p
    (ok? a p)
    true))

(defn only-if-not
  "Runs the task `only-if-not` the exchange path given with `:OnlyIfNot`
  evaluates to true."
  [a {p :OnlyIfNot}]
  (cond
    (nil? p) true
    (not (exists? a p)) false
    (not (ok? a p)) true
    (ok? a p) false))

(defn enclose-map
  "Encloses the given map `m` with respect to the key `k`.

  Example:
  ```clojure
  (enclose-map {:gg \"ff\"} \"mm.ll\")
  ;; gives:
  ;; {\"mm\" {:ll {:gg \"ff\"}}}

  (enclose-map {:gg \"ff\"} \"mm\")
  ;; gives:
  ;; {\"mm\" {:gg \"ff\"}}

  (enclose-map {\"mm\" \"ff\"} nil)
  ;; gives:
  ;; {\"mm\" \"ff\"}
  ```"
  [m p]
  (if p
    (let [p (name p)]
      (let [a (path->first-string p)]
        (if-let [b (path->second-kw p)]
          {a {b m}}
          {a m})))
    m))

(defn fit-in [o m]
  (if o
    (if (and (map? o) (map? m))
      (merge o m)
      m)
    m))

(defn to-vec [a {x :value p :exchpath :as m}]
  (µ/trace ::to-vec [:function "exchange/to-vec"]
           (mapv
            (fn [[k v]] (assoc m :exchpath (name k) :value (fit-in (get a (name k)) v)))
            (enclose-map x p))))
  
