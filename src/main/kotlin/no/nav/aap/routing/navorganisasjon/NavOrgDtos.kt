package no.nav.aap.routing.navorganisasjon

data class EnhetsKriteria(val diskresjonskode: String,
                      val oppgavetype: String,
                      val behandlingstype: String,
                      val behandlingstema: String,
                      val tema: String,
                      val geografiskOmraade: String,
                      val skjermet: Boolean)
