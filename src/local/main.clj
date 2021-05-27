(ns local.main
  (:require
   [clojure-lsp.main :as main]
   [clojure-lsp.db :as db]
   [taoensso.timbre :as log]
   [nrepl.server :as nrepl-server]
   [cider.nrepl :refer (cider-nrepl-handler)]))

(defn -main [& args]
  (let [{:keys [port]} (nrepl-server/start-server :handler cider-nrepl-handler
                                                  :port 7888)]
    (log/info "====== LSP CIDER nrepl server started on port" port)
    (swap! db/db assoc :port port))
  (apply main/-main args))
