(ns clojuredocs.css
  (:require [garden.stylesheet :refer [at-media]]
            [nsfw.css :as nc]))

(defn light-dark
  [light dark]
  (str "light-dark(" light ", " dark ")"))

(defn mix
  "`color` at `pct`% opacity."
  [color pct]
  (str "color-mix(in srgb, " color " " pct "%, transparent)"))

(def root-vars
  [":root"
   {:color-scheme "light dark"

    :--text (light-dark "#333" "#dde0e2")
    :--text-secondary (light-dark "#555" "#b2b8bc")
    :--text-muted (light-dark "#888" "#8f959b")
    :--text-faint (light-dark "#aaa" "#686e73")
    :--border (light-dark "#ccc" "#484d52")
    :--border-subtle (light-dark "#eee" "#2e3235")
    :--bg-subtle (light-dark "#fafafa" "#202325")
    :--bg-hover (light-dark "#eee" "#2e3235")

    :--rgb-white "255,255,255"
    :--rgb-black "0,0,0"

    ;; Surfaces
    :--bg (light-dark "#fff" "#181a1b")
    :--bg-input (light-dark "#fff" "#202325")
    :--border-strong (light-dark "#adadad" "#5a6065")
    :--color-shade (light-dark "#000" "#fff")
    :--color-on-solid "#fff"

    :--color-brand (light-dark "#428bca" "#6fa8dc")
    :--color-brand-hover (light-dark "#2a6496" "#9cc3e8")
    :--color-brand-soft (mix "var(--color-brand)" 80)
    :--color-brand-muted (mix "var(--color-brand)" 70)
    :--color-brand-nav-bg (light-dark "#47a3da" "#2b6f9a")
    :--color-brand-nav-hover (light-dark "#258ecd" "#235c80")

    :--color-warning-text (light-dark "#856404" "#ffd966")
    :--color-warning-bg (light-dark "#fff3cd" "rgba(255,193,7,0.12)")
    :--color-warning-border (light-dark "#ffc107" "#8a6d1a")
    :--color-warning-border-soft (light-dark "#ffeaa0" "#6b5614")
    :--color-test-pass (light-dark "#5a5" "#3f8f3f")
    :--color-test-fail (light-dark "#a55" "#a04848")
    :--color-test-error (light-dark "#555" "#666")
    :--color-test-pending (light-dark "#999" "#777")
    :--color-error-bg (light-dark "#fcc" "#5a2a2a")
    :--color-checker (light-dark "#f0f0f0" "#262a2c")
    :--color-highlight "#ffa500"

    :--color-code-text (light-dark "#c7254e" "#ee8aa3")
    :--color-code-bg (light-dark "#f9f2f4" "rgba(255,255,255,0.08)")
    :--color-close (light-dark "#000" "#fff")
    :--color-close-shadow (light-dark "#fff" "transparent")

    :--color-logo-bg (light-dark "transparent" "#fff")

    ;; Clojure syntax
    :--syntax-comment (light-dark "#008200" "#6dbb6d")
    :--syntax-string (light-dark "#2d2dfe" "#8d9bff")
    :--syntax-keyword (light-dark "#77f" "#a0a0ff")
    :--syntax-builtin (light-dark "#687868" "#9fb09f")
    :--syntax-def (light-dark "#55c" "#9d9dee")
    :--syntax-paren-0 (light-dark "#272" "#7ec27e")
    :--syntax-paren-1 (light-dark "#940" "#d99a5e")
    :--syntax-paren-2 (light-dark "#44a" "#8c8cdf")

    :--color-banner-staging (light-dark "rgb(201,48,44)" "rgb(160,40,37)")
    :--color-banner-survey (light-dark "rgb(91,164,207)" "rgb(52,112,150)")
    :--color-banner-header (light-dark "#3498db" "#2b78ad")

    :--space-2xs "5px"
    :--space-xs "10px"
    :--space-sm "15px"
    :--space-md "20px"
    :--space-lg "30px"
    :--space-xl "40px"

    :--font-size-2xs "10px"
    :--font-size-xs "12px"
    :--font-size-sm "13px"
    :--font-size-base "14px"
    :--font-size-md "15px"
    :--font-size-lg "16px"
    :--font-size-xl "20px"
    :--font-size-2xl "24px"}])

(def blue "var(--color-brand)")

(def light-blue "var(--color-brand-soft)")

(def light-black (str (mix "var(--color-shade)" 5) ";"))

(def code-bg (mix "var(--color-shade)" 1))

(def monospace-font "Monaco, Menlo, Consolas, \"Courier New\", monospace")

(defn arglists
  [extra]
  [:.arglists (merge {:margin-left 0
                      :padding-left 0
                      :font-family monospace-font
                      :font-size "var(--font-size-base)"}
                     extra)
   [:li {:margin 0
         :display 'inline-block
         :margin-right "var(--space-xs)"
         :margin-bottom "var(--space-xs)"
         :padding "0 5px"
         :background-color "var(--bg-subtle)"}]])

(defn dl-rows
  [{:keys [dt dd dd-md dd-sm xs-dd]}]
  [[:.dl-row {:clear 'both
              :padding "3px 0"}
    [:&:hover {:background-color "var(--bg-hover)"}]]
   [:dl {:font-size "var(--font-size-sm)"}]
   [:dt {:text-align 'right
         :margin-right "var(--space-xs)"
         :font-family monospace-font
         :font-weight 'normal}
    dt]
   [:dd nc/ellipsis-text dd
    (nc/at-bp :md dd-md)
    (nc/at-bp :sm dd-sm)]
   (nc/at-bp :xs
     [:.dl-row {:padding 0}]
     [:dt {:display 'block
           :text-align 'left
           :padding-top "var(--space-xs)"
           :clear 'both}]
     [:dd (merge {:display 'block
                  :border-bottom "solid var(--border) 1px"
                  :padding-bottom "var(--space-xs)"
                  :width "100%"}
                 xs-dd)])])

(def intro-page
  [[:.xkcd {:display 'block
            :margin-left 'auto
            :margin-right 'auto}]

   [:.intro {:margin-bottom "var(--space-lg)"}]
   [:.intro-page
    [:section
     {:margin-bottom "var(--space-xl)"}
     (nc/at-bp {:margin-bottom "var(--space-md)"})]
    [:.quick-lookup-wrapper
     {:background-color "var(--bg)"
      :margin "-30px -25px"
      :padding "30px 25px"}
     (nc/at-bp {:margin 0
                :padding 0})
     [:input.query
      {:font-size "var(--font-size-xl)"
       :padding "var(--space-xs) var(--space-sm)"}
      (nc/at-bp {:font-size "var(--font-size-lg)"})]]
    [:.code-example
     [:font-size "var(--font-size-base)"
      :padding "var(--space-md)"
      :border "solid transparent 1px"
      :background-color "var(--bg-subtle)"]]
    [:.row
     {:margin-bottom "0px"}]
    [:.top-contribs
     [:.avatar
      {:width "3.63%"
       :margin-right "0.53%"
       :margin-bottom "0.53%"
       :height 'auto}
      (nc/at-bp :xs
        {:width "7.80%"
         :height 'auto})]]
    [:.migrate-account
     {:text-align 'right
      :font-size "var(--font-size-xs)"}]
    [:.getting-started-resources
     {:margin-bottom "var(--space-md)"}
     [:li {:list-style-type 'disc
           :margin-left "var(--space-lg)"}]]
    [:.syntaxify
     {:padding "var(--space-sm)"
      :overflow 'auto
      :overflow-y 'hidden
      :font-family monospace-font
      :font-size "var(--font-size-md)"
      :line-height "1.3em"}
     (nc/at-bp :md {:font-size "var(--font-size-xs)"})
     (nc/at-bp :sm {:font-size "var(--font-size-sm)"})
     (nc/at-bp :xs {:font-size "var(--font-size-xs)"})
     [:table {:margin "0 auto"}]]
    [:ul
     [:h4
      [:i {:margin-right "12px"
           :margin-left "-30px"}]
      [:i.fa-map-marker {:padding "0 3px"}]]]
    [:.used-by
     [:ul {:text-align 'center}]
     [:li {:display 'inline-block
           :margin-right "var(--space-lg)"
           :margin-bottom "var(--space-md)"}]
     [:img {:max-width "200px"
            :max-height "50px"
            :background-color "var(--color-logo-bg)"
            :box-shadow "0 0 0 5px var(--color-logo-bg)"
            :border-radius "4px"}
      (nc/at-bp :md {:max-height "35px"})
      (nc/at-bp :sm {:max-height "50px"})]]
    [:.recently-updated
     {:margin-bottom "var(--space-2xs)"
      :font-size "var(--font-size-sm)"
      :color "var(--text-muted)"
      :line-height "17px"
      :clear 'both}
     [:.clear {:clear 'both}]
     [:.avatar-link
      {:margin-right "var(--space-xs)"
       :float 'left
       :display 'inline-block
       :padding-top "2px"}]]]
   [:.not-finding {:text-align 'right
                   :font-size "var(--font-size-xs)"
                   :line-height "18px"
                   :opacity 0.8
                   :margin-top "var(--space-2xs)"}]])

(def autocomplete
  [[:.ac-result-link:hover {:text-decoration 'none
                            :width "100%"}]

   [:.ac-result
    [:td {:padding "var(--space-xs)"}]
    [:.docstring {:color "var(--text)"}]]

   [:ul.ac-results
    {:margin-left 0
     :margin-bottom 0}
    [:li {:margin-left 0
          :list-style-type 'none
          :cursor 'pointer
          :padding "var(--space-xs)"
          :margin-top "var(--space-2xs)"
          :margin-bottom "var(--space-2xs)"
          :padding-bottom "var(--space-sm)"}
     [:&.highlighted {:background-color (mix "var(--color-shade)" 2)}]
     [:p {:margin-bottom 0}]
     [:i {:margin-left 0
          :color "var(--text-secondary)"
          :width "15px"
          :text-align 'right}]]]
   [:.ac-entry {:position 'relative}
    [:.ac-type
     {:position 'absolute
      :right 0
      :top 0
      :font-size "var(--font-size-2xs)"
      :text-transform 'uppercase
      :color "var(--text-faint)"
      :line-height "12px"}]
    [:p {:margin-bottom "var(--space-xs)"}]
    [:.see-alsos {:font-size "var(--font-size-base)"
                  :color "var(--text-faint)"}
     [:.see-also-label {:margin-right "var(--space-xs)"}]
     [:ul {:display 'inline-block}
      [:li {:margin 0
            :padding 0
            :margin-right "var(--space-xs)"
            :display 'inline-block}]]]]
   [:table.ac-results {:width "100%"
                       :font-size "var(--font-size-md)"}
    [:td {:padding-bottom "var(--space-md)"
          :vertical-align 'top}]
    [:.ac-result
     [:.name {:vertical-align 'top
              :margin-right "var(--space-xs)"
              :font-weight 'bold
              :white-space 'nowrap}]
     [:.docstring {:vertical-align 'top
                   :max-width "700px"}
      [:a {:display 'block
           :color "var(--text)"}]]]
    [:.ac-metadata
     {:font-weight 'normal
      :color "var(--text-muted)"
      :font-size "var(--font-size-xs)"
      :white-space 'nowrap}]]])

(def var-page
  [[:.var-header
    [:h1.var-name
     {:margin-bottom "var(--space-2xs)"}]
    [:.var-meta
     {:margin-bottom "var(--space-xs)"
      :font-size "var(--font-size-sm)"
      :line-height "1.3em"
      :color "var(--text-muted)"
      :text-align 'right}
     (nc/at-bp :xs {:text-align 'left})
     [:h2 {:margin-bottom "var(--space-2xs)"}]
     [:ul :li :.added
      {:margin-bottom "3px"}]
     [:li {:display 'inline-block}]]
    (arglists {})]

   [:.var-page :.example-page
    [:h1 :h2 {:margin-top 0}]
    [:pre {:border 'none}]
    [:section {:margin-bottom "2em"}]
    [:section.search {:margin-bottom "1em"}]
    [:.null-state {:margin-bottom "var(--space-lg)"}]
    [:.docstring
     [:pre {:background-color "var(--bg-subtle)"
            :color "var(--text-secondary)"
            :font-size "var(--font-size-md)"
            :line-height "1.4em"
            :margin-bottom 0
            :padding "var(--space-sm) var(--space-xs)"}
      (nc/at-bp :md {:font-size "var(--font-size-xs)"})
      (nc/at-bp :sm {:font-size "var(--font-size-sm)"})
      (nc/at-bp :xs {:font-size "var(--font-size-xs)"})]
     [:.copyright {:text-align 'right
                   :font-size "10px"
                   :color "var(--text-faint)"}
      [:a {:color "var(--text-faint)"}]]]
    [:.example {:margin-bottom "var(--space-lg)"}]]
   [:.example-meta :.note-meta
    {:font-size "10px"
     :margin-bottom 0
     :line-height "1.3em"
     :margin-right "var(--space-2xs)"
     :color "var(--text-muted)"}
    [:.avatar {:width "25px"
               :height "25px"
               :opacity 0.8}
     [:&:hover {:opacity 1}]]]
   [:.note {:margin-bottom "var(--space-lg)"}]
   [:.note-meta {:margin-bottom "var(--space-xs)"}
    [:.avatar {:margin "0 2px"}]]
   (let [r [:&
            {:font-size "var(--font-size-md)"}
            (nc/at-bp :md {:font-size "12.5px"})
            (nc/at-bp :sm {:font-size "11.5px"})]]
     [[:.var-example [:.syntaxify r]]
      [:.tabbed-editor
       [:.example-editor
        [:textarea.form-control r]
        [:.columns-guide r]]
       [:pre.syntaxify r]]])
   [:.var-example
    {:margin-bottom "var(--space-lg)"}
    [:&.highlighted
     {:border-top "solid var(--color-highlight) 4px"
      :border-bottom "solid var(--color-highlight) 4px"
      :padding-top "var(--space-2xs)"
      :padding-bottom "var(--space-2xs)"}]
    [:.example {:margin-bottom "var(--space-lg)"}]
    [:.syntaxify [:table {:margin 0}]]]
   [:.example-meta
    {:color "var(--text-faint)"}
    [:.avatar {:margin-right "var(--space-2xs)"}]
    [:.contributors :.created :.last-updated :.links
     {:display 'inline-block
      :margin-right "var(--space-2xs)"
      :margin-bottom "var(--space-2xs)"}]]
   [:.example-meta :.see-also
    :.delete-controls :.note-meta
    [:.loading {:width "9px"
                :margin-bottom "2px"}]
    [:.error-deleting
     {:padding-left "3px"
      :padding-right "3px"}]]
   [:.example-page
    [:.current-example
     {:border "solid rgba(255,243,109, 0.3) 5px"
      :padding "var(--space-xs)"
      :background-color 'transparent
      :margin-bottom "var(--space-md)"}]
    [:.example-meta
     [:.created {:display 'block}]]]
   [:.example-code
    {:margin-bottom "var(--space-sm)"
     :background-color "var(--bg-subtle)"
     :border-top "solid var(--border) 5px"}]
   [:.ns-tree
    {:font-size "var(--font-size-base)"}
    [:h3 {:font-size "var(--font-size-xs)"}]
    [:li {:margin-bottom 0}]
    ["li > ul" {:margin-left "var(--space-xs)"}]
    [:.current {:font-weight 'bold}]]
   [:.syntaxify [:* {:font-family monospace-font}]]
   [:.add-see-also
    [:.add-see-also-content
     {:border "solid var(--border-subtle) 1px"
      :padding "var(--space-md)"}]
    [:form {:margin-bottom "var(--space-xs)"}]
    [:input {:border-radius 0}]
    [:.error-message {:padding "var(--space-2xs)"}
     [:i {:margin-right "var(--space-2xs)"}]]
    [:.ac-results {:color "var(--text-secondary)"}]]
   [:.note-body
    {:background-color "var(--bg-subtle)"
     :padding "var(--space-xs)"
     :word-wrap 'normal}
    [:p {:margin-bottom "var(--space-xs)"}]]
   [:.add-example
    [:.add-example-content
     {:padding "var(--space-xs)"
      :background-color "var(--bg-subtle)"}]]
   [:.add-note
    [:.instructions
     {:color "var(--text-secondary)"
      :font-size "var(--font-size-xs)"}]
    [:textarea {:height "200px"}]]
   [:.examples-styleguide-content
    [:.syntaxify {:margin-bottom "var(--space-md)"
                  :background-color code-bg}]
    [:ul {:margin-bottom "var(--space-lg)"}]
    [:li {:list-style-type 'disc
          :margin-left "25px"}]]
   [:.dialect-badges
    {:display 'inline-block
     :margin-top "var(--space-2xs)"}
    [:.dialect-badge
     {:width "20px"
      :height "20px"
      :margin-right "4px"
      :vertical-align 'middle
      :transition "opacity 0.2s ease"}
     [:&.unsupported
      {:opacity 0.15
       :filter "grayscale(100%)"}]]]
   [:.implementation-badge
    {:display 'inline-block
     :margin-top "var(--space-2xs)"
     :padding "2px 8px"
     :font-size "11px"
     :font-weight 'bold
     :color "var(--color-warning-text)"
     :background-color "var(--color-warning-bg)"
     :border "1px solid var(--color-warning-border)"
     :border-radius "10px"}]
   [:.implementation-notice
    {:margin-bottom "var(--space-sm)"
     :padding "var(--space-xs) var(--space-sm)"
     :font-size "var(--font-size-sm)"
     :color "var(--color-warning-text)"
     :background-color "var(--color-warning-bg)"
     :border "1px solid var(--color-warning-border-soft)"
     :border-radius "4px"}]
   [:&.implementation-var
    [:.var-name {:opacity 0.7}]]])

(def quickref
  [[:.toc {:margin-bottom "var(--space-md)"}
    [:h5 {:margin 0
          :margin-bottom "var(--space-xs)"
          :font-size "var(--font-size-xs)"
          :margin-right 0
          :margin-top 0}]
    [:h6 {:margin-top 0}]
    [:ul {:margin-bottom "var(--space-2xs)"}]
    [:.toc-sphere
     [:h5 {:margin-bottom "var(--space-2xs)"}]
     [:ul {:padding 0
           :margin-bottom "var(--space-sm)"}]
     [:li {:margin-left "var(--space-xs)"
           :font-size "var(--font-size-xs)"
           :margin-bottom 0
           :line-height "1.5em"}]]]
   [:.quickref-mobile-toc
    [:h5 {:color "var(--color-on-solid)"
          :margin-bottom "var(--space-2xs)"
          :margin-top 0}]
    [:.categories {:font-size "var(--font-size-xs)"
                   :line-height "16px"}]]
   [:.sticky {:position 'fixed
              :overflow 'auto
              :bottom 0}]
   [:.sphere
    [:h2 :h3 :h4
     {:display 'inline-block
      :margin 0
      :margin-bottom "var(--space-xs)"}]
    [:h4 {:font-size "var(--font-size-base)"
          :font-weight 500}]
    [:.sphere-header
     {:border-bottom "solid var(--color-highlight) 3px"
      :margin-bottom "var(--space-xs)"}]
    [:.category-header
     {:border-bottom "solid var(--border) 2px"
      :margin-bottom "var(--space-xs)"}]
    [:.category
     {:margin-bottom "var(--space-xl)"}]
    [:.header-reference
     {:color "var(--text-faint)"
      :font-weight 'normal
      :float 'right
      :margin-right 0}]
    [:.quickref-header
     [:.header-reference
      {:color "var(--text-muted)"
       :font-size "var(--font-size-base)"}]]
    [:.examples-count
     {:color "var(--text-faint)"}]
    (dl-rows {:dt {:width "130px"}
              :dd {:width "550px"
                   :margin-left "150px"}
              :dd-md {:width "430px"}
              :dd-sm {:width "380px"}
              :xs-dd {:text-align 'left
                      :margin-left 0}})]
   [:.quickref-other
    {:display 'inline-block}
    [:li {:display 'inline-block}]]])

(def example-editor
  [[:.example-editor
    {:background-color "var(--bg)"
     :margin-bottom "var(--space-xs)"}
    [:&.disabled {:background-color "var(--bg-hover)"}]
    [:form {:margin-bottom "var(--space-md)"}]
    [:textarea {:font-family monospace-font
                :margin-bottom 0
                :padding "var(--space-xs)"
                :font-size "var(--font-size-sm)"}]
    [:.add-example-controls
     {:margin-bottom "var(--space-xs)"}]
    [:.add-example-content
     {:padding "var(--space-lg)"
      :background-color "var(--bg-subtle)"}]
    ]
   [:p.example-instructions
    {:margin-bottom "var(--space-xs)"
     :font-size "var(--font-size-xs)"
     :line-height "20px"
     :color "var(--text-muted)"}]])

(def styleguide-page
  [(let [color "var(--color-checker)"
         size 40]
     [:.checker-bg
      {:background-color "var(--bg-subtle)"
       :background-image (str "linear-gradient(45deg,"
                              color
                              " 25%,transparent 25%,transparent 75%,"
                              color " 75%," color "),"
                              "linear-gradient(45deg,"
                              color " 25%, transparent 25%, transparent 75%,"
                              color " 75%, " color ")")
       :background-size (str size "px " size "px")
       :background-position (str "0 0, "
                                 (/ size 2) "px"
                                 " "
                                 (/ size 2) "px")
       :padding "var(--space-md)"}])
   [:.styleguide-page
    [:.styleguide-section
     {:margin-bottom "50px"}
     ["> h2" {:border-bottom "solid var(--border) 1px"
                :padding-bottom "var(--space-2xs)"}]]
    [:.example
     {:margin-bottom "var(--space-lg)"}]
    [:.buttons-ex
     [:button
      {:margin-right "var(--space-xs)"}]]
    [:.contextual-bgs
     ["> div"
      {:display 'inline-block
       :padding "var(--space-xs) var(--space-md)"
       :margin-right "var(--space-xs)"}]]
    [:.headers-ex
     [:h1 :h2 :h3 :h4 :h5 :h6
      {:line-height "50px"
       :margin 0}]]]])

;; bootstrap.min.css has hardcoded colors, this overrides them
(def bootstrap-theme
  [[:a {:color "var(--color-brand)"}
    [:&:hover :&:focus {:color "var(--color-brand-hover)"}]]
   [:hr {:border-top-color "var(--border-subtle)"}]
   [:.text-muted {:color "var(--text-muted)"}]
   [:blockquote {:border-left-color "var(--border-subtle)"}]
   [:code {:color "var(--color-code-text)"
           :background-color "var(--color-code-bg)"}]
   [:pre {:color "var(--text)"
          :background-color "var(--bg-subtle)"
          :border-color "var(--border)"}]
   ["pre code" {:color 'inherit
                :background-color 'transparent}]
   [:.form-control {:color "var(--text-secondary)"
                    :background-color "var(--bg-input)"
                    :border-color "var(--border)"}]
   [".form-control::placeholder" {:color "var(--text-muted)"}]
   [".form-control[disabled]" ".form-control[readonly]"
    {:background-color "var(--bg-hover)"}]
   [:.btn-default {:color "var(--text)"
                   :background-color "var(--bg-input)"
                   :border-color "var(--border)"}]
   [".btn-default:hover" ".btn-default:focus" ".btn-default:active"
    ".btn-default.active"
    {:color "var(--text)"
     :background-color "var(--bg-hover)"
     :border-color "var(--border-strong)"}]
   [".nav > li > a:hover" ".nav > li > a:focus"
    {:background-color "var(--bg-hover)"}]
   [:.nav-tabs {:border-bottom-color "var(--border)"}]
   [".nav-tabs > li > a:hover"
    {:border-color "var(--border-subtle) var(--border-subtle) var(--border)"}]
   [".nav-tabs > li.active > a" ".nav-tabs > li.active > a:hover"
    ".nav-tabs > li.active > a:focus"
    {:color "var(--text-secondary)"
     :background-color "var(--bg)"
     :border-color "var(--border)"
     :border-bottom-color 'transparent}]
   [".table > thead > tr > th" ".table > tbody > tr > th"
    ".table > tfoot > tr > th" ".table > thead > tr > td"
    ".table > tbody > tr > td" ".table > tfoot > tr > td"
    {:border-top-color "var(--border)"}]
   [".table > thead > tr > th" {:border-bottom-color "var(--border)"}]
   [:.close {:color "var(--color-close)"
             :text-shadow "0 1px 0 var(--color-close-shadow)"}]
   [".close:hover" ".close:focus" {:color "var(--color-close)"}]])

(def syntax-theme
  [[:.tok-comment {:color "var(--syntax-comment)"}]
   [:.tok-string {:color "var(--syntax-string)"}]
   [:.tok-keyword {:color "var(--syntax-keyword)"}]
   [:.tok-builtin {:color "var(--syntax-builtin)"}]
   [:.tok-def {:color "var(--syntax-def)"}]
   [:.tok-paren-0 {:color "var(--syntax-paren-0)"}]
   [:.tok-paren-1 {:color "var(--syntax-paren-1)"}]
   [:.tok-paren-2 {:color "var(--syntax-paren-2)"}]])

(def app
  [root-vars

   nc/flex-defaults

   [:html :body {:-webkit-font-smoothing 'antialiased
                 :height "100%"
                 :font-family "\"Helvetica Neue\", Helvetica, sans-serif"
                 :color "var(--text)"
                 :background-color "var(--bg)"}]

   bootstrap-theme
   syntax-theme

   [:body {:font-size "16px"
           :line-height "1.5em"
           :transition "all 0.2s ease-in"
           :height "100%"}
    [:&.search-active {:background-color light-black}]]
   [:img {:max-width "100%"}]
   [:h1 :h2 :h3 :h4 :h5 :h6 {:font-weight 'normal}]
   [:h1 {:font-size "28px"
         :line-height "36px"
         :margin-bottom "var(--space-md)"}
    [:&:first-of-type {:margin-top 0}]]
   (at-media {:max-width "767px"}
     [:h1 {:font-size "var(--font-size-xl)"
           :line-height "30px"}])

   [:h2 {:font-size "var(--font-size-2xl)"
         :line-height "34px"
         :margin-bottom "var(--space-md)"}]
   [:h5 {:font-size "var(--font-size-md)"
         :font-weight 500
         :text-transform 'uppercase
         :letter-spacing "1px"
         :color "var(--text-muted)"
         :margin-bottom "var(--space-xs)"}]
   [:p {:font-size "17px"
        :margin-bottom "1.4em"
        :line-height "1.6em"}]
   [:li {:margin-bottom "0.5em"
         :list-style-type 'none}]
   [:pre {:border-radius "0px"
          :border 'none
          :word-wrap 'normal}]
   [:ul {:padding 0}]
   [:section {:margin-bottom "var(--space-xl)"}]
   [:code {:font-size "var(--font-size-base)"}]
   ["a > code" {:color blue}]
   [:.badge {:background-color 'transparent
             :color (mix "var(--color-shade)" 20)
             :border-radius "3px"
             :padding "1px 5px"
             :font-weight 500
             :margin-left "var(--space-2xs)"
             :font-size "var(--font-size-base)"
             :line-height "22px"}]

   ;; check
   [:.sticky-wrapper {:min-height "100px"
                      :height ["auto !important" "100%"]}]
   [:footer :.sticky-push {:height "200px"}]
   [:footer
    {:text-align 'center
     :font-size "var(--font-size-xs)"
     :padding-top "130px"}
    [:.divider {:font-size "16px"}]
    [:.ctas {:line-height "30px"}
     [:iframe {:margin-bottom "-6px"
               :margin-left "var(--space-xs)"}]
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
      :margin-bottom "var(--space-xs)"}]
    [:.error-message
     {:margin-top "var(--space-2xs)"
      :margin-right "var(--space-xs)"
      :padding "var(--space-xs)"}
     [:i {:margin-right "8px"}]]]

   [:.desktop-nav-bar
    {:position 'sticky
     :top 0
     :z-index 1000
     :background-color "var(--bg)"}]

   [:header.navbar
    {:border-radius 0
     :margin-bottom "var(--space-md)"}
    (nc/at-bp :xs {:margin-bottom 0})
    [".nav > li > a" {:padding "15px 12px"}]
    [:i {:margin-right "var(--space-2xs)"}]
    (nc/at-bp :xs [:.navbar-brand
                   {:float 'none
                    :display 'inline-block
                    :padding-left 0}])
    [:.btn.mobile-menu {:border 'none
                        :margin-top "var(--space-2xs)"
                        :font-size "18px"
                        :display 'none}
     [:i {:margin 0}]]
    [:.user-area {:font-size "var(--font-size-base)"
                  :color "var(--text-muted)"}]
    [:.navbar-nav {:padding-right 0
                   :margin 0}
     [:li {:margin-bottom 0
           :font-size "var(--font-size-base)"}]]
    [:.nav-search-widget
     [:form
      {:margin-right "var(--space-xs)"}]]
    [:.gh-starred-count
     {:margin-top "15px"
      :line-height "20px"
      :margin-left "var(--space-xs)"}]
    [:.clojure-version
     {:font-weight 300
      :font-size "var(--font-size-xs)"
      :font-family monospace-font
      :color "var(--text-faint)"
      :line-height "21px"}
     [:a {:line-height "20px"
          :color "var(--text-faint)"
          :font-weight 300}]]]
   [:.mobile-nav-bar {:display 'none}]
   [:.navbar-nav
    [:i {:margin-right "var(--space-2xs)"}]
    [:.avatar {:width "22px"
               :height "22px"
               :margin-right "var(--space-2xs)"}]]
   [:.mobile-nav-menu
    {:display 'none
     :overflow-y 'scroll
     :overflow-x 'hidden
     :-webkit-overflow-scrolling 'touch
     :height "100%"}]
   [:.desktop-side-nav
    {:overflow-y 'scroll
     :max-height "100%"}
    [:.badge
     {:float 'right}]]
   [:.mobile-nav-menu
    [:.badge
     {:font-size "16px"
      :color "rgba(var(--rgb-white),0.5)"
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
      [:.nav-search-widget {:float 'none
                            :clear 'both
                            :width "100%"
                            :padding "8px 15px 1px 15px"}
       [:form {:margin-right 0}]
       [:input {:width "100%"}]]
      [:.ac-results-widget {:display 'none}]
      [:.navbar-nav {:display 'none}]]
     [:body.search-active
      [:header.navbar
       {:position 'relative}
       [:.ac-results-widget {:display 'block
                            :position 'absolute
                            :left 0
                            :right 0
                            :z-index 1001
                            :max-height "60vh"
                            :overflow-y 'auto
                            :background-color "var(--bg)"
                            :box-shadow "0 2px 4px rgba(var(--rgb-black),0.1)"
                            :margin "0 -20px"
                            :padding "0 20px"}]]]
     [:.mobile-push-wrapper
      (merge
        {:right 0
         :position 'relative
         :transform "translate3d(0,0,0)"
         :margin-top "56px"} ; offset for sticky navbar
        (nc/transition "all 0.2s ease"))]
     [:.desktop-nav-bar :.desktop-side-nav {:display 'none}]
     [:.mobile-nav-bar
      (nc/transition "all 0.2s ease")
      {:background-color "var(--bg)"
       :position 'sticky
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
       :background-color "var(--color-brand-nav-bg)"
       :color "var(--color-on-solid)"
       :font-weight 300
       :top 0
       :bottom 0
       :right 0
       :margin 0
       :overflow-y 'scroll}
      [:h4 {:padding "5px 10px"
            :border-bottom "solid rgba(var(--rgb-white),0.3) 1px"
            :margin 0
            :color "rgba(var(--rgb-white),0.8)"
            :font-size "var(--font-size-base)"
            :letter-spacing "1px"
            :font-weight 500}
       [:i {:margin-right "var(--space-2xs)"}]]
      [:li {:margin 0}
       [:a {:color "var(--color-on-solid)"}
        [:&:hover {:background-color "var(--color-brand-nav-hover)"}]]]
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
                         :color "rgba(var(--rgb-white),0.7)"
                         :font-family monospace-font
                         :margin-left "var(--space-2xs)"
                         :font-size "10px"}])



   [:.null-state {:background-color "var(--bg-subtle)"
                  :text-align 'center
                  :padding "var(--space-md)"
                  :color "var(--text-muted)"}
    [:code {:color "var(--text-muted)"}]
    [:a {:color "var(--color-brand-muted)"}]]

   intro-page

   [:.see-also
    {:margin-bottom "var(--space-md)"}
    [:a {:color "var(--text-muted)"}
     [:.name {:color "var(--color-brand)"}
      [:&:hover {:color "var(--color-brand-hover)"}]]]
    [:p {:color "var(--text-secondary)"
         :margin-bottom 0
         :font-size "var(--font-size-md)"}]
    [:.var-title {:margin-bottom "3px"}]
    [:.meta {:font-size "10px"
             :color "var(--text-faint)"
             :text-transform 'uppercase}
     [:a {:color "var(--text-faint)"}]]
    [:.delete-confirmation [:a {:color "var(--color-brand)"}]]]

   [:input.loading
    {:background-image "url('/img/loading.gif')"
     :background-repeat 'no-repeat
     :background-position "right 8px center"}]

   [:form.search
    [:input.query :input.placeholder
     {:border-radius 0
      :min-width "200px"
      :height "100%"
      :font-size "var(--font-size-base)"
      :padding "6px 12px"}]]

   [:.syntaxify {:padding 0
                 :background-color 'transparent
                 :word-wrap 'normal}]

   autocomplete

   var-page

   quickref

   example-editor

   styleguide-page

   [:.example-body
    {:padding "var(--space-xs)"
     :font-size "var(--font-size-base)"
     :background-color "var(--bg-subtle)"
     :margin-bottom "var(--space-xs)"}]

   [:pre.raw-example
    {:background-color "var(--bg-subtle)"
     :margin-bottom "0px"
     :padding "var(--space-sm)"
     :font-size "var(--font-size-base)"}]
   [:.recent-pages
    {:font-size "var(--font-size-base)"
     :margin-bottom "var(--space-lg)"}
    [:li {:margin-bottom 0}]
    (nc/at-bp :xs
      [:& {:margin-bottom "var(--space-xs)"}]
      [:h3 {:display 'inline-block
            :margin-right "var(--space-md)"}]
      [:ul {:display 'inline-block}]
      [:li {:display 'inline-block
            :margin-right "var(--space-xs)"
            :background-color "var(--bg-subtle)"
            :padding "0 5px"}])]
   [:.var-page-nav {:margin-bottom "var(--space-md)"}]
   [:.ns-page
    [:h1 {:margin-bottom "var(--space-md)"}]
    [:pre.doc {:margin-bottom "var(--space-md)"}]
    [:section {:margin-bottom "50px"}]]
   [:.var-group
    {:clear 'both}
    [:h4 {:margin-top "var(--space-md)"
          :margin-bottom "var(--space-2xs)"
          :border-bottom "solid var(--border) 2px"}]
    (dl-rows {:dt {:min-width "150px"}
              :dd {:width "580px"}
              :dd-md {:width "460px"}
              :dd-sm {:width "400px"}})
    [:.no-doc {:color "var(--text-faint)"}]
    [:.ns-table
     {:width "100%"
      :border-bottom "solid var(--border) 2px"}
     [:.name {:text-align 'right}]
     [".name > span" {:padding-right "var(--space-md)"
                      :font-family monospace-font
                      :font-size "var(--font-size-xs)"
                      :line-height "24px"
                      :vertical-align 'middle}]
     [:.doc
      nc/ellipsis-text
      {:line-height "24px"
       :font-size "var(--font-size-base)"}]
     [:.heading {:margin-top "var(--space-md)"
                 :margin-bottom "var(--space-2xs)"
                 :border-bottom "solid var(--border) 2px"}]]]
   [:.error-page
    [:h1 {:text-align 'center
          :font-size "var(--font-size-xl)"
          :color "var(--text)"}]
    [:a.four-oh-four
     {:max-width "300px"
      :max-height "100%"
      :display 'block
      :margin "20px auto"}]]
   [:.muted {:color "var(--text-faint)"}]
   [:.var-link
    {:color "var(--text-muted)"}
    [:.namespace {:color "var(--text-muted)"}]
    [:.name {:color blue}]]
   [:.staging-banner
    {:text-align 'center
     :background-color "var(--color-banner-staging)"
     :color "rgba(var(--rgb-white), 0.8)"
     :font-size "var(--font-size-base)"
     :font-weight 400
     :padding "3px"}]
   [:.survey-banner
    {:text-align 'center
     :background-color "var(--color-banner-survey)"
     :color "rgba(var(--rgb-white), 0.95)"
     :font-size "var(--font-size-base)"
     :font-weight 400
     :padding "8px"}
    [:a {:color "rgba(var(--rgb-white), 1)"
         :text-decoration 'underline
         :font-weight 600}
     [:&:hover {:color "rgba(var(--rgb-white), 0.85)"}]]]
   [:.toggle-controls :.login-required-message
    {:text-align 'right
     :margin-bottom "4px"
     :font-size "var(--font-size-xs)"}]
   [:.toggle-link {:color light-blue}]
   [:.sidenav {:font-size "var(--font-size-base)"
               :max-height "100%"}
    [:h5 {:font-size "var(--font-size-base)"
          :margin-top 0}]
    ["> ul" {:margin-bottom "var(--space-md)"}]
    [:li {:margin-bottom 0}]
    ["li > ul" {:margin-left "var(--space-xs)"}]
    [:.current {:font-weight 'bold}]
    (nc/at-bp :xs {:display 'none})

    [:.library-nav {:max-height "100%"
                    :overflow-y 'scroll
                    :overflow-x 'hidden}]]
   [:.live-preview
    {:background-color "var(--bg-subtle)"
     :margin-bottom "var(--space-xs)"
     :word-wrap 'normal
     :overflow-x 'scroll}
    [:.empty-live-preview
     {:text-align 'center
      :color "var(--text-faint)"
      :font-size "var(--font-size-base)"
      :padding "var(--space-xs)"}]]
   [:.ac-results-widget {:margin "0 -20px"
                         :padding "0 20px"}]
   [:.search-active
    [:.ac-results-widget
     {:background-color "var(--bg)"}
     [:ul.ac-results
      {:overflow 'auto}]
     ["ul.ac-results > li"
      [:&:first-child {:margin-top "var(--space-md)"}]
      [:&:last-child {:margin-bottom "var(--space-md)"}]]]]
   [:.core-library-page
    {:padding-top "32px"}
    [:.nav-search-widget {
      :display 'block
      :position 'relative
      :z-index 1200
      :margin-bottom "24px"
    }]
    [:pre :.syntaxify
     {:background-color code-bg
      :padding "var(--space-md)"}]
    [:h2 {:border-bottom "solid var(--border) 2px"}]
    [:h3 {:margin-top "var(--space-lg)"}]]
   [:.nav-search-widget
    {:z-index 1100
     :position 'relative}]
   [:.markdown
    [:ol :ul
     {:padding-left 0
      :margin-bottom "var(--space-md)"}
     [:li {:margin-left "25px"
           :padding 0
           :margin-bottom 0}]]
    [:ul [:li {:list-style-type 'disc}]]
    [:ol [:li {:list-style-type 'decimal}]]
    [:.syntaxify [:pre {:margin-bottom "var(--space-xl)"}]]
    [:h1 :h2 :h3 :h4 :h5 {:margin-bottom "0.5em"}]]
   [:.sg-example
    {:margin-bottom "var(--space-lg)"
     :border "solid var(--border-subtle) 1px"
     :padding "3px"}
    [:.caption
     {:text-align 'center
      :background-color "var(--bg-subtle)"
      :padding "var(--space-2xs)"
      :color "var(--text-muted)"
      :font-size "11px"
      :letter-spacing "1px"}]]
   [:.field-schemas :table.schema
    [:td {:padding-right "var(--space-xs)"
          :vertical-align 'top}]]
   [:table.schema
    {:border-top "solid var(--border) 3px"
     :width "100%"
     :padding "var(--space-xs)"}
    [:td {:border 'none
          :padding "var(--space-2xs)"
          }]
    [:.aside {:font-size "var(--font-size-xs)"}]]
   [:.test-result
    {:padding "3px"
     :color "var(--color-on-solid)"}
    [:&.pass {:background-color "var(--color-test-pass)"}]
    [:&.fail {:background-color "var(--color-test-fail)"}]
    [:&.error {:background-color "var(--color-test-error)"}]
    [:&.pending {:background-color "var(--color-test-pending)"}]]
   [:.add-example-page
    [:.syntaxify
     [:code {:font-size "var(--font-size-xs)"}]]
    [:textarea :pre {:font-size "var(--font-size-xs)"}]]
   [:.tabbed-editor
    [:.example-editor
     {:border-color "var(--border-subtle)"
      :border "solid var(--border-subtle) 1px"
      :border-top 'none}
     [:.columns-guide
      {:background-color 'transparent
       :color "var(--border)"
       :margin 0}]]
    [:.null-state
     {:background-color "var(--bg)"
      :border 'none
      :border-top 'none}]
    [:textarea
     {:border 'none
      :border-radius 0
      :overflow 'auto
      :outline 'none
      :outline-width 0
      :resize 'none
      :box-shadow 'none}
     [:&:focus {:box-shadow 'none}]]
    [:.nav-tabs
     {:background-color "var(--bg-hover)"
      :border-bottom 'none}
     [:li {:margin-bottom 0
           :border-bottom 'none}]
     ["> li.active" {:border-bottom 'none}
      [:&:hover {:border-color "var(--border-subtle)"}]
      ["> a:focus" {:border-color "var(--border-subtle)"}]]
     ["li.active > a" "li.active>a:hover" "li.active>a:focus"
      {:border-color "var(--border-subtle)"
       :border-bottom 'none}
      [:&:hover {:border-bottom 'none
                 :margin-bottom 0}]]
     [:li
      [:a {:border-radius 0
           :border-bottom 'none
           :margin-bottom 0}
       [:&:hover {:border-bottom 'none
                  :margin-bottom 0}]]]]
    [:.live-preview
     {:min-height "227px"
      :border "solid var(--border-subtle) 1px"
      :border-top 'none
      :padding "var(--space-xs)"
      :background-color "var(--bg-subtle)"}
     [:li {:margin-left "var(--space-md)"
           :list-style-type 'disc
           :margin-bottom 0}]]]
   [:.sg-example-inspector
    [:.sg-inspector-state
     [:textarea
      {:width "100%"
       :border "solid var(--border-subtle) 1px"
       :padding "var(--space-2xs)"}
      [:&.error {:background-color "var(--color-error-bg)"}]]]
    [:.sg-inspector-outlets
     [:.message
      {:margin-bottom "var(--space-xs)"}
      [:h6 {:padding "3px"
            :margin 0
            :color "var(--text-secondary)"}]
      [:pre {:margin 0}]]]]
   [:.migrate-user-page
    [:.controls {:text-align 'center}
     [:.btn {:font-size "30px"
             :padding "5px 30px"}]]]
   [:.var-namespace-group
    [:h2 {:border-bottom "solid var(--color-highlight) 2px"}]]
   [:.var-group
    [:h3 {:border-bottom "solid var(--border) 1px"}]
    [:ul.var-list
     [:li {:display 'inline-block
           :margin-right "4px"
           :background-color "var(--bg-hover)"
           :padding "0 4px"}]]]
   [:.search-results-page
    [:.search-results-header
     {:border-bottom "solid var(--border) 1px"
      :margin-bottom "var(--space-md)"}
     [:h1 {:margin-bottom 0}]
     [:p {:color "var(--text-muted)"
          :margin-bottom "var(--space-2xs)"}]]
    [:.search-results
     {:margin-bottom "var(--space-lg)"}]
    [:.search-result
     {:margin-bottom "var(--space-lg)"
      :line-height "24px"}
     [:h1 {:border-bottom "solid var(--border) 2px"}]
     [:h2 {:margin-top "var(--space-2xs)"
           :margin-bottom 0
           :font-size "var(--font-size-2xl)"}]
     [:h3 {:margin-top 0
           :margin-bottom "var(--space-2xs)"
           :font-size "var(--font-size-lg)"
           :font-weight 500
           :color "var(--text-muted)"}]
     [:p {:margin-bottom "var(--space-2xs)"
          :font-size "var(--font-size-lg)"
          :line-height "24px"}]
     [:.examples-count {:font-size "var(--font-size-sm)"
                        :color "var(--text-muted)"}]
     (arglists {:margin-bottom 0})
     [:.meta-info
      {:font-size "var(--font-size-sm)"
       :color "var(--text-muted)"
       :line-height "20px"}]
     [:.see-alsos
      [:.see-also
       {:display 'inline-block
        :margin-bottom 0
        :line-height "12px"}
       [:a {:color blue}]
       [:.ns {:color "var(--text-muted)"}]]]]
    [:.search-controls
     {:text-align 'center
      :color "var(--text-muted)"}]]

   [:.header-banner
    {:background-color "var(--color-banner-header)"
     :color "var(--color-on-solid)"
     :text-align 'center
     :padding "3px"
     :font-weight '300
     :font-size "var(--font-size-sm)"}
    [:a {:color "var(--color-on-solid)"
         :text-decoration 'underline}]]])
