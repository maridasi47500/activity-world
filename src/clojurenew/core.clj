(ns clojurenew.core
  (:require [org.httpkit.server :refer [run-server]]
            [clojure.java.io :as io]

            [clj-time.core :as t]
            [clojure.java.jdbc :refer :all]
            [cheshire.core :as json]
            ))
  (use 'ring.util.codec)
  (use 'clojure.walk)
  (require '[clojure.string :as str])

(def testdata
  { :url "http://example.com",
   :title "SQLite Example",
   :body "Example using SQLite with Clojure"
   })



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
  (query db ["select * from bath"]))
(defn getbyid
  "execute query and return lazy sequence"
  [myid]
  (query db ["select * from bath where id = ?" myid]))

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
  (def figure (map #(format mytemplate  (get % :title "") (get % :body "") (get % :id "") (get % :url "") (get % :id "") (get % :id "")) (output)))
  (def body (format (slurp (io/resource "bains.html")) (str/join "" figure)))
  (def title "hey")
  (def hey (if (zero? (count (output))) "<p>il n'y a pas de bains à afficher</p>" body))
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
  (def id (get myhash :id ""))
  (execute! db
  ["delete from bath where id = ?"
    id])
  (println "OHOHOH")
  {:status 301
    :body (slurp (io/resource "redirect.html"))
    :contenttype "text/html"
    :redirect "/voir_bains"
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
  ["update bath set title = ?, url = ?, body = ? where id = ?"
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
  (def mytestdata {:title (get myhash :title "hye")
                   :url (get myhash :url "url")
                   :body (str/join "" (get myhash :body ["ur" "body"])) })
  (insert! db :bath mytestdata)
  (println "OHOHOH")
  {:status 301
    :body (slurp (io/resource "redirect.html"))
    :contenttype "text/html"
    :redirect "/voir_bains"
    })

(defn create-db
  "create db and table"
  []
  (try (db-do-commands db
                       (create-table-ddl :bath
                                         [[:id "integer primary key autoincrement" ]
                                          [:timestamp :datetime :default :current_timestamp ]
                                          [:url :text]
                                          [:title :text]
                                          [:body :text]]))
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
  (query db ["select * from bath"]))





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
          (re-find #"^/create_bath$" uri) "uhiuh"
          (re-find #"^/action_create_bath$" uri) "lk"
          (re-find #"^/voir_bains$" uri) "lilu"
          (re-find #"^/voir_bain/\d+$" uri) "i"
          (re-find #"^/edit_bain/\d+$" uri) "i"
          :else "404.html")))
  (println hey)
  (def html (let [mystr uri]
    (cond
          (re-find #"^/$" uri) (renderhtml "Camp" "welcome.html")
          (re-find #"^/app.css$" uri) (rendercss1 "Camp" "app.css")
          (re-find #"^/hello$" uri) (renderhtml "Camp" "hello.html")
          (re-find #"^/reserver$" uri) (renderhtml "Reservation" "form.html")
          (re-find #"^/action_create_bath$" uri) (actioncreate "hello" "form.html" req)
          (re-find #"^/action_update_bath$" uri) (actionupdate "hello" "formedit.html" req)
          (re-find #"^/action_delete_bath$" uri) (actiondelete "hello"  req)
          (re-find #"^/voir_bains$" uri) (rendercollection "hello" output "_bain.html" req)
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


(defn -main [& args]
  (create-db)
  (run-server app {:port 8080})
  (println "Server started on port 8080"))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
