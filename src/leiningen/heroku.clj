(ns leiningen.heroku
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cemerick.pomegranate.aether :as aether]
            [leiningen.core.project :as project]
            [leiningen.core.classpath :as classpath]
            [leiningen.core.main :as main])
  (:import [com.heroku.sdk.deploy App]
           [java.io File]))

(defn- root-file [project & [f]] (new File (str (:root project) (File/separator) f)))

(defn- log-warn [msg] (main/warn "WARNING:" msg))

(defn- deploy-uberjar
  "Deploy the uberjar to Heroku"
  [project]
  (let [app (proxy [App] [
          "lein-heroku"
          (get-in project [:heroku :app-name])
          (root-file project)
          (root-file project "target")
          []]
          (logWarn [msg] (log-warn msg))
          (logInfo [msg] (main/info msg))
          (logDebug [msg] (main/debug msg)))]
    (.deploy app
      (map (fn [x] (root-file project x)) (or
        (:include-files (:heroku project))
        ["target"]))
      {}
      (or (:jdk-version (:heroku project)) "1.8")
      "cedar-14"
      (or (:process-types (:heroku project)) {})
      "slug.tgz")))

(defn- deploy-lein
  "Deploy directories and dependencies to Heroku"
  [project]
  (println "This plugin only supports Uberjar deployment!\n"
           "See the Leiningen docs for more information:\n"
           "https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md#uberjar"))

(defn heroku
  "Deploy to Heroku PaaS"
  [project & cmd]
  (case (first cmd)
    "deploy" (if (contains? project :uberjar-name)
      (apply deploy-uberjar [project])
      (apply deploy-lein [project]))
    "deploy-lein"
      (apply deploy-lein [project])
    "deploy-uberjar"
      (apply deploy-uberjar [project])))
