(ns metis.page.interface
  [:require [metis.page.core :as core]])

(defn cont [conf data] (core/cont conf data))

(defn elem [conf data] (core/elem conf data))

