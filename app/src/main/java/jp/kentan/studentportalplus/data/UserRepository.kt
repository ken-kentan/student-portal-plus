package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider

class UserRepository(
        private val provider: ShibbolethDataProvider
) {
    fun getUser() = provider.getUser()
}