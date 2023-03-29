# aap-routing
___

Denne applikasjonen tar seg av opprettelse av oppgaver for saksbehandlere tilknyttet:
* Arkivsjournaler på tema AAP
* Automatisk og manuelle journalføring av dokumenter sendt in på vegne av borger i Norge.

## Kom i gang med utvikling
___
ygger på JAVA 17 og maven.
- For å jobbe mot devDB i GCP forutsetter det at du installerer [Colima for mac](https://github.com/abiosoft/colima) `brew install colima` eller benytt docker lokalt.
- Se nødvendig oppsett under [Backend for teamet](https://aap-team-innbygger.intern.nav.no/docs/Komme%20i%20gang/komme-i-gang-med-utvikling)
- å bygge lokalt, krever enten koblig mot GCP-dev db eller å [kommentere ut testene i](src/test/kotlin/no/nav/aap/api/søknad/SøknadDBTest.kt)
- `mvn clean install`


## Henvendelser
___

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub

## For NAV-ansatte
___

Interne henvendelser kan sendes via Slack i kanalen #po-aap-værsågod.

