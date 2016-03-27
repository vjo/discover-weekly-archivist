(ns discover-weekly-archivist.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clj-spotify.core :as sptfy]
            [clj-time.core :as t]
            [clj-time.periodic :as p]
            [clj-time.format :as f])
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
  (let [{:keys [error snapshot_id]} (sptfy/add-tracks-to-a-playlist {:user_id user_id :playlist_id playlist_id :uris tracks} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    snapshot_id))

(defn create-playlist [user_id name public]
  (let [{:keys [error id]} (sptfy/create-a-playlist {:user_id user_id :name name :public public} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    id)) ; return the new playlist id

(defn get-discover-weekly-playlist-id [user_id]
  (let [{:keys [error items]} (sptfy/get-a-list-of-a-users-playlists {:user_id user_id :limit 50 :offset 0} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    (:id (first (filter #(= "Discover Weekly" (:name %)) items))))) ;; return the "Discover Weekly" playlist id

(defn get-playlist-tracks [playlist_id owner_id]
  (let [{:keys [error items]} (sptfy/get-a-playlists-tracks {:playlist_id playlist_id :owner_id owner_id :fields "items(track.uri)" :limit 50 :offset 0} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    (map :uri (map :track items)))) ; return list of tracks uri

;; XXX find something nicer
(defn find-current-monday []
  (let [monday 1 ; Monday is the first day of the week
        today (t/day-of-week (t/now))
        diff (- monday today)
        seq (take 2 (p/periodic-seq (t/now) (t/days diff)))]
    (nth seq 1)))

(defn create-playlist-name []
  (str (#(f/unparse (f/formatters :year-month-day) %) (find-current-monday)) " Discover Weekly")) ; return something like "2016-03-21 Discover Weekly"

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    ;; Execute program with options

    (def user_id "") ; Add your login here, see README
    (def dw_owner_id "spotifydiscover")

    (let [playlist-name (if (string? (:name options)) (:name options) (create-playlist-name))
          dw_playlist_id (get-discover-weekly-playlist-id user_id)
          dw_tracks (get-playlist-tracks dw_playlist_id dw_owner_id)
          new_playlist_id (create-playlist user_id playlist-name (:public options))
          snapshot_id (add-tracks-to-playlist user_id new_playlist_id dw_tracks)]
      (println "Success, snapshot_id:" snapshot_id))))