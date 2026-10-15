# GeoZone Italia — fonti e criteri

Il dataset `italia.geojson` è intenzionalmente vuoto: non è stata verificata una fonte nazionale gratuita, completa e riutilizzabile di geometrie UAS. **Zero geometrie non significa assenza di restrizioni.**

## Requisiti prima di importare una zona

1. Fonte primaria identificabile e URL del documento/dataset.
2. Licenza o autorizzazione alla ripubblicazione verificata.
3. Coordinate in WGS84 EPSG:4326, geometria valida e controllata.
4. Identificativo, tipo di restrizione, quote, validità temporale e data della verifica.
5. Separazione fra zone UAS, spazio aereo aeronautico e semplici infrastrutture.
6. Aggiornamento documentato; non presentare dati incompleti come indicazioni di volo consentito.

Fonti candidate e stato: `fonti.json`. Per i voli consultare sempre https://www.d-flight.it/portal/ .
