package jp.kentan.studentportalplus.ui.lectureinfo.detail

import android.app.Application
import android.content.Intent
import androidx.lifecycle.*
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureAttend
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.ui.SingleLiveData
import jp.kentan.studentportalplus.util.formatYearMonthDay
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch

class LectureInfoDetailViewModel(
        private val context: Application,
        private val portalRepository: PortalRepository
) : AndroidViewModel(context) {

    private val idLiveData = MutableLiveData<Long>()

    val lectureInfo: LiveData<LectureInformation> = Transformations.switchMap(idLiveData) { id ->
        portalRepository.getLectureInfo(id)
    }
    val showAttendNotDialog = SingleLiveData<String>()
    val snackbar = SingleLiveData<Int>()
    val indefiniteSnackbar = SingleLiveData<Int>()
    val share = SingleLiveData<Intent>()
    val errorNotFound = SingleLiveData<Unit>()

    private val lectureInfoObserver = Observer<LectureInformation> { data ->
        if (data == null) {
            errorNotFound.value = Unit
        } else if (!data.isRead) {
            portalRepository.updateLectureInfo(data.copy(isRead = true))
        }
    }

    init {
        lectureInfo.observeForever(lectureInfoObserver)
    }

    fun onActivityCreated(id: Long) {
        idLiveData.value = id
    }

    fun onAttendClick(data: LectureInformation?) {
        data ?: return

        if (data.attend.canAttend()) {
            GlobalScope.launch {
                val isSuccess = portalRepository.addToMyClass(data.copy(attend = LectureAttend.USER)).await()

                if (isSuccess) {
                    snackbar.postValue(R.string.msg_register_class)
                } else {
                    indefiniteSnackbar.postValue(R.string.error_update)
                }
            }
        } else {
            showAttendNotDialog.value = data.subject
        }
    }

    fun onAttendNotClick(subject: String) {
        GlobalScope.launch {
            val isSuccess = portalRepository.deleteFromMyClass(subject).await()

            if (isSuccess) {
                snackbar.postValue(R.string.msg_unregister_class)
            } else {
                indefiniteSnackbar.postValue(R.string.error_update)
            }
        }
    }

    fun onShareClick() {
        val data = lectureInfo.value ?: return

        val text = StringBuilder(
                context.getString(R.string.share_lecture_info,
                        data.subject,
                        data.instructor,
                        data.week,
                        data.period,
                        data.category,
                        data.detailText,
                        data.createdDate.formatYearMonthDay()))

        if (data.createdDate != data.updatedDate) {
            text.append(context.getString(R.string.share_updated_date, data.updatedDate.formatYearMonthDay()))
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, data.subject)
            putExtra(Intent.EXTRA_TEXT, text.toString())
        }

        share.value = Intent.createChooser(intent, null)
    }

    override fun onCleared() {
        lectureInfo.removeObserver(lectureInfoObserver)
        super.onCleared()
    }
}