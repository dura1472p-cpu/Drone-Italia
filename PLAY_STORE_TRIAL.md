# Google Play — Trial e acquisto definitivo (preparazione)

- Durata desiderata della trial: **5 giorni**.
- Prezzo desiderato: **€ 4,99 una tantum** (il prezzo reale si configura in Play Console, non nel codice).
- ID prodotto in-app non consumabile: `drone_italia_full`.
- Application ID: `it.paolodurante.droneitalia`.
- Libreria: Google Play Billing 8.0.0.
- **Stato: preparazione, nessun blocco trial attivo.** Non dichiarare pronta la monetizzazione.

## Per attivare in sicurezza
1. Creare l'app in Play Console e configurare un prodotto in-app non consumabile `drone_italia_full` a € 4,99.
2. Pubblicare una build nel test interno e aggiungere account tester.
3. Implementare un backend autenticato per verificare i purchase token tramite Google Play Developer API, riconciliare acquisti/rimborsi e gestire il riconoscimento (acknowledgement) entro i tempi previsti.
4. Implementare ripristino e stato licenza verificato, inclusa gestione offline temporanea e errori di rete.
5. Registrare in modo affidabile il primo avvio / l'inizio della prova di 5 giorni lato server; prevenire azzeramenti con reinstallazione o modifica dell'orologio.
6. Solo dopo test end-to-end, attivare il paywall dopo i 5 giorni. Conservare una build di sviluppo senza blocchi.
7. Verificare policy Play Store, privacy e disclosure dei dati eventualmente raccolti.

## Stato codice
`DroneBilling.java` collega Google Play Billing, interroga acquisti e prepara il flusso d'acquisto; **non verifica token, non concede licenze e non blocca l'app**. Il prezzo non è hardcoded perché Google Play lo determina tramite il prodotto configurato in Console.
