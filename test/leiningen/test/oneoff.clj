(ns leiningen.test.oneoff
  (:use [leiningen.oneoff :only [execute-script]] :reload)
  (:use [clojure.test]
        [clojure.java.io :only [file delete-file]]))

(def output-file (file "test_projects/output"))

(defn- exec [filename & args]
  (delete-file output-file true)
  (let [script-file (file "test_projects" filename)]
    (apply execute-script (.getCanonicalPath script-file) args)))

(deftest test-execute-script
  (exec "sample1.clj" "bake" "honk!")
  (let [output (slurp output-file)]
    (is (re-find #"1\.3\.0-alpha5" output))
    (is (re-find #"arguments: \[\"bake\" \"honk!\"\]" output)))
  (exec "sample2.clj")
  (let [output (slurp output-file)]
    (is (re-find #"1\.2\.1" output))
    (is (re-find #"arguments: nil" output)))
  (delete-file output-file))
