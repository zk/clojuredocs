;; Let's define some data using list / map
;; literals:

(def scenes [{:subject  "Frankie"
              :action   "say"
              :object   "relax"}

             {:subject  "Lucy"
              :action   "❤s"
              :object   "Clojure"}

             {:subject  "Rich"
              :action   "tries"
              :object   "a new conditioner"}])

;; Define a function
(defn people-in-scenes [scenes]
  (->> scenes
       (map :subject)
       (interpose ", ")
       (reduce str)))


;; Who's in our scenes?

(println "People:" (people-in-scenes scenes))

;;=> People: Frankie, Lucy, Rich
