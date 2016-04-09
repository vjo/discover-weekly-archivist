(ns discover-weekly-archivist.core
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [clj-spotify.core :as sptfy]
            [clj-time.core :as t]
            [clj-time.format :as f])
  (:gen-class :main true))

(def cli-options
  [["-t" "--token TOKEN" "Required: Spotify API token."]
   ["-l" "--login LOGIN" "Required: Spotify login."]
   ["-n" "--name NAME" "Optional: name of the new playlist."]
   ["-p" "--public" "Optional: make the new playlist public." :default false]
   ["-h" "--help" "Display help."]])

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

(defn add-tracks-to-playlist [user-id playlist-id tracks token]
  "Add a list of tracks to a playlist and return a snapshot_id"
  (let [{:keys [error snapshot_id]} (sptfy/add-tracks-to-a-playlist {:user_id user-id :playlist_id playlist-id :uris tracks} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    snapshot_id))

(defn create-playlist [user-id name public? token]
  "Create a new playlist and return its id"
  (let [{:keys [error id]} (sptfy/create-a-playlist {:user_id user-id :name name :public public?} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    id))

(defn get-discover-weekly-playlist [user-id token]
  "Return Discover Weekly playlist id and owner from a user-id"
  (let [{:keys [error items]} (sptfy/get-a-list-of-a-users-playlists {:user_id user-id :limit 50 :offset 0} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    (let [{:keys [id owner]} (first (filter #(= "Discover Weekly" (:name %)) items))
          owner-id (:id owner)]
      {:dw-playlist-id id
       :dw-playlist-owner-id owner-id})))

(defn get-playlist-tracks [playlist-id owner-id token]
  "Return list of tracks URI from a playlist-id and owner-id"
  (let [{:keys [error items]} (sptfy/get-a-playlists-tracks {:playlist_id playlist-id :owner_id owner-id :fields "items(track.uri)" :limit 50 :offset 0} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    (map :uri (map :track items))))

(defn get-monday-from-week [date]
  "Return Monday from a day of a week"
  (let [monday 1 ; Monday is the first day of the week
        today (t/day-of-week date)
        diff (- monday today)]
    (t/plus date (t/days diff))))

(defn create-playlist-name []
  "Create a playlist name based on current week's Monday
   e.g. \"2016-03-21 Discover Weekly\"
  "
  (str (#(f/unparse (f/formatters :year-month-day) %) (get-monday-from-week (t/now))) " Discover Weekly"))

(defn do-transfer [user-id, token, name, public?]
  (let [playlist-name (if (not (nil? name)) name (create-playlist-name))
        {:keys [dw-playlist-id dw-playlist-owner-id]} (get-discover-weekly-playlist user-id token)
        dw-tracks (get-playlist-tracks dw-playlist-id dw-playlist-owner-id token)
        new-playlist-id (create-playlist user-id playlist-name public? token)
        snapshot-id (add-tracks-to-playlist user-id new-playlist-id dw-tracks token)]
    (println "Success, snapshot-id:" snapshot-id)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      (nil? (:token options)) (exit 1 (error-msg ["Token argument is required."]))
      (nil? (:login options)) (exit 1 (error-msg ["Login argument is required."]))
      errors (exit 1 (error-msg errors)))

    (do-transfer
      (:login options)
      (:token options)
      (:name options)
      (:public options))))