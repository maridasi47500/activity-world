(ns clojurenew.core
  (:require
   [clojurenew.db :refer [insert-country! country-exists?]]
   ;; Routing
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [cheshire.core :as wowjson]


   ;; Ring core
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.util.response :as response]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.nested-params :refer [wrap-nested-params]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.session.cookie :refer [cookie-store]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery *anti-forgery-token*]]
   ;[ring.middleware.stacktrace :refer [wrap-stacktrace]]
   ;[ring.middleware.reload :refer [wrap-reload]]

   ;; Logging
   ;[ring.middleware.logger :refer [wrap-with-logger]]

   ;; JSON & HTML
   [clojure.data.json :as json]
   [hiccup.core :refer [html]]

   ;; Utilities
   [clojure.string :as str]
   [clj-time.core :as time]

   ;; Server
   [org.httpkit.server :refer [run-server]]

   ;; App-specific
   [clojurenew.db :as db]
   [clojurenew.routes :refer [app-routes]]
   [buddy.core.keys :as keys]
   [aleph.http.client-middleware :as ahclient]))

(defn get-custom-token [request]
  (get-in request [:headers "x-forgery-token"]))
(def custom-error-response
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body (str "hey" "<h1>Reponse erreur 403 : Missing anti-forgery token</h1>")})
(defn custom-error-handler [request]
  (println (get-in request [:headers "x-forgery-token"]))
  ;(println (str request :form-params))
  (println "CSRF token missing or invalid for request:" (:uri request))
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body (str "Handler ::::: hey :: session contents<br>" (:session request) "<p>current token </p>" *anti-forgery-token* "<hr>" (get (:params request) "__anti-forgery-token") "<h3>test for a post route : title of news added</h3>" "<h1>403 Forbidden</h1><p>CSRF protection triggered.</p>" (get (:params request) "title") "<h3>title</h3>"  "<h3>photo content</h3>" (get (:params request) "photo") "<h3>photo</h3>" "<hr><br>" request "<h1>handler Missing anti-forgery token</h1>")})








(defn colorize [label color]
  (str "\u001B[" color "m" label "\u001B[0m"))


(def anti-forgery-handler
  "hey"
  *anti-forgery-token*)

(def custom-defaults
  (-> site-defaults
      (assoc-in [:security :anti-forgery] false))) ; disable built-in anti-forgery
(defn print-request-middleware [handler]
  (fn [request]

    (println "--- Incoming Request ---")
    (println
      (colorize (str
        (clojure.string/upper-case
          (clojure.string/replace (str (:request-method request)) ":" ""))
        " "
        (:uri request)
        " ") "32"))

    (println (colorize {:params (:params request)} "33")) ; jaune
    ;(println (colorize (json/write-str {:params (:params request)} :indent true) "33")) ; jaune

    (println "keys request : " (keys request))


    (println "Multipart params:" (:multipart-params request))
    (println "Form params:" (:form-params request))
    (println "Headers:" (:headers request))
    (println "--- End of Middleware ---")
    (handler request))) ; ← très important : transmettre la requête au handler suivant

(defn print-request-middleware-begin [handler]
  (fn [request]
    (println "--- Incoming Request ---")
    (println "params at the begin of middleware stack:" (:params request))
    (let [response (handler request)]
      (println "--- Outgoing Response ---")
      response)))
(defn print-request-middleware-end [handler]
  (fn [request]
    ;(println (:params ))
    (println "--- Incoming Request ---")
    (println "params at the end of middleware stack:" (:params request))
    (let [response (handler request)]
      (println "--- Outgoing Response ---")
      response)))
(defn enforce-utf8-encoding [handler]
  (fn [request]
    (let [req (assoc request :character-encoding "UTF-8")]
      (handler req))))

;(def app
;  (-> app-routes
;
;      ;enforce-utf8-encoding
;      (wrap-defaults
;        (-> site-defaults
;          ;(assoc-in [:security :anti-forgery]
;          ;          {:safe-header "X-CSRF-Protection"})
;          (assoc-in [:params :multipart]
;                       {:store (some-byte-array-store/byte-array-store)
;                         :max-size (* 10 1024 1024)})))
;      print-request-middleware-begin
;      print-request-middleware-end
;))
;(:b {:a 1 :b 6})


(defn print-debug-json [request]
  (let [debug-map {:method         (str/upper-case (name (:request-method request)))
                   :uri            (:uri request)
                   :headers        (:headers request)
                   :params         (:params request)
                   :form-params    (:form-params request)
                   :multipart-params (:multipart-params request)}]
    (println (colorize "\n--- Incoming Request (Debug JSON) ---" "36")) ; cyan
    (println (json/write-str debug-map :indent true))
    (println (colorize "--- End of Debug ---\n" "36"))

    ))
(defn wrap-debug-handler [handler]
  (fn [request]

    (println (colorize "\n--- Incoming Request ---" "36"))
    (println (colorize (str "Method:" (:request-method request)) "37"))
    (println (colorize (str "URI:" (:uri request)) "37"))
    (println "Params:" (:params request))
    (println (colorize "Params:" (:params request) "33"))

    (println "Keys in request:" (keys request))

    (println "Headers:" (:headers request))

    (println "Form params:" (:form-params request))
    (println "Multipart params:" (:multipart-params request))
    (println "Body class:" (when-let [b (:body request)] (class b)))
    (println (colorize "--- End of Middleware ---\n" "36"))
    (handler request)))
(defn wrap-slurp-body [handler]
  (fn [request]
    (let [body-stream (:body request)
          body-str (when body-stream (slurp body-stream))
          updated-request (-> request
                              (assoc :body (java.io.ByteArrayInputStream. (.getBytes body-str "UTF-8")))
                              (assoc :raw-body body-str))]
      (handler updated-request))))



(def app
  (-> app-routes


      print-request-middleware
      ;wrap-keyword-params




      wrap-nested-params
      wrap-keyword-params
      (wrap-multipart-params {:max-size (* 350 1024 1024)})





      wrap-params

      ;(wrap-session)
      (wrap-anti-forgery {:safe-header "X-CSRF-Protection"})

      ;wrap-with-logger
      ;wrap-stacktrace
      ;wrap-reload
      ;(wrap-defaults site-defaults)
))
(ns clojurenew.core
  (:require [clojurenew.db :refer [insert-country! country-exists?]]))

(def all-countries-fr
  ["Afghanistan" "Afrique du Sud" "Albanie" "Algérie" "Allemagne" "Andorre" "Angola"
   "Antigua-et-Barbuda" "Arabie Saoudite" "Argentine" "Arménie" "Australie" "Autriche"
   "Azerbaïdjan" "Bahamas" "Bahreïn" "Bangladesh" "Barbade" "Belgique" "Belize" "Bénin"
   "Bhoutan" "Biélorussie" "Birmanie" "Bolivie" "Bosnie-Herzégovine" "Botswana" "Brésil"
   "Brunei" "Bulgarie" "Burkina Faso" "Burundi" "Cambodge" "Cameroun" "Canada" "Cap-Vert"
   "Chili" "Chine" "Chypre" "Colombie" "Comores" "Corée du Nord" "Corée du Sud" "Costa Rica"
   "Côte d'Ivoire" "Croatie" "Cuba" "Danemark" "Djibouti" "Dominique" "Égypte"
   "Émirats arabes unis" "Équateur" "Érythrée" "Espagne" "Estonie" "États-Unis" "Éthiopie"
   "Fidji" "Finlande" "France" "Gabon" "Gambie" "Géorgie" "Ghana" "Grèce" "Grenade"
   "Guatemala" "Guinée" "Guinée équatoriale" "Guinée-Bissau" "Guyana" "Haïti" "Honduras"
   "Hongrie" "Îles Cook" "Îles Marshall" "Îles Salomon" "Inde" "Indonésie" "Irak" "Iran"
   "Irlande" "Islande" "Israël" "Italie" "Jamaïque" "Japon" "Jordanie" "Kazakhstan"
   "Kenya" "Kirghizistan" "Kiribati" "Koweït" "Laos" "Lesotho" "Lettonie" "Liban"
   "Libéria" "Libye" "Liechtenstein" "Lituanie" "Luxembourg" "Macédoine du Nord"
   "Madagascar" "Malaisie" "Malawi" "Maldives" "Mali" "Malte" "Maroc" "Maurice"
   "Mauritanie" "Mexique" "Micronésie" "Moldavie" "Monaco" "Mongolie" "Monténégro"
   "Mozambique" "Namibie" "Nauru" "Népal" "Nicaragua" "Niger" "Nigéria" "Norvège"
   "Nouvelle-Zélande" "Oman" "Ouganda" "Ouzbékistan" "Pakistan" "Palaos" "Palestine"
   "Panama" "Papouasie-Nouvelle-Guinée" "Paraguay" "Pays-Bas" "Pérou" "Philippines"
   "Pologne" "Portugal" "Qatar" "République centrafricaine" "République démocratique du Congo"
   "République dominicaine" "République du Congo" "République tchèque" "Roumanie" "Royaume-Uni"
   "Russie" "Rwanda" "Saint-Christophe-et-Niévès" "Saint-Marin" "Saint-Vincent-et-les-Grenadines"
   "Sainte-Lucie" "Salvador" "Samoa" "São Tomé-et-Principe" "Sénégal" "Serbie" "Seychelles"
   "Sierra Leone" "Singapour" "Slovaquie" "Slovénie" "Somalie" "Soudan" "Soudan du Sud"
   "Sri Lanka" "Suède" "Suisse" "Suriname" "Syrie" "Tadjikistan" "Tanzanie" "Tchad"
   "Thaïlande" "Timor oriental" "Togo" "Tonga" "Trinité-et-Tobago" "Tunisie" "Turkménistan"
   "Turquie" "Tuvalu" "Ukraine" "Uruguay" "Vanuatu" "Vatican" "Venezuela" "Vietnam"
   "Yémen" "Zambie" "Zimbabwe"])



(defn insert-all-countries! []
  (doseq [country all-countries-fr]
    (when-not (country-exists? country)
      (insert-country! {:name country})))
  (println "✅ Tous les pays ont été insérés dans la base."))






(defn -main
  "Main entrypoint: ensure DB/tables, start HTTP server."
  [& _]
  ;(run-jetty app {:port 8080})

  (db/ensure-db!)
  (insert-all-countries!)

  (println "Server started on port 8080"))
  (run-server #'app {:port 8080 :max-body (* 350 1024 1024)})


