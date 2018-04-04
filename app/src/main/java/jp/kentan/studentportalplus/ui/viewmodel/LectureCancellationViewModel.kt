package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.util.htmlToText
import jp.kentan.studentportalplus.util.toShortString

class LectureCancellationViewModel(private val repository: PortalRepository) : ViewModel() {

    lateinit var data: LectureCancellation
        private set

    @Throws(Exception::class)
    fun setId(id: Long) {
        data = repository.lectureCancellationList.value?.find { it.id == id } ?: throw Exception("Unknown LectureCancellation id: $id")
    }

    fun getShareText(context: Context): Pair<String, String> {
        val sb = StringBuilder()

        sb.append(context.getString(R.string.text_share_lecture_cancel,
                data.subject,
                data.instructor,
                data.week,
                data.period,
                data.cancelDate.toShortString(),
                data.detailHtml.htmlToText(),
                data.createdDate.toShortString()))

        return Pair(data.subject, sb.toString())
    }

    fun updateAttendByUser(isUser: Boolean): Boolean {
        val data = data.copy(attend = if (isUser) LectureAttendType.USER else LectureAttendType.NOT)

        val success = if (isUser) {
            repository.addToMyClass(data)
        } else {
            repository.deleteFromMyClass(data)
        }

        if (success) {
            this.data = data
        }

        return success
    }
}