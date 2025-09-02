(ns clojurenew.routes
  "Compojure routes for Activity World (routes unchanged for view compatibility)."
  (:require [compojure.core :refer :all]
            [clojurenew.handlers :as h]
            [compojure.route :as route]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.multipart-params :as mp]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token* wrap-anti-forgery]]
))


(defn multipart-error-handler [request]
  (println (get-in request [:headers "x-forgery-token"]))
  ;(println (str request :form-params))
  (println "multipart request:" (:uri request))
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body (str "<p>Hey :::: current token </p>"  "<hr>" (get (:params request) "__anti-forgery-token") "<h3>test for a post route : title of news added</h3>" "<h1>403 Forbidden</h1><p>multipart params not received.</p>" (get (:params request) "title") "<h3>title</h3>"  "<h3>photo content</h3>" (get (:params request) "photo") "<h3>photo</h3>" "<hr><br>" request "<h1>handler Missing params</h1>")})
(defn custom-error-handler [request]
  (println (get-in request [:headers "x-forgery-token"]))
  ;(println (str request :form-params))
  (println "CSRF token missing or invalid for request:" (:uri request))
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body (str "<p>Hey :::: current token </p>" *anti-forgery-token* "<hr>" (get (:params request) "__anti-forgery-token") "<h3>test for a post route : title of news added</h3>" "<h1>403 Forbidden</h1><p>CSRF protection triggered.</p>" (get (:params request) "title") "<h3>title</h3>"  "<h3>photo content</h3>" (get (:params request) "photo") "<h3>photo</h3>" "<hr><br>" request "<h1>handler Missing anti-forgery token</h1>")})



(defroutes app-routes
  (GET "/clear-session" [] h/clear-session)
  (GET "/poster_news"  [req] (h/poster-news req))
;(mp/wrap-multipart-params 
  (POST "/action_create_news" [params] 
     (println "Handler reached! Params: " params) 
     (h/action-create-news params))
  ;(POST "/action_create_news" req 
  ;   (println "Handler reached! request: " req) 
  ;   (h/my-debug-handler req))
  ;(wrap-multipart-params (POST "/action_create_news" [params] (h/action-create-news params) ))
  ;(wrap-multipart-params (POST "/action_create_news" [params] (wrap-anti-forgery (h/action-create-news params) {:error-handler custom-error-handler})))
  ;(POST "/action_create_news" [params] (wrap-anti-forgery (h/action-create-news params) {:error-handler custom-error-handler}))
  ;(POST "/action_create_news" [params] (h/action-create-news params) )

  ;; CSS and JS (if needed, adapt as per your previous code)
  (GET "/app.css" [] (h/render-css "Show my activity world" "app.css"))
  (GET "/app.js" [] (h/render-js "Show my activity world" "app.js"))

  ;;pic 
  (GET "/:mypic.jpeg" [mypic :as req] (h/voir-photo-mypic (assoc-in req [:params :mypic] mypic)))

  ;; Home and hello
  (GET "/hello" [] h/home)
  (GET "/" [] h/home)

  ;; News: routes preserved as in original code

  (GET "/voir_news" [] h/voir-news)
  (GET "/voir_news/:id" [id :as req] (h/voir-news-id (assoc-in req [:params :id] id)))
  (GET "/edit_news/:id" [id :as req] (h/edit-news-id (assoc-in req [:params :id] id)))

  (POST "/action_update_news" req (h/action-update-news req))
  (POST "/deletenews/:id" [id :as req] (h/action-delete-news (assoc-in req [:params :id] id)))

  ;; 404 fallback
  (route/not-found (h/not-found-handler {})))
