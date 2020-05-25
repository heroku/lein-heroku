Heroku Leiningen Plugin [![Build Status](https://travis-ci.org/heroku/lein-heroku.svg?branch=master)](https://travis-ci.org/heroku/lein-heroku)
=================

This plugin is used to deploy Clojure applications directly to Heroku without pushing to a Git repository.
This is can be useful when deploying from a CI server or when the Leiningen build is complex.

## Deprecation Note
⚠️ **`heroku-lein` is deprecated and no longer under active development.** Existing setups using this plugin will continue to work for the forseeable future. However, we advise customers to migrate to a another deployment mechanism since this plugin will not be updated to include security fixes, support for more recent Java/Clojure versions or new features.

Customers that require deployment of pre-built uberjars can use [the Heroku Java CLI plugin](https://devcenter.heroku.com/articles/deploying-executable-jar-files#using-the-heroku-java-cli-plugin).

For more information about the deployment of Clojure applications on Heroku, see [Deploying Clojure Apps on Heroku](https://devcenter.heroku.com/articles/deploying-clojure) on DevCenter.


## Requirements

+  Your application must be built as an [uberjar](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md#uberjar).
+  You must use Java 1.7 or higher locally.

## Using the Plugin

Add the following to your `project.clj` file's `:plugins` vector:

 [![Clojars Project](http://clojars.org/lein-heroku/latest-version.svg)](http://clojars.org/lein-heroku)

If you do not have a Heroku Git repo in your `git remote`, add something like this to your `project.clj`:

```clj
:heroku {:app-name "your-heroku-app-name"}
```

Now, if you have the [Heroku Toolbelt](https://toolbelt.heroku.com/) installed, run:

```sh-session
$ lein heroku deploy
```

If you do not have the toolbelt installed, then run:

```sh-session
$ HEROKU_API_KEY="xxx-xxx-xxxx" lein heroku deploy
```

And replace "xxx-xxx-xxxx" with the value of your Heroku API token.

### Configuring the Plugin

You may set a `:heroku` element in your `project.clj` like so:

```clj
:heroku {
  :app-name "your-heroku-app-name"
  :jdk-version "1.8"
  :include-files ["target/myapp.jar"]
  :process-types { "web" "java -jar target/myapp.jar" }}
```

By default, the plugin will include the `target` directory.


## License

Source Copyright © 2015 Heroku.
Distributed under the Eclipse Public License, the same as Clojure
uses. See the file COPYING.
