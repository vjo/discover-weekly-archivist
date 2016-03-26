(defproject discover-weekly-archivist "0.1.0-SNAPSHOT"
  :description "Archive your Spotify Descover Weekly playlists"
  :url "https://github.com/vjo/discover-weekly-archivist"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot discover-weekly-archivist.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
