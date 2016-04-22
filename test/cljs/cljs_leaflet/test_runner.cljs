(ns cljs-leaflet.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [cljs-leaflet.core-test]))

(enable-console-print!)

(doo-tests 'cljs-leaflet.core-test)
