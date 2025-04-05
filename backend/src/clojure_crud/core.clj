(ns clojure_crud.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.json :as middleware]
            [clojure.data.json :as json]))


;; In-memory database
(defonce db (atom {:items {1 {:id 1 :name "Sample" :value 5}}}))

;; CRUD Handlers
(defn create-item [item]
  (let [id (inc (apply max (keys (:items @db))))]
    (swap! db assoc-in [:items id] (assoc item :id id))))

(defn read-items []
  (vals (:items @db)))

(defn update-item [id updates]
  (swap! db update-in [:items id] merge updates))

(defn delete-item [id]
  (swap! db update :items dissoc id))

;; HTTP Handlers
(defn app [request]
  (case [(:request-method request) (:uri request)]
    [:get "/items"] {:status 200
                     :headers {"Content-Type" "application/json"}
                     :body (json/write-str (read-items))}

    [:post "/items"] (let [item (json/read-str (slurp (:body request)) :key-fn keyword)]
                       (create-item item)
                       {:status 201 :body "Created"})

    [:put "/items/:id"] (let [id (-> request :params :id Integer/parseInt)
                              updates (json/read-str (slurp (:body request)) :key-fn keyword)]
                          (update-item id updates)
                          {:status 200 :body "Updated"})

    [:delete "/items/:id"] (let [id (-> request :params :id Integer/parseInt)]
                             (delete-item id)
                             {:status 204 :body ""})

    {:status 404 :body "Not Found"}))

(defn -main []
  (jetty/run-jetty (middleware/wrap-json-body app {:keywords? true})
                   {:port 3000 :join? false}))