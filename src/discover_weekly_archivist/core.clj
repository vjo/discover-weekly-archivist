(ns discover-weekly-archivist.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clj-spotify.core :as sptfy])
  (:gen-class :main true))

(def cli-options
  [["-n" "--name NAME" "Optional: name of the new playlist."]
   ["-p" "--public" "Optional: make the new playlist public." :default false]
   ["-h" "--help" "Display help."]])

(def token
  "BQA9-dNJhR3VQl0CeGxTr9PxJZw8_s9yCj_Xa11-Xd1O2GX90XwWkKnXnkWEy2tu5a0qxzfI2Yyx4nQ61Hqdn4jg1UHVYhjwnwIDj-av8RKtJb6Zu8dX1nKUhATuzWfg-nn6xnIt9syrDp-hKtdcBP20e-cS9GsKL1AHEVMdysTv2yj-wIr7ZN8IBd4MaghEbAGncdFwDyCK6mBw7mrk3ZGh4ZCiDboj3Hrc1m0GAP5e2xp7Yy5y3_vColt7hD4TSk1Msei5_bopMlo4DXpUjD5MQ5QpcDZgeRlgopI")

(defn usage [options-summary]
  (->> ["discover-weekly-archivist will create a new Spotify playlist with the content of your \"Discover Weekly\" playlist."
        ""
        "Usage: discover-weekly-archivist [options]"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn error-sptfy [error]
  (str "[Spotify API] Error " (:status error) ": " (:message error)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn create-playlist [user_id name public]
  (let [{:keys [error id]} (sptfy/create-a-playlist {:user_id user_id :name name :public public} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    id)) ;; return the new playlist id

(defn find-current-discover-weekly-playlist [user_id]
  (let [{:keys [error items]} (sptfy/get-a-list-of-a-users-playlists {:user_id user_id :limit 50 :offset 0} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    (:id (first (filter #(= "Discover Weekly" (:name %)) items))))) ;; return the "Discover Weekly" playlist id

(defn now [] (new java.util.Date))

(defn create-playlist-name []
  "yolllloooooooo") ;; this could be "2016-01-01 DW" with `2016-01-01` beeing the date of the Monday of the week when the script run

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    ;; Execute program with options

    (def playlist-name (if (string? (:name options)) (:name options) (create-playlist-name)))
    (println "Public:" (:public options) " - Name:" playlist-name)

    (def user_id "y3ty")

    (def dw_playlist_id (find-current-discover-weekly-playlist user_id))
    (println "\"Discover Weekly\" playlist id:" dw_playlist_id)

    (def new_playlist_id (create-playlist user_id playlist-name (:public options)))
    (println "New playlist id:" new_playlist_id)
    ))