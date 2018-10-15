package jp.kentan.studentportalplus.ui.login

import android.app.Application
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethClient
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider
import jp.kentan.studentportalplus.ui.SingleLiveData
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class LoginViewModel(
        private val context: Application,
        private val provider: ShibbolethDataProvider
) : AndroidViewModel(context) {

    val loading = ObservableBoolean()
    val username = ObservableField<String>()
    val password = ObservableField<String>()
    val message = ObservableField<String>()

    val isEnabledErrorUsername = SingleLiveData<Boolean>()
    val isEnabledErrorPassword = SingleLiveData<Boolean>()

    val loginSuccess = SingleLiveData<Unit>()
    val validation = SingleLiveData<ValidationResult>()
    val hideSoftInput = SingleLiveData<Unit>()

    private var loginJob: Job? = null

    init {
        username.setErrorCancelCallback(isEnabledErrorUsername)
        password.setErrorCancelCallback(isEnabledErrorPassword)
    }

    fun onActivityCreated() {
        val username = provider.getUsername() ?: return

        this.username.set(username)
    }

    fun cancelLogin() {
        loginJob?.cancel()
    }

    fun onLoginClick() {
        val username = username.get() ?: ""
        val password = password.get() ?: ""

        var result = ValidationResult()

        if (password.isEmpty()) {
            result = result.copy(isEmptyPassword = true)
        } else if (!password.isValidPassword()) {
            result = result.copy(isInvalidPassword = true)
        }

        if (username.isEmpty()) {
            result = result.copy(isEmptyUsername = true)
        } else if (!username.isValidUsername()) {
            result = result.copy(isInvalidUsername = true)
        }

        if (result.isError) {
            validation.value = result
            return
        }

        login(username, password)
    }

    private fun login(username: String, password: String) {
        hideSoftInput.value = Unit
        loginJob?.cancel()

        loginJob = GlobalScope.launch {
            loading.set(true)

            val (isSuccess, errorMessage) = async {
                ShibbolethClient(this@LoginViewModel.context, provider).auth(username, password)
            }.await()

            if (isSuccess) {
                loginSuccess.postValue(Unit)
            } else {
                message.set(errorMessage
                        ?: this@LoginViewModel.context.getString(R.string.error_unknown))
                loading.set(false)
            }
        }
    }

    private fun ObservableField<String>.setErrorCancelCallback(error: SingleLiveData<Boolean>) {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                error.value = false
            }
        })
    }

    private fun String.isValidUsername() = startsWith('b') || startsWith('m') || startsWith('d')

    private fun String.isValidPassword() = length in 8..24
}