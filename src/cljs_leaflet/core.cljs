(ns cljs-leaflet.core
  (:require [cljsjs.leaflet]))

(enable-console-print!)

(defonce map-info {
   :center [51.505 -0.09]
   :tiles {
           :url "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw",
           :options {
                     :maxZoom 18
                     :attribution (str "Map data &copy; <a href='http://openstreetmap.org'>OpenStreetMap</a> contributors, "
                                       "<a href='http://creativecommons.org/licenses/by-sa/2.0/'>CC-BY-SA</a>, "
                                       "Imagery © <a href='http://mapbox.com'>Mapbox</a>")
                     :id "mapbox.streets"} }})

;; cljsjs doesn't come with leaflet image assets, so we create our own icons
(defonce map-icon
  {:icon (.icon js/L (clj->js {
    :iconUrl "https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-icon-2x.png"
    :shadowUrl "https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png"
    :iconSize [25 41]
    :iconAnchor [12 41] }))})

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {}))

(defonce main-map
  (.setView (.map js/L "map") (clj->js (:center map-info)) 13))

(defonce tile-layer
  (.tileLayer js/L
              (:url (:tiles map-info))
              (clj->js (:options (:tiles map-info))) ))

(defn setup-map! []
  (println "Adding tiles to the map")
  (.addTo tile-layer main-map))

(setup-map!)

(defn add-point! [lmap coords]
  (println (str "Adding point to map at coords" coords))
  (.addTo (.marker js/L
                   (clj->js coords)
                   (clj->js map-icon))
          lmap))

(add-point! main-map [51.5, -0.09])

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)
