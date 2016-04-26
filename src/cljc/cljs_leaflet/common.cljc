(ns cljs-leaflet.common)

;; -- Common Functions to be used both server- and client-side

(defn random-coords-in-bounds [bounds]
  (let [lngs (take-nth 2 bounds)
        lats (take-nth 2 (rest bounds))
        lng-range (- (last lngs) (first lngs) )
        lat-range (- (last lats) (first lats) ) ]
    [(+ (first lngs) (rand lng-range)) (+ (first lats) (rand lat-range))] ))

(defn generate-random-point-in-bounds [bounds]
  {:coordinates (random-coords-in-bounds bounds) :type "Point"})
