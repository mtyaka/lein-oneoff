# lein-oneoff

Dealing with dependencies and the classpath can be a
pain. [Leiningen](http://github.com/technomancy/leiningen) takes most
of the pain away, but creating a new leiningen project for a simple
one-off script may sometimes feel like overkill. This is where
[lein-oneoff](http://github.com/mtyaka/lein-oneoff) comes in.

With the help of lein-oneoff you can open a file, declare
dependencies at the top, and write the rest of the code as
usually. lein-oneoff will let you run the file or open a repl session
while taking care of fetching dependencies and constructing the
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
should be stated at the top using the `defdeps` form. You may
optionally prefix the `defdeps` form with the `#_` reader macro (ignore
next form). Here's an example:

    #_(defdeps
        [[org.clojure/clojure "1.2.0"]
         [compojure "0.5.2"]
         [ring/ring-jetty-adapter "0.3.3"]])

    (ns example
      (:use [compojure.core :only [defroutes GET]]
            [ring.adapter.jetty :only [run-jetty]]))

    (defroutes routes
      (GET "/" [] "Hello world!"))

    (def server
      (run-jetty routes {:port 8080 :join? false}))

Save this file as `example.clj`, then run it with:

    $ lein oneoff example.clj

This command will resolve the specified dependencies and install them
into the local maven repository (`~/.m2/repository`) unless already
installed, and then run `example.clj` with the classpath properly set.

### The defdeps form

The `defdeps` form must be the first form in the file. It has the following
signature:

    (defdeps dependencies additional-entries?)

where dependencies should be specified as a vector using the same
syntax as inside regular leiningen `defproject` form under the
`:dependencies` key. The second argument is an optional map of
additional standard `defproject` entries. Please note that not all of
the available leinigen options make sense for a one-off script and
might not work correctly. Adding a `#_` prefix will make it possible
to ignore the `defdeps` form when re-compiling the file in a repl.

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

### exec

To execute (load) a one-off script, use the `--exec` (or `-e` for short)
command. Any arguments positioned after the script name are passed to
the script as `*command-line-args*`.

    $ lein oneoff --exec example.clj arg1 arg2
    $ lein oneoff -e example.clj

The `--exec` command is the default, so you can omit it altogether.

    $ lein oneoff example.clj
    $ lein oneoff example.clj 8080 127.0.0.1

### repl

To start a repl in the context of a one-off script, use the `--repl`
command (or its shorter equivalent, `-r`):

    $ lein oneoff --repl example.clj
    $ lein oneoff -r example.clj

### classpath

lein-oneoff offers an equivalent to leiningen's built-in `classpath`
task which prints the project's classpath for one-off scripts:

    $ lein oneoff --classpath example.clj
    $ lein oneoff -cp example.clj

## Installation

This plugin should be installed as a global user-level leiningen
plugin. You can install it by adding the following line to
`~/.lein/profiles.clj`:

    {:user {:plugins [[lein-oneoff "0.3.0"]]}}

This version of `lein-oneoff` works with leiningen 2.0.0 or newer.
If you are using Leinigen 1.x, please check out
[release 0.2.0](https://github.com/mtyaka/lein-oneoff/tree/v0.2.0).


## License

Copyright (C) 2010 Matjaz Gregoric

Distributed under the Eclipse Public License, the same as Clojure.
