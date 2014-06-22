(ns leiningen.oneoff
  (:require [clojure.string :refer [replace-first]]
            [clojure.java.io :refer [file]]
            [leiningen.core.project]
            [leiningen.core.eval]
            [leiningen.core.main]
            [leiningen.repl]
            [leiningen.classpath]))

(defn parse-defdeps [script]
  (let [contents (slurp script)
        contents (replace-first contents #"^\s*#_" "")
        [sym deps opts] (read-string contents)]
    (if (= sym 'defdeps)
      (assoc opts :dependencies deps)
      {:dependencies `[[org.clojure/clojure ~(clojure-version)]]})))

(defn oneoff-project [script]
  (let [dir (System/getProperty "user.dir")
        tmpdir (System/getProperty "java.io.tmpdir")
        declarations (parse-defdeps script)
        defaults {:name "oneoff"
                  :group "oneoff"
                  :version "0.1"
                  :oneoff true
                  :eval-in :subprocess
                  :injections ['(defmacro defdeps [& args])]
                  :prep-tasks []
                  :root dir
                  :source-paths [dir]
                  :target-path (.getAbsolutePath (file tmpdir "oneoff"))
                  :compile-path (.getAbsolutePath (file tmpdir "oneoff" "classes"))
                  :test-paths []
                  :resource-paths []
                  :dev-resources-path dir}
        ;; project-with-profiles function was added in Leiningen 2.1,
        ;; add a workaround for 2.0.
        with-profiles (resolve 'leiningen.core.project/project-with-profiles)
        with-profiles (or with-profiles identity)]
    (-> (merge defaults declarations)
        (leiningen.core.project/make)
        (with-profiles)
        ;; init-profiles is marked as :internal, but it looks like
        ;; there's no other way of doing this.
        (leiningen.core.project/init-profiles [:default]))))

(defn execute-script [project script args]
  (let [args (when args (vec args))
        form `(binding [*command-line-args* ~args]
                (clojure.main/load-script ~script))]
    (leiningen.core.eval/eval-in-project project form)))

(defn ^:no-project-needed oneoff
  "Manages dependencies of one-off clojure scripts.

Useful in situations when creating a proper leiningen project feels like
overkill.

Syntax: lein oneoff <command> <file>
  <command> can be one of: --exec, --repl, --classpath.
  Short forms (-e, -r, -cp, -s) may be used instead.
  If <command> is omitted, --exec is assumed.

See http://github.com/mtyaka/lein-oneoff for more information."
  ([_ cmd script & args]
     (if (= (first cmd) \-)
       (let [project (oneoff-project script)]
         (case cmd
           ("--exec" "-e") (execute-script project script args)
           ("--repl" "-r") (leiningen.repl/repl project)
           ("--classpath" "-cp") (leiningen.classpath/classpath project)
           (leiningen.core.main/abort
            (format "Unknown command: %s\n"  cmd)
            "Supported commands: --exec/-e, --repl/-r, --classpath/-cp")))
       (apply oneoff nil "--exec" cmd script args)))
  ([_ script]
     (oneoff nil "--exec" script)))
