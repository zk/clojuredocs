(ns clojuredocs.http)

(defn html-resp [body]
  {:headers {"Content-Type" "text/html;charset=utf-8"}
   :body body})
