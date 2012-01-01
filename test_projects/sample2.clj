#_(defdeps
    [[org.clojure/clojure "1.2.1"]])

(ns sample1
  (:use [clojure.java.io :only [file]]))

(spit (file *file* "../output")
      (str "Running sample.clj under clojure " (clojure-version) " "
           "with arguments: " (prn-str *command-line-args*)))
