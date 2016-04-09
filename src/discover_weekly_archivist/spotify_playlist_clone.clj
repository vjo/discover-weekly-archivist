(ns discover-weekly-archivist.spotify-playlist-clone
    (:require [clj-spotify.core :as sptfy]))

(defn error-sptfy [error]
  (str "[Spotify API] Error " (:status error) ": " (:message error)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn add-tracks-to-playlist [user-id playlist-id tracks token]
  (let [{:keys [error snapshot_id]} (sptfy/add-tracks-to-a-playlist {:user_id user-id :playlist_id playlist-id :uris tracks} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    snapshot_id))

(defn create-playlist [user-id name public? token]
  (let [{:keys [error id]} (sptfy/create-a-playlist {:user_id user-id :name name :public public?} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    id))

(defn get-playlist-to-clone [user-id playlist-name-to-clone token]
      (println "get playlist")
  (let [{:keys [error items]} (sptfy/get-a-list-of-a-users-playlists {:user_id user-id :limit 50 :offset 0} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    (let [{:keys [id owner]} (first (filter #(= playlist-name-to-clone (:name %)) items))
          owner-id (:id owner)]
      {:playlist-id-to-clone id
       :playlist-owner-id-to-clone owner-id})))

(defn get-playlist-tracks [playlist-id owner-id token]
  (let [{:keys [error items]} (sptfy/get-a-playlists-tracks {:playlist_id playlist-id :owner_id owner-id :fields "items(track.uri)" :limit 50 :offset 0} token)]
    (cond
      error (exit 1 (error-sptfy error)))
    (map :uri (map :track items))))

(defn do-clone [user-id, token, playlist-name-to-clone, playlist-name-new, public?]
  (let [{:keys [playlist-id-to-clone playlist-owner-id-to-clone]} (get-playlist-to-clone user-id playlist-name-to-clone token)
        tracks (get-playlist-tracks playlist-id-to-clone playlist-owner-id-to-clone token)
        playlist-id-new (create-playlist user-id playlist-name-new public? token)
        snapshot-id (add-tracks-to-playlist user-id playlist-id-new tracks token)]))