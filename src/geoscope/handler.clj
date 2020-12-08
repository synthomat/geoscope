(ns geoscope.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :as r]
            [geoscope.database :as db]
            [geoscope.views :as v]
            [next.jdbc.sql :as sql]
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
    [(.x g) (.y g)]))

(defn data-handler
  "docstring"
  [req]
  (let [range (when-let [range-param (-> req :params :range)]
                (clojure.string/split range-param #":"))
        drange (LocalDateTime/ofInstant (Instant/ofEpochSecond (Integer/parseInt (first range))) (.toZoneId (TimeZone/getTimeZone "UTC")))
        data (sql/query @db/ds ["SELECT * FROM geopoints WHERE timestamp > ? ORDER BY timestamp ASC" drange]
                        {:builder-fn as-unqualified-maps})
        processed (->> (map #(geom->coordinates (:geom %)) data)
                       ;(take-nth 20)
                       (flatten))]
    (r/response {:data processed})))


(defroutes
  app-routes
  (POST "/receive" [] receive-handler)
  ;(GET "/" [] v/index-view)
  ;(GET "/data" [] data-handler)
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true})
      (wrap-defaults (-> site-defaults
                         (assoc-in [:security :anti-forgery] false)))))
