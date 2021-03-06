(ns metis.page.core
  (:require [hiccup.page :as hp]
            [metis.page.utils :as u]
            [metis.page.home :as index]
            [metis.page.head :as head]
            [metis.page.body :as body]
            [metis.page.mpd-ppc-gas_dosing :as mpd-ppc-gas_dosing]
            [metis.page.mpd-ref :as mpd-ref]
            [metis.page.elements :as elem]
            [metis.page.container :as cont]))

(defn cont [conf data]
  (hp/html5
   (head/head conf data)
   (body/default conf data cont/content)))

(defn elem [conf data]
  (hp/html5
   (head/head conf data)
   (body/default conf data elem/content)))

(defn home [conf data]
  (hp/html5
   (head/head conf data)
   (body/home conf data index/content)))

(defn special [conf {mp-id :mp-id :as data}]
  (hp/html5
   (head/head conf data)
   (body/default conf data
              (condp = (-> mp-id keyword)
                :mpd-ppc-gas_dosing mpd-ppc-gas_dosing/content
                :mpd-ref mpd-ref/content
                cont/content))))
