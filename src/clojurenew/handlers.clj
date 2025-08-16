(ns clojurenew.handlers
  "Ring handlers for Activity World."
  (:use ring.adapter.jetty)
  (:import [javax.imageio ImageIO])
  (:require [clojurenew.db :as db]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clojure.walk :refer [keywordize-keys]]
            [ring.util.response :as response]
            [ring.util.codec :as codec]))
(defn clear-session [request]
  {:status 302
   :headers {"Location" "/"}
   :session nil}) ;; This clears the session
(require '[ring.middleware.session.cookie :refer [cookie-store]])

;;;;;;;;;;;;;;;;;;;
;(def session-config
;  {:store (cookie-store {:key (.getBytes "1234567890abcdef")})}) ;; 16-byte key
;(import 'java.security.SecureRandom)
;
;(defn generate-key []
;  (let [bytes (byte-array 16)]
;    (.nextBytes (SecureRandom.) bytes)
;    bytes))
;
;(require '[ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
;         '[ring.middleware.session :refer [wrap-session]])
;
;(def app
;  (-> handler
;      wrap-anti-forgery
;      (wrap-session session-config)))
;
;(require '[ring.util.anti-forgery :refer [anti-forgery-field]])
;
;(defn form-page [request]
;  [:form {:method "POST" :action "/submit"}
;   (anti-forgery-field)
;   [:input {:type "text" :name "data"}]
;   [:input {:type "submit"}]])
;
;;<input type="hidden" name="__anti-forgery-token" value="...">

;;;;;;;;;;;



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
  ;(cookie-store {:key (.getBytes "1234567890abcdef")})
  (response/content-type
    (response/response (render-html "form.html" "he" "hi"))
    "text/html"))

(defn action-create-news [req]
  ;(cookie-store {:key (.getBytes "1234567890abcdef")})
  (let [params (if (:form-params req) (:form-params req) (:params req))]
    (def photo ((params :photo) :tempfile))
    (def scores {"title" (params :title), "photo" ((params :photo) :filename), "content" (params :content)})

    (db/insert-news! params)
    (-> (response/redirect "/voir_news")
        (response/status 303))))

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
