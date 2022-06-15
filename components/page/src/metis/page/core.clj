(ns metis.page.core
  (:require [hiccup.page :as hp]
            [metis.page.utils :as u]
            [metis.page.home :as index]
            [metis.page.head :as head]
            [metis.page.body :as body]
            [metis.page.elements :as elem]
            [metis.page.container :as cont]))

(defn cont [conf data]
  (hp/html5 (head/head conf data)
            (body/default conf data cont/content)))

(defn elem [conf data]
  (hp/html5
   (head/head conf data)
   (body/default conf data elem/content)))

(defn home [conf data] (hp/html5 (head/head conf data) (body/home conf data index/content)))
