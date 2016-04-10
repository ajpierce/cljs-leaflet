(ns cljs-leaflet.core
  (:require [cljsjs.leaflet]))

(enable-console-print!)

(defonce map-info {
   :center [51.505 -0.09]
   :zoom 13
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

;; cljsjs doesn't come with image assets, so substitute with circlemarker settings
(defonce gj-marker-settings
  {:radius 8
   :fillColor "#ff7800"
   :color "#000"
   :weight 1
   :opacity 1
   :fillOpacity 0.8} )

(defn point-to-layer [feature, latlng]  ;; http://leafletjs.com/examples/geojson.html
  (.circleMarker js/L latlng (clj->js gj-marker-settings)))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state
  (atom {:latest-id 0
         :map-layers {} }))

(defonce main-map
  (.setView (.map js/L "map") (clj->js (:center map-info)) (:zoom map-info)))

(defonce tile-layer
  (.tileLayer js/L
              (:url (:tiles map-info))
              (clj->js (:options (:tiles map-info))) ))

(defonce vector-layer (.layerGroup js/L))

(defn setup-map! [lmap]
  (.addTo tile-layer lmap)
  (.addTo vector-layer lmap))

(setup-map! main-map)

;; -- Function to add a point directly to the map -- ;;
(defn add-point-directly! [layer coords]
  (println (str "Adding point to map at coords" coords))
  (.addTo (.marker js/L
                   (clj->js coords)
                   (clj->js map-icon))
          layer))
;;(add-point-directly! vector-layer [51.5, -0.09])
;; -------------------------------------------------- ;;

;; Gets a unique identifier for layers by incrementing an int in the app state
(defn get-layer-id! []
  (:latest-id (swap! app-state update-in [:latest-id] inc)))

;; Functions to swap data into (or out of) our atom
(defn add-point! [point-data]
  (swap! app-state assoc-in [:map-layers (get-layer-id!)] point-data))

(defn remove-point! [id]
  (println (str "Removing point " id))
  (swap! app-state update-in [:map-layers] dissoc id))

(defn create-geojson [features]
  (let [fc {:type "FeatureCollection" :features features}
        fcjs (clj->js fc)]
    (.geoJson js/L fcjs
                   (clj->js {:pointToLayer point-to-layer}) )))

 ;; Listens for state changes in the atom and updates the map accordingly
(add-watch app-state :map-layers-watcher
  (fn [r k old-state new-state]
    (when (not= (:map-layers old-state) (:map-layers new-state))
      (let [features (vals (:map-layers new-state))
            gj (create-geojson features)]
        ;; Clear map layers and re-add the new layer
        (.clearLayers vector-layer)
        (.addLayer vector-layer gj) ))))

(add-point! {:coordinates [-0.09, 51.5] :type "Point"})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)
