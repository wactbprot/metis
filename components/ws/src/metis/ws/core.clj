(ns metis.ws.core
  ^{:author "wactbprot"
    :doc "Registers/de-registers listener. Sends data to clients using
    httpkit websockets."}
  (:require [cheshire.core :as che]
            [metis.document.interface :as doc]
            [metis.exchange.interface :as exch]
            [com.brunobonacci.mulog :as µ]
            [org.httpkit.server :refer [with-channel
                                        on-receive
                                        on-close
                                        send!]]
            [metis.stmem.interface :as stmem]))

(defonce ws-clients (atom {}))

(defn connected-received [m] (µ/log ::msg-received :message "client connected"))

(defn try-decode [m]
  (try
    (che/decode m true)
    (catch Exception e (µ/log ::msg-received :error (.getMessage e)))))

(defn msg-received [m]
  (when-let [m (try-decode m)]
    (µ/log ::msg-received :message "receive data" :m m)
    (let [m (update m :struct keyword)
          m (update m :func keyword)]
      (cond
        (:ok m) (connected-received m)
        (= (:struct m)
           :exch) (exch/to (exch/all m) m)
        :else (stmem/set-val m)))))
  
(defn main [req]
  (with-channel req channel
    (µ/log ::ws :message "connected")
    (swap! ws-clients assoc channel true)
    (on-receive channel #'msg-received)
    (on-close channel (fn [status]
                        (swap! ws-clients dissoc channel)
                        (µ/log ::ws :message (str "closed, status: "status))))))

(defn send-to-ws-clients [m]
  (doseq [client (keys @ws-clients)]
    (send! client (che/encode m))))

(defn get-doc-ids [m]
  (send-to-ws-clients (assoc m :value (doc/ids m))))
  
(defn start []
  (stmem/register {:mp-id :* :struct :id :level 3} get-doc-ids)
  (stmem/register {:mp-id :* :struct :* :no-idx :* :func :state  :level 3} send-to-ws-clients)
  (stmem/register {:mp-id :* :struct :* :no-idx :* :func :msg :level 3} send-to-ws-clients)
  (stmem/register {:mp-id :* :struct :* :no-idx :* :func :ctrl :level 3} send-to-ws-clients))
  
(defn stop []
  (stmem/de-register {:mp-id :* :struct :id :level 3})
  (stmem/de-register {:mp-id :* :struct :* :no-idx :* :func :state :level 3})
  (stmem/de-register {:mp-id :* :struct :* :no-idx :* :func :msg :level 3})
  (stmem/de-register {:mp-id :* :struct :* :no-idx :* :func :ctrl :level 3}))
    
