(ns geoscope.gpxgen
  (:require [clojure.data.xml :refer :all]
            [clojure.tools.logging :as log])
  (:import (java.io File)))

(def map-command "/Users/synth/go/bin/create-static-map")

(defn coords->gpx
  "Converts a vector of coordinate tuples to a GPX string"
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
             "--width" "800"
             "--height" "600"
             "--output" out-file-path
             "--path" path-params]
        cmd-res (apply shell/sh cmd)]
    (if (= (:exit cmd-res) 0)
      out-file
      (log/error "Creation of static map failed with errors: " (:err cmd-res)))))


(defn store-in-file
  "docstring"
  [^String gpx-data file]
  (with-open [file (io/writer file)]
    (.write file gpx-data))
  file)

(defn coords->image-file
  "docstring"
  [coords]
  (let [gpx-data (coords->gpx coords)
        img-file (->> (random-file)
                      (store-in-file gpx-data)
                      (call-map-cmd))]
    img-file))