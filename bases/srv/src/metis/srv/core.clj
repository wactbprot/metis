(ns  metis.srv.core
    ^{:author "wactbprot"
      :doc "Provides a frontend. Starts up the configured mpds."}
  (:require [metis.config.interface :as c]
            [metis.log.interface :as log]
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

(defonce server (atom nil))

(declare restart)

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
