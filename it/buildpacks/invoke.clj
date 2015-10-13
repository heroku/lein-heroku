(let [app-name (str "lein-heroku-test-" (rand-int 9999999))]
  [[:exec "git" "init"]
   [:exec "heroku" "create" app-name]
   [:lein "uberjar"]
   [:lein "heroku" "deploy"]
   [:contains?
    "/usr/bin/java"
    [:exec "heroku" "run" "which java"]]
   [:after
    [:exec "heroku" "destroy" app-name "--confirm" app-name]]
   ])
