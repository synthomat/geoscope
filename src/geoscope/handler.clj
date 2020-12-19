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
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.java.shell :as shell]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io])
  (:import (java.time LocalDateTime Instant ZoneOffset)
           (java.util TimeZone)
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


(defn coords->gpx
  "Converts a vector of coordinate tuples to a gpx as xml-string"
  [coords]
  (let [trkpt-fn (fn [x] (element "trkpt" {:lat (first x)
                                           :lon (second x)}))
        gpx (element "gpx" {}
                     (element "trk" {}
                              (element "trkseg" {}
                                       (map trkpt-fn coords))))]
    (emit-str gpx)))


(defn random-file
  "docstring"
  ([suffix] (File/createTempFile "track_" suffix))
  ([] (random-file "")))

(defn store-in-file
  "docstring"
  [^String gpx-data file]
  (with-open [file (io/writer file)]
    (.write file gpx-data))
  file)

(def map-command "/Users/synth/go/bin/create-static-map")

(defn csm-params
  ""
  [params]
  (->> (clojure.walk/stringify-keys params)
       (into [])
       (map #(clojure.string/join ":" %))
       (clojure.string/join "|")))

(defn call-map-cmd
  "docstring"
  [^File in-file]
  (let [out-file (random-file ".png")
        out-file-path (.getAbsolutePath out-file)
        in-file-path (.getAbsolutePath in-file)
        path-params (csm-params {:color  "blue"
                                 :weight "2"
                                 :gpx    in-file-path})
        cmd [map-command
             "--width" "800" "--height" "600"
             "--output" out-file-path
             "--path" path-params]
        cmd-res (apply shell/sh cmd)]
    (if (= (:exit cmd-res) 0)
      out-file
      (log/error "Creation of static map failed with errors: " (:err cmd-res)))))

(defn coords->image-file
  "docstring"
  [coords]
  (let [gpx-data (coords->gpx coords)
        img-file (->> (random-file)
                      (store-in-file gpx-data)
                      (call-map-cmd))]
    img-file))


(defn range->dates
  "docstring"
  [range]
  (->> (clojure.string/split range #":")
       (map #(Integer/parseInt %))
       (map #(LocalDateTime/ofEpochSecond % 0 (ZoneOffset/UTC)))))

(defn data-handler
  "docstring"
  [req]
  (let [[min max] (range->dates (-> req :params :range))
        data (sql/query @db/ds ["SELECT * FROM geopoints WHERE timestamp >= ? and timestamp < ? order by timestamp asc" min max]
                        {:builder-fn as-unqualified-maps})
        processed (->> (map #(geom->coords (:geom %)) data)
                       (take-nth 20))]
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
