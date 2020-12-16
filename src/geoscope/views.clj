(ns geoscope.views
  (:require [hiccup.page :refer [html5]]))

(defn index-view
  "docstring"
  [req]

  (html5
    [:html {:lang "en"}
     [:head
      ;[:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.4.3/css/ol.css" :type "text/css"}]
      ;[:script {:src "https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.4.3/build/ol.js"}]
      ;[:script {:src "https://unpkg.com/mithril/mithril.js"}]
      [:link {:rel "stylesheet" :href "https://unpkg.com/leaflet@1.7.1/dist/leaflet.css"}]
      [:script {:src "https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"}]
      [:title "OpenLayers example"]]
     [:body
      [:div {:style "width: 300px; height: 600px; float: left; background-color: #999"}
       ]
      [:div [:select#day]]
      [:div#map {:style "height: 800px; border: #999 5px solid"}]
      [:script {:src "/app2.js"}]]]))
