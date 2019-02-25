package jp.kentan.studentportalplus.ui.lecturecancel.detail

import android.app.Application
import android.content.Intent
import androidx.lifecycle.*
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureAttend
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.ui.SingleLiveData
import jp.kentan.studentportalplus.util.formatYearMonthDay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class LectureCancelDetailViewModel(
        private val context: Application,
        private val portalRepository: PortalRepository
) : AndroidViewModel(context) {

    private val idLiveData = MutableLiveData<Long>()

    val lectureCancel: LiveData<LectureCancellation> = Transformations.switchMap(idLiveData) { id ->
        portalRepository.getLectureCancel(id)
    }
    val showAttendNotDialog = SingleLiveData<String>()
    val snackbar = SingleLiveData<Int>()
    val indefiniteSnackbar = SingleLiveData<Int>()
    val share = SingleLiveData<Intent>()
    val errorNotFound = SingleLiveData<Unit>()

    private val lectureCancelObserver = Observer<LectureCancellation> { data ->
        if (data == null) {
            errorNotFound.value = Unit
        } else if (!data.isRead) {
            portalRepository.updateLectureCancel(data.copy(isRead = true))
        }
    }

    init {
        lectureCancel.observeForever(lectureCancelObserver)
    }

    fun onActivityCreated(id: Long) {
        idLiveData.value = id
    }

    fun onAttendClick(data: LectureCancellation?) {
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
        } else if (data.attend != LectureAttend.PORTAL) {
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
        val data = lectureCancel.value ?: return

        val text = context.getString(R.string.share_lecture_cancel,
                data.subject,
                data.instructor,
                data.week,
                data.period,
                data.cancelDate.formatYearMonthDay(),
                Jsoup.parse(data.detailHtml).text(),
                data.createdDate.formatYearMonthDay())

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, data.subject)
            putExtra(Intent.EXTRA_TEXT, text.toString())
        }

        share.value = Intent.createChooser(intent, null)
    }

    override fun onCleared() {
        lectureCancel.removeObserver(lectureCancelObserver)
        super.onCleared()
    }
}