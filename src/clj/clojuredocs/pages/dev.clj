(ns clojuredocs.pages.dev
  (:require [compojure.core :refer [defroutes GET]]
            [clojuredocs.pages.common :as common]
            [clojuredocs.pages.quickref :as quickref]))

(defn section [title & body]
  [:secton
   [:h2 title]
   body])

(defn $section [{:keys [title nav-target content]}]
  [:section.styleguide-section {:id nav-target}
   [:h2 title]
   (vec (concat [:div.section-content] content))])

(defn $nav [sections]
  [:div
   {:data-sticky-offset "20"}
   [:h5 "Styleguide"]
   [:ul
    [:li [:h6 [:a {:href "#"
                   :data-animate-scroll "true"} "Top"]]]
    (for [{:keys [nav-target title]} sections]
      [:li
       [:h6
        [:a {:href (str "#" nav-target)
             :data-animate-scroll "true"
             :data-animate-buffer "20"} title]]])]])

(def styleguide-sections
  [{:title "Bootstrap"
    :nav-target "bootstrap-overrides"
    :content
    [[:p
      "ClojureDocs is built on, among other things, the amazing "
      [:a {:href "https://getbootstrap.com"} "Bootstrap framework"]
      ". In this section, you'll find the ways we've overridden the Bootstrap defaults."]
     [:section.headers-ex
      [:h3 "Headers"]
      [:h1 "h1. Heading 1 " [:small "With small"]]
      [:h2 "h2. Heading 2 " [:small "With small"]]
      [:h3 "h3. Heading 3 " [:small "With small"]]
      [:h4 "h4. Heading 4 " [:small "With small"]]
      [:h5 "h5. Heading 5 " [:small "With small"]]
      [:h6 "h6. Heading 6 " [:small "With small"]]]
     [:section.buttons-ex
      [:h3 "Buttons"]
      [:button.btn.btn-default "Default"]
      [:button.btn.btn-primary "Primary"]
      [:button.btn.btn-success "Success"]
      [:button.btn.btn-info "Info"]
      [:button.btn.btn-warning "Warning"]
      [:button.btn.btn-danger "Danger"]]
     [:section.contextual-bgs
      [:h3 "Contextual Backgrounds"]
      [:div.bg-primary "Primary"]
      [:div.bg-success "Success"]
      [:div.bg-info "Info"]
      [:div.bg-warning "Warning"]
      [:div.bg-danger "Danger"]]
     [:section.forms-ex
      [:h3 "Forms"]
      [:form {:role "form"}
       [:div.form-group
        [:label {:for "email"} "Email"]
        [:input.form-control {:type "email" :id "email" :placeholder "Enter email"}]]
       [:div.form-group
        [:label {:for "password"} "Password"]
        [:input.form-control {:type "password" :id "password" :placeholder "Password"}]]
       [:div.form-group
        [:div.checkbox
         [:label
          [:input {:type "checkbox"}]
          "Check me out"]]]
       [:button.btn.btn-default {:type "submit"} "Submit"]]]]}
   {:title "Common Elements"
    :nav-target "common-elements"
    :content
    [[:p "Null-state plate, shown where there's nothing of something."
      [:div.example.checker-bg
       [:div.null-state
        "We don't have any of those!"]]]
     [:p "Namespace nav tree, nests namespaces to save on horizontal space. Namespaces are linked, non-namespace bridge parts (e.g. "
      [:code "clojure"]
      ", "
      [:code "java"]
      ") are unlinked."]
     [:div.example.checker-bg
      (common/$namespaces ["clojure.core"
                           "clojure.java.shell"
                           "clojure.test"
                           "clojure.test.junit"
                           "clojure.test.tap"
                           "clojure.zip"])]]}
   (let [sphere '{:title "Simple Values",
                  :categories
                  ({:title "Regular Expressions",
                    :groups
                    ({:syms (re-pattern re-matcher), :title "Create"}
                     {:syms (re-find re-matches re-seq re-groups), :title "Use"})})}]
     {:title "Quick Reference"
      :nav-target "quickref"
      :content
      [[:div.example.checker-bg
        (quickref/$toc [sphere])]
       [:div.example.checker-bg
        (quickref/$sphere sphere)]]})
   {:title "Quick Search"
    :nav-target "quick-search"
    :content
    [[:p "The main search widget on the home page. This should feel immediately accessible and get users where they want to go, fast."]
     [:div.example.checker-bg.sg-quick-lookup]
     [:p "When loading autocomplete"]
     [:div.example.checker-bg.sg-quick-lookup-loading]
     [:p "Null state"]
     [:div.example.checker-bg.sg-quick-lookup-null-state]
     [:p "Landing search w/ autocomplete: heterogenous results linking to vars, namespaces, and concept pages."]
     [:div.example.checker-bg.sg-quick-lookup-autocomplete]]}
   {:title "Examples"
    :nav-target "examples"
    :content
    [[:p "Null state for " [:code "clojure.core/map"]]
     [:div.example.checker-bg
      [:div.sg-examples-null-state]]
     [:p "Single example:"]
     [:div.example.checker-bg
      [:div.sg-examples-single]]
     [:p "Examples of varying length:"]
     [:div.example.checker-bg
      [:div.sg-examples-lengths]]
     [:p "Adding an example:"]
     [:div.example.checker-bg.sg-add-example]
     [:p "Loading:"]
     [:div.example.checker-bg.sg-add-example-loading]
     [:p "With general error:"]
     [:div.example.checker-bg.sg-add-example-errors]]}
   {:title "See Alsos"
    :nav-target "see-alsos"
    :content
    [[:p "Null state"]
     [:div.example.checker-bg.sg-see-alsos-null-state]
     [:p "Populated"]
     [:div.example.checker-bg.sg-see-alsos-populated]
     [:p "Add new"]
     [:div.example.checker-bg.sg-add-see-also]]}
   {:title "Notes"
    :nav-target "notes"
    :content
    [[:p "Null state:"]
     [:div.example.checker-bg.sg-notes-null-state]
     [:p "Populated with examples:"]
     [:div.example.checker-bg.sg-notes-populated]
     [:p "Adding a note:"]
     [:div.example.checker-bg.sg-add-note]]}])

(defn $tpl [{:keys [body-class user page-uri nav content]}]
  (common/$main
    {:body-class "dev-page"
     :user user
     :page-uri page-uri
     :content
     [:div.row
      [:div.col-md-2
       [:div.sidenav
        [:section
         [:h5 "Dev"]
         [:ul
          [:li [:a {:href "/dev/styleguide"} "Styleguide"]]
          [:li [:a {:href "/dev/search-perf"} "Search Perf"]]
          [:li [:a {:href "/dev/canary"} "Canary Tests"]]]]
        nav]]
      [:div.col-md-10
       content]]}))

(defn styleguide-handler [{:keys [user uri]}]
  ($tpl
    {:user user
     :page-uri uri
     :nav ($nav styleguide-sections)
     :content
     [:div
      [:h1 "Styleguide"]
      [:p.lead "Here you'll find various UI elements used on the ClojureDocs site. This styleguide is designed to help you see how changes will the vairous states of our UI elements when making changes."]
      (map $section styleguide-sections)]}))

(defn perf-handler [{:keys [user uri]}]
  ($tpl
    {:user user
     :page-uri uri
     :content
     [:div
      [:h1 "Search Performance"]]}))

(defn canary-tests-handler [{:keys [user uri]}]
  ($tpl
    {:user user
     :page-uri uri
     :content
     [:div [:h1 "Canary"]]}))
