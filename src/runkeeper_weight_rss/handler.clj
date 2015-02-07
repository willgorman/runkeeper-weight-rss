(ns runkeeper-weight-rss.handler
  (:require [clojure.data.json :as json]
            [clj-rss.core :as rss]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [org.httpkit.client :as http]
            [pandect.algo.sha1 :refer [sha1]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import (java.text SimpleDateFormat)))

(def auth-redirect-url "http://localhost:3000/redirect")

(def auth-page
  (str "<a href=\"https://runkeeper.com/apps/authorize?client_id=" (env :client-id)
       "&response_type=code&redirect_uri=" auth-redirect-url "\">Authorize</a>"))

(defn request-token
  "docstring"
  [code]
  (http/post "https://runkeeper.com/apps/token"
             {:query-params {:grant_type    "authorization_code"
                             :code          code
                             :client_id     (env :client-id)
                             :client_secret (env :client-secret)
                             :redirect_uri  auth-redirect-url}}))

(defn fetch-weight-feed
  "docstring"
  []
  (-> @(http/get "https://api.runkeeper.com/weight"
                 {:headers {"Authorization" (str "Bearer " (env :token))
                            "Accept"        "application/vnd.com.runkeeper.WeightFeed+json"}})
      (:body)
      (json/read-str)
      (get "items")))

(defn to-feed-item
  "docstring"
  [weight]
  (let [weight-val (str (get weight "weight"))
        timestamp (.parse (SimpleDateFormat. "EEE, d MMM yyyy HH:mm:ss") (get weight "timestamp"))]
    {:title   weight-val
     :pubDate timestamp
     :link    (env :item-url)
     :guid    (sha1 (str weight-val (get weight "timestamp")))}))

(defn channel-for
  "docstring"
  [items]
  (rss/channel-xml {:title "Weight Feed" :link (env :item-url) :description "Nunya"}
                   (map to-feed-item (filter #(get % "weight") items))))

(defn rss-response
  "docstring"
  [channel]
  {:status 200
   :headers {"Content-Type" "application/rss+xml; charset=utf-8"}
   :body channel})


(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET (env :feed-url) [] (rss-response (channel-for (fetch-weight-feed))))
  (GET "/auth" [] auth-page)
  (GET "/redirect" [code] (str code))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))
