(ns meetup.compojure
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [clojure.walk :refer [keywordize-keys]]
            [compojure.core :refer [defroutes POST]]
            [compojure.route :as route]))

(defonce orders (atom {}))

(def recipes #{:hawaian :italian :vegan})

(def max-count 5)


(defn handle-order [pizza nb]

  (cond (not (some #{(keyword pizza)} recipes)) {:status 404 :body "the requested recipe does not exists"}
        (> nb max-count) {:status 503 :body "you ordered too many pizzas"}
        :else (do (swap! orders into {(keyword (str (java.util.UUID/randomUUID))) {:pizza pizza
                                                                                   :count nb}})
                {:status 200
                 :body "your pizza will be available shortly"})))

(defn cancel-pizza-handler [params]
  (let [id (:id (keywordize-keys params))]
    (if-not (nil? (get @orders (keyword id)))
      (do (swap! orders dissoc (keyword id))
        {:status 200
         :body "your order has been canceled"})
      {:status 200
       :body "your order does not exists"})))

(defroutes handler
  (POST "/order/:pizza" [pizza nb] (handle-order pizza (read-string nb)))
  (POST "/cancel/pizza" {params :params} (cancel-pizza-handler params))

  (route/not-found "<h1>Invalid route.</h1>"))

(defonce server
  (run-jetty (-> #'handler
                 (wrap-stacktrace)
                 (wrap-params))
             {:port 3001 :join? false}))
