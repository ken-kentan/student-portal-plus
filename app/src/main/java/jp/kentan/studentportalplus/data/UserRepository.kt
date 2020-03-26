package jp.kentan.studentportalplus.data

import android.util.Log
import jp.kentan.studentportalplus.data.entity.User
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.source.ShibbolethDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface UserRepository {
    suspend fun login(username: String, password: String)

    suspend fun get(): User?

    fun getAsFlow(): Flow<User>
}

@ExperimentalCoroutinesApi
class DefaultUserRepository(
    private val shibbolethClient: ShibbolethClient,
    private val shibbolethDataSource: ShibbolethDataSource,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())
) : UserRepository {

    private val userChannel = ConflatedBroadcastChannel<User>()

    override suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) {
        val user = shibbolethClient.authenticate(username, password)

        shibbolethDataSource.save(user.name, username, password)
        userChannel.send(user)
    }

    override suspend fun get(): User? = withContext(Dispatchers.IO) {
        runCatching {
            User(shibbolethDataSource.name, shibbolethDataSource.username)
        }.getOrNull()
    }

    override fun getAsFlow(): Flow<User> {
        coroutineScope.launch {
            runCatching {
                User(shibbolethDataSource.name, shibbolethDataSource.username)
            }.fold(
                onSuccess = {
                    userChannel.offer(it)
                },
                onFailure = {
                    Log.e("UserRepository", "Failed to get user", it)
                }
            )
        }

        return userChannel.asFlow()
    }
}
