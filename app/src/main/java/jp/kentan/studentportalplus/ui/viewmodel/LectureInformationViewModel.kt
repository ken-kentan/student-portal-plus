package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.share

class LectureInformationViewModel(private val repository: PortalRepository) : ViewModel() {

    val lectureInfoId = MutableLiveData<Long>()
    val lectureInfo: LiveData<LectureInformation> = Transformations.map(lectureInfoId) { id -> findLectureInfoById(id)}

    fun getCurrentAttendType() = findLectureInfoById(lectureInfoId.value)?.attend

    fun onClickShare(context: Context) {
        val data = findLectureInfoById(lectureInfoId.value) ?: return

        val text = StringBuilder(
                context.getString(R.string.text_share_lecture_info,
                        data.subject,
                        data.instructor,
                        data.week,
                        data.period,
                        data.category,
                        data.detailText,
                        data.createdDate.toShortString()))

        if (data.createdDate != data.updatedDate) {
            text.append(context.getString(R.string.text_share_updated_date, data.updatedDate.toShortString()))
        }

        context.share(text.toString(), data.subject)
    }

    fun onClickAttendToUser(onUpdated: (isSuccess: Boolean) -> Unit) {
        val data = findLectureInfoById(lectureInfoId.value) ?: return

        if (!data.attend.canAttend()) {
            return
        }

        launch(UI) {
            val success = bg { repository.addToMyClass(data.copy(attend = LectureAttendType.USER)) }.await()
            onUpdated(success)
        }
    }

    fun onClickAttendToNot(onUpdated: (isSuccess: Boolean) -> Unit) {
        val data = findLectureInfoById(lectureInfoId.value) ?: return

        // Allow only LectureAttendType.USER
        if (data.attend != LectureAttendType.USER) {
            return
        }

        launch(UI) {
            val success = bg { repository.deleteFromMyClass(data.copy(attend = LectureAttendType.NOT)) }.await()
            onUpdated(success)
        }
    }

    private fun findLectureInfoById(id: Long?): LectureInformation? {
        return if (id == null || id < 1) {
            null
        } else {
            repository.getLectureInformationById(id)?.apply {
                if (!this.hasRead) {
                    bg { repository.update(this.copy(hasRead = true)) }
                }
            }
        }
    }
}