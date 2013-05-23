(defdeps
  [[org.clojure/clojure "1.4.0-beta1"]])

(ns sample1
  (:use [clojure.java.io :only [file]]))

(spit (.getCanonicalFile (file *file* "../output"))
      (str "Running sample.clj under clojure " (clojure-version) " "
           "with arguments: " (prn-str *command-line-args*)))
