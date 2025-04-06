(ns clojure_crud.core
  (:require [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.net ServerSocket Socket]
           [java.io BufferedReader InputStreamReader PrintWriter]))

;; In-memory database
(def db (atom {}))

;; CRUD Operations
(defn create-item [item]
  (let [id (str (java.util.UUID/randomUUID))]
    (swap! db assoc id (assoc item :id id))
    (get @db id)))

(defn read-item [id]
  (get @db id))

(defn read-all-items []
  (vals @db))

(defn update-item [id item]
  (swap! db assoc id (assoc item :id id))
  (get @db id))

(defn delete-item [id]
  (swap! db dissoc id)
  {:message "Item deleted"})

;; HTTP Server Utilities
(defn parse-request [^BufferedReader reader]
  (let [request-line (.readLine reader)
        [method path _] (str/split request-line #" ")
        headers (loop [headers {}
                       line (.readLine reader)]
                  (if (or (nil? line) (empty? line))
                    headers
                    (let [[k v] (str/split line #": " 2)]
                      (recur (assoc headers (str/lower-case k) v) (.readLine reader)))))
        content-length (Long/parseLong (get headers "content-length" "0"))
        body (when (> content-length 0)
               (let [buf (char-array content-length)]
                 (.read reader buf 0 content-length)
                 (String. buf)))]
    {:method method
     :path path
     :headers headers
     :body (when (and body (not (empty? body)))
             (json/read-str body :key-fn keyword))}))

(defn json-response [status body]
  (let [body-str (json/write-str body)]
    (str "HTTP/1.1 " status " OK\r\n"
         "Content-Type: application/json\r\n"
         "Access-Control-Allow-Origin: *\r\n"
         "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n"
         "Access-Control-Allow-Headers: Content-Type\r\n"
         "Content-Length: " (count body-str) "\r\n"
         "\r\n"
         body-str)))

(defn handle-options []
  (str "HTTP/1.1 200 OK\r\n"
       "Access-Control-Allow-Origin: *\r\n"
       "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n"
       "Access-Control-Allow-Headers: Content-Type\r\n"
       "Content-Length: 0\r\n\r\n"))

(defn route-request [request]
  (let [{:keys [method path body]} request
        path-parts (str/split path #"/")
        base-path (str "/" (nth path-parts 1 ""))
        id (when (> (count path-parts) 2) (nth path-parts 2))]
    (cond
      (= method "OPTIONS") (handle-options)
      :else
      (case [method base-path]
        ["GET" "/api/items"] (json-response 200 (read-all-items))
        ["GET" (str "/api/items")] (json-response 200 (read-all-items))
        ["GET" (str "/api/items/" id)] (if-let [item (read-item id)]
                                         (json-response 200 item)
                                         (json-response 404 {:error "Not found"}))
        ["POST" "/api/items"] (json-response 201 (create-item body))
        ["PUT" (str "/api/items/" id)] (json-response 200 (update-item id body))
        ["DELETE" (str "/api/items/" id)] (json-response 200 (delete-item id))
        (json-response 404 {:error "Not found"})))))

(defn handle-client [^Socket client-socket]
  (try
    (with-open [reader (BufferedReader. (InputStreamReader. (.getInputStream client-socket)))
                writer (PrintWriter. (.getOutputStream client-socket))]
      (let [request (parse-request reader)
            response (route-request request)]
        (.print writer response)
        (.flush writer)))
    (catch Exception e
      (println "Error handling client:" (.getMessage e)))))

(defn start-server [port]
  (println (str "Server started on port " port))
  (with-open [server-socket (ServerSocket. port)]
    (while true
      (try
        (let [client-socket (.accept server-socket)]
          (future (handle-client client-socket)))
        (catch Exception e
          (println "Server error:" (.getMessage e)))))))

(defn -main []
  (start-server 3000))