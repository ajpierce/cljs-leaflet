(ns cljs-leaflet.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljsjs.leaflet]
            [cljs.core.async :refer [<!]]
            [cljs-leaflet.http :as h]
            [cljs-leaflet.geo :as g]
            [clojure.data :refer [diff]]
            [reagent.core :as r]))

;; Global application state, `defonce` to persist data between updates
(defonce app-state
  (r/atom {:latest-id 0
           :add-points-qty 10
           :remove-points-qty 10
           :map-layers {} }))

(defonce main-map (g/instantiate-map! "map"))

;; Gets a unique identifier for layers by incrementing an int in the app state
(defn get-layer-id! []
  (:latest-id (swap! app-state update-in [:latest-id] inc)))

;; Get n new layer IDs, updating the app-state to track the next latest
(defn get-new-layer-ids! [n]
  (let [id (:latest-id (swap! app-state update-in [:latest-id]  #(+ % n)))]
    (range (- id n) id) ))

;; Get the IDs of the last n layers; not updating app-state
(defn get-last-layer-ids [n]
  (let [id (:latest-id @app-state)]
    (range (- id n) id)))

;; Functions to swap map data into (or out of) our app state (atom)
(defn add-point! [point-data]
  (swap! app-state assoc-in [:map-layers (get-layer-id!)] point-data))

;; Takes an array of GeoJSON Point geometries and plots them on the map
(defn add-points! [points-list]
  (let [ids (get-new-layer-ids! (count points-list))
        points-map (zipmap ids points-list)]
    (swap! app-state assoc :map-layers (merge (:map-layers @app-state) points-map)) ))

;; Takes one or more IDs and removes the points associated with them
(defn remove-point! [id]
   (swap! app-state assoc :map-layers (apply dissoc (:map-layers @app-state) id))
   (let [max-id (apply max (-> @app-state :map-layers keys))
         latest-id (if (nil? max-id)
                     0
                     (+ 1 max-id) )]
     (swap! app-state assoc :latest-id latest-id) ))

;; Listens for state changes in the atom and updates the map accordingly
(add-watch app-state :map-layers-watcher
  (fn [r k old-state new-state]
    (when (not= (:map-layers old-state) (:map-layers new-state))
      (let [diffs (diff
                    (:map-layers old-state)
                    (:map-layers new-state) )
            values-added (vals (second diffs))
            values-removed (vals (first diffs))
            values-added? (< 0 (count values-added))
            values-removed? (< 0 (count values-removed))]
        (if (and values-added? (not values-removed?))
          (doseq [p values-added] (g/add-to-gj-layer! p))
          (g/re-render-gj-layer! (vals (:map-layers new-state))) )
        ))))

(defn random-points-in-main-map-view [n]
  (g/generate-random-points main-map n))

;; -- React Components

(defn add-qty-input [ratom]
  [:input {:type "number"
           :value (:add-points-qty @ratom)
           :on-change #(swap! ratom assoc :add-points-qty
                              (-> % .-target .-value js/parseInt)) }])

(defn add-points-html []
  [:div
   [:b "Add points: " (add-qty-input app-state)]
   [:input {:type "button"
            :value "Plot"
            :on-click #(-> @app-state :add-points-qty
                           random-points-in-main-map-view
                           add-points!
                           time) }]])

(defn add-points-async-html []
  [:div
   [:b "Add points ASYNC: " (add-qty-input app-state)]
   [:input {:type "button"
            :value "Request Points"
            :on-click #(let [map-bounds (g/get-map-bounds main-map)
                             num-points (:add-points-qty @app-state)
                             response-chan (h/random-points num-points map-bounds)]
                           (go (let [response (<! response-chan)]
                                 (-> response add-points! time) )) )}]])

(defn remove-qty-input [ratom]
  [:input {:type "number"
           :value (:remove-points-qty @ratom)
           :on-change #(swap! ratom assoc :remove-points-qty
                              (-> % .-target .-value js/parseInt)) }])

(defn remove-points-html []
  [:div
   [:b "Remove points: " (remove-qty-input app-state)]
   [:input {:type "button"
            :value "Remove points from Map"
            :on-click #(-> @app-state :remove-points-qty
                           get-last-layer-ids
                           remove-point!
                           time) }] ])

(defn total-points-count []
  [:div
   [:b "Total Points: "] (count (:map-layers @app-state)) ])

(defn render-page []
  (g/setup-map! main-map)
  (r/render-component
    [:div
      [add-points-html]
       [add-points-async-html]
      [:br]
      [remove-points-html]
      [:br]
      [total-points-count] ]
    (.getElementById js/document "app")))

(render-page)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc))
