(ns clojuredocs.css
  (:require [garden.stylesheet :refer [at-media]]
            [nsfw.css :as nc]))

(def blue "rgba(66, 139, 202, 1)")

(def light-blue "rgba(66, 139, 202, 0.8)")

(def lightest-blue "rgba(66, 139, 202, 0.1)")

(def light-black "rgba(0,0,0,0.05);")

(def code-bg "rgba(0,0,0,0.01)")

(def app
  [[:html :body {:-webkit-font-smothing 'antialiased
                 :height "100%"
                 :font-family "\"Helvetica Neue\", Helvetica, sans-serif"
                 :color "#444"}]
   [:body {:font-size "16px"
           :line-height "1.5em"
           :transition "all 0.2s ease-in"
           :height "100%"}
    [:&.search-active {:background-color light-black}]]
   [:img {:max-width "100%"}]
   [:h1 :h2 :h3 :h4 :h5 :h6 {:font-weight 'normal}]
   [:h1 {:font-size "28px"
         :line-height "36px"
         :margin-bottom "20px"}
    [:&:first-of-type {:margin-top 0}]]
   (at-media {:max-width "767px"}
     [:h1 {:font-size "20px"
           :line-height "30px"}])

   [:h2 {:font-size "24px"
         :line-height "34px"
         :margin-bottom "20px"}]
   [:h5 {:font-size "15px"
         :font-weight 500
         :text-transform 'uppercase
         :letter-spacing "1px"
         :color "#777"
         :margin-bottom "10px"}]
   [:p {:font-size "17px"
        :margin-bottom "1.4em"
        :line-height "1.6em"}]
   [:li {:margin-bottom "0.5em"
         :list-style-type 'none}]
   [:pre {:border-radius "0px"
          :border 'none
          :word-wrap 'normal}]
   [:ul {:padding 0}]
   [:section {:margin-bottom "40px"}]
   [:code {:font-size "14px"}]
   ["a > code" {:color blue}]
   [:.badge {:background-color #_"rgba(0,0,0,0.05)" 'transparent
             :color "rgba(0,0,0,0.2)"
             :border-radius "3px"
             :padding "1px 5px"
             :font-weight 500
             :margin-left "5px"
             :font-size "14px"
             :line-height "22px"}]

   ;; check
   [:.sticky-wrapper {:min-height "100px"
                      :height ["auto !important" "100%"]}]
   [:footer :.sticky-push {:height "200px"}]
   [:footer
    {:text-align 'center
     :font-size "12px"
     :padding-top "130px"}
    [:.divider {:font-size "16px"}]
    [:.ctas {:line-height "30px"}
     [:iframe {:margin-bottom "-6px"
               :margin-left "10px"}]
     [:.gh-starred-count {:width "95px"}]]
    [:.left {:text-align 'right}]
    [:.right {:text-align 'left}]

    (nc/at-bp :xs
      [:.left :.right {:text-align 'center}])]

   [:.avatar
    {:display 'inline-block
     :border-radius "3px"
     :width "48px"
     :height "48px"}]
   [:body.user-page
    [:.user-avatar
     [:.avatar {:width "200px"
                :height "200px"}]]]
   [:.form-group
    [:.loading
     {:margin-top "9px"
      :margin-bottom "10px"}]
    [:.error-message
     {:margin-top "5px"
      :margin-right "10px"
      :padding "10px"}
     [:i {:margin-right "8px"}]]]

   [:header.navbar
    {:border-radius 0
     :margin-bottom "20px"}
    (nc/at-bp :xs {:margin-bottom "10px"})
    [".nav > li > a" {:padding "15px 12px"}]
    [:i {:margin-right "5px"}]
    (nc/at-bp :xs [:.navbar-brand
                     {:float 'none
                      :display 'inline-block
                      :padding-left 0}])
    [:.btn.mobile-menu {:border 'none
                        :margin-top "5px"
                        :font-size "18px"
                        :display 'none}
     [:i {:margin 0}]]
    [:.user-area {:font-size "14px"
                  :color "#999"}]
    [:.navbar-nav {:padding-right 0
                   :margin 0}
     [:li {:margin-bottom 0
           :font-size "14px"}]]
    [:.quick-search-widget
     [:form
      {:margin-right "10px"}]]
    [:.gh-starred-count
     {:margin-top "15px"
      :line-height "20px"
      :margin-left "10px"}]
    [:.clojure-version
     {:font-weight 300
      :font-size "12px"
      :font-family "Monaco, Menlo, Consolas, \"Courier New\", monospace"
      :color "#ccc"
      :line-height "21px"}
     [:a {:line-height "20px"
          :color "#ccc"
          :font-weight 300}]]]
   [:.mobile-nav-bar {:display 'none}]
   [:.navbar-nav
    [:i {:margin-right "5px"}]
    [:.avatar {:width "22px"
               :height "22px"
               :margin-right "5px"}]]
   [:.mobile-nav-menu
    {:display 'none
     :overflow-y 'scroll
     :overflow-x 'hidden
     :-webkit-overflow-scrolling 'touch
     :height "100%"}]
   [:.desktop-side-nav
    [:.badge
     {:float 'right}]]

   [:.mobile-nav-menu
    [:.badge
     {:font-size "16px"
      :color "rgba(255,255,255,0.5)"
      :font-weight 300
      :float 'right
      :line-height "22px"
      :margin 0
      :padding 0}]]
   (nc/at-bp :xs
     [:header.navbar
      [:.btn.mobile-menu
       {:display 'block
        :margin-right "-11px"}]
      [:.quick-search-widget {:display 'none}]
      [:.navbar-nav {:display 'none}]]
     [:.mobile-push-wrapper
      (merge
        {:right 0
         :position 'relative
         :padding-top "60px"
         :transform "translate3d(0,0,0)"}
        (nc/transition "all 0.2s ease"))]
     [:.desktop-nav-bar :.desktop-side-nav {:display 'none}]
     [:.mobile-nav-bar
      (nc/transition "all 0.2s ease")
      {:background-color 'white
       :position 'fixed
       :top 0
       :width "100%"
       :z-index 1000
       :display 'block}
      [:header {:margin-bottom 0}]]
     [:.mobile-nav-menu
      (nc/transition "all 0.2s ease")
      {:transform "translate3d(200px,0,0)"}
      {:display 'block
       :position 'fixed
       :width "200px"
       :z-index 10000
       :background-color "#47a3da"
       :color 'white
       :font-weight 300
       :top 0
       :bottom 0
       :right 0
       :margin 0
       :overflow-y 'scroll}
      [:h4 {:padding "5px 10px"
            :border-bottom "solid rgba(255,255,255,0.3) 1px"
            :margin 0
            :color "rgba(255,255,255,0.8)"
            :font-size "14px"
            :letter-spacing "1px"
            :font-weight 500}
       [:i {:margin-right "5px"}]]
      [:li {:margin 0}
       [:a {:color 'white}
        [:&:hover {:background-color "#258ecd"}]]]
      [:.navbar-nav {:margin 0}]]
     [:.mobile-push-wrapper.mobile-push
      (nc/transition "all 0.2s ease")
      {:transform "translate3d(-200px,0,0)"
       :right 0}]
     [:.mobile-nav-menu.mobile-push
      (nc/transition "all 0.2s ease")
      {:transform "translate3d(0,0,0)"}
      [:header.navbar
       (nc/transition "all 0.2s ease")
       {:right "200px"}]]
     [:.mobile-nav-bar.mobile-push
      (nc/transition "all 0.2s ease")
      {:transform "translate3d(-200px,0,0)"}]
     [:.page-toc {:display 'none}]
     [:body.search-active {:background-color 'transparent}]
     [:.clojure-version {:font-weight 300
                         :color "rgba(255,255,255,0.7)"
                         :font-family "Monaco, Menlo, Consolas, \"Courier New\", monospace"
                         :margin-left "5px"
                         :font-size "10px"}])
   ])
