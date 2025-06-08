(defproject calorie-calculator "0.1.0-SNAPSHOT"
  :description "Calculadora de Calorias em Clojure"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring "1.9.0"]
                 [compojure "1.6.2"]
                 [ring/ring-json "0.5.1"]
                 [cheshire "5.10.0"]
                 [clj-http "3.12.3"]]
  :main calorie-calculator.core)
