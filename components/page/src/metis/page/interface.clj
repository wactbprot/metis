(ns metis.page.interface
  [:require [metis.page.core :as core]])

(defn index [conf req] (core/index conf req))

