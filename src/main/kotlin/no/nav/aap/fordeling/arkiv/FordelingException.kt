package no.nav.aap.fordeling.arkiv

class FordelingException(msg: String? = null, val journalpost: Journalpost? = null,  cause: Throwable? = null) : RuntimeException(msg, cause)