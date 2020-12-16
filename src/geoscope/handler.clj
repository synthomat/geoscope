(ns geoscope.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :as r]
            [geoscope.database :as db]
            [geoscope.views :as v]
            [next.jdbc.sql :as sql]
            [clojure.data.xml :refer :all]
            [aero.core :refer [read-config]]
            [next.jdbc.result-set :refer [as-unqualified-maps]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import (java.time LocalDateTime Instant)
           (java.util TimeZone)))

(def config (read-config (clojure.java.io/resource "config.edn")))


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

(defn geom->coordinates
  "Transforms a Postgres Geometry object to a corresponding coordinates vec"
  [geom]
  (let [g (.getGeometry geom)]
    [(.y g) (.x g)]))


(defn gpx
  "docstring"
  [coords]
  (let [st (element "gpx" {}
                    (element "trk" {}
                             (element "trkseg" {}
                                      (map (fn [x] (element "trkpt" {:lat (first x) :lon (second x)})) coords))))]
    (spit "sample.gpx" (emit-str st))))

(defn data-handler
  "docstring"
  [req]
  (let [range (when-let [range-param (-> req :params :range)]
                (clojure.string/split range-param #":"))
        drange (LocalDateTime/ofInstant (Instant/ofEpochSecond (Integer/parseInt (first range))) (.toZoneId (TimeZone/getTimeZone "UTC")))
        drange2 (LocalDateTime/ofInstant (Instant/ofEpochSecond (Integer/parseInt (second range))) (.toZoneId (TimeZone/getTimeZone "UTC")))
        data (sql/query @db/ds ["SELECT * FROM geopoints WHERE timestamp >= ? and timestamp < ? order by timestamp asc" drange drange2]
                        {:builder-fn as-unqualified-maps})
        processed (->> (map #(geom->coordinates (:geom %)) data)
                       (take-nth 20)
                       ;(flatten)
                       )]
    (gpx processed)
    (r/response {:data processed})))


(defroutes
  app-routes
  (POST "/receive" [] receive-handler)
  (context "/api" []
    (GET "/dates" [] (let [days (sql/query @db/ds ["select distinct(extract(epoch from date_trunc('day', timestamp)))::integer as day from geopoints order by day desc"]
                                               {:builder-fn as-unqualified-maps})]
                           (r/response {:days (map :day days)})))
    (GET "/data" [] data-handler))
  (GET "/" [] v/index-view)
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})
      (wrap-defaults (-> site-defaults
                         (assoc-in [:security :anti-forgery] false)))))
