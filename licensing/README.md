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

## Stripe in modalità TEST (aggiunto)

Il server supporta un primo collaudo **esclusivamente con chiave Stripe `sk_test_`**. Non usa chiavi live e non rilascia licenze.

1. Configurare `STRIPE_SECRET_KEY=sk_test_...`, `STRIPE_WEBHOOK_SECRET=whsec_...`, `PUBLIC_BASE_URL=https://<dominio-reale>`, `TEST_CHECKOUT_TOKEN` (segreto casuale di almeno 32 caratteri), `DB_PATH`.
2. Avviare il server dietro proxy HTTPS. Il processo ascolta solo su `127.0.0.1`.
3. Configurare webhook Stripe per `POST /v1/webhooks/stripe` (almeno `checkout.session.completed`, `checkout.session.async_payment_succeeded`, `charge.refunded`). Il server verifica la firma e registra gli ID evento per deduplicazione, **senza sbloccare l'app**.
4. Da terminale amministratore, richiedere `POST /v1/admin/stripe-test-checkout` con header `x-test-checkout-token`. Restituisce URL Stripe Checkout sandbox a 4,99 EUR. Non inserire il token nell'app.
5. I percorsi `/payment-test/success` e `/payment-test/cancel` sono placeholder non ancora implementati: configurare pagine HTTPS dedicate prima di testare il redirect.

**Non distribuire il token amministratore.** Il percorso test va disabilitato/rimosso prima di un lancio commerciale. Restano da implementare autenticazione, verifica pagamento collegata all'utente, ripristino, PayPal e trial server-side. Nessun blocco è attivo.
