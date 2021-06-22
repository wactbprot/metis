(ns metis.ws.core
  ^{:author "wactbprot"
    :doc "Registers/de-registers listener. Sends data to clients using
    httpkit websockets."}
  (:require [cheshire.core :as che]
            [com.brunobonacci.mulog :as µ]
            [org.httpkit.server :refer [with-channel
                                        on-receive
                                        on-close
                                        send!]]
            [metis.stmem.interface :as stmem]))

(defonce ws-clients (atom {}))

(defn msg-received [msg] (µ/log ::msg-received :message "msg/data received"))

(defn main [req]
  (with-channel req channel
    (µ/log ::ws :message "connected")
    (swap! ws-clients assoc channel true)
    (on-receive channel #'msg-received)
    (on-close channel #((swap! ws-clients dissoc channel)
                        (µ/log ::ws :message "closed, status")))))

(defn send-to-ws-clients [m]
  (doseq [client (keys @ws-clients)]
    (send! client (che/encode m))))

(defn start [{mp-id :mp-id}]
  (stmem/register {:mp-id mp-id :struct :* :no-idx :* :func :state :level 3} send-to-ws-clients)
  (stmem/register {:mp-id mp-id :struct :* :no-idx :* :func :ctrl :level 3} send-to-ws-clients))
  
(defn stop [{mp-id :mp-id}]
  (stmem/de-register {:mp-id mp-id :struct :* :no-idx :* :func :state :level 3})
  (stmem/de-register {:mp-id mp-id :struct :* :no-idx :* :func :ctrl :level 3}))
    
