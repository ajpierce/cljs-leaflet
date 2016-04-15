(ns cljs-leaflet.core
  (:require [cljsjs.leaflet]
            [reagent.core :as r]
            [clojure.string :as str]
            [clojure.data]))

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
   :fillOpacity 0.6} )

(defn point-to-layer [feature, latlng]  ;; http://leafletjs.com/examples/geojson.html
  (.circleMarker js/L latlng (clj->js gj-marker-settings)))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state
  (r/atom {:latest-id 0
           :create-points-qty 10
           :map-layers {} }))

(defonce main-map
  (.setView (.map js/L "map") (clj->js (:center map-info)) (:zoom map-info)))

(defonce tile-layer
  (.tileLayer js/L
              (:url (:tiles map-info))
              (clj->js (:options (:tiles map-info))) ))

(defonce vector-layer (.layerGroup js/L))

(defn create-geojson [features]
  (let [fc {:type "FeatureCollection" :features features}
        fcjs (clj->js fc)]
    (.geoJson js/L fcjs
                   (clj->js {:pointToLayer point-to-layer}) )))

(defonce gj-layer (create-geojson []))

(defn setup-map! [lmap]
  (.addTo tile-layer lmap)
  (.addTo vector-layer lmap)
  (.addLayer vector-layer gj-layer))

;; Gets a unique identifier for layers by incrementing an int in the app state
(defn get-layer-id! []
  (:latest-id (swap! app-state update-in [:latest-id] inc)))

;; Get n valid IDs
(defn get-layer-ids! [n]
  (let [id (:latest-id (swap! app-state update-in [:latest-id]  #(+ % n)))]
    (range (- id n) id) ))

;; Functions to swap data into (or out of) our atom
(defn add-point! [point-data]
  (swap! app-state assoc-in [:map-layers (get-layer-id!)] point-data))

(defn add-points! [points-list]
  (let [ids (get-layer-ids! (count points-list))
        points-map (zipmap ids points-list)]
    (swap! app-state assoc :map-layers (merge (:map-layers @app-state) points-map)) ))

(defn remove-point! [id]
  (println (str "Removing point " id))
  (swap! app-state update-in [:map-layers] dissoc id))

(defn add-to-geojson! [data]
  (.addData gj-layer (clj->js data)))

 ;; Listens for state changes in the atom and updates the map accordingly
(add-watch app-state :map-layers-watcher
  (fn [r k old-state new-state]
    (when (not= (:map-layers old-state) (:map-layers new-state))
      (let [diffs (clojure.data/diff
                    (:map-layers old-state)
                    (:map-layers new-state) )
            values-added (vals (second diffs)) ]
        (doseq [p values-added] (add-to-geojson! p)) ))))

(defn get-map-bounds [lmap]
  (let [bbox-string (.toBBoxString (.getBounds lmap))
        bbox-vector (str/split bbox-string ",")]
    (map js/parseFloat bbox-vector)))

(defn random-coords-in-view [lmap]
  (let [bounds (get-map-bounds lmap)
        lngs (take-nth 2 bounds)
        lats (take-nth 2 (rest bounds))
        lng-range (- (last lngs) (first lngs) )
        lat-range (- (last lats) (first lats) ) ]
    [(+ (first lngs) (rand lng-range)) (+ (first lats) (rand lat-range))] ))

(defn generate-random-point []
  {:coordinates (random-coords-in-view main-map) :type "Point"})

(defn generate-random-points [n]
  (repeatedly n #(generate-random-point)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc))

;; Button to generate random points on the map
(defn add-points-btn []
  [:div
   [:br]
   [:input {:type "button"
            :value "Add points to Map"
            :on-click #(-> @app-state :create-points-qty
                           generate-random-points
                           add-points!
                           time) }]])

(defn point-qty-input [ratom]
  [:input {:type "number"
           :value (:create-points-qty @ratom)
           :on-change #(swap! ratom assoc :create-points-qty (-> % .-target .-value js/parseInt)) }])

(defn total-points-count []
  [:div
   [:b "Add points: " (point-qty-input app-state)]
   [:br]
   [:b "Total Points: "] (count (:map-layers @app-state)) ])

(defn render-page []
  (r/render-component
    [:div
     [add-points-btn]
     [:br]
     [total-points-count] ]
    (.getElementById js/document "app"))
  (setup-map! main-map))

(render-page)
