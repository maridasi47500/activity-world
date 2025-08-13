(ns clojurenew.handlers
  "Ring handlers for Activity World."
  (:require [clojurenew.db :as db]
            [clojure.java.io :as io]
            [clojure.string :as str]
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
;(defn render-html
;  "Render an HTML template from resources."
;  [template & [params]]
;  (let [tpl (slurp (io/resource template))]
;    (if params
;      (format tpl params)
;      tpl)))

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

(defn mes-mots [template debut-mot-string fin-mot-html]
    (render-html (str template) (str debut-mot-string) (str (my-html fin-mot-html))))

(defn home [_]
  (response/content-type
    (response/response (mes-mots "index.html" "title" "welcome.html"))
    "text/html"))

(defn poster-news [_]
  (response/content-type
    (response/response (render-html "form.html" "he" "hi"))
    "text/html"))

(defn voir-news [_]
  (response/content-type
    (response/response
      (render-collection
        "Actualités"
        (db/get-news)
        (slurp (io/resource "_reservation.html"))))
    "text/html"))

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

(defn action-create-news [req]
  (let [params (if (:form-params req) (:form-params req) (:params req))]
    (db/insert-news! params)
    (-> (response/redirect "/voir_news")
        (response/status 303))))

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
