(ns clojure_crud.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! go]]))

;; State
(defonce state (r/atom {:items []
                        :new-item ""
                        :editing nil}))

;; API Calls
(defn fetch-items! []
      (go (let [response (<! (http/get "http://localhost:3000/items"))]
               (swap! state assoc :items (:body response)))))

(defn create-item! [name value]
      (go (<! (http/post "http://localhost:3000/items"
                         {:json-params {:name name :value value}}))
          (fetch-items!)))

(defn update-item! [id name value]
      (go (<! (http/put (str "http://localhost:3000/items/" id)
                        {:json-params {:name name :value value}}))
          (fetch-items!)))

(defn delete-item! [id]
      (go (<! (http/delete (str "http://localhost:3000/items/" id)))
          (fetch-items!)))

;; Components
(defn item-form []
      (let [name (r/atom "")
            value (r/atom 0)]
           (fn []
               [:div.form
                [:input {:type "text" :value @name
                         :on-change #(reset! name (-> % .-target .-value))}]
                [:input {:type "number" :value @value
                         :on-change #(reset! value (-> % .-target .-value int))}]
                [:button {:on-click #(do (create-item! @name @value)
                                         (reset! name "")
                                         (reset! value 0))}
                 "Add Item"]])))

(defn item-list []
      [:div.items
       (for [{:keys [id name value]} (:items @state)]
            ^{:key id}
            [:div.item
             (if (= id (:editing @state))
               [:div
                [:input {:default-value name
                         :on-blur #(update-item! id (-> % .-target .-value) value)}]
                [:input {:type "number" :default-value value
                         :on-blur #(update-item! id name (-> % .-target .-value int))}]]
               [:div
                [:span name " (" value ")"]
                [:button {:on-click #(swap! state assoc :editing id)} "✏️"]
                [:button {:on-click #(delete-item! id)} "🗑️"]])])

       (defn bar-chart []
             [:div.chart
              (for [{:keys [name value]} (:items @state)]
                   ^{:key name}
                   [:div.bar-container
                    [:div.bar-label name]
                    [:div.bar {:style {:height (str (* 5 value) "px")}}
                     value]])])

       (defn app []
             [:div
              [:h1 "CRUD Visualization"]
              [item-form]
              [item-list]
              [bar-chart]])

       ;; Initialize
       (defn ^:export init []
             (rdom/render [app] (js/document.getElementById "app"))(fetch-items!))])