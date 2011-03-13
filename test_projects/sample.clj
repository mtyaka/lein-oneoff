(defdeps
  [[org.clojure/clojure "1.3.0-alpha5"]])

(ns sample1
  (:use [clojure.java.io :only [file]]))

(spit (file *file* "../output")
      (str "Running sample.clj under clojure " (clojure-version) " "
           "with arguments: " (prn-str *command-line-args*)))
