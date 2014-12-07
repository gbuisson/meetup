(ns meetup.basic
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [clojure.walk :refer [keywordize-keys]]))

(defonce orders (atom []))

(defn handler [request]
  (let [recipes #{:hawaian :italian :vegan}
        max-count 5
        p (keywordize-keys (:params request))]

    (if (and (= (:request-method request) :post)
             (= (:uri request) "/order-pizza"))

      (if ((keyword(:recipe p)) recipes)
        (if-not (> (read-string (:count p)) max-count)
          (do (swap! orders conj p)
              {:status 200
               :body "your pizza will be available shortly"})
          {:status 404
           :body "you ordered too many pizzas"})

        {:status 404
         :body "the requested recipe does not exists"})

      {:status 404
       :body "Invalid route or method"})))

(defonce server
  (run-jetty (-> #'handler
                 (wrap-stacktrace)
                 (wrap-params))
             {:port 3000 :join? false}))
