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
  ;; VIDEO
  (GET "/render_videos" req (h/voir-videos req))
  (GET "/poster_video" [req] (h/poster-video req))
  ;   (println "Handler reached! Params: " req) 
  (POST "/action_create_video" req 
     (println "Handler reached! Params: " req)
     (h/action-create-video req))
  (GET "/video/:id" [id :as req] (h/voir-video-id (assoc-in req [:params :id] id)))
  (GET "/edit_video/:id" [id :as req] (h/edit-video-id (assoc-in req [:params :id] id)))
  (POST "/action_update_album" req (h/action-update-album req))
  (POST "/action_update_video" req (h/action-update-video req))
  (POST "/delete_video/:id" [id :as req] (h/action-delete-video (assoc-in req [:params :id] id)))

  ;; ALBUM_PHOTO
  (GET "/render_albums" req (h/voir-albums req))
  (GET "/album/:id/photos" [id :as req] (h/voir-photos-by-album (assoc-in req [:params :id] id)))
  (GET "/poster_album" [req] (h/poster-album req))
  (POST "/action_create_album" req (h/action-create-album req))
  (POST "/delete_album/:id" [id :as req] (h/action-delete-album (assoc-in req [:params :id] id)))

  ;; PHOTO
  (GET "/album/:album_id/photos" [album_id :as req] (h/voir-photos-by-album (assoc-in req [:params :album_id] album_id)))
  (GET "/poster_photo/:album_id" [album_id :as req] (h/poster-photo req album_id))
  (POST "/action_create_photo" req (h/action-create-photo req))
  (POST "/delete_photo/:id" [id :as req] (h/action-delete-photo (assoc-in req [:params :id] id)))
  (GET "/clear-session" [] h/clear-session)
  (GET "/edit_news/:id" [id :as req] (h/edit-news-id (assoc-in req [:params :id] id)))
  (GET "/edit_album/:id" [id :as req] (h/edit-album-id (assoc-in req [:params :id] id)))
  (GET "/poster_news"  [req] (h/poster-news req))
;(mp/wrap-multipart-params 
  (POST "/action_create_news" req
     (println "Handler reached! Params: " req) 
     (h/action-create-news req))

  ;; CSS and JS (if needed, adapt as per your previous code)
  (GET "/app.css" [] (h/render-css "Show my activity world" "app.css"))
  (GET "/app.js" [] (h/render-js "Show my activity world" "app.js"))

  ;;pic 
  (GET "/:mypic.jpeg" [mypic :as req] (h/voir-photo-mypic (assoc-in req [:params :mypic] mypic)))
  (GET "/pics/:mypic" [mypic :as req] (h/voir-photo-mypic (assoc-in req [:params :mypic] mypic)))
  (GET "/uploads/:mypic" [mypic :as req] (h/voir-photo-upload (assoc-in req [:params :mypic] mypic)))

  ;; Home and hello
  (GET "/hello" [] h/home)

  (GET "/" [] h/home)
  (GET "/activites" [] h/activites)
  (POST "/addactivity" [] h/action-create-activity)
  (GET "/activity/:id" [id :as req] (h/voir-activity-id (assoc-in req [:params :id] id)))
  (GET "/activity/:id/competitions" [id :as req] (h/voir-activity-competitions-id (assoc-in req [:params :id] id)))
  (GET "/activity/:id/calendars" [id :as req] (h/voir-activity-calendars-id (assoc-in req [:params :id] id)))
  (GET "/activity/:id/results" [id :as req] (h/voir-activity-results-id (assoc-in req [:params :id] id)))
  (GET "/activity/:id/athletes" [id :as req] (h/voir-activity-athletes-id (assoc-in req [:params :id] id)))
  (GET "/activity/:id/ranking" [id :as req] (h/voir-activity-rankings-id (assoc-in req [:params :id] id)))
  (GET "/activity/:id/records" [id :as req] (h/voir-activity-records-id (assoc-in req [:params :id] id)))
  (GET "/activity/:id/rules" [id :as req] (h/voir-activity-rules-id (assoc-in req [:params :id] id)))
  (GET "/activity/:id/points" [id :as req] (h/voir-activity-points-id (assoc-in req [:params :id] id)))

  ;; News: routes preserved as in original code

  (GET "/results" req h/results)

  ;;competitions
  (GET "/competitions" [] h/competitions)
  (GET "/competitions/:id" [id :as req] (h/voir-event-id (assoc-in req [:params :id] id)))
  (POST "/createcompetitions" req (h/action-create-competition req))

  ;;live schedule
  (POST "/create_live_schedule" req (h/action-create-live-schedule req))
  ;;results
  (POST "/create_result" req (h/action-create-result req))
  ;;athletes
  (GET "/athletes" [] h/athletes)
  (POST "/create-athlete" req (h/action-create-athlete req))


  ;;news
  (GET "/render_news" req (h/voir-news req))
  (GET "/voir_news/:id" [id :as req] (h/voir-news-id (assoc-in req [:params :id] id)))
  (GET "/edit_news/:id" [id :as req] (h/edit-news-id (assoc-in req [:params :id] id)))

  (POST "/action_update_news" req (h/action-update-news req))
  (POST "/delete_news/:id" [id :as req] (h/action-delete-news (assoc-in req [:params :id] id)))

  ;; 404 fallback
  (route/not-found (h/not-found-handler {})))
