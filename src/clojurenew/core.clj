
(ns clojurenew.core
  "Main entry for Activity World: server, middleware, and startup."
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.util.response :as response]
            [org.httpkit.server :refer [run-server]]
            [clojurenew.db :as db]
            [clojurenew.routes :refer [app-routes]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]))

(def app
  (-> app-routes
      (wrap-defaults (-> site-defaults
                         (assoc-in [:session :store] (cookie-store {:key "a 16-byte secret"}))))
      wrap-multipart-params))

(defn -main
  "Main entrypoint: ensure DB/tables, start HTTP server."
  [& _]
  (db/ensure-db!)
  (run-server #'app {:port 8080})
  (println "Server started on port 8080"))
