(ns geoscope.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response]]
            [cheshire.core :refer [generate-string]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [hiccup.page :refer [html5]]
            [next.jdbc.result-set :refer [as-unqualified-maps]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import (org.postgresql.geometric PGpoint)
           (org.postgis PGgeometry Point)
           (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

(def db-url (str "jdbc:postgresql://localhost/geoscope-dev"))
(def ds (jdbc/get-datasource db-url))

(defn timestamp-from-str
  "docstring"
  [ts]
  (LocalDateTime/parse ts (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss'Z'")))

(defn ingress-handler
  "docstring"
  [req]

  (let [data (:body req)
        locations (:locations data)
        entries (map (fn [p]
                       (let [geo (:geometry p)
                             coord (:coordinates geo)]
                         (-> {:point     (PGgeometry. (Point. (nth coord 1) (nth coord 0)))
                              :timestamp (timestamp-from-str (get-in p [:properties :timestamp]))})))
                     locations)]

    (doall (map (fn [e] (sql/insert! ds :geodata e)) entries))
    (response {:result "ok"}))
  )

(defn data-view
  "docstring"
  [req]
  (let [data (sql/query ds ["SELECT * FROM geodata ORDER BY timestamp ASC"]
                        {:builder-fn as-unqualified-maps})
        processed (map (fn [d] (-> (assoc d :timestamp "")
                                   (assoc :point [(.y (.getGeometry (:point d)))
                                                  (.x (.getGeometry (:point d)))
                                                  ] ))) data)]
    (response {:data processed})
    )
  )

(defn index-view
  "docstring"
  [req]
  (html5
    [:html {:lang "en"}
     [:head
      [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.4.3/css/ol.css" :type "text/css"}]
      [:style ".map { height: 750px; width: 100%; }"]
      [:script {:src "https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.4.3/build/ol.js"}]
      [:title "OpenLayers example"]]
     [:body
      [:h2 "My Map"]
      [:div#map.map]
      [:script {:src "/app.js"}]]])
  )

(defroutes app-routes
           (POST "/ingress" [] ingress-handler)
           (GET "/" [] index-view)
           (GET "/data" [] data-view)
           (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
