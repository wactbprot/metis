(ns metis.worker.devproxy
  ^{:author "wactbprot"
    :doc "Worker to interact with a json api."}
  (:require [metis.config.interface :as c]
            [cheshire.core :as che]
            [clj-http.client :as http]
            [com.brunobonacci.mulog :as µ]
            [metis.worker.resp :as resp]
            [metis.stmem.interface :as stmem]))

(defn url
  "Builds up the `url` for a `devproxy` request."
  ([task]
   (url c/config task))
  ([{dev-proxy-url :dev-proxy-url} {path :RequestPath}]
   (str dev-proxy-url "/" path)))

(defn req
  "Builds up the `req`est map for a `devproxy` request."
  ([task]
   (req c/config task))
  ([{header :json-post-header} {value :Value}]
   (assoc header :body (che/encode value))))

(defn devproxy!
  "Interacts with a the `devproxy`. "
  [{value :Value :as task} m]
  (stmem/set-state-working m)
  (stmem/set-val (assoc m :func :req :value task))
  (µ/log ::devproxy! :message "stored request, will send request" :m m)
  (if-not value
    (try ; get
      (resp/check (http/get (url task)) task m)
      (catch Exception e (stmem/set-state-error (assoc m :message (.getMessage e)))))
    (try ; post
      (resp/check (http/post (url task) (req task)) task m)
        (catch Exception e (stmem/set-state-error (assoc m :message (.getMessage e)))))))

(comment
  (def m {:mp-id "test" :struct :cont :no-idx 0 :par-idx 0 :seq-idx 0 :func :resp})
  (def t {:Value {:DocPath "Calibration.Measurement.Values.Position",
                  :Target_pressure_value 1,
                  :Target_pressure_unit "Pa"}
          :RequestPath "dut_max"})
  (devproxy! t m))
