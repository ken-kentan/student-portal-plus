package jp.kentan.studentportalplus.data

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import jp.kentan.studentportalplus.data.component.*
import jp.kentan.studentportalplus.data.dao.*
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.data.model.Notice
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

    val noticeLiveData              = MutableLiveData<List<Notice>>()
    val lectureInformationLiveData  = MutableLiveData<List<LectureInformation>>()
    val lectureCancellationLiveData = MutableLiveData<List<LectureCancellation>>()
    val myClassLiveData             = MutableLiveData<List<MyClass>>()


    fun loadFromDb() {
        noticeLiveData.postValue(noticeDao.getAll())
        lectureInformationLiveData.postValue(lectureInfoDao.getAll())
        lectureCancellationLiveData.postValue(lectureCancelDao.getAll())
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

    fun searchNotices(query: NoticeQuery) = noticeDao.search(query)

    fun searchLectureInformations(query: LectureQuery) = lectureInfoDao.search(query)

    fun update(data: Notice) {
        if (noticeDao.update(data) > 0) {
            noticeLiveData.postValue(noticeDao.getAll())
        }
    }

    fun update(data: LectureInformation) {
        if (lectureInfoDao.update(data) > 0) {
            lectureInformationLiveData.postValue(lectureInfoDao.getAll())
        }
    }

    fun update(data: LectureCancellation) {
        if (lectureCancelDao.update(data) > 0) {
            lectureCancellationLiveData.postValue(lectureCancelDao.getAll())
        }
    }

    fun addToMyClass(data: LectureInformation): Pair<Boolean, String?> {
        try {
            val list = myClassParser.parse(data)
            myClassDao.add(list)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add my class", e)
            return Pair(false, e.message)
        }

        myClassLiveData.postValue(myClassDao.getAll())
        lectureInformationLiveData.postValue(lectureInfoDao.getAll())
        lectureCancellationLiveData.postValue(lectureCancelDao.getAll())

        return Pair(true, null)
    }

    fun deleteFromMyClass(data: LectureInformation): Pair<Boolean, String?> {
        try {
            myClassDao.delete(data.subject)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete my class", e)
            return Pair(false, e.message)
        }

        myClassLiveData.postValue(myClassDao.getAll())
        lectureInformationLiveData.postValue(lectureInfoDao.getAll())
        lectureCancellationLiveData.postValue(lectureCancelDao.getAll())

        return Pair(true, null)
    }

    @Throws(Exception::class)
    private fun fetchDocument(type: PortalDataType) = shibbolethClient.fetch(type.url)
}