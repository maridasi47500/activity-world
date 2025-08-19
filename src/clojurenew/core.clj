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
            [hiccup.core :refer [html]]
         
         [ring.middleware.anti-forgery.signed-token :as signed-token]
         [ring.middleware.anti-forgery :refer :all]
         [buddy.core.keys :as keys]
         [clj-time.core :as time]))











(defn get-custom-token [request]
  (get-in request [:headers "x-forgery-token"]))
(def custom-error-response
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body (str "hey" "<h1>Reponse : Missing anti-forgery token</h1>")})
(defn custom-error-handler [request]
  (println (get-in request [:headers "x-forgery-token"]))
  (println (str request :form-params))
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body (str request "<h1>handler Missing anti-forgery token</h1>")})






(let [expires-in-one-hour (time/hours 1)
      ;secret "secret-to-validate-token-after-decryption-to-make-sure-i-encrypted-stuff"
      secret "secretkey"
      signed-token-strategy (signed-token/signed-token
                              (keys/public-key "dev-resources/test-certs/pubkey.pem")
                              (keys/private-key "dev-resources/test-certs/privkey.pem" "secretkey")
                              expires-in-one-hour
                              :identity)]
      (wrap-anti-forgery custom-error-handler {:strategy signed-token-strategy}))

(def app
  (-> app-routes


      (wrap-defaults (-> site-defaults

)) ;a 16 long bytes string
      (wrap-session)

      ;(wrap-anti-forgery {:safe-header "X-CSRF-Protection"})
      ;(wrap-anti-forgery {:read-token get-custom-token})
      ;(wrap-anti-forgery {:error-response custom-error-response})
      ;(wrap-anti-forgery {:error-handler custom-error-handler})
      ;(wrap-anti-forgery handler {:strategy custom-strategy})
      ;(wrap-anti-forgery handler {:strategy encrypted-token-strategy})

      (wrap-multipart-params {:store (byte-array-store)})))

(defn -main
  "Main entrypoint: ensure DB/tables, start HTTP server."
  [& _]
  (db/ensure-db!)
  (run-server #'app {:port 8080})
  (println "Server started on port 8080"))

