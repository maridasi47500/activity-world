(defn voirbain [title template req myid]
  (println "action voirbain")
  (println req)
  (def mytemplate (slurp (io/resource template)))
  (def figure (map #(format mytemplate (get % :title "") (get % :body "") (get % :id "") (get % :id "")) (getbyid myid)))
  (def body (format (slurp (io/resource "bains.html")) (str/join "" figure)))
  (def title "hey")
  (def hey (if (zero? (count (output))) "<p>il n'y a pas de bain à afficher</p>" body))
  (def reponsebody (format (slurp (io/resource "index.html")) title hey))
  (secondringhandler req reponsebody))

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
  (secondringhandler req reponsebody))


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
  ;(handlertworedirect req "/voir_reservations"))
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

