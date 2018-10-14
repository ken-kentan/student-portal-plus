package jp.kentan.studentportalplus.ui.login

data class ValidationResult(
        val isEmptyUsername: Boolean = false,
        val isInvalidUsername: Boolean = false,
        val isEmptyPassword: Boolean = false,
        val isInvalidPassword: Boolean = false
) {
    val isError: Boolean
        get() = isEmptyUsername || isInvalidUsername || isEmptyPassword || isInvalidPassword
}