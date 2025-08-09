          (re-find #"^/poster_news$" uri) (renderhtml "poster des news" "form.html")
          (re-find #"^/ver$" uri) (renderhtml "Reservation" "form.html")
          (re-find #"^/action_create_news$" uri) (actioncreate "hello" "form.html" req)
          (re-find #"^/action_update_news$" uri) (actionupdate "hello" "formedit.html" req)
          (re-find #"^/deletenews" uri) (actiondelete "hello"  req)
          (re-find #"^/voir_news$" uri) (rendercollection "hello" output "_reservation.html" req)
          (re-find #"^/voir_news/\d+$" uri) (voirnews "hello" "voirnews.html" req (get (re-find #"^/voir_news/(\d+)$" uri) 1))
          (re-find #"^/edit_news/\d+$" uri) (editnews "hello" "formedit.html" req (get (re-find #"^/edit_news/(\d+)$" uri) 1))

