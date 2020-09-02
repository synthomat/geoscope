(ns geoscope.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response]]
            [cheshire.core :refer [generate-string]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [hiccup.page :refer [html5]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import (org.postgresql.geometric PGpoint)
           (org.postgis PGgeometry Point)))

(def db-url (str "jdbc:postgresql://localhost/geotest"))
(def ds (jdbc/get-datasource db-url))

;

(defn ingress-handler
  "docstring"
  [req]

  (let [data (:body req)
        locations (:locations data)
        entries (map (fn [p]
                       (let [geo (:geometry p)
                             coord (:coordinates geo)]
                         (-> {:point (PGgeometry. (Point. (nth coord 1) (nth coord 0)))})))
                     locations)]

    (doall (map (fn [e] (sql/insert! ds :geodata e)) entries))
    (response {:result "ok"}))
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
           (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
