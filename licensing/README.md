# Servizio licenze Drone Italia (bozza tecnica)

Servizio separato dall'APK. **Non è ancora collegato all'app, né distribuito, né abilitato al pagamento reale.**

## Obiettivo
Trial di 5 giorni; acquisto definitivo di EUR 4,99 via Stripe e PayPal; ripristino licenza. Il blocco nell'APK resta disabilitato finché il sistema non sarà collaudato.

## Preparazione
1. Node.js >=20, `npm install` nella cartella `licensing`.
2. Copiare `.env.example` in `.env` e configurare segreti reali sul server (non su GitHub).
3. `npm start` e verificare `GET /health`.
4. Distribuire su HTTPS, configurare webhook firmati e account Stripe / PayPal Business.
5. Collegare in una fase successiva l'APK a un'identità utente verificata e a questo server.

## Endpoint preparati
- `GET /health`: stato del servizio.
- `GET /v1/config`: prezzo, trial e disponibilità (attualmente checkout disabilitato).

## Da realizzare prima di attivare gli acquisti
Identità verificata (email con OTP o login), database transazionale, creazione sessioni Stripe Checkout e ordini PayPal server-side, webhook verificati, rimborsi, ripristino e rate limiting. Mai considerare il redirect del browser prova di acquisto. Non usare un ID dispositivo come unica identità per una licenza a pagamento.

Non usare questo scheletro per vendite finché non sono stati implementati e testati i punti precedenti.
