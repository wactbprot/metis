(ns metis.page.utils
  (:require [clojure.string :as string]))

  (def s "_")

(defn gen-state-id [{a :mp-id b :struct c :no-idx  d :seq-idx e :par-idx}]
  (string/join s [a (name b) c "state" d e ]))

(defn gen-ctrl-id [{a :mp-id b :struct c :no-idx}]
  (string/join s [a (name b) c "ctrl"]))

