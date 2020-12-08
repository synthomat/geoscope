(ns geoscope.views
  (:require [hiccup.page :refer [html5]]))

(defn index-view
  "docstring"
  [req]

  (html5
    [:html {:lang "en"}
     [:head
      [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.4.3/css/ol.css" :type "text/css"}]
      [:script {:src "https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.4.3/build/ol.js"}]
      [:script {:src "https://unpkg.com/mithril/mithril.js"}]

      [:title "OpenLayers example"]]
     [:body
      [:script {:src "/app.js"}]]]))
