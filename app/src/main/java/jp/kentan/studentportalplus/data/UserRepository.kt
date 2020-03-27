package jp.kentan.studentportalplus.data

import android.util.Log
import jp.kentan.studentportalplus.data.entity.User
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

    fun getAsFlow(): Flow<User>

    suspend fun get(): User?

    suspend fun add(user: User, password: String)
}

@ExperimentalCoroutinesApi
class DefaultUserRepository(
    private val shibbolethDataSource: ShibbolethDataSource,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())
) : UserRepository {

    private val userChannel = ConflatedBroadcastChannel<User>()

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

    override suspend fun get(): User? = withContext(Dispatchers.IO) {
        runCatching {
            User(shibbolethDataSource.name, shibbolethDataSource.username)
        }.getOrNull()
    }

    override suspend fun add(user: User, password: String) {
        shibbolethDataSource.save(user.name, user.username, password)
        userChannel.send(user)
    }
}
