(ns calorie-calculator.db)

(def state
  (atom {:user    nil
         :entries []}))

(defn set-user! [user]
  (swap! state assoc :user user))

(defn add-entry! [entry]
  (swap! state update :entries conj entry))

(defn get-entries [from to]
  (filter (fn [{:keys [date]}]
            (and (>= date from) (<= date to)))
          (:entries @state)))

(defn balance [from to]
  (reduce (fn [acc {:keys [tipo calorias]}]
            (if (= tipo :ganho)
              (+ acc calorias)
              (- acc calorias)))
          0
          (get-entries from to)))
