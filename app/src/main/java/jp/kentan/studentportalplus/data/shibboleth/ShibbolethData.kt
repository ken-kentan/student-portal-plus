package jp.kentan.studentportalplus.data.shibboleth

data class ShibbolethData(
        val username: String,
        val password: String
) {

    companion object {
        val DEMO = ShibbolethData(
                username = "demo",
                password = "EaXZlUbz"
        )
    }
}