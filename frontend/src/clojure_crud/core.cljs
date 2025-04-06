(ns clojure-crud.core)

;; Debugging check
(js/console.log "Core namespace loaded")

(defn render []
      (let [app (js/document.getElementById "app")]
           (set! (.-innerHTML app)
                 "<h1 style='color: green'>It Works!</h1>
                  <p>ClojureScript is running properly</p>")))

;; Properly exported init function
(defn ^:export init []
      (js/console.log "Init function executed")
      (render))

;; Call init immediately to test
(init)