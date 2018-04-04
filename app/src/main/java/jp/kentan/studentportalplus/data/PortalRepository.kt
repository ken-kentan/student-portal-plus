package jp.kentan.studentportalplus.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.component.NoticeQuery
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.data.dao.*
import jp.kentan.studentportalplus.data.model.*
import jp.kentan.studentportalplus.data.parser.LectureCancellationParser
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.data.parser.MyClassParser
import jp.kentan.studentportalplus.data.parser.NoticeParser
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethClient


class PortalRepository(context: Context) {

    private companion object {
        const val TAG = "PortalRepository"
    }

    private val shibbolethClient = ShibbolethClient(context)

    private val noticeParser        = NoticeParser()
    private val lectureInfoParser   = LectureInformationParser()
    private val lectureCancelParser = LectureCancellationParser()
    private val myClassParser       = MyClassParser()

    private val noticeDao        = NoticeDao(context.database)
    private val lectureInfoDao   = LectureInformationDao(context.database)
    private val lectureCancelDao = LectureCancellationDao(context.database)
    private val myClassDao       = MyClassDao(context.database)

    private val _noticeList              = MutableLiveData<List<Notice>>()
    private val _lectureInformationList  = MutableLiveData<List<LectureInformation>>()
    private val _lectureCancellationList = MutableLiveData<List<LectureCancellation>>()
    val myClassLiveData             = MutableLiveData<List<MyClass>>()

    val noticeList: LiveData<List<Notice>>
        get() = copyLiveData(_noticeList)

    val lectureInformationList: LiveData<List<LectureInformation>>
        get() = copyLiveData(_lectureInformationList)

    val lectureCancellationList: LiveData<List<LectureCancellation>>
        get() = copyLiveData(_lectureCancellationList)


    fun loadFromDb() {
        _noticeList.postValue(noticeDao.getAll())
        _lectureInformationList.postValue(lectureInfoDao.getAll())
        _lectureCancellationList.postValue(lectureCancelDao.getAll())
        myClassLiveData.postValue(myClassDao.getAll())
    }

    fun syncWithWeb(): Pair<Boolean, String?> {
        try {
            val noticeList        = noticeParser.parse(fetchDocument(PortalDataType.NOTICE))
            val lectureInfoList   = lectureInfoParser.parse(fetchDocument(PortalDataType.LECTURE_INFORMATION))
            val lectureCancelList = lectureCancelParser.parse(fetchDocument(PortalDataType.LECTURE_CANCELLATION))
            val myCLassList       = myClassParser.parse(fetchDocument(PortalDataType.MY_CLASS))

            noticeDao.updateAll(noticeList)
            lectureInfoDao.updateAll(lectureInfoList)
            lectureCancelDao.updateAll(lectureCancelList)
            myClassDao.updateAll(myCLassList)

            loadFromDb()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync", e)
            return Pair(false, e.message)
        }

        return Pair<Boolean, String?>(true, null)
    }

    fun getNoticeById(id: Long) = noticeDao.get(id)

    fun getLectureInformationById(id: Long) = lectureInfoDao.get(id)

    fun getLectureCancellationById(id: Long) = _lectureCancellationList.value?.find { it.id == id }

    fun getMyClassById(id: Long) = myClassDao.get(id)

    fun searchNotices(query: NoticeQuery) = noticeDao.search(query)

    fun searchLectureInformations(query: LectureQuery) = lectureInfoDao.search(query)

    fun searchLectureCancellations(query: LectureQuery) = lectureCancelDao.search(query)

    fun update(data: Notice) {
        if (noticeDao.update(data) > 0) {
            _noticeList.postValue(noticeDao.getAll())
        }
    }

    fun update(data: LectureInformation) {
        if (lectureInfoDao.update(data) > 0) {
            _lectureInformationList.postValue(lectureInfoDao.getAll())
        }
    }

    fun update(data: LectureCancellation) {
        if (lectureCancelDao.update(data) > 0) {
            _lectureCancellationList.postValue(lectureCancelDao.getAll())
        }
    }

    fun addToMyClass(data: Lecture): Boolean {
        try {
            val list = myClassParser.parse(data)
            myClassDao.add(list)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add to MyClass", e)
            return false
        }

        myClassLiveData.postValue(myClassDao.getAll())
        _lectureInformationList.postValue(lectureInfoDao.getAll())
        _lectureCancellationList.postValue(lectureCancelDao.getAll())

        return true
    }

    fun deleteFromMyClass(data: Lecture): Boolean {
        try {
            myClassDao.delete(data.subject)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete from MyClass", e)
            return false
        }

        myClassLiveData.postValue(myClassDao.getAll())
        _lectureInformationList.postValue(lectureInfoDao.getAll())
        _lectureCancellationList.postValue(lectureCancelDao.getAll())

        return true
    }

    @Throws(Exception::class)
    private fun fetchDocument(type: PortalDataType) = shibbolethClient.fetch(type.url)

    /**
     * Create new LiveData instance from source
     */
    private fun <T> copyLiveData(source: LiveData<T>): LiveData<T> {
        val result = MediatorLiveData<T>()
        result.addSource(source) {result.value = it }
        return result
    }
}