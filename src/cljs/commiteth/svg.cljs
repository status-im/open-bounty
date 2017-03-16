(ns commiteth.svg)

(defn app-logo []
  [:svg
   {:width "38px"
    :height "38px"
    :viewBox "0 0 38 38"
    :version "1.1"}
   [:g
    {:id "page-1"
     :transform "translate(-160.000000, -35.000000)"
     :fillRule "nonzero"
     :fill "#B0F1EE"}
    [:g
     {:id "logo"
      :transform "translate(160.000000, 35.000000)"}
     [:path
      {:d "M5.97918472,32.0208153 C-0.65972824,25.3819023 -0.65972824,12.6180977 5.97918472,5.97918472 C9.29958963,2.65877981 13.6518068,0.999051701 18.0037292,1.00000041 L33.9948134,1 C35.3783898,1 36.5,2.10966206 36.5,3.5 C36.5,4.88071187 35.3906622,6 33.9948134,6 L18.5,6 C15.264488,5.87626016 11.9850564,7.04438088 9.51471863,9.51471863 C4.82842712,14.2010101 4.82842712,23.7989899 9.51471863,28.4852814 C11.9850564,30.9556191 15.264488,32.1237398 18.5,31.9896435 L33.9948134,32 C35.3783898,32 36.5,33.1096621 36.5,34.5 C36.5,35.8807119 35.3906622,37 33.9948134,37 L18.0051866,37 C13.65181,37.0009495 9.29959083,35.3412214 5.97918472,32.0208153 Z M17.5,19 C17.5,17.6192881 18.6176026,16.5 20.0034561,16.5 L33.9965439,16.5 C35.3791645,16.5 36.5,17.6096621 36.5,19 C36.5,20.3807119 35.3823974,21.5 33.9965439,21.5 L20.0034561,21.5 C18.6208355,21.5 17.5,20.3903379 17.5,19 Z"
       :id "shape"}]]]])

(defn dropdown-icon []
  [:svg
   {:width "16px"
    :height "16px"
    :viewBox "0 0 16 16"
    :version "1.1"}
   [:g
    {:id "page-1"
     :stroke "none"
     :stroke-width "1"
     :fill "none"
     :fill-rule "evenodd"}
    [:g
     {:id "commiteth-desktop_repositories"
      :transform "translate(-1098.000000, -32.000000)"
      :fill "#FFFFFF"}
     [:g {:id "header"}
      [:g {:id "user" :transform "translate(956.000000, 24.000000)"}
       [:g
        {:id "icon_dropdown"
         :transform "translate(142.000000, 8.000000)"}
        [:path
         {:d "M7.76596419,11.5564843 C8.07705085,11.6115965 8.40862642,11.5177766 8.64937808,11.277025 L13.3038251,6.62257794 C13.6942764,6.23212667 13.695553,5.59768507 13.3050287,5.20716078 C12.9117818,4.81391382 12.2808006,4.81717535 11.8896116,5.20836438 L7.76949482,9.32848112 L3.64937808,5.20836438 C3.25892681,4.81791311 2.62448521,4.81663648 2.23396092,5.20716078 C1.84071396,5.60040773 1.84397549,6.23138892 2.23516452,6.62257794 L6.88961157,11.277025 C7.12783981,11.5152532 7.45689724,11.6086052 7.76596419,11.5564843 L7.76596419,11.5564843 Z"
          :id "shape"}]]]]]]])

(defn github-fork-icon []
  [:svg
   {:width 10
    :height 16
    :version "1.1"
    :viewBox "0 0 10 16"}
   [:path
    {:fill-rule "evenodd"
     :fill "#a8aab1"
     :d "M8 1a1.993 1.993 0 0 0-1 3.72V6L5 8 3 6V4.72A1.993 1.993 0 0 0 2 1a1.993 1.993 0 0 0-1 3.72V6.5l3 3v1.78A1.993 1.993 0 0 0 5 15a1.993 1.993 0 0 0 1-3.72V9.5l3-3V4.72A1.993 1.993 0 0 0 8 1zM2 4.2C1.34 4.2.8 3.65.8 3c0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2zm3 10c-.66 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2zm3-10c-.66 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2z"}]])
