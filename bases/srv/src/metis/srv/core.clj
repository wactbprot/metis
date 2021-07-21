(ns metis.srv.core
    ^{:author "wactbprot"
      :doc "Provides a frontend. Starts up the configured mpds."}
  (:require [metis.config.interface :as c]
            [metis.log.interface :as log]
            [metis.ltmem.interface :as ltmem]
            [metis.utils.interface :as utils]
            [metis.model.interface :as model]
            [metis.scheduler.interface :as scheduler]
            [compojure.route :as route]
            [com.brunobonacci.mulog :as µ]
            [compojure.core :refer [defroutes
                                    GET]]
            [compojure.handler :as handler]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.json :as middleware]
            [ring.util.response :as res]
            [metis.page.interface :as page]
            [metis.ws.interface :as ws]
            [metis.srv.handler :as h])
    (:use   [clojure.repl])
    (:gen-class))


(defn mpd-build [mp-id] (-> mp-id ltmem/get-doc model/build-mpd))
 
(defn mpd-clear [mp-id] (model/clear-mpd {:mp-id mp-id}))  

(defn mpd-start [mp-id] (scheduler/start {:mp-id mp-id}))

(defn mpd-stop [mp-id] (scheduler/stop {:mp-id mp-id}))

(defonce server (atom nil))

(defroutes app-routes
  (GET "/ws" [:as req] (ws/main req))
  (GET "/cont/:mp-id" [:as req] (page/cont c/config (h/cont req)))
  (GET "/elem/:mp-id" [:as req] (page/elem c/config (h/elem req)))  
  (route/resources "/")
  (route/not-found (res/response {:error "not found"})))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))

(defn stop []
  (when @server (@server :timeout 100)
        (µ/log ::stop :message "stop ui web socket listener")
        (ws/stop)
        (µ/log ::stop :message "stop server")
        (reset! server nil)
        (log/stop)
        {:ok true}))

(defn start []
  (log/start)
  (let [mpd-ref (c/mpd-ref)
        ref-id (:_id mpd-ref)]
    (µ/log ::start :message (str "clear mpd: " ref-id))
    (mpd-clear ref-id)
    (µ/log ::start :message (str "build mpd: " ref-id))
    (model/build-mpd mpd-ref)
    (µ/log ::start :message (str "start mpd: " ref-id))
    (mpd-start ref-id))
  (run! (fn [mp-id]
          (µ/log ::start :message (str "clear mpd: " mp-id))
          (mpd-clear mp-id)
          (µ/log ::start :message (str "build mpd: " mp-id))
          (mpd-build mp-id)
          (µ/log ::start :message (str "start mpd: " mp-id))
          (mpd-start mp-id)          )
        (:build-on-start c/config))
  (µ/log ::start :message "start server")
  (reset! server (run-server #'app (:api c/config)))
  (µ/log ::start :message "start ui web socket listener")
  (ws/start)
  {:ok true})

(defn restart []
  (Thread/sleep 1000)
  (stop)
  (Thread/sleep 1000)
  (start))

(defn -main [& args] (start))
