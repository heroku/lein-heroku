(ns leiningen.heroku
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cemerick.pomegranate.aether :as aether]
            [leiningen.core.project :as project]
            [leiningen.core.classpath :as classpath]
            [leiningen.core.main :as main])
  (:import [com.heroku.sdk.deploy App]
           [java.io File]))

(def lein-default-profile "{:user  { :offline? true :checksum :ignore
  :mirrors  {\"local\" {:url \"file:///app/.m2/repository\" :checksum :ignore}}}}")

(defn- root-file [project & [f]] (new File (str (:root project) (File/separator) f)))

(defn- log-warn [msg] (main/warn "WARNING:" msg))

(defn- vendor-dependencies [dependency-key project]
  (classpath/get-dependencies dependency-key (merge (select-keys project [dependency-key
    :update :checksum])
    {:repositories [["central" {:url (str "file:" (.getPath (io/file (System/getProperty "user.home") ".m2" "repository")))}]]
    :local-repo (java.io.File. (:root project) "target/heroku/app/.m2/repository")})))

(defn- deploy-uberjar
  "Deploy the uberjar to Heroku"
  [project]
  (let [logLevel (Integer/parseInt (System/getProperty "heroku.logLevel" "3"))
        app (proxy [App] [
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
          (.mkdirs (java.io.File. (:root project) "target/heroku/app/.lein"))
          (spit (java.io.File. (:root project) "target/heroku/app/.lein/profiles.clj") lein-default-profile)
          (vendor-dependencies :dependencies project)
          (vendor-dependencies :plugins project)
          (proxy-super prepare includedFiles processTypes) ))]
    (.deploy app
      (map (fn [x] (root-file project x)) (or
        (:include-files (:heroku project))
        ["target", "src", "resources", "project.clj"]))
      {}
      (or (:jdk-version (:heroku project)) "1.8")
      "cedar-14"
      (or (:process-types (:heroku project)) {"web" "lein with-profile production trampoline run"})
      "slug.tgz")))

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
