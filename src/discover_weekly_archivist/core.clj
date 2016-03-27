(ns discover-weekly-archivist.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clj-spotify.core :as sptfy])
  (:import [java.net URLEncoder])
  (:gen-class :main true))

(def cli-options
  [["-n" "--name NAME" "Optional: name of the new playlist."]
   ["-p" "--public" "Optional: make the new playlist public." :default false]
   ["-h" "--help" "Display help."]])

(def token
  "") ;; Add your token here, see README

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

(defn add-tracks-to-playlist [user_id playlist_id tracks]
  (def uris (URLEncoder/encode (string/join "," tracks)))
  (println uris) ;; problem here?

  (let [{:keys [error]} (sptfy/add-tracks-to-a-playlist {:user_id user_id :playlist_id playlist_id :uris uris} token)]
    (cond
      error (exit 1 (error-sptfy error)))))

(defn create-playlist [user_id name public]
  (let [{:keys [error id]} (sptfy/create-a-playlist {:user_id user_id :name name :public public} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    id)) ;; return the new playlist id

(defn get-discover-weekly-playlist-id [user_id]
  (let [{:keys [error items]} (sptfy/get-a-list-of-a-users-playlists {:user_id user_id :limit 50 :offset 0} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    (:id (first (filter #(= "Discover Weekly" (:name %)) items))))) ;; return the "Discover Weekly" playlist id

(defn get-playlist-tracks [playlist_id owner_id]
  (let [{:keys [error items]} (sptfy/get-a-playlists-tracks {:playlist_id playlist_id :owner_id owner_id :fields "items(track.uri)" :limit 50 :offset 0} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    (map :uri (map :track items)))) ;; return list of tracks uri

(defn now [] (new java.util.Date))

(defn create-playlist-name []
  "yolo") ;; this could be "2016-01-01 DW" with `2016-01-01` beeing the date of the Monday of the week when the script run

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    ;; Execute program with options

    (def playlist-name (if (string? (:name options)) (:name options) (create-playlist-name)))
    (println "Public:" (:public options) " - Name:" playlist-name)

    (def user_id "") ;; Add your login here, see README
    (def dw_owner_id "spotifydiscover")

    (def dw_playlist_id (get-discover-weekly-playlist-id user_id))
    (println "\"Discover Weekly\" playlist id:" dw_playlist_id)

    (def dw_tracks (get-playlist-tracks dw_playlist_id dw_owner_id))
    (println "Tracks:" dw_tracks)

    (def new_playlist_id (create-playlist user_id playlist-name (:public options)))
    (println "New playlist id:" new_playlist_id)

    (add-tracks-to-playlist user_id new_playlist_id dw_tracks)
    ))