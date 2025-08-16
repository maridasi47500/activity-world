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

(def app
  (-> app-routes
      (wrap-defaults (-> site-defaults
                         (assoc-in [:session :store] (cookie-store {:key "abcdefg123456789"})))) ;a 16 long character string
      (wrap-anti-forgery app-protected-routes)
      (wrap-session)
      (wrap-multipart-params {:store (byte-array-store)})))

(defn -main
  "Main entrypoint: ensure DB/tables, start HTTP server."
  [& _]
  (db/ensure-db!)
  (run-server #'app {:port 8080})
  (println "Server started on port 8080"))

