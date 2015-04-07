(ns lein-auto-install.plugin
  (:require [robert.hooke]
            [leiningen.core.eval]
            [clojure.java.io :as io]
            [clojure.pprint]
            [digest]
            [plumbing.core :refer :all]
            [me.raynes.conch :refer [with-programs]]))

(defn- slurp-if-exists [filename]
  (let [f (io/file filename)] (when (.exists f) (slurp f))))

(defn- project-hash [project-root-dir]
  "Returns the hash of the projects.clj in project-root-dir."
  (let [f (io/file project-root-dir "project.clj")]
    (if-let [c (slurp-if-exists f)]
      (digest/md5 c)
      (throw (RuntimeException. (str (.getPath f) " not found."))))))

(defn- checkouts-tree
  "Returns a tree of checkout paths of all checkouts from the project in
  project-root-dir. The tree format is:
  ((checkout1-path (checkouts-tree checkout1-path))
   (checkout2-path ...)
   ...)"
  ([project-root-dir] (checkouts-tree project-root-dir #{}))
  ([project-root-dir already-seen]
   (map #(let [path (.getPath %)
               h (project-hash %)]
           (assert (not (already-seen h)) "Cycle found in dependencies.")
           (cons path (checkouts-tree % (conj already-seen h))))
        (.listFiles (io/file project-root-dir "checkouts")))))

(defn- traverse-seq
  "Takes a tree of checkout paths as returned by checkouts-tree and returns a
  seq of checkout paths representing a post order traversal."
  [trees]
  (mapcat #(concat (traverse-seq (rest %)) [(first %)]) trees))

(defn- install-checkouts [f & args]
  (let [project (first args)
        state-file (io/file (:root project) ".lein-auto-install")
        last-state (set
                    (when-let [c (slurp-if-exists state-file)] (read-string c)))
        checkouts-seq (traverse-seq (checkouts-tree (:root project)))
        ; maps project path -> project.clj hash
        project-hash-map (for-map [c checkouts-seq] c (project-hash c))]
    (with-programs [lein]
      (doseq [c checkouts-seq]
        (when-not (last-state (project-hash-map c))
          (lein "install" {:out *out*, :err *err* :dir c})
          (flush))))
    (let [current-state (set (vals project-hash-map))]
      (when (not= last-state current-state)
        (spit state-file (pr-str current-state)))))
  (apply f args))

(defn hooks []
  (robert.hooke/add-hook #'leiningen.core.eval/eval-in-project
                         #'install-checkouts))
