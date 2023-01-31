package no.nav.aap.routing.navorganisasjon

data class EnhetsKriteria(val skjermet: Boolean,
                          val geografiskOmraade: String,
                          val diskresjonskode: String="ANY",
                          val tema: String="AAP"
)
