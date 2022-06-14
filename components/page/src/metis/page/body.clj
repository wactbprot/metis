(ns metis.page.body
  ^{:author "Thomas Bock thomas.bock@ptb.de"
    :doc "Provides body elements for different pages."}
  (:require [metis.page.utils :as u]
            [hiccup.page :as hp]
            [metis.page.nav :as nav]
            [clojure.string :as string]))



(defn default [conf {:keys [mp-id] :as data} content-fn]
  [:body#body {:data-mp-id mp-id}
   (nav/links conf data)
   (nav/mpd conf data)
   (content-fn conf data)
   (hp/include-js "/js/jquery.js")
   (hp/include-js "/js/uikit.js")
   (hp/include-js "/js/uikit-icons.js")
   (hp/include-js "/js/ws.js")])

(defn home [conf data content-fn]
  [:body
   (nav/links conf data)
   (content-fn conf data)
   (hp/include-js "/js/jquery.js")
   (hp/include-js "/js/uikit.js")
   (hp/include-js "/js/uikit-icons.js")])
