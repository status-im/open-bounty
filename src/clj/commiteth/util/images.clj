(ns commiteth.util.images
  (:import [java.awt GraphicsEnvironment RenderingHints]
           [java.awt.image BufferedImage]
           [javax.swing JEditorPane]
           [java.io ByteArrayInputStream ByteArrayOutputStream]
           [javax.imageio ImageIO]))

(defn ^BufferedImage create-image
  [width height]
  (new BufferedImage width height BufferedImage/TYPE_INT_ARGB))

(defn html->image
  [html width height]
  (let [image    (create-image width height)
        graphics (.createGraphics image)
        jep      (new JEditorPane "text/html" html)]
    (.setRenderingHint graphics
      (RenderingHints/KEY_TEXT_ANTIALIASING)
      (RenderingHints/VALUE_TEXT_ANTIALIAS_GASP))
    (. jep (setSize width height))
    (. jep (print graphics))
    image))

(defn combine-images
  [^BufferedImage image1
   ^BufferedImage image2]
  (let [left-width (.getWidth image1)
        width      (+ left-width (.getWidth image2))
        height     (max (.getHeight image1) (.getHeight image2))
        combined   (create-image width height)
        graphics   (.createGraphics combined)]
    (.drawImage graphics image1 nil 0 0)
    (.drawImage graphics image2 nil left-width 0)
    (let [output-stream (ByteArrayOutputStream. 2048)]
      (ImageIO/write combined "png" output-stream)
      (ByteArrayInputStream. (.toByteArray output-stream)))))
