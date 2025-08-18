(ns clojurenew.routes
  "Compojure routes for Activity World (routes unchanged for view compatibility)."
  (:require [compojure.core :refer :all]
            [clojurenew.handlers :as h]
            [compojure.route :as route]))

(defroutes app-protected-routes
  ;(GET "/poster_news" [] h/poster-news)
  (POST "/action_create_news" req (h/action-create-news req)))

(defroutes app-routes
  (GET "/clear-session" [] h/clear-session)
  (GET "/poster_news" req (h/poster-news req))
  (POST "/action_create_news" [title photo content] (h/action-create-news title photo content))

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
