package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureInformation
import org.jetbrains.anko.coroutines.experimental.bg

class LectureInformationViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    private var data: LectureInformation? = null

    fun get(id: Long) = bg {
        data = portalRepository.getLectureInformationById(id)
        return@bg data
    }

    fun getShareText(context: Context): Pair<String, String> {
        val data = data ?: return Pair("", "")

        val sb = StringBuilder()

//        sb.append(context.getString(R.string.text_share_title, data.title))
//
//        if (data.detailText != null) {
//            sb.append(context.getString(R.string.text_share_detail, data.detailText))
//        }
//
//        if (data.link != null) {
//            sb.append(context.getString(R.string.text_share_link, data.link))
//        }
//
//        sb.append(context.getString(R.string.text_share_created_date, data.createdDate.toShortString()))

        return Pair(data.subject, sb.toString())
    }

    fun getAttendType() = data?.attend ?: throw NullPointerException("Not found a target data")

    fun setAttendByUser(isUser: Boolean) = bg {
        val attend = if (isUser) LectureAttendType.USER else LectureAttendType.NOT

        val data = data?.copy(attend = attend) ?: return@bg Pair(false, null)

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