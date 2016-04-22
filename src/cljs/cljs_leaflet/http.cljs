(ns cljs-leaflet.http
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn req-github-users [since]
  (go (let [response (<! (http/get "https://api.github.com/users"
                                   {:with-credentials? false
                                    :query-params {"since" since}}))]
        (println (:status response))
        (println (map :login (:body response))))))

(defn print-usr-btn-txt [since]
  (if (< 0 since)
    (str "Print Github users (in console), offset by " since)
    "Print Github users (in console)" ))

(defn print-github-users-btn [& {:keys [since] :or {since 0}}]
  [:input {:type "button"
           :value (print-usr-btn-txt since)
           :on-click #(req-github-users since) }])
