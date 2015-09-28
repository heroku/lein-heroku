(let [app-name (str "lein-heroku-test-" (rand-int 9999999))]
  [[:exec "git" "init"]
   [:exec "heroku" "create" app-name]
   [:lein "uberjar"]
   [:lein "heroku" "deploy"]
   [:exec "sleep" "10"]
   [:contains?
    "Welcome to happy-path"
    [:get (str "http://" app-name ".herokuapp.com")]]
   [:after
    [:exec "heroku" "destroy" app-name "--confirm" app-name]]
   ])
