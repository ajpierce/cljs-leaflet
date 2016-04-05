(ns cljs-leaflet.core
  (:require [cljsjs.leaflet]))
(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state
  (atom { :map {
                :center {:lat -0.09, :lng 51.505 }
                :tiles {
                        :url "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw",
                        :options {
                                  :maxZoom 18
                                  :attribution (str "Map data &copy; <a href='http://openstreetmap.org'>OpenStreetMap</a> contributors, "
                                                    "<a href='http://creativecommons.org/licenses/by-sa/2.0/'>CC-BY-SA</a>, "
                                                    "Imagery © <a href='http://mapbox.com'>Mapbox</a>")
                                  :id "mapbox.streets"} }}}))

(defonce main-map
  (let [lat (:lat (:center (:map @app-state)))
        lng (:lng (:center (:map @app-state)))]
    (.setView (.map js/L "map") #js [lng lat] 13) ))

(defonce tile-layer
  (.tileLayer js/L 
              (:url (:tiles (:map @app-state)))
              (clj->js (:options (:tiles (:map @app-state)))) ))

(defn setup-map! []
  (println "Adding tiles to the map")
  (.addTo tile-layer main-map))

(setup-map!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)
