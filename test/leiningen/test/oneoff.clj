(ns leiningen.test.oneoff
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :refer [file delete-file]]))

(def output-file (file "test_projects/output"))

(defn- exec [filename & args]
  (delete-file output-file true)
  (let [script (format "test_projects/%s" filename)]
    (apply sh "lein" "oneoff" "-e" script args)))

(defn- cp [filename]
  (let [script (format "test_projects/%s" filename)]
    (sh "lein" "oneoff" "-cp" script)))

(deftest test-execute-script
  (exec "sample1.clj" "bake" "honk!")
  (let [output (slurp output-file)]
    (is (re-find #"1\.4\.0-beta1" output))
    (is (re-find #"arguments: \[\"bake\" \"honk!\"\]" output)))
  (exec "sample2.clj")
  (let [output (slurp output-file)]
    (is (re-find #"1\.5\.0-RC6" output))
    (is (re-find #"arguments: nil" output)))
  (delete-file output-file))

(deftest test-classpath
  (let [output (:out (cp "sample1.clj"))]
    (is (re-find #"clojure-1\.4\.0-beta1\.jar" output)))
  (let [output (:out (cp "sample2.clj"))]
    (is (re-find #"clojure-1\.5\.0-RC6\.jar" output))))
