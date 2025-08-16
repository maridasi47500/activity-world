
(ns clojurenew.core
  "Main entry for Activity World: server, middleware, and startup."
  (:require [compojure.core :refer [defroutes GET]]
         [compojure.route :as route]
         [ring.util.response :as response]
         [org.httpkit.server :refer [run-server]]
         [clojurenew.db :as db]
         [clojurenew.routes :refer [app-routes app-protected-routes]]
         [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
         [ring.middleware.multipart-params.byte-array :as byte-array]
         [ring.middleware.multipart-params :refer [wrap-multipart-params]]
         [ring.middleware.session :refer [wrap-session]]
         [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
         [ring.middleware.session.cookie :refer [cookie-store]]
         [hiccup.core :refer [html]]))

(defn get-custom-token [request]
  (get-in request [:headers "x-forgery-token"]))


(ns my-app.core
  (:require [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [response]]
            [hiccup.core :refer [html]]))
(ns my-app.core
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [response]]
            [hiccup.core :refer [html]]))

;; Fonction utilitaire pour générer le champ caché
(defn anti-forgery-field []
  [:input {:type "hidden"
           :name "__anti-forgery-token"
           :value *anti-forgery-token*}])

(defn protected-form []
  (html
   [:form {:method "POST" :action "/submit"}
    (anti-forgery-field)
    [:input {:type "text" :name "data"}]
    [:input {:type "submit"}]]))

(defroutes protected-routes
  (GET "/form" [] (response (protected-form)))
  (POST "/submit" [] (response "Form submitted!")))

(def app
  ;; Appliquer le middleware uniquement aux routes protégées
  (wrap-anti-forgery protected-routes))





(def app
  (-> app-routes
      (wrap-defaults (-> site-defaults
                         (assoc-in [:session :store] (cookie-store {:key "a 16-byte secret"}))))
      (wrap-anti-forgery app-protected-routes)
      (wrap-session)
      (wrap-multipart-params {:store (ring.middleware.multipart-params.byte-array/byte-array-store)})))

(defn -main
  "Main entrypoint: ensure DB/tables, start HTTP server."
  [& _]
  (db/ensure-db!)
  (run-server #'app {:port 8080})
  (println "Server started on port 8080"))
