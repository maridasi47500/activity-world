(ns clojurenew.handlers
  "Ring handlers for Activity World."
  (:gen-class)
  (:use ring.adapter.jetty)
  (:import [javax.imageio ImageIO])
  (:require [hiccup.page :as hic-p]
            [hiccup.element :as hic-e]
            [hiccup.def :as hic-d]
            [hiccup2.core :as hic-c]
            [hiccup.form :as hf]
            [cheshire.core :as wowjson]
            [clojurenew.db :as db]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clojure.walk :refer [keywordize-keys]]
            [ring.util.response :as response]
            [ring.util.codec :as codec]))
(defn handle-news [request]
  (let [params (:multipart-params request)
        title (get params "news[title]")
        content (get params "news[content]")
        photo (get params "news[photo]")
        filename (:filename photo)
        tempfile (:tempfile photo)
        target-path (str "resources/public/uploads/" filename)]
    (if (and title content photo tempfile filename)
      (do
       (println "yessssss")
       (println "yessssss")
      )
      (println "nooooo")
    )
   
        

    (println "Title:" title)
    (println "Content:" content)
    (println "Filename:" filename)
        (clojure.java.io/make-parents target-path)
        ;; Copie le fichier
        (clojure.java.io/copy tempfile (clojure.java.io/file target-path))

    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str "<h2>News Received</h2>"
                "<p>Title: " title "</p>"
                "<p>Content: " content "</p>"
                "<p>File: " filename "</p>")}))


(defn my-debug-handler [req]
  (println "Params:" (:params req))
  (println "Form params:" (:form-params req))
  (println "Multipart params:" (:multipart-params req))
  (println "Raw body class:" (class (:body req)))
  (response/response (wowjson/generate-string {:status "ok"})))

(defn render-html
  "Render an HTML template from resources."
  [template title content]
  (let [tpl (slurp (io/resource template))]
    (if title
    (if content
      (format tpl title content)
      ))))
(defn render-json
  "Render an json template from resources."
  [template]
  (println template)
  (let [tpl (slurp (io/resource template))]
      (println tpl)
      tpl)
      )

(defn my-html
  "Render an HTML template from resources."
  [template & [params]]
  (slurp (io/resource template)))
;(defn replace-several [s & {:as replacements}]
;  (reduce (fn [s [match replacement]]
;            (clojure.string/replace s match replacement))
;          s replacements))
(defn replace-several [s & {:as replacements}]
  (reduce (fn [s [match replacement]]
            (if (and match replacement)
              (clojure.string/replace s match replacement)
              s)) ; skip if either is nil
          s replacements))


(defn render-collection-params
  "Render a collection of news into a template with name of params in view."
  [title coll template]
  (println "coll:" coll)
  (println "title:" title)
  (println "template:" template)
  (let [body (if (seq coll)
               (str/join "" (map #(replace-several template
                 "$title" (:title %)
                 "$image" (:image %)
                 "$content" (:content %)) coll))
               "<p>Il n'y a rien à afficher.</p>")
        page (slurp (io/resource "index.html"))]
    (println "body:" body)
    (format page title body)))
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


(defn my-form-news-page
  [_]
  ;(;hic-p/html5
    ;(gen-page-head "Json Parser Home.")
    [:h1 "Welcome."]
    [:p "Json Web App."]
    [:div
         (anti-forgery-field)
      ]
     (hic-e/link-to "/action_create_news" "accueil world activity")
    [:p (hf/form-to {:enctype "multipart/form-data"} [:post "/action_create_news"]
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

         (hf/submit-button "Submit"))]);)
(defn form-news-page
  [req]
    (str/replace (hic-p/html5 





(hf/form-to {:id "form-create-news", :enctype "multipart/form-data"} [:post "/action_create_news"]

[:div (hf/form-to {:id "form-create-news"} [:post "/action_create_news"]
    [:div
         (anti-forgery-field)
      ]


    [:div
         (hf/label "news[title]" "title")    
         (hf/text-field "news[title]")    
      ]
    [:div
         (hf/label "news[photo]" "photo")    
         (hf/file-upload "news[photo]")    
      ]
    [:div
         (hf/label "news[content]" "content")    
         (hf/text-area "news[content]")    
      ]
         (hf/submit-button "Submit"))])) #"(<html>|<\/html>)" "")
                 )



(defn poster-news [request]
  (println (str request))
  (println (:anti-forgery-token request))
  (response/content-type
    (response/response (render-html "index.html" "ajouter une news" (form-news-page request)))
    "text/html"))




(defn mes-mots [template debut-mot-string fin-mot-html]
    (render-html (str template) (str debut-mot-string) (str (my-html fin-mot-html))))

(defn home [_]
  (response/content-type
    (response/response (mes-mots "index.html" "title" "welcome.html"))
    "text/html"))

(defn action-create-news [request]
  (println "request" (str request))
  (println "Headers:" (:headers request))
  (println "Multipart params:" (:multipart-params request))
  (println "Form params:" (:form-params request))
  (println "Params:" (:params request))
  (println "yeah")
  (let [params (:multipart-params request)
        title (get params "news[title]")
        content (get params "news[content]")
        photo (get params "news[photo]")
        filename (:filename photo)
        tempfile (:tempfile photo)
        target-path (str "resources/public/uploads/" filename)]
    (if (and title content photo tempfile filename)
      (do
        (println "yeeeeeeees wow")
        ;; Crée les dossiers si besoin
        (clojure.java.io/make-parents target-path)
        ;; Copie le fichier
        (clojure.java.io/copy tempfile (clojure.java.io/file target-path))
        ;; Enregistre en base
        (println "news to insert" title filename content)
        (db/insert-news! {:title title
                          :image filename
                          :content content})
        ;; Retourne une réponse JSON
        (response/content-type
         (response/response (render-json "index.json"))
         "application/json"))
      ;; Champs manquants
      (response/content-type
       (response/response (render-json "myform.json"))
       "application/json"))))




(defn voir-news [req]
  (println "voir news handler reached")
  (response/content-type
    (response/response
      (render-collection-params
        "Actualités"
        (db/get-news)
        (slurp (io/resource "_news.html"))))
    "text/html"))
(defn render-pic [hey]
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
  (-> (response/response (str (render-html "404.html" "hey" "hi") _))
      (response/status 404)
      (response/content-type "text/html")))
