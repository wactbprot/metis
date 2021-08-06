(ns metis.log.core
  (:require [metis.config.interface :as c]
            [com.brunobonacci.mulog :as µ]))

(defonce logger (atom nil))

(defn stop
  ([] (stop c/config))
  ([conf]
   (µ/log ::stop)
   (@logger)
   (reset! logger nil)))

(defn start
  ([] (start c/config))
  ([{conf :mulog log-context :global-log-context}]
   (µ/set-global-context! log-context)
   (reset! logger (µ/start-publisher! conf))
   conf))
