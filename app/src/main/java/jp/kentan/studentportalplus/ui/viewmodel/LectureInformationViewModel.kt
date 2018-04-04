package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.util.toShortString

class LectureInformationViewModel(private val repository: PortalRepository) : ViewModel() {

    lateinit var data: LectureInformation
        private set

    @Throws(Exception::class)
    fun setId(id: Long) {
        data = repository.lectureInformationList.value?.find { it.id == id } ?: throw Exception("Unknown LectureInformation id $id")
    }

    fun getShareText(context: Context): Pair<String, String> {
        val sb = StringBuilder()

        sb.append(context.getString(R.string.text_share_lecture_info,
                data.subject,
                data.instructor,
                data.week,
                data.period,
                data.category,
                data.detailText,
                data.createdDate.toShortString()))

        if (data.createdDate != data.updatedDate) {
            sb.append(context.getString(R.string.text_share_updated_date, data.updatedDate.toShortString()))
        }

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