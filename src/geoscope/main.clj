(ns geoscope.main
  (:gen-class)
  (:require [geoscope.handler :refer [app init-app!]])
  (:use ring.adapter.jetty))


(defn get-port []
  (if-let [port (System/getenv "PORT")]
    (Integer/valueOf port)
    5000))

(defn -main []
  (init-app!)
  (run-jetty app {:port (get-port)}))
