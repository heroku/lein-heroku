(ns leiningen.heroku
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cemerick.pomegranate.aether :as aether]
            [leiningen.core.project :as project]
            [leiningen.core.classpath :as classpath]
            [leiningen.core.main :as main])
  (:import [com.heroku.sdk.deploy App]
           [java.io File]))

(defn file-join [f1 f2] (str f1 (File/separator) f2))

(defn uberjar-missing [project]
  (and (not (.exists (io/file (file-join "target" (:uberjar-name project)))))
       (not (:include-files (:heroku project)))))

(defn- root-file [project & [f]]
  (new File (str (:root project) (File/separator) f)))

(defn- log-error [msg] (do (main/warn "ERROR:" msg) (System/exit 1)))

(defn- log-warn [msg] (main/warn "WARNING:" msg))

(defn- deploy-uberjar
  "Deploy the uberjar to Heroku"
  [project]
  (if (uberjar-missing project)
    (log-error "Uberjar file not found! Have you run `lein uberjar' yet?`")
    (let [app (proxy [App] [
          "lein-heroku"
          (get-in project [:heroku :app-name])
          (root-file project)
          (root-file project "target")
          (or (:buildpacks (:heroku project)) [])]
          (logWarn [msg] (log-warn msg))
          (logInfo [msg] (main/info msg))
          (logDebug [msg] (main/debug msg)))]
      (.deploy app
        (map (fn [x] (root-file project x)) (or
          (:include-files (:heroku project))
          [(file-join "target" (:uberjar-name project)) "project.clj"]))
        {}
        (or (:jdk-version (:heroku project)) "1.8")
        "cedar-14"
        (or (:process-types (:heroku project)) {})
        "slug.tgz"))))

(defn- deploy-lein
  "Deploy directories and dependencies to Heroku"
  [project]
  (main/info "This plugin only supports Uberjar deployment!\n"
             "See the Leiningen docs for information on how to configure this:\n"
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
