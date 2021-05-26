(ns local.main
  (:require
   [clojure-lsp.main :as main]
   [nrepl.server :as nrepl-server]
   [cider.nrepl :refer (cider-nrepl-handler)]))

(defn -main [& args]
  (nrepl-server/start-server :port 7888
                             :handler cider-nrepl-handler)
  (apply main/-main args))
