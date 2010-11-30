(ns leiningen.oneoff
  (:use [robert.hooke :only [add-hook]]
        [leiningen.core :only [abort]])
  (:require
     [lancet]
     [clojure.main]
     [leiningen compile classpath repl deps])
  (:import java.io.File))

(try
  (require 'leiningen.swank)
  (catch java.io.FileNotFoundException e))

(def lein-swank-ns (find-ns 'leiningen.swank))

(def swank-form-var
     (when lein-swank-ns (ns-resolve lein-swank-ns 'swank-form)))

(def default-deps
  `[[org.clojure/clojure ~(clojure-version)]])

(def defdeps-defmacro-form
  `(defmacro ~'defdeps [& args#]))

(defn deps-classpath
  "Resolves and installs dependencies to the local maven repository.
Returns a sequence of paths referencing jars in the repository."
  [project]
  (let [deps-task (leiningen.deps/make-deps-task project :dependencies)
        _ (.execute deps-task)
        fileset (.getReference lancet/ant-project
                               (.getFilesetId deps-task))
        dir-scanner (.getDirectoryScanner fileset lancet/ant-project)
        base-dir (.getBasedir dir-scanner)]
    (for [fpath (.getIncludedFiles dir-scanner)]
      (.getCanonicalPath (File. base-dir fpath)))))

(defn get-oneoff-classpath
  "Returns a sequence of paths that constitute the full classpath
for a one-off project."
  [project]
  (concat [(:root project)]
          (deps-classpath project)
          (leiningen.classpath/user-plugins)))

(defn oneoff-deps-hook [deps project]
  (when-not (:oneoff project) (deps project)))

(defn oneoff-get-classpath-hook [get-classpath project]
  (if (:oneoff project)
    (get-oneoff-classpath project)
    (get-classpath project)))

(defn oneoff-eval-in-project-hook
  [eval-in-project project form & [handler skip-auto-compile init]]
  (let [skip-auto-compile (or (:oneoff project) skip-auto-compile)]
    (eval-in-project project form handler skip-auto-compile init)))

(defn oneoff-repl-server-hook [repl-server project host port]
  (let [server-form (repl-server project host port)]
    (if (:oneoff project)
      `(do ~defdeps-defmacro-form ~server-form)
      server-form)))

(defn oneoff-swank-form-hook [swank-form project port host opts]
  (let [server-form (swank-form project port host opts)]
    (if (:oneoff project)
      `(do ~defdeps-defmacro-form ~server-form)
      server-form)))

(add-hook #'leiningen.deps/deps oneoff-deps-hook)
(add-hook #'leiningen.compile/eval-in-project oneoff-eval-in-project-hook)
(add-hook #'leiningen.classpath/get-classpath oneoff-get-classpath-hook)
(add-hook #'leiningen.repl/repl-server oneoff-repl-server-hook)

(when swank-form-var
  (add-hook swank-form-var oneoff-swank-form-hook))

(defn parse-defdeps [script]
  (let [form (read-string (slurp script))]
    (if (= (first form) 'defdeps)
      [(nth form 1) (nth form 2 {})]
      [default-deps {}])))

(defn oneoff-project [script]
  (let [dir (System/getProperty "user.dir")
        [deps opts] (parse-defdeps script)]
    (merge
      {:oneoff true
       :name "A oneoff project"
       :version "1.0.0"
       :dependencies deps
       :root dir
       :compile-path (str dir "/classes")
       :library-path (str dir "/lib")}
      opts)))

(defn print-usage []
  (abort "Usage: lein oneoff <command> <file>
  <command> can be one of: --exec, --repl, --classpath, --swank.
  Short forms (-e, -r, -cp, -s) may be used instead.
  If <command> is omitted, --exec is assumed."))

(defn execute-script [script & args]
  (let [project (oneoff-project script)
        args (when args (vec args))
        form `(do
                ~defdeps-defmacro-form
                (binding [*command-line-args* ~args]
                  (clojure.main/load-script ~script)))]
    (leiningen.compile/eval-in-project project form)))

(defn start-repl-server [script]
  (leiningen.repl/repl (oneoff-project script)))

(defn start-swank-server [script & args]
  (if lein-swank-ns
    (if swank-form-var
      (let [swank-fn (ns-resolve lein-swank-ns 'swank)]
        (apply swank-fn (oneoff-project script) args))
      (abort "The oneoff swank task only works with
swank-clojure 1.3.0-SNAPSHOT or newer."))
    (abort "You'll need to install swank-clojure as a user plugin
for this task to work.")))

(defn print-classpath [script]
  (leiningen.classpath/classpath (oneoff-project script)))

(defn oneoff
  "Handles dependencies and execution of one-off scripts when creating a
proper leiningen project feels like overkill.

Syntax: lein oneoff <command> <file>
  <command> can be one of: --exec, --repl, --classpath, --swank.
  Short forms (-e, -r, -cp, -s) may be used instead.
  If <command> is omitted, --exec is assumed.

See http://github.com/mtyaka/lein-oneoff for more information."
  ([cmd script & args]
   (case cmd
         ("--exec" "-e") (apply execute-script script args)
         ("--repl" "-r") (start-repl-server script)
         ("--classpath" "-cp") (print-classpath script)
         ("--swank" "-s") (apply start-swank-server script args)
         (apply oneoff "--exec" cmd script args)))
  ([script]
   (oneoff "--exec" script))
  ([]
   (print-usage)))
