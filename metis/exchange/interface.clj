✔ (ns metis.exchange.interface
?   (:require [metis.exchange.api :as api]
?             [metis.exchange.core :as core]))
  
~ (defn all [m] (api/all m))
  
~ (defn from [a m] (api/from a m))
  
~ (defn from-path [a p] (core/get-val a p))
  
~ (defn to [a m] (api/to a m))
  
~ (defn run-if [a m] (core/run-if a m))
  
~ (defn stop-if [a m] (core/stop-if a m))
  
~ (defn only-if-not [a m] (core/only-if-not a m))
