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
(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "renvoie toujours Hello World"})
(defn ringhandler [request] (ring.util.response/response "Hello"))

 handler1 [request] (ring.util.response/bad-request "Hello"))

(def app-handler
  (-> your-handler
      wrap-params
      (wrap-multipart-params {:store (ring.middleware.multipart-params.byte-array/byte-array-store)})
  )) ; file upload available in params key

(defn handler1 [request] (ring.util.response/created "/post/123"))
(defn handler1 [request] (ring.util.response/redirect "https://ring-clojure.github.io/ring/"))
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
  {:status 200
    :body (format (slurp (io/resource "index.html")) title (slurp (io/resource hey)))
    :contenttype "text/html"
    })
(defn renderjs1 [title hey]
  {:status 200
    :body (slurp (io/resource hey))
    :contenttype "text/javascript"
    })
(defn rendercss1 [title hey]
  {:status 200
    :body (slurp (io/resource hey))
    :contenttype "text/css"
    })


;; create db
(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn output
  "execute query and return lazy sequence"
  []
  (query db ["select * from booking"]))
(defn getbyid
  "execute query and return lazy sequence"
  [myid]
  (query db ["select * from booking where id = ?" myid]))

(defn voirbain [title template req myid]
  (println "action voirbain")
  (println req)
  (def mytemplate (slurp (io/resource template)))
  (def figure (map #(format mytemplate (get % :title "") (get % :body "") (get % :id "") (get % :id "")) (getbyid myid)))
  (def body (format (slurp (io/resource "bains.html")) (str/join "" figure)))
  (def title "hey")
  (def hey (if (zero? (count (output))) "<p>il n'y a pas de bain à afficher</p>" body))
  (def reponsebody (format (slurp (io/resource "index.html")) title hey))
  {:status 200
    :body reponsebody
    :contenttype "text/html"
    :redirect ""
    })
(defn editbain [title template req myid]
  (println "action voirbain")
  (def mytemplate (slurp (io/resource template)))
  (println "action EDIT bain")
  (println (getbyid myid))
  (def figure (map #(format mytemplate (get % :id "") (get % :title "") (get % :body "") (get % :url "") (get % :id "")) (getbyid myid)))
  (def body (format (slurp (io/resource "bains.html")) (str/join "" figure)))
  (def title "hey")
  (def hey (if (zero? (count (output))) "<p>il n'y a pas de bain à afficher</p>" body))
  (def reponsebody (format (slurp (io/resource "index.html")) title hey))
  {:status 200
    :body reponsebody
    :contenttype "text/html"
    :redirect ""
    })
(defn rendercollection [title bdd template req]
  (println "action create")
  (def mytemplate (slurp (io/resource template)))
  (def figure (map #(format mytemplate   (get % :nom "") (get % :prenom "") (get % :date "") (get % :email "") (get % :numero "") (if (= (get % :journee "") "1") "journée" "demi-journée") (get % :content "") (get % :date "") (get % :id "")) (output)))
  (def body (format (slurp (io/resource "reservations.html")) (str/join "" figure)))
  (def body1 (format (slurp (io/resource "reservations1.html")) "<p>il n'y a pas de réservations à afficher</p>"))
  (def title "hey")
  (def hey (if (zero? (count (output))) body1 body))
  (def reponsebody (format (slurp (io/resource "index.html")) title hey))
  {:status 200
    :body reponsebody
    :contenttype "text/html"
    :redirect ""
    })
(defn actiondelete [title req]
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
  ["delete from booking where id = ?"
    id])
  (println "OHOHOH")
  {:status 301
    :body (slurp (io/resource "redirect.html"))
    :contenttype "text/html"
    :redirect "/voir_reservations"
    })
(defn actionupdate [title hey req]
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
  (def mytitle (get myhash :title "hye"))
  (def url (get myhash :url "url"))
  (def body (str/join "" (get myhash :body ["ur" "body"]) ))
  (def id (get myhash :id ""))
  (execute! db
  ["update booking set title = ?, url = ?, body = ? where id = ?"
   mytitle url body id])
  (println "OHOHOH")
  {:status 301
    :body (slurp (io/resource "redirect.html"))
    :contenttype "text/html"
    :redirect "/voir_bains"
    })

(defn actioncreate [title hey req]
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
  (def mytestdata {:nom (get myhash :nom "hye")
                   :prenom (get myhash :prenom "url")
                   :date (get myhash :date "url")
                   :email (get myhash :email "url")
                   :numero (get myhash :numero "url")
                   :content (get myhash :content "url")
                   :journee (get myhash :journee "url") })
  (insert! db :booking mytestdata)
  (println "OHOHOH")
  {:status 301
    :body (slurp (io/resource "redirect.html"))
    :contenttype "text/html"
    :redirect "/voir_reservations"
    })

(defn create-db
  "create db and table"
  []
  (try (db-do-commands db
                       (create-table-ddl :booking
                                         [[:id "integer primary key autoincrement" ]
                                          [:timestamp :datetime :default :current_timestamp ]
                                          [:prenom :text]
                                          [:nom :text]
                                          [:date :date]
                                          [:numero :text]
                                          [:email :text]
                                          [:journee :text]
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
  (query db ["select * from booking"]))





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
          (re-find #"^/create_booking$" uri) "uhiuh"
          (re-find #"^/action_create_booking$" uri) "lk"
          (re-find #"^/voir_bains$" uri) "lilu"
          (re-find #"^/voir_bain/\d+$" uri) "i"
          (re-find #"^/edit_bain/\d+$" uri) "i"
          :else "404.html")))
  (println hey)

  (defn handler [request] (ring.util.response/response "Hello"))


  (def html (let [mystr uri]
    (cond
          (re-find #"^/$" uri) (renderhtml "Show my activity world" "welcome.html")
          (re-find #"^/app.css$" uri) (rendercss1 "Show my activity world" "app.css")
          (re-find #"^/app.js$" uri) (rendercss1 "Show my activity world" "app.js")
          (re-find #"^/hello$" uri) (renderhtml "Show my activity world" "hello.html")
          (re-find #"^/reserver$" uri) (renderhtml "Reservation" "form.html")
          (re-find #"^/action_create_booking$" uri) (actioncreate "hello" "form.html" req)
          (re-find #"^/action_update_booking$" uri) (actionupdate "hello" "formedit.html" req)
          (re-find #"^/deletebooking$" uri) (actiondelete "hello"  req)
          (re-find #"^/voir_reservations$" uri) (rendercollection "hello" output "_reservation.html" req)
          (re-find #"^/voir_bain/\d+$" uri) (voirbain "hello" "voirbain.html" req (get (re-find #"^/voir_bain/(\d+)$" uri) 1))
          (re-find #"^/edit_bain/\d+$" uri) (editbain "hello" "formedit.html" req (get (re-find #"^/edit_bain/(\d+)$" uri) 1))
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
(defn wrap-content-type [handler content-type] ;middleware
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] content-type))))
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

(def app-handler
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
