(ns metis.page.head
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides elements for the input page."}
  (:require [metis.page.utils :as u]
            [hiccup.page :as hp]
            [clojure.string :as string]))

(defn head [conf data]
  [:head [:title "metis"]
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (hp/include-css "/css/uikit.css")])
