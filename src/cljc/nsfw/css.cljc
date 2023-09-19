(ns nsfw.css
  (:require [clojure.string :as str]
            [garden.units :as u]
            [garden.color :as co]
            [garden.stylesheet :refer [at-media]]
            [garden.core :as garden]
            [garden.stylesheet :as gs])
  #? (:cljs (:require-macros [nsfw.css :refer [inject-css-defs]])))

(defn prefix [[k v]]
  (->> ["-webkit-"
        "-moz-"
        "-ms-"
        ""]
       (map (fn [prefix]
              [(->> k
                    name
                    (str prefix)
                    keyword)
               v]))
       (into {})))

(prefix [:foo "bar"])

(defn px [n]
  (if (and (string? n) (str/includes? n "px"))
    n
    (str n "px")))

(defn transition [v]
  {:transition v
   :-webkit-transition (if (string? v)
                         (str/replace v #"transform" "-webkit-transform")
                         v)
   :-moz-transition v
   :-ms-transition v
   :-o-transition v})

(defn transform [s]
  {:transform s})

(def screen-lg-min (u/px 1200))

(def screen-md-min (u/px 992))
(def screen-md-max (u/px- screen-lg-min 1))

(def screen-sm-min (u/px 768))
(def screen-sm-max (u/px- screen-md-min 1))

(def screen-xs-min (u/px 480))
(def screen-xs-max (u/px- screen-sm-min 1))

(def key->breakpoint
  {:xs {:max-width screen-xs-max}
   :>sm {:min-width screen-sm-min}
   :<sm {:max-width screen-sm-min}
   :>md {:min-width screen-md-min}
   :<md {:max-width screen-md-min}
   :>lg {:min-width screen-lg-min}
   :<lg {:max-width screen-lg-min}

   :sm {:min-width screen-sm-min
        :max-width screen-sm-max}
   :md {:min-width screen-md-min
        :max-width screen-md-max}
   :lg {:min-width screen-lg-min}})

(defn at-bp [breakpoint-key & rules]
  (let [rules (if (map? (first rules))
                [(vec (concat [:&] rules))]
                rules)]
    (apply
      at-media
      (key->breakpoint breakpoint-key)
      rules)))

(defn checkerboard-gen [{:keys [light-bg dark-bg]}]
  (let [light-bg (or light-bg "#fafafa")
        dark-bg (or dark-bg "#f3f3f3")]
    {:background-color light-bg
     :background-image (str "linear-gradient(45deg, "
                            dark-bg
                            " 25%, transparent 25%, transparent 75%, "
                            dark-bg
                            " 75%, "
                            dark-bg
                            "),
linear-gradient(45deg, "
                            dark-bg
                            " 25%, transparent 25%, transparent 75%, "
                            dark-bg
                            " 75%, "
                            dark-bg
                            ")")
     :background-size "60px 60px"
     :background-position "0 0, 30px 30px"}))

(def shadows
  {:sm {:box-shadow "0 2px 4px 0 rgba(0,0,0,0.10)"}
   :md {:box-shadow "0 4px 8px 0 rgba(0,0,0,0.12), 0 2px 4px 0 rgba(0,0,0,0.08)"}
   :lg {:box-shadow "0 15px 30px 0 rgba(0,0,0,0.11), 0 5px 15px 0 rgba(0,0,0,0.08)"}
   :inner {:box-shadow "inset 0 2px 4px 0 rgba(0,0,0,0.06)"}})

(def shadow-sm (:sm shadows))
(def shadow-md (:md shadows))
(def shadow-lg (:lg shadows))
(def shadow-inner (:inner shadows))

(defn margin [n] {:margin (px n)})
(defn margin-top [n] {:margin-top (px n)})
(defn margin-bot [n] {:margin-bottom (px n)})

(defn padding [n] {:padding (px n)})
(defn padding-top [n] {:padding-top (px n)})
(defn padding-bot [n] {:padding-bot (px n)})

(defn cover-bg [{:keys [src] :as opts}]
  (merge
    {:background-image (str "url('" src "')")
     :background-position "center center"
     :background-size 'cover}
    (dissoc opts :src)))

(def flex-center {:display 'flex
                  :justify-content 'center
                  :align-items 'center})

(def flex-vcenter {:display "flex"
                   :flex-direction "column"
                   :justify-content "center"
                   :align-items "stretch"})

(def flex-apart {:display 'flex
                 :flex-direction 'row
                 :justify-content 'space-between
                 :align-items 'center})

(def flex-left {:display 'flex
                :justify-content 'flex-start
                :align-items 'center
                :flex-wrap 'wrap})

(def flex-right {:display 'flex
                 :justify-content 'flex-end
                 :align-items 'center
                 :flex-wrap 'wrap})

(def flex-bot {:display 'flex
               :flex-direction 'column
               :justify-content 'flex-end
               :align-items 'flex-start})

(def flex-bot-center
  {:display 'flex
   :flex-direction 'column
   :justify-content 'flex-end
   :align-items 'center})


(def parent-size-abs {:position 'absolute
                      :top 0
                      :left 0
                      :right 0
                      :bottom 0})

(def parent-size-rel
  {:width "100%"
   :height "100%"
   :position 'relative})

(defmacro inject-css-defs [{:keys [sizes fonts] :as spec}]
  (let [{:keys [xs sm md lg xl]} sizes
        {:keys [header copy impact monospace]} fonts]
    `(do
       (def ~'css-spec ~spec)

       (def ~'xs ~xs)
       (def ~'sm ~sm)
       (def ~'md ~md)
       (def ~'lg ~lg)
       (def ~'xl ~xl)

       (def ~'xspx (px ~xs))
       (def ~'smpx (px ~sm))
       (def ~'mdpx (px ~md))
       (def ~'lgpx (px ~lg))
       (def ~'xlpx (px ~xl))

       (def ~'pad-none (padding 0))
       (def ~'pad-xs (padding ~xs))
       (def ~'pad-sm (padding ~sm))
       (def ~'pad-md (padding ~md))
       (def ~'pad-lg (padding ~lg))
       (def ~'pad-xl (padding ~xl))

       (def ~'mg-none (margin 0))
       (def ~'mg-xs (margin ~xs))
       (def ~'mg-sm (margin ~sm))
       (def ~'mg-md (margin ~md))
       (def ~'mg-lg (margin ~lg))
       (def ~'mg-xl (margin ~xl))

       (def ~'mg-top-xs (margin-top ~xs))
       (def ~'mg-top-sm (margin-top ~sm))
       (def ~'mg-top-md (margin-top ~md))
       (def ~'mg-top-lg (margin-top ~lg))
       (def ~'mg-top-xl (margin-top ~xl))

       (def ~'mg-bot-xs (margin-bot ~xs))
       (def ~'mg-bot-sm (margin-bot ~sm))
       (def ~'mg-bot-md (margin-bot ~md))
       (def ~'mg-bot-lg (margin-bot ~lg))
       (def ~'mg-bot-xl (margin-bot ~xl))

       (def ~'header-font ~header)
       (def ~'copy-font ~copy)
       (def ~'impact-font ~impact)
       (def ~'monospace-font ~monospace)

       (def ~'shadow-sm (:sm shadows))
       (def ~'shadow-md (:md shadows))
       (def ~'shadow-lg (:lg shadows))
       (def ~'shadow-inner (:inner shadows))

       (def ~'flex-center flex-center)

       (def ~'flex-vcenter flex-vcenter)

       (def ~'parent-size-rel parent-size-rel)
       (def ~'parent-size-abs parent-size-abs)

       (def ~'flex-apart {:display "flex"
                          :flex-direction "row"
                          :justify-content "space-between"
                          :align-items "center"})

       (def ~'flex-left {:display "flex"
                         :justify-content "flex-start"
                         :align-items "center"
                         :flex-wrap "wrap"})

       (def ~'flex-bot flex-bot)

       (def ~'flex-bot-center flex-bot-center)


       (def ~'cover-bg cover-bg))))


;;;

(def text-center {:text-align 'center})
(def text-right {:text-align 'right})
(def text-left {:text-align 'left})

(def lh100 {:line-height "100%"})
(def lh120 {:line-height "120%"})
(def lh150 {:line-height "150%"})

(def ellipsis
  {:flex 1
   :overflow 'hidden
   :white-space 'nowrap
   :text-overflow 'ellipsis})

(def border-radius-sm
  {:border-radius (px 2)})

(def border-radius-md
  {:border-radius (px 3)})

(def border-radius-lg
  {:border-radius (px 5)})

(def border-radius-round
  {:border-radius "999px"})

(defn utilities [css-spec]
  (let [{:keys [xs sm md lg xl]} (:sizes css-spec)
        {:keys [header copy impact monospace]} (:fonts css-spec)]
    [[:.text-center text-center]
     [:.text-right text-right]
     [:.text-left text-left]

     [:.pad-none (padding 0)]
     [:.pad-xs (padding xs)]
     [:.pad-sm (padding sm)]
     [:.pad-md (padding md)]
     [:.pad-lg (padding lg)]
     [:.pad-xl (padding xl)]

     [:.mg-none (margin 0)]
     [:.mg-xs (margin xs)]
     [:.mg-sm (margin sm)]
     [:.mg-md (margin md)]
     [:.mg-lg (margin lg)]
     [:.mg-xl (margin xl)]

     [:.mg-bot-xs (margin-bot xs)]
     [:.mg-bot-sm (margin-bot sm)]
     [:.mg-bot-md (margin-bot md)]
     [:.mg-bot-lg (margin-bot lg)]
     [:.mg-bot-xl (margin-bot xl)]

     [:.mg-top-xs (margin-top xs)]
     [:.mg-top-sm (margin-top sm)]
     [:.mg-top-md (margin-top md)]
     [:.mg-top-lg (margin-top lg)]
     [:.mg-top-xl (margin-top xl)]

     [:.lh100 lh100]
     [:.lh120 lh120]
     [:.lh150 lh150]

     [:.bold {:font-weight 'bold}]
     [:.ellipsis ellipsis]
     [:.spacer-xs {:height (px xs)}]
     [:.spacer-sm {:height (px sm)}]
     [:.spacer-md {:height (px md)}]
     [:.spacer-lg {:height (px lg)}]

     [:.br-sm border-radius-sm]
     [:.br-md border-radius-md]
     [:.br-lg border-radius-lg]

     [:.shadow-sm shadow-sm]
     [:.shadow-md shadow-md]
     [:.shadow-lg shadow-lg]
     [:.shadow-inner shadow-inner]

     [:.header-font {:font-family header}]
     [:.copy-font {:font-family copy}]
     [:.impact-font {:font-family impact}]
     [:.monospace-font {:font-family monospace}]


     [:.full-size
      {:width "100%"
       :height "100%"
       :position 'relative}]
     [:.full-size-abs
      {:position 'absolute
       :width "100%"
       :height "100%"
       :top 0
       :left 0}]


     [:.parent-size-rel parent-size-rel]

     [:.parent-size-abs parent-size-abs]


     [:.scroll-y
      {:overflow-y 'scroll
       :-webkit-overflow-scrolling 'touch}]

     [:.visible-xs
      {:display 'none}
      (at-bp :xs
             {:display 'block})]

     [:.rel {:position 'relative}]]))

(defn defaults [css-spec]
  (let [{:keys [sm md]} (:sizes css-spec)
        {:keys [monospace]} (:fonts css-spec)]
    [[:body {:-webkit-font-smoothing 'antialiased
             :-moz-osx-font-smoothing 'grayscale}]
     [:ul :ol
      {:padding-left (px 40)}
      [:li
       {:padding 0
        :margin 0}
       (margin-bot sm)]]

     [:ul :ol
      {:padding 0}
      [:&.bare
       [:li {:list-style-type 'none
             :padding 0
             :margin 0}]]]
     [:pre
      {:background-color 'white
       :font-family monospace}
      (padding sm)]

     [:.code
      {:font-family monospace
       :white-space 'pre-wrap
       :font-weight 'bold}]
     [:p (margin-bot md)]]))

(defn progress [css-spec]
  (let [{:keys [sm md lg]} (:sizes css-spec)]
    [(gs/at-keyframes
       :sk-rotatePlane
       ["0%" {:transform "perspective(120px) rotateX(0deg) rotateY(0deg)"}]
       ["50%" {:transform "perspective(120px) rotateX(-180.1deg) rotateY(0deg)"}]
       ["100%" {:transform "perspective(120px) rotateX(-180deg) rotateY(-179.9deg)"}])
     [:.prog-rot-sm
      :.prog-rot-md
      :.prog-rot-lg
      {:opacity 0}
      (transition "opacity 0.1s ease")
      [::&.loading
       {:opacity 1}]
      [:.box
       {:background-color 'black
        :animation "sk-rotatePlane 1.2s infinite ease-in-out"}]
      [:&.slow
       [:.box
        {:animation "sk-rotatePlane 2.4s infinite ease-in-out"}]]]

     [:.prog-rot-sm
      [:.box
       {:width (px sm)
        :height (px sm)}]]
     [:.prog-rot-md
      [:.box
       {:width (px md)
        :height (px md)}]]
     [:.prog-rot-lg
      [:.box
       {:width (px lg)
        :height (px lg)}]]]))

(defn button
  [k {:keys
      [base-bg-color
       base-fg-color
       base-border-color

       hover-bg-color
       hover-fg-color
       hover-border-color

       active-bg-color
       active-fg-color
       active-border-color

       border-radius]
      :as opts}]
  (let [hover-bg-color (or hover-bg-color
                           active-bg-color
                           base-bg-color)

        hover-fg-color (or hover-fg-color
                           active-fg-color
                           base-fg-color)

        hover-border-color (or hover-border-color
                               active-border-color
                               base-border-color)

        active-bg-color (or active-bg-color
                            base-bg-color)

        active-fg-color (or active-fg-color
                            base-fg-color)

        active-border-color (or active-border-color
                                base-border-color)

        border-radius (or border-radius 0)

        style (dissoc opts
                :base-bg-color
                :base-fg-color
                :base-border-color

                :hover-bg-color
                :hover-fg-color
                :hover-border-color

                :active-bg-color
                :active-fg-color
                :active-border-color

                :border-radius)

        s (name k)
        selector (str ".btn-" s)

        hover-amount 6
        active-amount 14

        root-styles (merge
                      {:background-color 'transparent
                       :outline 0
                       :border-color 'transparent
                       :border-width 0
                       :font-size (px 15)
                       :font-weight 'normal
                       :padding "5px 20px"
                       :cursor 'pointer}
                      (when base-border-color
                        {:border-color base-border-color
                         :border-style 'solid
                         :border-width (px 1)})
                      {:background-color base-bg-color
                       :color base-fg-color}
                      (transition
                        "background-color 0.08s ease, border-color 0.08s ease")
                      #_style)

        hover-styles {:background-color hover-bg-color
                      :color hover-fg-color
                      :border-color hover-border-color}

        active-styles {:background-color active-bg-color
                       :border-color active-border-color
                       :color active-fg-color}]
    [[selector
      root-styles
      [:&:hover hover-styles]
      [:&:active active-styles]
      [:&:focus
       {:outline 0}]
      [:&.round
       {:border-radius (px border-radius)}]
      [:&.hover hover-styles]
      [:&.active active-styles]]]))

(defn buttons [css-spec]
  (->> css-spec
       :buttons
       (map #(apply button %))
       vec))

(defn headers [css-spec & style-overrides]
  (let [sm (-> css-spec :sizes :sm)
        header-font (-> css-spec :fonts :header)]
    [:h1 :h2 :h3 :h4 :h5 :h6
     (merge
       (margin-bot sm)
       lh100
       {:font-family header-font
        :font-weight 'normal}
       (reduce merge style-overrides))]))

(def flexbox
  [[:.flex-apart flex-apart]

   [:.flex-apart-top {:display 'flex
                      :flex-direction 'row
                      :justify-content 'space-between
                      :align-items 'flex-start}]

   [:.flex-around {:display 'flex
                   :justify-content 'space-around
                   :align-items 'center}]

   [:.flex-center flex-center]

   [:.flex-vcenter flex-vcenter]


   [:.flex-center-top {:display 'flex
                       :justify-content 'center
                       :align-items 'flex-start}]
   [:.flex-right flex-right]

   [:.flex-right-stretch {:display 'flex
                          :align-itmes 'stretch
                          :justify-content 'flex-end}]


   [:.flex-left flex-left]

   [:.flex-left-top {:display 'flex
                     :flex-direction 'row
                     :justify-content 'flex-start
                     :align-items 'flex-start
                     :flex-wrap 'wrap}]

   [:.flex-left-bot {:flex-direction 'column
                     :justify-content 'flex-start
                     :align-items 'flex-end
                     :flex-wrap 'wrap}]

   [:.flex-top {:display 'flex
                :justify-content 'flex-start
                :align-items 'flex-start}]

   [:.flex-bot flex-bot]

   [:.flex-masonry {:display 'flex
                    :flex-direction 'column
                    :flex-wrap 'wrap
                    :align-content 'stretch}]

   [:.flex-column {:display 'flex
                   :flex-direction 'column
                   :justify-content 'flex-start}]

   [:.flex-column-center {:display 'flex
                          :flex-direction 'column
                          :align-items 'center}]

   [:.flex-column-top {:display 'flex
                       :flex-direction 'column
                       :align-items 'flex-start}]

   [:.flex-column-vcenter {:display 'flex
                           :flex-direction 'column
                           :justify-content 'center}]

   [:.flex-column-right {:display 'flex
                         :flex-direction 'column
                         :align-items 'flex-end}]

   [:.flex-column-right-center {:display 'flex
                                :flex-direction 'column
                                :align-items 'flex-end
                                :justify-content 'center}]

   [:.flex-column-center-both {:display 'flex
                               :flex-direction 'column
                               :align-items 'center
                               :justify-content 'center}]])


(defn gen-all-rules [spec]
  [flexbox
   (headers spec)])

(defn compile-to-string [rules & [override-opts]]
  (garden/css
    (merge
      {
       :pretty-print? false
       :vendors ["webkit" "moz" "ms"]
       :auto-prefix #{:justify-content
                      :align-items
                      :flex-direction
                      :flex-wrap
                      :align-self
                      :transition
                      :transform
                      :background-clip
                      :background-origin
                      :background-size
                      :filter
                      :font-feature-settings
                      :appearance}}
      override-opts)
    rules))

(def display-flex
  {:display ^:prefix #{"flex" "-webkit-flex"
                       "-moz-box" "-ms-flexbox"}})

(defn flex-box [opts]
  (merge
   display-flex
   (->> opts
        (map (fn [[k v]]
               (prefix [k v])))
        (reduce merge))))
(def flex-defaults
  [[:.flex-apart
    (flex-box {:justify-content 'space-between
               :align-items 'center})]

   [:.flex-around
    (flex-box {:justify-content 'space-around
               :align-items 'center})]

   [:.flex-apart-top
    (flex-box {:justify-content 'space-between
               :align-items 'top})]

   [:.flex-center
    (flex-box {:justify-content 'center
               :align-items 'center})]

   [:.flex-center-top
    (flex-box {:justify-content 'center
               :align-items 'top})]
   [:.flex-right
    (flex-box {:justify-content 'flex-end
               :align-items 'center})]

   [:.flex-left
    (flex-box {:justify-content 'flex-start
               :align-items 'center
               :flex-wrap 'wrap})]

   [:.flex-masonry
    (flex-box {:flex-direction 'column
               :flex-wrap 'wrap
               :align-content 'stretch
               :align-items 'stretch})]])

(def ellipsis-text
  {:white-space 'nowrap
   :overflow 'hidden
   :text-overflow 'ellipsis})
