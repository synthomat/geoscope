(ns geoscope.database
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [migratus.core :as migratus]
            [aero.core :refer [read-config]]
            [next.jdbc.result-set :as rs]
            [next.jdbc.prepare :as jprep]
            [cheshire.core :as json])
  (:import (java.time ZonedDateTime ZoneId)
           (java.time.format DateTimeFormatter)))

(def config (read-config (clojure.java.io/resource "config.edn")))

(defonce ds (atom nil))

(defn run-migrations!
  "docstring"
  [ds]
  (migratus/migrate {:store         :database
                     :migration-dir "migrations/"
                     :db            {:datasource ds}}))

(defn init!
  "docstring"
  [db-config]
  (let [db-spec {:jdbcUrl               (str "jdbc:" (-> db-config :url))
                 :reWriteBatchedInserts true}]
    (reset! ds (jdbc/get-datasource db-spec)))
  (run-migrations! @ds))

;(init! (:database config))

(defn access-token
  "docstring"
  [token]
  (sql/get-by-id @ds "access_tokens" token :token
                 {:builder-fn rs/as-unqualified-lower-maps}))

(defn localdatetime-from-str
  "Parses UTC date time string to a LocalDateTime and adds the local TZ offset to it"
  [ts]
  (-> (ZonedDateTime/parse ts DateTimeFormatter/ISO_DATE_TIME)
      (.withZoneSameInstant (ZoneId/systemDefault))
      (.toOffsetDateTime)))

(defn prep-geo-insert-params
  "docstring"
  [loc]
  (let [geom (json/generate-string (:geometry loc))
        props (json/generate-string (:properties loc))
        timestamp (localdatetime-from-str (-> loc :properties :timestamp))]
    [geom props timestamp]))

(defn pad-partition
  "docstring"
  [n e]
  (partition n n nil e))

(defn batch-insert-locations!
  "docstring"
  ([locations]
   (batch-insert-locations! locations 100))
  ([locations batch-size]
   (let [records (map prep-geo-insert-params locations)
         partitions (pad-partition batch-size records)]
     (with-open [dc (jdbc/get-connection @ds)]
       (jdbc/with-transaction [tx dc]
                              (with-open [ps (jdbc/prepare tx ["INSERT INTO geopoints (geom, props, timestamp) VALUES (ST_GeomFromGeoJSON(?), ?::json, ?)"])]
                                (doseq [batch partitions]
                                  (jprep/execute-batch! ps batch))))))))