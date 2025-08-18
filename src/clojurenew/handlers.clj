(ns clojurenew.handlers
  "Ring handlers for Activity World."
  (:gen-class)
  (:use ring.adapter.jetty)
  (:import [javax.imageio ImageIO])
  (:require [hiccup.page :as hic-p]
            [hiccup.element :as hic-e]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.form :as hf]
            [clojurenew.db :as db]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clojure.walk :refer [keywordize-keys]]
            [ring.util.response :as response]
            [ring.util.codec :as codec]))
(defn render-html
  "Render an HTML template from resources."
  [template title content]
  (let [tpl (slurp (io/resource template))]
    (if title
    (if content
      (format tpl title content)
      ))))

(defn my-html
  "Render an HTML template from resources."
  [template & [params]]
  (slurp (io/resource template)))

(defn render-collection
  "Render a collection of news into a template."
  [title coll template]
  (let [body (if (seq coll)
               (str/join "" (map #(format template (:id %) (:title %) (:content %) (:url %) (:id %)) coll))
               "<p>Il n'y a rien à afficher.</p>")
        page (slurp (io/resource "index.html"))]
    (format page title body)))

(defn render-css [title file]
  (-> (response/response (slurp (io/resource file)))
      (response/content-type "text/css")))

(defn render-js [title file]
  (-> (response/response (slurp (io/resource file)))
      (response/content-type "text/javascript")))
(defn clear-session [request]
  {:status 302
   :headers {"Location" "/"}
   :session nil}) ;; This clears the session

(defn gen-page-head
  [title]
  [:head
   [:title title]])


(defn form-news-page
  [req]
  (hic-p/html5
    (gen-page-head "Json Parser Home.")
    [:h1 "Welcome."]
    [:p "Json Web App."]
     (hic-e/link-to "/action_create_news" "accueil world activity")
    [:p (hf/form-to [:post "/action_create_news"]
    [:div
         (hf/label "title" "title")    
         (hf/text-field "title")    
      ]
    [:div
         (hf/label "photo" "photo")    
         (hf/file-upload "photo")    
      ]
    [:div
         (hf/label "content" "content")    
         (hf/text-area "content")    
      ]
         (anti-forgery-field)
         (hf/submit-button "Submit"))]))



(defn poster-news [request]
  (response/content-type
    (response/response (render-html "index.html" "ajouter une news" (form-news-page request)))
    "text/html"))




(defn mes-mots [template debut-mot-string fin-mot-html]
    (render-html (str template) (str debut-mot-string) (str (my-html fin-mot-html))))

(defn home [_]
  (response/content-type
    (response/response (mes-mots "index.html" "title" "welcome.html"))
    "text/html"))



(defn action-create-news [title photo content]
  (if ( and (some? title) (some? photo) (some? content))
;1 if paramsj
     (
    (def myphoto (photo :tempfile))
    (def scores {"title" title, "photo" (photo :filename), "content" content})

    (db/insert-news! scores)
    (-> (response/redirect "/voir_news")
        (response/status 303))

)
;2
   (response/content-type
    (response/response (render-html "index.html" "ajouter une news" (form-news-page request)))
    "text/html") 
)
  
)

(defn voir-news [_]
  (response/content-type
    (response/response
      (render-collection
        "Actualités"
        (db/get-news)
        (slurp (io/resource "_reservation.html"))))
    "text/html"))
(defn render-pic [hey]
    ;    (slurp (io/resource hey)))
    (let [image (ImageIO/read (io/resource hey))
      image-file (java.io.File. (io/resource hey))]
  (ImageIO/write image "jpg" image-file)))



(defn voir-photo-mypic [req]
  (let [somepic ((req :params) :mypic)]
    (if somepic
      (response/content-type
        (response/response (render-pic somepic))
        "image/jpeg")
      (response/not-found "image not found"))))

(defn voir-news-id [req]
  (let [id (get-in req [:params :id])
        news (db/get-news-by-id id)]
    (if news
      (response/content-type
        (response/response (render-html "voirnews.html" "hey" "wow" (:title news) (:content news) (:url news)))
        "text/html")
      (response/not-found "News not found"))))

(defn edit-news-id [req]
  (let [id (get-in req [:params :id])
        news (db/get-news-by-id id)]
    (if news
      (response/content-type
        (response/response (render-html "formedit.html" "hey" "wow" (:title news) (:content news) (:url news)))
        "text/html")
      (response/not-found "News not found"))))


(defn action-update-news [req]
  (let [params (if (:form-params req) (:form-params req) (:params req))]
    (db/update-news! params)
    (-> (response/redirect "/voir_news")
        (response/status 303))))

(defn action-delete-news [req]
  (let [id (get-in req [:params :id])]
    (db/delete-news! id)
    (-> (response/redirect "/voir_news")
        (response/status 303))))

(defn not-found-handler [_]
  (-> (response/response (render-html "404.html" "hey" "hi"))
      (response/status 404)
      (response/content-type "text/html")))
