(ns clojure_crud.handler
  (:require [ring.util.response :as resp]
            [clojure.data.json :as json]))

(def items (atom {1 {:id 1 :text "Sample item"}}))

(defn handle-items [request]
  (case (:request-method request)
    :get (resp/response (json/write-str (vals @items)))
    :post (let [item (-> request :body slurp (json/read-str :key-fn keyword))]
            (swap! items assoc (:id item) item)
            (resp/response (json/write-str item)))
    :put (let [item (-> request :body slurp (json/read-str :key-fn keyword))]
           (swap! items assoc (:id item) item)
           (resp/response (json/write-str item)))
    :delete (do (swap! items dissoc (-> request :route-params :id read-string))
                (resp/response ""))))