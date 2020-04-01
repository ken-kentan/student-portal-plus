package jp.kentan.studentportalplus.domain.login

import jp.kentan.studentportalplus.data.Preferences
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val shibbolethClient: ShibbolethClient,
    private val userRepository: UserRepository,
    private val preferences: Preferences
) {

    suspend operator fun invoke(username: String, password: String) = withContext(Dispatchers.IO) {
        val user = shibbolethClient.authenticate(username, password)
        userRepository.add(user, password)

        preferences.isAuthenticatedUser = true
    }
}
