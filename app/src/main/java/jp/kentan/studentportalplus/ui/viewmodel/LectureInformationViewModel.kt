package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.util.toShortString
import org.jetbrains.anko.coroutines.experimental.bg

class LectureInformationViewModel(private val repository: PortalRepository) : ViewModel() {

    private lateinit var data: LectureInformation

    fun get(id: Long): LiveData<LectureInformation> = Transformations.map(repository.lectureInformationList) {
        data = it.find { it.id == id } ?: return@map null
        return@map data
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

    fun updateAttendByUser(isUser: Boolean) = bg {
        val type = if (isUser) LectureAttendType.USER else LectureAttendType.NOT

        if (isUser) {
            repository.addToMyClass(data.copy(attend = type))
        } else {
            repository.deleteFromMyClass(data.copy(attend = type))
        }
    }
}