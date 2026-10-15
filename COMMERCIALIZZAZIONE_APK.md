# Drone Italia — vendita APK con PayPal e codici manuali

## Decisione commerciale attuale
- Distribuzione: APK firmato fuori dal Google Play Store.
- Prova gratuita: **5 giorni**.
- Alla scadenza, acquisto obbligatorio per continuare a usare l'app.
- Opzione standard: **4,99 EUR**, sblocco permanente.
- Opzione sostegno extra: **7,99 EUR**, identico sblocco permanente, contributo aggiuntivo sul prezzo base.
- Metodo di pagamento: **PayPal**; Stripe non fa più parte del flusso commerciale scelto.
- Consegna licenza: **manuale**. Il gestore verifica il pagamento PayPal e invia un codice di attivazione al cliente.
- I pagamenti obbligatori sono **acquisti**, non donazioni.
- **Stato: progetto in preparazione. Nessun paywall, trial o pagamento attivo nell'APK.**

## Esperienza utente prevista
1. Al primo utilizzo il cliente inizia la prova di 5 giorni, con indicazione chiara della scadenza.
2. Scaduta la prova, compare una schermata con le due opzioni PayPal (4,99/7,99 EUR), la spiegazione dei tempi di invio del codice e un campo «Ho già un codice».
3. Il cliente paga sul sito PayPal esterno. Per associare pagamento e richiesta di attivazione serve un identificativo d'ordine e un recapito comunicato con informativa privacy.
4. Il gestore controlla il pagamento effettivo nell'account PayPal, inclusi importo, valuta, stato, eventuali duplicati o rimborsi; non si fida di screenshot o schermate di successo.
5. Il gestore genera e invia un codice **univoco**, non prevedibile e non riutilizzabile da altri clienti.
6. L'app verifica la firma crittografica del codice oppure interroga un servizio di verifica. Non inserire nell'APK segreti che permettano di creare codici validi.
7. Il cliente può recuperare la licenza in caso di reinstallazione o cambio telefono tramite procedura di assistenza e identificazione dell'acquisto.

## Sicurezza e limiti
- Un semplice link PayPal.me o pulsante «Dona» non conferma automaticamente il pagamento all'app e non genera codici.
- Per ridurre la dipendenza da un server si può usare un sistema di **licenze firmate asimmetricamente**: chiave privata custodita solo dal gestore, chiave pubblica incorporata nell'app. I codici devono essere legati a un'identità o installazione con una politica esplicita di trasferimento.
- La prova basata soltanto su orologio e memoria locale è aggirabile cancellando dati/reinstallando l'app; per una trial robusta servono identità persistente e verifica server-side.
- Preparare procedura di emissione codici, registro pagamenti, assistenza, rimborsi, privacy e obblighi fiscali prima del lancio.
- Non attivare il blocco dopo 5 giorni finché non sono stati verificati pagamento, generazione codice, attivazione, reinstallazione e ripristino.
- Non inserire credenziali o password PayPal nella chat o nel repository.

## Prossimi dati necessari
- Link PayPal pubblico destinato ai clienti, preferibilmente **due link di pagamento con importi preimpostati**.
- Contatto per richiesta codice: **dura1472P@gmail.com**. Il cliente invia il riferimento della transazione PayPal, non password o dati della carta.
- Regola confermata: **un solo telefono attivo alla volta**, con trasferimento assistito dopo verifica dell'acquisto.

## Componenti precedenti
La cartella `licensing/` contiene un **prototipo Stripe in sandbox non distribuito**. È materiale sperimentale precedente alla decisione PayPal; non costituisce il sistema di licenze attuale e non va attivato per la vendita. Google Play Billing è stato rimosso.

## Link PayPal.Me comunicato dal gestore
- Profilo pubblico: https://paypal.me/pdura1472p
- Link candidato licenza standard 4,99 EUR: https://paypal.me/pdura1472p/4.99EUR
- Link candidato licenza sostegno extra 7,99 EUR: https://paypal.me/pdura1472p/7.99EUR
- **Da verificare manualmente sul sito PayPal:** intestatario corretto, importi in EUR, possibilità che il cliente modifichi l'importo e modalità di pagamento.
- PayPal.Me **non comunica automaticamente all'APK** l'avvenuto pagamento. I codici vanno emessi solo dopo verifica manuale della transazione nell'account PayPal.
- I link sono visibili nell'APK di prova; **non** attivare la scadenza della trial fino a test end-to-end.

## Decisione confermata: un dispositivo e uso offline
- **Una sola attivazione per licenza alla volta**; il codice firmato da solo non impedisce l'uso simultaneo su più telefoni.
- **Prima attivazione online obbligatoria** con servizio di licenze HTTPS che registra un'identità d'installazione e impedisce attivazioni concorrenti.
- **Utilizzo offline consentito** dopo l'attivazione con un'attestazione del server firmata e salvata localmente; non memorizzare segreti di firma nell'APK.
- **Controllo online almeno ogni 30 giorni**, con una **tolleranza offline aggiuntiva di 7 giorni** per evitare blocchi improvvisi in viaggio. I 30+7 giorni sono parametri di progetto da collaudare, non un comportamento già attivo.
- In caso di revoca, rimborso o trasferimento, il server nega il rinnovo; la revoca non può essere garantita istantaneamente mentre il vecchio telefono è offline.
- Per cambio o perdita del telefono: assistenza via dura1472P@gmail.com, verifica del pagamento PayPal, revoca della precedente attivazione e nuova attivazione. Prevedere una procedura sicura per evitare trasferimenti fraudolenti.
- L'app deve distinguere **licenza permanente** da **attestazione periodica di utilizzo**: l'acquisto non scade, ma il controllo periodico richiede Internet.
- Il tempo di prova di 5 giorni deve essere fissato dal server alla prima attivazione/registrazione, non soltanto dall'orologio del telefono; reinstallazione e cambio data non devono azzerarlo.
- Conservare solo i dati indispensabili per licenze, supporto e obblighi legali; definire informativa privacy e cancellazione dati.
- **Stato attuale:** nell'APK funziona soltanto il collaudo locale di codici firmati; non esistono ancora attivazione online, vincolo dispositivo, rinnovo offline, revoche o prova di 5 giorni effettiva. Il blocco resta disattivato.

### Requisiti prima del rilascio commerciale
1. Server HTTPS raggiungibile, database persistente e backup.
2. Endpoint per registrazione trial, attivazione codice, rinnovo attestazione, trasferimento e verifica stato.
3. Identificatore d'installazione non invasivo; attestazioni firmate con scadenza, identificatore di licenza e dispositivo, e contatore/versione revoche.
4. Generatore di codici con chiave privata **definitiva** custodita fuori da GitHub e dall'app; registro transazioni PayPal e controllo emissioni duplicate.
5. Test su due telefoni, modalità aereo, scadenza 30+7 giorni, reinstallazione, cambio data, revoca, rimborso e recupero.
6. Solo dopo tutti i test, attivare l'enforcement della prova di 5 giorni e distribuire un APK firmato per la produzione.
