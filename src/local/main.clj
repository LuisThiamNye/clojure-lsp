(ns local.main
  (:require
   [clojure-lsp.main :as main]
   [clojure-lsp.db :as db]
   [clojure-lsp.logging :as logging]
   [taoensso.timbre :as log]
   [nrepl.server :as nrepl-server]
   [cider.nrepl :refer (cider-nrepl-handler)]))

(defn -main [& args]
  (let [{:keys [port]} (nrepl-server/start-server :handler cider-nrepl-handler
                                                  :port 7888)]
    (log/info "====== LSP CIDER nrepl server started on port" port)
    (swap! db/db assoc
           :port port
           :log-path "/Users/luis/Desktop/lsp-log.txt"))
  (logging/update-log-path "/Users/luis/Desktop/lsp-log.txt")
  (apply main/-main args))
