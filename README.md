# lein-oneoff

Dealing with dependencies and the classpath can be a
pain. [Leiningen](http://github.com/technomancy/leiningen) takes most
of the pain away, but creating a new leiningen project for a simple
one-off script may sometimes feel like overkill. This is where
[lein-oneoff](http://github.com/mtyaka/lein-oneoff) comes in.

With the help of lein-oneoff you can open a file, declare
dependencies at the top, and write the rest of the code as
usually. lein-oneoff will let you run the file, open a repl or start a swank
server while taking care of fetching dependencies and constructing the
classpath automatically.

You might find lein-oneoff useful when you want to play with a brand
new alpha release of clojure, but would rather not dowload the jar
manually, when you think you know the answer to a question about a
particular library posted to the clojure mailing list, but would
rather test your idea out in the repl before posting the answer, or when
you quickly want to analyse and plot some data using
[Incanter](http://incanter.org/).

## Usage

lein-oneoff scripts usually consist of a single file. Dependencies
should be stated at the top using the `defdeps` form. Here's an example:

    (defdeps
      [[org.clojure/clojure "1.2.0"]
       [compojure "0.5.2"]
       [ring/ring-jetty-adapter "0.3.3"]])

    (ns example
      (:use [compojure.core]
            [ring.adapter.jetty :only [run-jetty]]))

    (defroutes routes
      (GET "/" [] "Hello world!"))

    (def server
      (run-jetty routes {:port 8080 :join? false}))

Save this file as `example.clj`, then run it with:

    $ lein oneoff example.clj

This command will check the specified dependencies and install them
into the local maven repository (`~/.m2/repository`) unless already
installed, and then run `example.clj` with the necessary dependencies
in the classpath. Note that the dependencies are referenced directly
from the local maven repository.

### The defdeps form

The `defdeps` form must be the first form in the file. It has the following
signature:

    (defdeps dependencies additional-entries?)

where dependencies should be specified as a vector using the same
syntax as inside regular leiningen `defproject` form under the
`:dependencies` key. The second argument is an optional map of
additional standard `defproject` entries. Please note that not
all of the available leinigen options make sense for a one-off script
and might not work correctly.

One of the entries that can be useful is the `:repositories` entry. Here's
an example:

    (defdeps
      [[org.clojure/clojure "1.3.0-alpha3"]
       [org.apache.pivot/pivot-web "1.5.2"]]
      {:repositories
       {"apache" "https://repository.apache.org/content/repositories/releases/"}})

The `defdeps` form may be omitted in which case the only assumed
dependency is `org.clojure/clojure` of the same version as your leiningen
installation is using.

### repl

To start a repl in the context of a one-off script, use the `--repl`
command (or its shorter equivalent, `-r`):

    $ lein oneoff --repl example.clj
    $ lein oneoff -r example.clj

### swank

A swank server can be started with the `--swank` (or `-s`)
command:

    $ lein oneoff --swank example.clj
    $ lein oneoff -s example.clj

Please note that for the swank command to work, you'll need to have
`swank-clojure` installed as a user-level leiningen plugin. At the moment,
only `swank-clojure 1.3.0-SNAPSHOT` is supported.

### classpath

lein-oneoff offers an equivalent to leiningen's built-in `classpath`
task which prints the project's classpath for one-off scripts:

    $ lein oneoff --classpath example.clj
    $ lein oneoff -cp example.clj

## Installation

This plugin should be installed as a user-level leiningen
plugin. Leiningen 1.4.0 comes with a built-in task for installing
user-level plugins:

    $ lein plugin install lein-oneoff 0.1.0

lein-oneoff works with leiningen 1.4.0 or newer.

## License

Copyright (C) 2010 Matjaz Gregoric

Distributed under the Eclipse Public License, the same as Clojure.
