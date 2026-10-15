# Drone Italia — distribuzione diretta e licenze (progetto)

## Impostazioni concordate
- Distribuzione: APK firmato, senza Google Play Store.
- Trial: **5 giorni** dal primo avvio verificato dal servizio licenze.
- Sblocco permanente: **4,99 EUR**, pagamento singolo.
- Metodi di pagamento previsti: **Stripe Checkout** e **PayPal Checkout**.
- Identificativo prodotto interno: `drone_italia_full`.
- **Stato: preparazione, nessun blocco attivo, nessun pagamento attivo.**

## Architettura da implementare
1. Servizio licenze HTTPS con database, account o identità utente verificabile e data di inizio trial registrata sul server. La reinstallazione e il cambio dell'orologio non devono azzerare la trial.
2. Endpoint per creare sessioni Stripe Checkout e ordini PayPal. **Prezzo e valuta stabiliti sul server**, non dall'APK; nessuna chiave privata nell'app o nel repository.
3. Webhook Stripe con verifica firma e webhook PayPal con verifica autenticità. Registrare pagamenti confermati in modo idempotente; gestire rimborsi e contestazioni.
4. Emissione della licenza solo dopo pagamento confermato server-side. Endpoint autenticato per verificare e ripristinare l'accesso su nuovo dispositivo.
5. Gestire interruzioni di rete con un periodo di tolleranza offline e stato verificato in cache; definire politica sui dispositivi.
6. UI app: visualizzazione giorni residui, pagina acquisto esterna, ripristino licenza. **Non bloccare l'app** prima di test end-to-end riusciti.
7. Informativa privacy, termini, gestione richieste clienti, imposte/fatturazione e policy di distribuzione APK.

## Prerequisiti
- Account Stripe e PayPal Business verificati.
- Dominio HTTPS e servizio backend (hosting/database); indirizzo di supporto.
- Chiavi API e segreti webhook conservati solo nelle variabili d'ambiente del server.
- APK firmato con chiave stabile e distribuito da una fonte HTTPS affidabile.

## Sicurezza
Non fidarsi di localStorage, timestamp del telefono, query string `success`, schermate di conferma o codici statici nell'APK per concedere licenze. L'esito del browser non costituisce prova del pagamento.

## Stato implementazione
Google Play Billing è stato rimosso. Questo file definisce la configurazione e le attività necessarie; non costituisce un checkout funzionante. La versione attuale dell'app resta libera e utilizzabile.
