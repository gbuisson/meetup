(ns meetup.compojure-api
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))

(defonce orders (atom {}))

(s/defschema Supplement
  {:type (s/enum :cheese :beef)})

(s/defschema Pizza
  {:recipe (s/enum :hawaian :italian :vegan)
   :size (s/enum :small :medium :large)
   (s/optional-key :supplements) [Supplement]})

(s/defschema Dessert
  {:type (s/enum :yoghourt :cake :fruit)})

(s/defschema Drink
  {:brand (s/enum :coca-cola :fanta :sprite)})

(s/defschema Order
  {:type (s/enum :in-place :take-away)
   :pizzas [Pizza]
   :drinks [Drink]
   :desserts [Dessert]})

(defn place [order]
  (swap! orders into {(keyword (str (java.util.UUID/randomUUID))) order}) order)

(defapi app
  (swagger-ui)
  (swagger-docs :title "Guillaume & Yann's quality pizzas")
  (swaggered "Order"
             :description "Order operations"
             (context "/order" []
             (POST* "/" []
                    :return Order
                    :body [order Order]
                    :summary "Place an order"
                    (place order)))))

(defonce server
  (run-jetty (-> #'app)
             {:port 3003 :join? false}))
