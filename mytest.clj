(ns clojurenew.mytest
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [your-app.core :refer [app]]))

(deftest post-news-with-photo-test
  (testing "POST /action_create_news with valid multipart data"
    (let [fake-image-bytes (.getBytes "fake image content" "UTF-8")
          request (-> (mock/request :post "/action_create_news")
                      (mock/content-type "multipart/form-data")
                      (assoc :multipart-params
                             {"title" "srtghse"
                              "content" "dtyjsrjts"
                              "photo" {:filename "test.png"
                                       :content-type "image/png"
                                       :bytes fake-image-bytes}
                              "__anti-forgery-token" "valid-token"}))
          response (app request)]
      (is (= 200 (:status response)))
      (is (re-find #"News created" (:body response))))))

