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

(defn- default-process-types [project]
  (if (.exists (root-file project "Procfile")) {}
    (do
      (if (contains? project :uberjar-name) (log-warn "Uberjar detected but no Procfile found!"))
      {"web" "lein with-profile production trampoline run"})))

(defn- vendor-dependencies [dependency-key project]
  (classpath/get-dependencies dependency-key (merge (select-keys project [dependency-key
    :offline? :update :checksum :mirrors])
    {:repositories [["local" {:checksum :ignore :url (str "file:" (.getPath (io/file (System/getProperty "user.home") ".m2" "repository")))}]]
    :local-repo (java.io.File. (:root project) "target/heroku/app/.m2/repository")})))

(defn deploy-uberjar
  "Deploy the uberjar to Heroku"
  [project]
  (main/info "todo"))

(defn deploy
  "Deploy directories and dependencies to Heroku"
  [project]
  (let [logLevel (Integer/parseInt (System/getProperty "heroku.logLevel" "3"))
        app (proxy [App] [
          "lein-heroku"
          (get-in project [:heroku :app-name])
          (root-file project)
          (root-file project "target")
          ["https://codon-buildpacks.s3.amazonaws.com/buildpacks/heroku/jvm-common.tgz"
           "https://github.com/jkutner/heroku-buildpack-lein"]]
        (logWarn [msg] (log-warn msg))
        (logInfo [msg] (main/info msg))
        (logDebug [msg] (main/debug msg))
        (prepare [includedFiles processTypes]
          (main/info "-----> Vendoring dependencies...")
          (vendor-dependencies :dependencies project)
          (vendor-dependencies :plugins project)
          (proxy-super prepare includedFiles processTypes)
          ))]
    (.deploy app
      (map (fn [x] (root-file project x)) (or
        (:include-files (:heroku project))
        ["target", "src", "project.clj"]))
      {}
      (or (:jdk-version (:heroku project)) "1.8")
      "cedar-14"
      (or (:process-types (:heroku project)) (default-process-types project))
      "slug.tgz")))

(defn heroku
  "Deploy to Heroku PaaS"
  [project & cmd]
  (case (first cmd)
    "deploy" (apply deploy [project])))
