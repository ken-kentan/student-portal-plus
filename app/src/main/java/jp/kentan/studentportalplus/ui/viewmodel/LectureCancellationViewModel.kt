package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.util.htmlToText
import jp.kentan.studentportalplus.util.toShortString
import org.jetbrains.anko.coroutines.experimental.bg

class LectureCancellationViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    private lateinit var data: LectureCancellation

    @Throws(Exception::class)
    fun get(id: Long) = bg {
        data = portalRepository.getLectureCancellationById(id) ?: throw Exception("Unknown LectureCancellation id: $id")
        return@bg data
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

    fun getAttendType() = data.attend

    fun setAttendByUser(isUser: Boolean) = bg {
        val attend = if (isUser) LectureAttendType.USER else LectureAttendType.NOT

        val data = data.copy(attend = attend)

        val result = if (isUser) {
            portalRepository.addToMyClass(data)
        } else {
            portalRepository.deleteFromMyClass(data)
        }

        if (result.first) {
            this.data = data
        }

        return@bg result
    }
}