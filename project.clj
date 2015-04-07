(defproject lein-auto-install "0.1.0"
  :description "A leiningen plugin to automatically run install for checkouts."
  :url "https://github.com/hyperscience/lein-auto-install"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[digest "1.4.4"]
                 [prismatic/plumbing "0.4.1"]
                 [me.raynes/conch "0.8.0"]]
  :eval-in-leiningen true)
