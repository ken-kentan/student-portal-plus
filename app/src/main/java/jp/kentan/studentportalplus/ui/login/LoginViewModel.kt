package jp.kentan.studentportalplus.ui.login

import android.app.Application
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.ui.Event
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository,
    private val localPreferences: LocalPreferences
) : AndroidViewModel(application) {

    companion object {
        private const val LOGIN_SUCCESSFUL_DELAY_MILLS = 1000L
    }

    val isLoading = MutableLiveData<Boolean>()

    val isSuccessful = MutableLiveData<Boolean>()

    val message = MutableLiveData<String>()

    val username = MutableLiveData<String>().apply {
        viewModelScope.launch {
            value = userRepository.get()?.username
        }
    }

    val password = MutableLiveData<String>()

    private val _errorUsername = MediatorLiveData<Int>().apply {
        addSource(username) { value = null }
    }
    val errorUsername: LiveData<Int>
        get() = _errorUsername

    private val _errorPassword = MediatorLiveData<Int>().apply {
        addSource(password) { value = null }
    }
    val errorPassword: LiveData<Int>
        get() = _errorPassword

    val onPasswordEditorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
            onLoginClick()
            return@OnEditorActionListener true
        }

        return@OnEditorActionListener false
    }

    private val _navigate = MutableLiveData<Event<Int>>()
    val navigate: LiveData<Event<Int>>
        get() = _navigate

    private val _navigateUp = MutableLiveData<Event<Unit>>()
    val navigateUp: LiveData<Event<Unit>>
        get() = _navigateUp

    private val _hideSoftInput = MutableLiveData<Event<Unit>>()
    val hideSoftInput: LiveData<Event<Unit>>
        get() = _hideSoftInput

    private val unknownErrorMessage = application.getString(R.string.login_unknown_error)

    @IdRes
    private var navigateResId: Int? = null

    fun onViewCreated(@IdRes navigateResId: Int?) {
        this.navigateResId = navigateResId
    }

    fun onLoginClick() {
        _hideSoftInput.value = Event(Unit)

        val username = username.value.orEmpty()
        val password = password.value.orEmpty()

        _errorUsername.value = null
        _errorPassword.value = null

        if (username.isBlank()) {
            _errorUsername.value = R.string.all_field_empty
        } else if (!username.isValidUsername()) {
            _errorUsername.value = R.string.login_invalid_username
        }
        if (password.isBlank()) {
            _errorPassword.value = R.string.all_field_empty
        } else if (!password.isValidPassword()) {
            _errorPassword.value = R.string.login_invalid_password
        }

        if (_errorUsername.value != null || _errorPassword.value != null) {
            return
        }

        login(username, password)
    }

    private fun login(username: String, password: String) {
        isLoading.value = true

        viewModelScope.launch {
            runCatching {
                userRepository.login(username, password)
            }.fold(
                onSuccess = {
                    isSuccessful.value = true
                    message.value = null

                    localPreferences.isAuthenticatedUser = true

                    viewModelScope.launch {
                        delay(LOGIN_SUCCESSFUL_DELAY_MILLS)

                        val resId = navigateResId
                        if (resId != null) {
                            _navigate.value = Event(resId)
                        } else {
                            _navigateUp.value = Event(Unit)
                        }
                    }

                    return@fold
                },
                onFailure = {
                    message.value = it.message ?: unknownErrorMessage
                }
            ).also {
                isLoading.value = false
            }
        }
    }

    private fun String.isValidUsername() = startsWith('b') || startsWith('m') || startsWith('d')

    private fun String.isValidPassword() = length in 8..24
}
