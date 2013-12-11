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


;; Who's in our scenes?

(println "People:" (->> scenes
                        (map :subject)
                        (interpose ", ")
                        (reduce str)))

;;=> People: Frankie, Lucy, Rich
