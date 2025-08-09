(ns clojurenew.core
  (:require [org.httpkit.server :refer [run-server]]
            [clojure.java.io :as io]

            [clj-time.core :as t]
            [clojure.java.jdbc :refer :all]
            [cheshire.core :as json]
            ))
(ns ring.core
  (:use ring.adapter.jetty))

  (use 'ring.util.codec)
  (use 'clojure.walk)
  (require '[clojure.string :as str])
(use 'ring.middleware.resource
     'ring.middleware.content-type
     'ring.middleware.not-modified)




(def testdata
  { :url "http://example.com",
   :title "SQLite Example",
   :body "Example using SQLite with Clojure"
   })
(defn helo-wold-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "renvoie toujours Hello World"})
(defn ringhandler [request] (ring.util.response/response "Hello"))
(defn secondringhandler [request codehtml] (ring.util.response/response codehtml))
(defn wrap-content-type [handler content-type] ;middleware
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] content-type))))

 handler1 [request] (ring.util.response/bad-request "Hello"))

(def upload-app-handler
  (-> your-handler
      wrap-params
      (wrap-multipart-params {:store (ring.middleware.multipart-params.byte-array/byte-array-store)})
  )) ; file upload available in params key

(defn handlerone [request] (ring.util.response/created "/post/123")) ; create a response to route that is empty ""
(defn handlertwo [request] (ring.util.response/redirect "https://ring-clojure.github.io/ring/")) ; redirct answer 301
(defn handlertworedirect [request myurl] (ring.util.response/redirect myurl)) ; redirct answer 301l
(def your-app-handler ;to serve static 
  (-> ringhandler
      (wrap-resource "resources")
      wrap-content-type
      wrap-not-modified)
(def parse-params-app-handler ;to parse params in your app of th url
  (-> ringhandler ;ringhandler is your handler
      (wrap-params {:encoding "UTF-8"})
  ))
(defn echo-handler [{params :params}]
    (ring.util.response/content-type
        (ring.util.response/response (get params "input"))
        "text/plain")) ;response of a value fo a param in a url string 
(ring.util.response/set-cookie 
    (ring.util.response/response "Setting a cookie.") 
    "session_id" 
    "session-id-hash")
(def wrap-cookies-app-handler
  (-> ringhandler
      wrap-cookies
  ));wrap cookie in cookie string in answr
(def store-session-app-handler
  (-> your-handler
      wrap-session
  ))
(def store-your-sesionapp-handler
  (-> your-handler
      wrap-cookies
      (wrap-session {:store (cookie-store {:key "a 16-byte secret"})})
      (wrap-session {:cookie-attrs {:max-age 3600}})

  ))
(defn compte-requests-handler [{session :session}];compte requete
  (let [count   (:count session 0)
        session (assoc session :count (inc count))]
    (-> (response (str "You accessed this page " count " times."))
        (assoc :session session))))

(defn vide-session-handler [request]
  (-> (response "Session deleted.")
      (assoc :session nil)));empty session

(def receiving-file-app-handler ; apphandler able to upoad fils
  (-> ringhandler
      wrap-params
      wrap-multipart-params
  ))





(defn renderfigure [title body]
  #_(println-str "<html><head><title>" title "</title></head><body>" body "</body></html>" "")
  (format (slurp (io/resource "index.html")) title body))

(defn lirefichier [hey]
  (slurp (io/resource hey)))

(defn renderhtml [title hey]
    (secondringhandler (format (slurp (io/resource "index.html")) title (slurp (io/resource hey)))))
(defn renderjs1 [title hey]
    (wrap-content-type (secondringhandler (slurp (io/resource hey))) "text/javascript" ))
(defn rendercss1 [title hey]
    (wrap-content-type (secondringhandler (slurp (io/resource hey))) "text/css" ))


;; create db
(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn output
  "execute query and return lazy sequence"
  []
  (query db ["select * from news"]))
(defn getbyid
  "execute query and return lazy sequence"
  [myid]
  (query db ["select * from news where id = ?" myid]))

(defn voirnews [title template req myid]
  (println "action voirnew")
  (println req)
  (def mytemplate (slurp (io/resource template)))
  (def figure (map #(format mytemplate (get % :title "") (get % :body "") (get % :id "") (get % :id "")) (getbyid myid)))
  (def body (format (slurp (io/resource "news.html")) (str/join "" figure)))
  (def title "hey")
  (def hey (if (zero? (count (output))) "<p>il n'y a pas de news à afficher</p>" body))
  (def reponsebody (format (slurp (io/resource "index.html")) title hey))
  (secondringhandler req reponsebody))
(defn editnews [title template req myid]
  (println "action voirnews")
  (def mytemplate (slurp (io/resource template)))
  (println "action EDIT bain")
  (println (getbyid myid))
  (def figure (map #(format mytemplate (get % :id "") (get % :title "") (get % :body "") (get % :url "") (get % :id "")) (getbyid myid)))
  (def body (format (slurp (io/resource "news.html")) (str/join "" figure)))
  (def title "hey")
  (def hey (if (zero? (count (output))) "<p>il n'y a pas de news à afficher</p>" body))
  (def reponsebody (format (slurp (io/resource "index.html")) title hey))
  (secondringhandler req reponsebody))

(defn rendercollection [title bdd template req]
  (println "action create")
  (def mytemplate (slurp (io/resource template)))
  (def figure (map #(format mytemplate   (get % :nom "") (get % :prenom "") (get % :date "") (get % :email "") (get % :numero "") (if (= (get % :journee "") "1") "journée" "demi-journée") (get % :content "") (get % :date "") (get % :id "")) (output)))
  (def body (format (slurp (io/resource "reservations.html")) (str/join "" figure)))
  (def body1 (format (slurp (io/resource "reservations1.html")) "<p>il n'y a pas de réservations à afficher</p>"))
  (def title "hey")
  (def hey (if (zero? (count (output))) body1 body))
  (def reponsebody (format (slurp (io/resource "index.html")) title hey))
  (secondringhandler req reponsebody))

(defn actiondeletenews [title req]
  (println "action delete")
  (def heyreader (io/reader (:body req) :encoding "UTF-8"))
  (println heyreader)
  (println "hey HEY action create")
  (println "hHo")
  (def a (slurp heyreader) )
  (println a)

  (def myhash (keywordize-keys (form-decode a)))
  (println "hey HEY he")
  (println myhash)
  (def id (get myhash :myid ""))
  (execute! db
  ["delete from news where id = ?"
    id])
  (println "OHOHOH")
  ;(handlertworedirect req "/voir_reservations"))
  {:status 301
    :body (slurp (io/resource "redirect.html"))
    :contenttype "text/html"
    :redirect "/voir_news"
    })
(defn actionupdatenews [title hey req]
  (println "action create")
  (def heyreader (io/reader (:body req) :encoding "UTF-8"))
  (println heyreader)
  (println "hey HEY action create")
  (def a (slurp heyreader) )
  (println a)
  (def myhash (keywordize-keys (form-decode a)))
  (println "hey HEY he")
  (println myhash)
  (def mytitle (get myhash :title "hye"))
  (def url (get myhash :url "url"))
  (def body (str/join "" (get myhash :body ["ur" "body"]) ))
  (def id (get myhash :id ""))
  (execute! db
  ["update news set title = ?, image = ?, body = ? where id = ?"
   mytitle url body id])
  (println "OHOHOH")
  {:status 301
    :body (slurp (io/resource "redirect.html"))
    :contenttype "text/html"
    :redirect "/voir_news"
    })

(defn actioncreatenews [title hey req]
  (println "action create")
  (def heyreader (io/reader (:body req) :encoding "UTF-8"))
  (println heyreader)
  (println "hey HEY action create")
  (println "hHo")
  (def a (slurp heyreader) )
  (println a)

  (def myhash (keywordize-keys (form-decode a)))
  (println "hey HEY he")
  (println myhash)
  (def mytestdata {:image (get myhash :nom "hye")
                   :title (get myhash :prenom "url")
                   :content (get myhash :journee "url") })
  (insert! db :news mytestdata)
  (println "OHOHOH")
  {:status 301
    :body (slurp (io/resource "redirect.html"))
    :contenttype "text/html"
    :redirect "/voir_news"
    })

(defn create-db
  "create db and table"
  []
  (try (db-do-commands db
                       (create-table-ddl :news
                                         [[:id "integer primary key autoincrement" ]
                                          [:timestamp :datetime :default :current_timestamp ]
                                          [:title :text]
                                          [:image :text]
                                          [:content :text]]))
       (catch Exception e
         (println (.getMessage e)))))

(defn print-result-set
  "prints the result set in tabular form"
  [result-set]
  (doseq [row result-set]
    (println row)))


(defn output
  "execute query and return lazy sequence"
  []
  (query db ["select * from news"]))





(defn app [req]
  (println "bdd")
  (println (output))
  (println "my application")
  #(println req)
  (println (get req :uri "aucune adresse url"))
  (def uri (get req :uri "aucune adresse url"))
  (def hey (let [mystr uri]
    (cond
          (re-find #"^/$" uri) "welcome.html"
          (re-find #"^/$" uri) "welcome.html"
          (re-find #"^/hello$" uri) "hello.html"
          (re-find #"^/create_news$" uri) "uhiuh"
          (re-find #"^/action_create_news$" uri) "lk"
          (re-find #"^/voir_bains$" uri) "lilu"
          (re-find #"^/voir_bain/\d+$" uri) "i"
          (re-find #"^/edit_bain/\d+$" uri) "i"
          :else "404.html")))
  (println hey)

  (defn awesome-handler [request] (ring.util.response/response "Hello"))


  (def html (let [mystr uri]
    (cond
          (re-find #"^/$" uri) (renderhtml "Show my activity world" "welcome.html")
          (re-find #"^/app.css$" uri) (rendercss1 "Show my activity world" "app.css")
          (re-find #"^/app.js$" uri) (rendercss1 "Show my activity world" "app.js")
          (re-find #"^/hello$" uri) (renderhtml "Show my activity world" "hello.html")
          (re-find #"^/poster_news$" uri) (renderhtml "poster des news" "form.html")
          (re-find #"^/action_create_news$" uri) (actioncreatenews "hello" "form.html" req)
          (re-find #"^/action_update_news$" uri) (actionupdatenews "hello" "formedit.html" req)
          (re-find #"^/deletenews" uri) (actiondeletenews "hello"  req)
          (re-find #"^/voir_news$" uri) (rendercollection "hello" output "_reservation.html" req)
          (re-find #"^/voir_news/\d+$" uri) (voirnews "hello" "voirnews.html" req (get (re-find #"^/voir_news/(\d+)$" uri) 1))
          (re-find #"^/edit_news/\d+$" uri) (editnews "hello" "formedit.html" req (get (re-find #"^/edit_news/(\d+)$" uri) 1))
          ;(re-find #"^/poster_video$" uri) (renderhtml "poster des video" "form.html")
          ;(re-find #"^/action_create_video$" uri) (actioncreate "hello" "form.html" req)
          ;(re-find #"^/action_update_video$" uri) (actionupdate "hello" "formedit.html" req)
          ;(re-find #"^/deletevideo" uri) (actiondelete "hello"  req)
          ;(re-find #"^/voir_video$" uri) (rendercollection "hello" output "_reservation.html" req)
          ;(re-find #"^/voir_video/\d+$" uri) (voirvideo "hello" "voirvideo.html" req (get (re-find #"^/voir_video/(\d+)$" uri) 1))
          ;(re-find #"^/edit_video/\d+$" uri) (editvideo "hello" "formedit.html" req (get (re-find #"^/edit_video/(\d+)$" uri) 1))
          ;(re-find #"^/poster_photo$" uri) (renderhtml "poster des photo" "form.html")
          ;(re-find #"^/action_create_photo$" uri) (actioncreate "hello" "form.html" req)
          ;(re-find #"^/action_update_photo$" uri) (actionupdate "hello" "formedit.html" req)
          ;(re-find #"^/deletephoto" uri) (actiondelete "hello"  req)
          ;(re-find #"^/voir_photo$" uri) (rendercollection "hello" output "_reservation.html" req)
          ;(re-find #"^/voir_photo/\d+$" uri) (voirphoto "hello" "voirphoto.html" req (get (re-find #"^/voir_photo/(\d+)$" uri) 1))
          ;(re-find #"^/edit_photo/\d+$" uri) (editphoto "hello" "formedit.html" req (get (re-find #"^/edit_photo/(\d+)$" uri) 1))
          :else (renderhtml "Erreur" "404.html"))))
  (def status (get html :status 200))
  (def content (get html :contenttype "text/html"))
  (def body (get html :body "<h1>erreur 404 desole</h1>"))
  (def redirect (get html :redirect ""))
  {:status  status
   :headers {"Content-Type" content
             "Location" redirect}
   :body    body})


;(defn -main [& args]
;  (create-db)
;  (run-server app {:port 8080})
;  (println "Server started on port 8080"))
(defn check-ip-handler [request]
    (ring.util.response/content-type
        (ring.util.response/response (:remote-addr request))
        "text/plain"))

(def app-handler (wrap-content-type handler "text/html"))
(def other-app-handler
  (-> handler
      (wrap-content-type "text/html")
      wrap-keyword-params
      wrap-params))

(defn your-handler [request] (ring.util.response/response "Your handler Hello"))
(def app-your-handler (wrap-resource your-handler "public"))

(defn echo-handler [{params :params}]
    (ring.util.response/content-type
        (ring.util.response/response (get params "input"))
        "text/plain"))
(def my-new-app-handler ; original handler (your handler or ring handler) wrapped in 3 middleware function
  (-> your-handler
      (wrap-content-type "text/html")
      wrap-keyword-params
      wrap-params))
(def any-app-handler (wrap-resource your-handler "resources")) ;inresurces in here

(def file-upload-app-handler
  (-> your-handler
      wrap-params
      (wrap-multipart-params {:store (ring.middleware.multipart-params.byte-array/byte-array-store)})
  ))

(defn -main
  [& args]
  (create-db)
  (run-jetty ringhandler {:port 8080})
  (println "Server started on port 8080"))


(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
