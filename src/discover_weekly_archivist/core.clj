(ns discover-weekly-archivist.core
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-spotify-playlist-copier.core :as sptfy-playlist-copier])
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

(defn exit [status msg]
  (println msg)
  (System/exit status))

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
        {:keys [new-playlist-id]} (sptfy-playlist-copier/do-copy :user-id user-id
                                                                 :token token
                                                                 :playlist-name-to-copy "Discover Weekly"
                                                                 :playlist-name-new playlist-name
                                                                 :public? public?)]
    (println (str "Success: https://open.spotify.com/user/" user-id "/playlist/" new-playlist-id))))

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
