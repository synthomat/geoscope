(ns geoscope.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :as r]
            [geoscope.database :as db]
            [geoscope.views :as v]
            [next.jdbc.sql :as sql]
            [clojure.data.xml :refer :all]
            [aero.core :as [read-config]]
            [next.jdbc.result-set :refer [as-unqualified-maps]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.java.shell :as shell]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io])
  (:import (java.time LocalDateTime ZoneOffset)
           (java.io File)))


(def config (read-config (io/resource "config.edn")))

(defn init-app!
  "docstring"
  []
  (db/init! (:database config)))

(def ack {:result "ok"})

(defn unauthorized
  "docstring"
  ([] (unauthorized {:message "Not Authenticated"}))
  ([body]
   (-> (r/response body)
       (r/status 401))))

(defn locations
  "docstring"
  [req]
  (-> req :body :locations))

(defn receive-handler
  "docstring"
  [req]
  (let [token (-> req :params :token)]
    (if-not (db/access-token token)
      (unauthorized)
      (let [locations (locations req)]
        (db/batch-insert-locations! locations)
        (r/response ack)))))

(defn geom->coords
  "Transforms a Postgres Geometry object to a corresponding coordinates vec"
  [geom]
  (let [g (.getGeometry geom)]
    [(.y g) (.x g)]))


(defn range->dates
  "docstring"
  [range]
  (->> (clojure.string/split range #":")
       (map #(Integer/parseInt %))))

(defn ts->time
  "docstring"
  [ts]
  (map #(LocalDateTime/ofEpochSecond % 0 (ZoneOffset/UTC)) ts))

(defn get-coords
  "docstring"
  [rng]
  (let [[min max] (ts->time rng)
        query ["SELECT * FROM geopoints WHERE timestamp >= ? and timestamp < ? order by timestamp asc" min max]
        data (sql/query @db/ds query {:builder-fn as-unqualified-maps})
        processed (->> (map #(geom->coords (:geom %)) data)
                       (take-nth 20))]
    processed))

(defn data-handler
  "docstring"
  [req]
  (let [rng (range->dates (-> req :params :range))
        processed (get-coords rng)]
    (r/response {:data processed})))

(defn available-days
  "docstring"
  []
  (let [query ["select distinct(extract(epoch from date_trunc('day', timestamp)))::integer as day from geopoints order by day desc"]]
    (sql/query @db/ds query {:builder-fn as-unqualified-maps})))

(defroutes
  app-routes
  (POST "/receive" [] receive-handler)
  (context "/api" []
    (GET "/dates" [] (let [days (available-days)]
                       (r/response {:days (map :day days)})))
    (GET "/data" [] data-handler))
  ;(GET "/" [] v/index-view)
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})
      (wrap-defaults (-> site-defaults
                         (assoc-in [:security :anti-forgery] false)))))
