
#_(defdeps
    [[org.clojure/clojure "1.5.0-RC6"]])

(ns sample1
  (:require [clojure.java.io :refer [file]]))

(spit (.getCanonicalFile (file *file* "../output"))
      (str "Running sample.clj under clojure " (clojure-version) " "
           "with arguments: " (prn-str *command-line-args*)))
