package jp.kentan.studentportalplus.data.shibboleth


class ShibbolethException(override val message: String) : Exception(message)

class ShibbolethAuthenticationException(override val message: String) : Exception(message)