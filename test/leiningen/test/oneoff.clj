(ns leiningen.test.oneoff
  (:use [leiningen.oneoff] :reload)
  (:use [clojure.test]
        [clojure.java.io :only [file delete-file]]))

(def sample-project (file "test_projects/sample.clj"))
(def output-file (file "test_projects/output"))

(deftest test-execute-script
  (delete-file output-file true)
  (execute-script (.getCanonicalPath sample-project) "bake" "honk!")
  (let [output (slurp output-file)]
    (is (re-find #"1.3.0-alpha5" output))
    (is (re-find #"\[\"bake\" \"honk!\"\]" output)))
  (delete-file output-file))
