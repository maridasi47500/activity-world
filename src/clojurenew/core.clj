(ns clojurenew.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :as response]
            [org.httpkit.server :refer [run-server]]
            [clojurenew.db :as db]
            [clojurenew.routes :refer [app-routes app-protected-routes]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.multipart-params.byte-array :refer [byte-array-store]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [hiccup.core :refer [html]]))
(import 'java.security.SecureRandom)
(import 'java.util.Base64)

(defn generate-key-hex []
  (let [bytes (byte-array 16)]
    (.nextBytes (SecureRandom.) bytes)
    (apply str (map #(format "%02x" %) bytes))))



(defn generate-key-base64 []
  (let [bytes (byte-array 16)]
    (.nextBytes (SecureRandom.) bytes)
    (.encodeToString (Base64/getEncoder) bytes)))


(defn generate-key []
  (let [bytes (byte-array 16)]
        (.nextBytes (SecureRandom.) bytes)))






(def app
  (-> app-routes
      (wrap-defaults (-> site-defaults

)) ;a 16 long bytes string
      (wrap-session)
      (wrap-anti-forgery)
      (wrap-multipart-params {:store (byte-array-store)})))

(defn -main
  "Main entrypoint: ensure DB/tables, start HTTP server."
  [& _]
  (db/ensure-db!)
  (run-server #'app {:port 8080})
  (println "Server started on port 8080"))

