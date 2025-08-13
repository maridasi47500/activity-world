(ns clojurenew.db
  "Database functions for Activity World."
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "db/database.db"})

(defn ensure-db!
  "Create the news table if it doesn't exist. Safe to call multiple times."
  []
  (try
    (jdbc/db-do-commands
      db-spec
      (jdbc/create-table-ddl :news
        [[:id "integer primary key autoincrement"]
         [:timestamp :datetime :default :current_timestamp]
         [:title :text]
         [:image :text]
         [:content :text]]))
    (catch Exception e
      (println "DB already exists or error:" (.getMessage e)))))

(defn get-news []
  (jdbc/query db-spec ["select * from news"]))

(defn get-news-by-id [id]
  (first (jdbc/query db-spec ["select * from news where id = ?" id])))

(defn insert-news! [{:keys [title url content image]}]
  (jdbc/insert! db-spec :news
                {:title title :url url :content content :image image}))

(defn update-news! [{:keys [id title url content image]}]
  (jdbc/execute! db-spec
    ["update news set title = ?, url = ?, content = ?, image = ? where id = ?"
     title url content image id]))

(defn delete-news! [id]
  (jdbc/delete! db-spec :news ["id=?" id]))