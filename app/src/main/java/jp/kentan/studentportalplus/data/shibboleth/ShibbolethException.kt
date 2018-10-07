package jp.kentan.studentportalplus.data.shibboleth

open class ShibbolethException(override val message: String) : Exception(message)

class ShibbolethAuthenticationException(override val message: String) : ShibbolethException(message)