(ns cljs-leaflet.geo
  (:require [cljsjs.leaflet]
            [cljs-leaflet.secrets :as 🙈]
            [cljs-leaflet.common :as c]
            [clojure.string :as str] ))

(enable-console-print!)

(defonce map-info {
   :center [51.505 -0.09]
   :zoom 13
   :tiles {
           :url (str "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=" 🙈/mapbox-api-key)
           :options {
                     :maxZoom 18
                     :attribution (str "Map data &copy; <a href='http://openstreetmap.org'>OpenStreetMap</a> contributors, "
                                       "<a href='http://creativecommons.org/licenses/by-sa/2.0/'>CC-BY-SA</a>, "
                                       "Imagery © <a href='http://mapbox.com'>Mapbox</a>")
                     :id "mapbox.streets"} }})

; return L.map(div-id).setView(map-info.center, map-info.zoom);
(defn instantiate-map! [div-id]
  (.setView (.map js/L div-id)
            (clj->js (:center map-info))
            (:zoom map-info)))

(defonce tile-layer
  (.tileLayer js/L
              (:url (:tiles map-info))
              (clj->js (:options (:tiles map-info))) ))

(defonce vector-layer (.layerGroup js/L))

;; cljsjs doesn't come with image assets, so substitute with circlemarker settings
(defonce gj-marker-settings
  {:radius 8
   :fillColor "#ff7800"
   :color "#000"
   :weight 1
   :opacity 1
   :fillOpacity 0.6} )

(defn point-to-layer [feature, latlng]  ;; http://leafletjs.com/examples/geojson.html
  (.circleMarker js/L latlng (clj->js gj-marker-settings)))

(defn create-geojson [features]
  (let [fc {:type "FeatureCollection" :features features}]
    (.geoJson js/L (clj->js fc)
                   (clj->js {:pointToLayer point-to-layer}) )))

(defonce gj-layer (create-geojson []))

(defn setup-map! [lmap]
  (.addTo tile-layer lmap)
  (.addTo vector-layer lmap)
  (.addLayer vector-layer gj-layer))

(defn get-map-bounds [lmap]
  (let [bbox-string (.toBBoxString (.getBounds lmap))
        bbox-vector (str/split bbox-string ",")]
    (map js/parseFloat bbox-vector)))

(defn add-to-gj-layer! [data]
  (.addData gj-layer (clj->js data)))

(defn re-render-gj-layer! [data]
  (if-not (nil? data)
    (do
      (.clearLayers gj-layer)
      (.addData gj-layer (clj->js data)))
    ; If data is nil, just clear the map
    (.clearLayers gj-layer) ))

(defn random-coords-in-map-view [lmap]
  (let [bounds (get-map-bounds lmap)]
    (c/random-coords-in-bounds bounds)))

(defn generate-random-point-on-map [lmap]
  {:coordinates (random-coords-in-map-view lmap) :type "Point"})

(defn generate-random-points [lmap, n]
  (repeatedly n #(generate-random-point-on-map lmap)))
