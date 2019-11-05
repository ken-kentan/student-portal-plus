package jp.kentan.studentportalplus.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.kentan.studentportalplus.data.entity.User
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.source.ShibbolethDataSource
import kotlinx.coroutines.*

class UserRepository(
    private val shibbolethClient: ShibbolethClient,
    private val shibbolethDataSource: ShibbolethDataSource,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())
) {

    private val userLiveData = MutableLiveData<User>()

    suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) {
        val user = shibbolethClient.authenticate(username, password)

        shibbolethDataSource.save(user.name, username, password)

        userLiveData.postValue(user)
    }

    fun getUser(): LiveData<User> {
        coroutineScope.launch {
            runCatching {
                User(shibbolethDataSource.name, shibbolethDataSource.username)
            }.fold(
                onSuccess = userLiveData::postValue,
                onFailure = {
                    Log.e("UserRepository", "Failed to get user", it)
                }
            )
        }

        return userLiveData
    }

}
