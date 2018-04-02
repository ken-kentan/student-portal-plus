package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.MyClass
import org.jetbrains.anko.coroutines.experimental.bg

class MyClassViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    private lateinit var data: MyClass

    private var id: Long = -1
    private val result = MediatorLiveData<MyClass>()

    init {
        result.addSource(portalRepository.myClassLiveData) {
            bg {
                if (id > 0) {
                    result.postValue(portalRepository.getMyClassById(id))
                }
            }
        }
    }

//    fun get(id: Long) = bg {
//        data = portalRepository.getMyClassById(id) ?: throw Exception("Unknown MyClass id: $id")
//        return@bg data
//    }

    fun get(id: Long): LiveData<MyClass> {
        this.id = id
        bg { result.postValue(portalRepository.getMyClassById(id)) }

        return result
    }

    fun getShareText(context: Context): Pair<String, String> {
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
}