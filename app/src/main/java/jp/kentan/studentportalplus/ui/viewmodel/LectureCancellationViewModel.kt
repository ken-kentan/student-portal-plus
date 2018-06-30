package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.share
import org.jsoup.Jsoup

class LectureCancellationViewModel(private val repository: PortalRepository) : ViewModel() {

    val lectureCancelId = MutableLiveData<Long>()
    val lectureCancel: LiveData<LectureCancellation> = Transformations.map(lectureCancelId) { id -> findLectureCancelById(id) }

    fun getCurrentAttendType() = findLectureCancelById(lectureCancelId.value)?.attend

    fun onClickShare(context: Context) {
        val data = lectureCancel.value ?: return
        val text = context.getString(R.string.text_share_lecture_cancel,
                data.subject,
                data.instructor,
                data.week,
                data.period,
                data.cancelDate.toShortString(),
                Jsoup.parse(data.detailHtml).text(),
                data.createdDate.toShortString())

        context.share(text, data.subject)
    }

    fun onClickAttendToUser(onUpdated: (isSuccess: Boolean) -> Unit) {
        val data = findLectureCancelById(lectureCancelId.value) ?: return

        if (!data.attend.canAttend()) {
            return
        }

        launch(UI) {
            val success = bg { repository.addToMyClass(data.copy(attend = LectureAttendType.USER)) }.await()
            onUpdated(success)
        }
    }

    fun onClickAttendToNot(onUpdated: (isSuccess: Boolean) -> Unit) {
        val data = findLectureCancelById(lectureCancelId.value) ?: return

        // Allow only LectureAttendType.USER
        if (data.attend != LectureAttendType.USER) {
            return
        }

        launch(UI) {
            val success = bg { repository.deleteFromMyClass(data.copy(attend = LectureAttendType.NOT)) }.await()
            onUpdated(success)
        }
    }

    private fun findLectureCancelById(id: Long?): LectureCancellation? {
        return if (id == null || id < 1) {
            null
        } else {
            repository.getLectureCancellationById(id)?.apply {
                if (!this.hasRead) {
                    bg { repository.update(this.copy(hasRead = true)) }
                }
            }
        }
    }
}