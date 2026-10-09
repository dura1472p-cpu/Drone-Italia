# Drone Italia — APK di prova tramite GitHub Actions

1. Su GitHub crea un repository privato vuoto (senza README).
2. Carica **il contenuto della cartella drone-italia** nella radice del repository, inclusa `.github/workflows/android-apk.yml`. Il sito mobile di GitHub può non consentire l'upload di intere cartelle: usa l'interfaccia desktop del browser o un client Git Android che preservi le cartelle e i file nascosti.
3. Controlla che nella radice ci siano `settings.gradle`, `build.gradle`, `app/` e `.github/workflows/android-apk.yml`.
4. Apri la scheda Actions, seleziona «Compila APK demo Android» e premi «Run workflow». Un push su `main` avvia anche la compilazione automaticamente.
5. Al termine, apri la compilazione e scarica l'artifact «Drone-Italia-demo-debug-apk». Estrai lo ZIP per ottenere `app-debug.apk`.
6. Sul telefono apri l'APK e consenti l'installazione da questa fonte solo se ti fidi del file. Disattiva il permesso quando hai finito.

Non è stato compilato o testato su un telefono. Se Actions segnala un errore, condividi il log della fase fallita. APK debug: solo test, non pubblicazione sul Play Store.

**DEMO**: zone inventate, dati aeronautici non ufficiali. L'assenza di restrizioni non significa che il volo sia consentito. La mappa richiede Internet e dipende da servizi esterni.
