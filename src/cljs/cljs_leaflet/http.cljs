(ns cljs-leaflet.http
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.string :as str]))

(defn random-points [num-points map-bounds]
  (go (let [response (<! (http/get "/points"
                                   {:query-params {"n" num-points
                                                   "bounds" (str/join "," map-bounds)}}))]
        (:body response) )))
