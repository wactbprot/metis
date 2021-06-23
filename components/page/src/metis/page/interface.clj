(ns metis.page.interface
  [:require [metis.page.core :as core]])

(defn index [conf data] (core/index conf data))

