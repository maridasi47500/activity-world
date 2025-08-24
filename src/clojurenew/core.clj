(ns clojurenew.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :as response]
            [org.httpkit.server :refer [run-server]] [clojurenew.db :as db]
            [clojurenew.routes :refer [app-routes app-protected-routes]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.params :as ring-params]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.multipart-params.byte-array :refer [byte-array-store]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery *anti-forgery-token*]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [aleph.http.client-middleware :as ahclient]
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
  ;(println (str request :form-params))
  (println "CSRF token missing or invalid for request:" (:uri request))
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body (str "session contents<br>" (:session request) "<p>current token </p>" *anti-forgery-token* "<hr>" (get (:params request) "__anti-forgery-token") "<h3>test for a post route : title of news added</h3>" "<h1>403 Forbidden</h1><p>CSRF protection triggered.</p>" (get (:params request) "title") "<h3>title</h3>"  "<h3>photo content</h3>" (get (:params request) "photo") "<h3>photo</h3>" "<hr><br>" request "<h1>handler Missing anti-forgery token</h1>")})







(let [expires-in-one-hour (time/hours 1)
      ;secret "secret-to-validate-token-after-decryption-to-make-sure-i-encrypted-stuff"
      secret "secretkey"
      signed-token-strategy (signed-token/signed-token
                              (keys/public-key "dev-resources/test-certs/pubkey.pem")
                              (keys/private-key "dev-resources/test-certs/privkey.pem" "secretkey")
                              expires-in-one-hour
                              :identity)]
      (wrap-anti-forgery custom-error-handler {:strategy signed-token-strategy}))



(def anti-forgery-handler
  "hey"
  *anti-forgery-token*)
(def custom-defaults
  (-> site-defaults
      (assoc-in [:security :anti-forgery] false))) ; disable built-in anti-forgery
(def app
  (-> app-routes 
      (wrap-defaults (-> site-defaults))
      ;(wrap-defaults custom-defaults)
      (wrap-session)

;a 16 long bytes string

      ;(wrap-anti-forgery {:safe-header "X-CSRF-Protection"})
      ;(wrap-anti-forgery {:read-token get-custom-token})
      ;(wrap-anti-forgery {:error-response custom-error-response})
      (wrap-anti-forgery {:safe-header "X-CSRF-Protection", :error-handler custom-error-handler})
      ;(wrap-anti-forgery handler {:strategy custom-strategy})
      ;(wrap-anti-forgery handler {:strategy encrypted-token-strategy})
      (ahclient/wrap-form-params)
      (ring-params/wrap-params)
      (wrap-multipart-params {:store (byte-array-store)})


))

(defn -main
  "Main entrypoint: ensure DB/tables, start HTTP server."
  [& _]
  (db/ensure-db!)
  (run-server #'app {:port 8080})
  (println "Server started on port 8080"))

