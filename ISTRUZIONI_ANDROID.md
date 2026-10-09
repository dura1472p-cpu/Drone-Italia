# Drone Italia — beta Android

Questa cartella contiene un **progetto Android Studio**, non un APK installabile.

## Compilazione
1. Aprire la cartella `drone-italia` in Android Studio.
2. Installare Android SDK Platform 36 e JDK 17 se richiesti.
3. Sincronizzare Gradle; se necessario usare una versione Android Gradle Plugin compatibile con SDK 36 (il progetto dichiara 8.13.0).
4. Menu Build > Build APK(s); trasferire l'APK di debug sul telefono e installarlo autorizzando l'installazione da quella fonte.
5. Connettersi a Internet e verificare mappa, GPS, strumenti, persistenza locale e richieste di rete.

## Limiti
- Non compilato né testato su dispositivo in questo ambiente.
- Leaflet caricato da CDN e tessere OSM online: il caricamento dipende dalla rete e dalle policy dei fornitori.
- Tre zone di Roma completamente inventate; nessun dato ENAC/d-flight.
- Piani, preferiti e diario sono locali, non approvazioni o autorizzazioni.
- Nessuna telemetria, controllo del drone o valutazione legale del volo.
- Non pubblicare sul Play Store senza verificare licenze, privacy, test e dati ufficiali.
