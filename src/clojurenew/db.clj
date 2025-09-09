(ns clojurenew.db
  "Database functions for Activity World."
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "db/database.db"})

(defn ensure-db!
  []
  ;; Table news existante…
  ;; Ajoute ces commandes :
  (try
    (jdbc/db-do-commands
      db-spec
      (jdbc/create-table-ddl :video
        [[:id "integer primary key autoincrement"]
         [:timestamp :datetime :default :current_timestamp]
         [:myvideo :text]
         [:title :text]
         [:content :text]]))
    (catch Exception e
      (println "Video table: " (.getMessage e))))
  (try
    (jdbc/db-do-commands
      db-spec
      (jdbc/create-table-ddl :album_photo
        [[:id "integer primary key autoincrement"]
         [:timestamp :datetime :default :current_timestamp]
         [:title :text]
         [:subtitle :text]]))
    (catch Exception e
      (println "Album table: " (.getMessage e))))
  (try
    (jdbc/db-do-commands
      db-spec
      (jdbc/create-table-ddl :photo
        [[:id "integer primary key autoincrement"]
         [:album_id :integer]
         [:timestamp :datetime :default :current_timestamp]
         [:myphoto :text]]))
    (catch Exception e
      (println "Photo table: " (.getMessage e))))
  (try
    (println "trying to create the news db")
    (jdbc/db-do-commands
      db-spec
      (jdbc/create-table-ddl :news
        [[:id "integer primary key autoincrement"]
         [:timestamp :datetime :default :current_timestamp]
         [:title :text]
         [:content :text]
         [:image :text]
         ]))
    (catch Exception e
      (println "DB already exists or error:" (.getMessage e)))))

(defn get-news []
  (jdbc/query db-spec ["select * from news ORDER BY timestamp DESC"]))

(defn get-news-by-id [id]
  (first (jdbc/query db-spec ["select * from news where id = ?" id])))

(defn insert-news! [{:keys [title content image]}]
  (println "Inserting news:" title content image)

  (jdbc/insert! db-spec :news
                {:title title :content content :image image}))

(defn update-news! [{:keys [id title content image]}]
  (jdbc/execute! db-spec
    ["update news set title = ?, content = ?, image = ? where id = ?"
     title content image id]))
(defn update-video! [{:keys [id title content myvideo]}]
  (jdbc/execute! db-spec
    ["update video set title = ?, content = ?, myvideo = ? where id = ?"
     title content myvideo id]))

(defn delete-news! [id]
  (jdbc/delete! db-spec :news ["id=?" id]))
;; VIDEO
(defn insert-video! [params]
  (jdbc/insert! db-spec :video params))

(defn get-videos []
  (jdbc/query db-spec ["select * from video ORDER BY timestamp DESC"]))

(defn get-video-by-id [id]
  (first (jdbc/query db-spec ["select * from video where id = ?" id])))


(defn delete-video! [id]
  (jdbc/delete! db-spec :video ["id=?" id]))

;; ALBUM_PHOTO
(defn insert-album! [params]
  (let [result (jdbc/insert! db-spec :album_photo params)
        album (first result)
        album-id (get album (keyword "last_insert_rowid()"))]
    (println "Raw insert result:" album)
    (println "Extracted album ID:" album-id)
    (println "Album keys:" (keys album))
    {:id album-id :title (:title params) :subtitle (:subtitle params)}))


(defn get-albums []
  (jdbc/query db-spec ["select * from album_photo ORDER BY timestamp DESC"]))

(defn get-album-by-id [id]
  (first (jdbc/query db-spec ["select * from album_photo where id = ?" id])))

(defn update-album! [params]
  (jdbc/update! db-spec :album_photo params ["id=?" (:id params)]))

(defn delete-album! [id]
  (jdbc/delete! db-spec :album_photo ["id=?" id]))

;; PHOTO
(defn insert-photo! [params]
  (jdbc/insert! db-spec :photo params))

(defn get-photos-by-album [album_id]
  (jdbc/query db-spec ["select * from photo where album_id = ?" album_id]))

(defn get-photo-by-id [id]
  (first (jdbc/query db-spec ["select * from photo where id = ?" id])))

(defn delete-photo! [id]
  (jdbc/delete! db-spec :photo ["id=?" id]))
