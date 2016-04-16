(ns cljs-leaflet.core
  (:require [cljsjs.leaflet]
            [cljs-leaflet.geo :as g]
            [clojure.data]
            [reagent.core :as r]))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state
  (r/atom {:latest-id 0
           :create-points-qty 10
           :map-layers {} }))

(defonce main-map (g/instantiate-map! "map"))

;; Gets a unique identifier for layers by incrementing an int in the app state
(defn get-layer-id! []
  (:latest-id (swap! app-state update-in [:latest-id] inc)))

;; Get n valid IDs
(defn get-layer-ids! [n]
  (let [id (:latest-id (swap! app-state update-in [:latest-id]  #(+ % n)))]
    (range (- id n) id) ))

;; Functions to swap map data into (or out of) our app state (atom)
(defn add-point! [point-data]
  (swap! app-state assoc-in [:map-layers (get-layer-id!)] point-data))

(defn add-points! [points-list]
  (let [ids (get-layer-ids! (count points-list))
        points-map (zipmap ids points-list)]
    (swap! app-state assoc :map-layers (merge (:map-layers @app-state) points-map)) ))

(defn remove-point! [id]
  (println "Removing point " id)
  (swap! app-state update-in [:map-layers] dissoc id))

 ;; Listens for state changes in the atom and updates the map accordingly
(add-watch app-state :map-layers-watcher
  (fn [r k old-state new-state]
    (when (not= (:map-layers old-state) (:map-layers new-state))
      (let [diffs (clojure.data/diff
                    (:map-layers old-state)
                    (:map-layers new-state) )
            values-added (vals (second diffs)) ]
        (doseq [p values-added] (g/add-to-gj-layer! p)) ))))

(defn random-points-in-main-map-view [n]
  (g/generate-random-points main-map n))

;; Button to generate random points on the map
(defn add-points-btn []
  [:div
   [:br]
   [:input {:type "button"
            :value "Add points to Map"
            :on-click #(-> @app-state :create-points-qty
                           random-points-in-main-map-view
                           add-points!
                           time) }]])

(defn point-qty-input [ratom]
  [:input {:type "number"
           :value (:create-points-qty @ratom)
           :on-change #(swap! ratom assoc :create-points-qty
                              (-> % .-target .-value js/parseInt)) }])

(defn total-points-count []
  [:div
   [:b "Add points: " (point-qty-input app-state)]
   [:br]
   [:b "Total Points: "] (count (:map-layers @app-state)) ])

(defn render-page []
  (g/setup-map! main-map)
  (r/render-component
    [:div
     [add-points-btn]
     [:br]
     [total-points-count] ]
    (.getElementById js/document "app")))

(render-page)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc))
