(ns calorie-calculator.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [calorie-calculator.db :as db]
            [calorie-calculator.services :as svc]))

(defroutes app-routes
  (POST "/user" req
    (db/set-user! (:body req))
    {:status 200 :body {:msg "Usuário cadastrado"}})

  (POST "/food" req
    (let [{:keys [barcode date qty]} (:body req)
          cals  (* (svc/fetch-food-calories barcode) qty)
          entry {:tipo     :ganho
                 :descricao barcode
                 :date     date
                 :calorias cals}]
      (db/add-entry! entry)
      {:status 200 :body entry}))

  (POST "/activity" req
    (let [{:keys [name date duration]} (:body req)
          cals  (svc/fetch-activity-calories name duration)
          entry {:tipo      :perda
                 :descricao name
                 :date      date
                 :calorias  cals}]
      (db/add-entry! entry)
      {:status 200 :body entry}))

  (GET "/extract" [from to]
    {:status 200 :body (db/get-entries from to)})

  (GET "/balance" [from to]
    {:status 200 :body {:balance (db/balance from to)}})

  (not-found {:status 404 :body {:error "Rota não encontrada"}}))

(def app
  (-> app-routes
      wrap-json-body
      wrap-json-response))
