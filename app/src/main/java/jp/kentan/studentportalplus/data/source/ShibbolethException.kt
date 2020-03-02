package jp.kentan.studentportalplus.data.source

abstract class ShibbolethException(override val message: String) : Exception(message)

class ShibbolethDecryptException(message: String) : ShibbolethException(message)

class ShibbolethAuthenticationException(message: String) : ShibbolethException(message)

class ShibbolethResponseException(message: String) : ShibbolethException(message)
