(ns calorie-calculator.core
  (:gen-class)
  (:require [calorie-calculator.db :as db]
            [calorie-calculator.services :as svc]
            [calorie-calculator.config :refer [load-config]]
            [clojure.string :as str]))

(defn prompt [msg]
  (println msg)
  (print "> ")
  (flush)
  (read-line))

(defn parse-int [s]
  (try
    (Integer/parseInt s)
    (catch Exception _ 0)))      ;; agora captura Exception corretamente

(defn parse-float [s]
  (try
    (Double/parseDouble s)
    (catch Exception _ 0.0)))   ;; idem para floats

(defn register-user-cli []
  (let [nome   (prompt "Nome:")
        idade  (prompt "Idade:")
        sexo   (prompt "Sexo (M/F):")
        peso   (prompt "Peso (kg):")
        altura (prompt "Altura (m):")]
    (db/set-user! {:nome   nome
                   :idade  (parse-int idade)
                   :sexo   (keyword (str/upper-case sexo))
                   :peso   (parse-float peso)
                   :altura (parse-float altura)})
    (println "\nUsuário cadastrado com sucesso!" (:user @db/state))))

(defn add-food-cli []
  (let [descricao (prompt "Nome do alimento:")
        date      (prompt "Data (YYYY-MM-DD):")
        qty       (parse-int (prompt "Quantidade:"))
        cals      (* (svc/fetch-food-calories descricao) qty)
        entry     {:tipo      :ganho
                   :descricao descricao
                   :date      date
                   :calorias  cals}]
    (db/add-entry! entry)
    (println "\nAlimento registrado:" entry)))

(defn add-activity-cli []
  (let [descricao (prompt "Atividade física:")
        date      (prompt "Data (YYYY-MM-DD):")
        dur       (parse-int (prompt "Duração (min):"))
        cals      (svc/fetch-activity-calories descricao dur)
        entry     {:tipo      :perda
                   :descricao descricao
                   :date      date
                   :calorias  cals}]
    (db/add-entry! entry)
    (println "\nAtividade registrada:" entry)))

(defn show-extract-cli []
  (let [from  (prompt "Extrato de (YYYY-MM-DD):")
        to    (prompt "Até (YYYY-MM-DD):")
        itens (db/get-entries from to)]
    (println "\n--- Extrato de Transações ---")
    (if (seq itens)
      (doseq [e itens] (println e))
      (println "Nenhuma transação no período."))
    (println "-----------------------------")))

(defn show-balance-cli []
  (let [from (prompt "Saldo de (YYYY-MM-DD):")
        to   (prompt "Até (YYYY-MM-DD):")
        bal  (db/balance from to)]
    (println (format "\nSaldo de calorias entre %s e %s: %d"
                     from to bal))))

(defn menu []
  (println "\n=== Calculadora de Calorias ===")
  (println "1) Cadastrar usuário")
  (println "2) Registrar alimento")
  (println "3) Registrar atividade")
  (println "4) Ver extrato")
  (println "5) Ver saldo")
  (println "0) Sair"))

(defn -main [& _]
  (load-config)
  (loop []
    (menu)
    (case (str/trim (prompt "Escolha uma opção"))
      "1" (register-user-cli)
      "2" (add-food-cli)
      "3" (add-activity-cli)
      "4" (show-extract-cli)
      "5" (show-balance-cli)
      "0" (do (println "Até logo!") (System/exit 0))
      (println "Opção inválida!"))
    (recur)))
