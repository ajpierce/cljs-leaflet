(ns cljs-leaflet.server
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.util.response :refer [response]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]

            ;; Application Imports
            [cljs-leaflet.common :as c] )
  (:gen-class))

(defn random-points-response [params]
  (let [num-points (if (nil? (:n params))
                     0
                     (. Integer parseInt (:n params)))
        ; TODO: Add better error handling for invalid bounds arguments
        bounds (if (nil? (:bounds params))
                 [-180 -90 180 90]
                 (map #(. Double parseDouble %)
                      (str/split (:bounds params) #",") ))]
    (response (repeatedly num-points #(c/generate-random-point-in-bounds bounds))) ))

(defroutes routes
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})
  (GET "/json" _
       (response {:foo "bar"}))
  (GET "/points" {params :params}
       (random-points-response params))
  (resources "/"))

(def http-handler
  (-> routes
      (wrap-json-body)
      (wrap-json-response)
      (wrap-defaults api-defaults)
      wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (println "Starting server on port " port)
    (run-jetty http-handler {:port port :join? false})))
