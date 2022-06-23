(ns metis.page.interface
  [:require [metis.page.core :as core]])

(defn home [conf data] (core/home conf data))

(defn cont [conf data] (core/cont conf data))

(defn elem [conf data] (core/elem conf data))

(defn special [conf data] (core/special conf data))
