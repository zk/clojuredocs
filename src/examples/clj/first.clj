(def scenes [{:subject  "Frankie"
              :action   "say"
              :object   "relax"}

             {:subject  "Lucy"
              :action   "❤s"
              :object   "Clojure"}

             {:subject  "Rich"
              :action   "tries"
              :object   "a new conditioner"}])


(println "People:" (->> scenes
                        (map :subject)
                        (interpose ", ")
                        (reduce str)))

;;=> People: Frankie, Lucy, Rich
