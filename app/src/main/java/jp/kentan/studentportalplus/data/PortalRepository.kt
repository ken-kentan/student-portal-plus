package jp.kentan.studentportalplus.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.component.NoticeQuery
import jp.kentan.studentportalplus.data.component.PortalDataSet
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.data.dao.*
import jp.kentan.studentportalplus.data.model.*
import jp.kentan.studentportalplus.data.parser.LectureCancellationParser
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.data.parser.MyClassParser
import jp.kentan.studentportalplus.data.parser.NoticeParser
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethClient
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider


class PortalRepository(private val context: Context, shibbolethDataProvider: ShibbolethDataProvider) {

    private companion object {
        const val TAG = "PortalRepository"
    }

    private val shibbolethClient = ShibbolethClient(context, shibbolethDataProvider)

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
    private val _myClassList             = MutableLiveData<List<MyClass>>()
    private val _portalDataSet           = MutableLiveData<PortalDataSet>()

    val noticeList: LiveData<List<Notice>>
        get() = copyLiveData(_noticeList)

    val lectureInformationList: LiveData<List<LectureInformation>>
        get() = copyLiveData(_lectureInformationList)

    val lectureCancellationList: LiveData<List<LectureCancellation>>
        get() = copyLiveData(_lectureCancellationList)

    val myClassList: LiveData<List<MyClass>>
        get() = copyLiveData(_myClassList)

    val portalDataSet: LiveData<PortalDataSet>
        get() = copyLiveData(_portalDataSet)

    val subjectList: LiveData<List<String>>
        get() {
            val result = MediatorLiveData<List<String>>()

            result.addSource(_lectureInformationList) {
                it?.let {
                    val old = result.value?.toList() ?: emptyList()
                    result.value = it.map { it.subject }.plus(old).distinct()
                }
            }
            result.addSource(_lectureCancellationList) {
                it?.let {
                    val old = result.value?.toList() ?: emptyList()
                    result.value = it.map { it.subject }.plus(old).distinct()
                }
            }
            result.addSource(_myClassList) {
                it?.let {
                    val old = result.value?.toList() ?: emptyList()
                    result.value = it.map { it.subject }.plus(old).distinct()
                }
            }

            return result
        }


    fun loadFromDb() {
        val noticeList = noticeDao.getAll()
        _noticeList.postValue(noticeList)

        val lectureInfoList = lectureInfoDao.getAll()
        _lectureInformationList.postValue(lectureInfoList)

        val lectureCancelList = lectureCancelDao.getAll()
        _lectureCancellationList.postValue(lectureCancelList)

        val myClassList = myClassDao.getAll()
        _myClassList.postValue(myClassList)

        _portalDataSet.postValue(PortalDataSet(
                noticeList        = noticeList,
                lectureInfoList   = lectureInfoList,
                lectureCancelList = lectureCancelList,
                myClassList       = myClassList
        ))
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

    fun getMyClassById(id: Long) = _myClassList.value?.find { it.id == id }

    fun searchNotices(query: NoticeQuery) = noticeDao.search(query)

    fun searchLectureInformation(query: LectureQuery) = lectureInfoDao.search(query)

    fun searchLectureCancellations(query: LectureQuery) = lectureCancelDao.search(query)

    fun update(data: Notice): Boolean {
        if (noticeDao.update(data) > 0) {
            postValues(noticeList = noticeDao.getAll())
            return true
        }
        return false
    }

    fun update(data: LectureInformation) {
        if (lectureInfoDao.update(data) > 0) {
            postValues(lectureInfoList = lectureInfoDao.getAll())
        }
    }

    fun update(data: LectureCancellation) {
        if (lectureCancelDao.update(data) > 0) {
            postValues(lectureCancelList = lectureCancelDao.getAll())
        }
    }

    fun update(data: MyClass): Boolean {
        if (myClassDao.update(data) > 0) {
            postValues(myClassDao.getAll())
            return true
        }
        return false
    }

    fun add(data: MyClass): Boolean {
        if (myClassDao.add(listOf(data)) > 0) {
            postValues(myClassDao.getAll())
            return true
        }
        return false
    }

    fun delete(subject: String): Boolean {
        if (myClassDao.delete(subject) > 0) {
            postValues(myClassDao.getAll())
            return true
        }
        return false
    }

    fun addToMyClass(data: Lecture): Boolean {
        try {
            val list = myClassParser.parse(data)
            myClassDao.add(list)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add to MyClass", e)
            return false
        }

        postValues(
                myClassDao.getAll(),
                lectureInfoDao.getAll(),
                lectureCancelDao.getAll()
        )

        return true
    }

    fun deleteFromMyClass(data: Lecture): Boolean {
        try {
            myClassDao.delete(data.subject)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete from MyClass", e)
            return false
        }

        postValues(
                myClassDao.getAll(),
                lectureInfoDao.getAll(),
                lectureCancelDao.getAll()
        )

        return true
    }

    fun deleteAllFromDb() {
        context.deleteDatabase(context.database.databaseName)

        loadFromDb()
    }

    @Throws(Exception::class)
    private fun fetchDocument(type: PortalDataType) = shibbolethClient.fetch(type.url)

    private fun postValues(
            myClassList: List<MyClass>? = null,
            lectureInfoList: List<LectureInformation>? = null,
            lectureCancelList: List<LectureCancellation>? = null,
            noticeList: List<Notice>? = null
    ) {
        var postCount = 0
        var set = _portalDataSet.value ?: PortalDataSet()

        if (myClassList != null) {
            postCount++
            _myClassList.postValue(myClassList)

            set = set.copy(myClassList = myClassList)
        }
        if (lectureInfoList != null) {
            postCount++
            _lectureInformationList.postValue(lectureInfoList)

            set = set.copy(lectureInfoList = lectureInfoList)
        }
        if (lectureCancelList != null) {
            postCount++
            _lectureCancellationList.postValue(lectureCancelList)

            set = set.copy(lectureCancelList = lectureCancelList)
        }
        if (noticeList != null) {
            postCount++
            _noticeList.postValue(noticeList)

            set = set.copy(noticeList = noticeList)
        }

        if (postCount > 0) {
            _portalDataSet.postValue(set)
        }

        Log.d(TAG, "posted $postCount lists")
    }

    /**
     * Create new LiveData instance from source
     */
    private fun <T> copyLiveData(source: LiveData<T>): LiveData<T> {
        val result = MediatorLiveData<T>()
        result.addSource(source) {result.value = it }
        return result
    }
}