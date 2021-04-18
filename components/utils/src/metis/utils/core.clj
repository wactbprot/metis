(ns metis.utils.core
  (:require [metis.config.interface :as c]
            [cheshire.core :as che]
            [clojure.string :as string]
            [clj-time.core :as tm]
            [clj-time.format :as tm-f]
            [clj-time.coerce :as tm-c]))

(def ok-set #{"ok" :ok "true" true "yo!"})

;;------------------------------
;; date time
;;------------------------------
(defn get-date-object [] (tm/now))
(defn get-hour  [d] (tm-f/unparse (tm-f/formatter "HH")   d))
(defn get-min   [d] (tm-f/unparse (tm-f/formatter "mm")   d))
(defn get-sec   [d] (tm-f/unparse (tm-f/formatter "ss")   d))
(defn get-day   [d] (tm-f/unparse (tm-f/formatter "dd")   d))
(defn get-month [d] (tm-f/unparse (tm-f/formatter "MM")   d))
(defn get-year  [d] (tm-f/unparse (tm-f/formatter "YYYY") d))

(defn get-date 
  ([]
   (get-date (get-date-object)))
  ([d]
   (tm-f/unparse (tm-f/formatters :date) d)))

(defn get-time
  ([]
   (str (tm-c/to-long (get-date-object))))
  ([d]
   (str (tm-c/to-long d))))

;;------------------------------
;; strings
;;------------------------------
(defn short-string
  "Short a `string` `s` to `n` or `45` chars. Returns `nil` is `s` is
  not a `string`."
  ([s]
   (when (string? s) (short-string s 30)))
  ([s n]
   (when (string? s) (if (< (count s) n) s (str (subs s 0 n) "...")))))

;;------------------------------
;; numbers
;;------------------------------
 (defn ensure-int
  "Ensures `i` to be integer. Returns 0 as default."
  [i]
  (cond
    (integer? i) i
    (string? i) (try (Integer/parseInt i) (catch Exception e 0))))

;;------------------------------
;; maps
;;------------------------------
(defn map->safe-map
  "Replaces all of the `@`-signs (if followed by letters 1)
  by a `%`-sign  because `:%kw` is a valid keyword but `:@kw` not
  (or at least problematic).

  1) There are devices annotating channels by `(@101:105)`.
  This expressions should remain as they are."
  ([m]
   (map->safe-map c/config m))
  ([{a :at-replace} m]
  (che/decode (string/replace (che/encode m) #"(@)([a-zA-Z])" (str a "$2")) true)))

(defn date-map
  "Returns a map with replacements
  of general intrest.

  ```clojure
  (globals)
  ;; {\"%hour\"  \"14\",
  ;; \"%minute\" \"07\",
  ;; \"%second\" \"54\",
  ;; \"%year\"   \"2020\",
  ;; \"%month\"  \"02\",
  ;; \"%day\"    \"02\",
  ;; \"%time\"   \"1580652474824\"}
  ```"
  ([]
   (date-map c/config))
  ([{a :at-replace}]
   (let [d (get-date-object)]
     {(str a "hour") (get-hour d)
      (str a "minute") (get-min d)
      (str a "second") (get-sec d)
      (str a "year") (get-year d)
      (str a "month") (get-month d)
      (str a "day") (get-day d)
      (str a "time") (get-time d)})))

(defn apply-to-map-values
  "Applies function `f` to the values of the map `m`."
  [f m]
  (into {} (map (fn [[k v]]
                  (if (map? v)
                    [k (apply-to-map-values f v)]
                    [k (f v)]))
                m)))

(defn apply-to-map-keys
  "Applies function `f` to the keys of the map `m`."
  [f m]
  (into {} (map (fn [[k v]]
                  (if (map? v)
                    [(f k) (apply-to-map-keys f v)]
                    [(f k) v]))
                m)))
