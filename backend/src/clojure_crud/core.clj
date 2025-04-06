(ns clojure_crud.core
  (:require [ring.middleware.cors :refer [wrap-cors]]
            [ring.adapter.jetty :as jetty]))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body "{\"message\":\"Hello from backend\"}"})

(def app
  (-> handler
      (wrap-cors
        :access-control-allow-origin [#"http://localhost:8080"]
        :access-control-allow-methods [:get :post :put :delete])))

(defn -main []
  (jetty/run-jetty app {:port 3000}))