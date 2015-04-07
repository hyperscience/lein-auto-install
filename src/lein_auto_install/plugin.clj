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

(defn- install-checkouts [f & args]
  (let [project (first args)
        state-file (io/file (:root project) ".lein-auto-install")
        last-state (when-let [c (slurp-if-exists state-file)] (read-string c))
        current-state
        (for-map [checkout (.listFiles (io/file (:root project) "checkouts"))]
          (.getName checkout)
          (digest/md5 (slurp (io/file checkout "project.clj"))) )]
    (with-programs [lein]
      (doseq [checkout (keys current-state)]
        (when (not= (current-state checkout) (get last-state checkout))
          (lein "install"
                {:out *out*, :err *err*
                 :dir (io/file (:root project) "checkouts" checkout)})
          (flush))))
    (when (not= last-state current-state)
      (spit state-file (pr-str current-state))))
  (apply f args))

(defn hooks []
  (robert.hooke/add-hook #'leiningen.core.eval/eval-in-project
                         #'install-checkouts))
