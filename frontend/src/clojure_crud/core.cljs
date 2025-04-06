(ns clojure-crud.core
  (:require [cljs-http.client :as http]
            [reagent.dom :as rdom]))

(defn api-test []
      (http/get "http://localhost:3000/api"
                {:with-credentials? false}
                #(js/console.log "Success:" %)
                #(js/console.error "Error:" %)))

(defn app []
      [:div
       [:h1 "Frontend"]
       [:button {:on-click api-test} "Call Backend API"]])

(defn ^:export init []
      (rdom/render [app] (js/document.getElementById "app")))