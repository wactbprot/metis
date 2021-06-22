(ns metis.page.interface
  [:require [metis.page.core :as core]])

(defn index [conf req data] (core/index conf req data))

