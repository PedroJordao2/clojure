(ns calorie-calculator.core
  (:gen-class)
  (:require [clojure.string :as str]
            [calorie-calculator.db :as db]
            [calorie-calculator.services :as svc]
            [calorie-calculator.config :refer [load-config]]))

(defn prompt [msg]
  (println msg)
  (print "> ")
  (flush)
  (read-line))

(defn parse-int [s]
  (try (Integer/parseInt s) (catch Exception _ 0)))

(defn parse-float [s]
  (try (Double/parseDouble s) (catch Exception _ 0.0)))

(defn register-user-cli []
  (let [nome   (prompt "Nome:")
        peso   (prompt "Peso (kg):")
        altura (prompt "Altura (m):")]
    (db/set-user! {:nome nome
                   :peso (parse-float peso)
                   :altura (parse-float altura)})
    (println "\n✅ Usuário cadastrado:" @db/state)))

(defn add-food-cli []
  (let [barcode (prompt "Código de barras:")
        date    (prompt "Data (YYYY-MM-DD):")
        qty     (prompt "Quantidade:")]
    (let [cals  (* (svc/fetch-food-calories barcode)
                   (parse-int qty))
          entry {:tipo      :ganho
                 :descricao barcode
                 :date      date
                 :calorias  cals}]
      (db/add-entry! entry)
      (println "\n✅ Adicionado alimento:" entry))))

(defn add-activity-cli []
  (let [name     (prompt "Atividade (ex: running):")
        date     (prompt "Data (YYYY-MM-DD):")
        duration (prompt "Duração (min):")]
    (let [cals  (svc/fetch-activity-calories name (parse-int duration))
          entry {:tipo      :perda
                 :descricao name
                 :date      date
                 :calorias  cals}]
      (db/add-entry! entry)
      (println "\n✅ Adicionada atividade:" entry))))

(defn show-extract-cli []
  (let [from (prompt "Extrato de: (YYYY-MM-DD)")
        to   (prompt "Até: (YYYY-MM-DD)")
        items (db/get-entries from to)]
    (println "\n--- Extrato de Calorias ---")
    (doseq [e items]
      (println e))
    (println "---------------------------")))

(defn show-balance-cli []
  (let [from    (prompt "Saldo de: (YYYY-MM-DD)")
        to      (prompt "Até: (YYYY-MM-DD)")
        bal     (db/balance from to)]
    (println (str "\n⚖️  Saldo de calorias entre " from " e " to ": " bal))))

(defn menu []
  (println "\n=== Calculadora de Calorias ===")
  (println "1) Cadastrar usuário")
  (println "2) Adicionar alimento")
  (println "3) Adicionar atividade")
  (println "4) Ver extrato")
  (println "5) Ver saldo")
  (println "0) Sair"))

(defn -main [& _]
  ;; carrega config apenas para inicialização, caso queira usar
  (load-config)
  (loop []
    (menu)
    (let [opt (prompt "Escolha uma opção")]
      (case (str/trim opt)
        "1" (register-user-cli)
        "2" (add-food-cli)
        "3" (add-activity-cli)
        "4" (show-extract-cli)
        "5" (show-balance-cli)
        "0" (do (println "Até logo!") (System/exit 0))
        (println "\n❌ Opção inválida!")))
    (recur)))
