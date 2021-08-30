(ns metis.worker.ctrl-mp
  ^{:author "wactbprot"
    :doc "run-mp worker."} 
  (:require [com.brunobonacci.mulog :as µ]
            [metis.stmem.interface :as stmem]))

(defn title->no-idx
  "Returns the `no-idx` belonging to the given `title`." 
  [title m]
  (let [v (stmem/get-maps (assoc (dissoc m :par-idx :seq-idx) :no-idx :* :func :title))]
    (:no-idx (first (filter  #(= title (:value %)) v)))))

(comment
  (def m {:mp-id "mpd-ref" :struct :cont :no-idx 7 :func :state :par-idx 0 :seq-idx 0})
  (title->no-idx "date & time" m))

(defn gen-callback
  [{mp :Mp no-idx :Container cmd :Cmd :as task} m]
  #(condp = (keyword (:value %)) 
     :ready (do
              (stmem/set-state-executed m)
              (stmem/de-register (assoc % :level 1)))
     :stop  (do
              (stmem/set-state-executed m)
              (stmem/de-register (assoc % :level 1)))
     :error (do
              (µ/log ::exec-index :error "error callback for" :m m)
              (stmem/set-state-error m))
     (µ/log ::exec-index :message "run callback not :ready nor :error" :m m)))

(defn exec-index
  "Registers a level 1 callback for the `i`th container of the mpd `mp`."
  [{mp :Mp no-idx :Container cmd :Cmd :as task} m]
  (let [run-m {:mp-id mp
               :no-idx no-idx
               :struct :cont
               :func :ctrl
               :value (keyword (or cmd "run"))
               :level 1}] 
    (stmem/register run-m (gen-callback task m))
    (stmem/set-val run-m)))

(defn exec-title
  "Searches for the given  `:ContainerTitle`. Extracts the `no-idx`
  and starts the `exec-index` function."
  [{mp :Mp cont-title :ContainerTitle :as task} m]
  (if-let [no-idx (title->no-idx cont-title {:mp-id mp :struct :cont})]
    (exec-index (assoc task :Container no-idx) m)
    (stmem/set-state-error (assoc m :message (str "no container with title: "cont-title)))))

(defn run-mp!
  "Runs a certain container of a `mpd` given by the tasks entry
  `:Mp`. `:ContainerTitle` is prefered over `:Container` if both
  entries given. The `task` is marked as `:executed` if all tasks in
  the container are executed."
  [{title :ContainerTitle index :Container :as task} m]
  (cond
    title (exec-title task m)
    index (exec-index task m)
    :not-found (stmem/set-state-error (assoc m :message "neither title nor index"))))

(defn stop-mp!
  "Stops a container of a `mpd`. `:ContainerTitle` is prefered
  over `:Container` if both are given. Checks if the container to stop
  is the `same?` as the task runs in:

  * If so: the `ctrl` interface is set to `stop` (and nothing
  else). The stop process turns all states to `ready`.
  * If not: the task (resp. the :value of `:StateKey`) is set to
  `:executed` after  stopping."
  [{mp :Mp title :ContainerTitle no-idx :Container :as task} m]
  (let [stop-m  {:mp-id mp :struct :cont}
        no-idx  (or (title->no-idx title stop-m) no-idx)
        stop-m  (assoc stop-m :no-idx no-idx :func :ctrl :value "stop")
        same?   (and  (= (:mp-id m) (:mp-id stop-m))
                      (= (:no-idx m) no-idx))]
    (stmem/set-val stop-m)
    (Thread/sleep 100)
    (when-not same? (stmem/set-state-executed m))))


