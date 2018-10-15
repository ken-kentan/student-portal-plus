package jp.kentan.studentportalplus.ui.welcome

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.ui.SingleLiveData

class WelcomeViewModel : ViewModel() {

    val isCheckedAgree = ObservableBoolean()
    val startLoginActivity = SingleLiveData<Unit>()

    fun onClickShibboleth() {
        startLoginActivity.value = Unit
    }
}