package jp.kentan.studentportalplus.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import jp.kentan.studentportalplus.data.component.*
import jp.kentan.studentportalplus.data.dao.*
import jp.kentan.studentportalplus.data.model.*
import jp.kentan.studentportalplus.data.parser.LectureCancellationParser
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.data.parser.MyClassParser
import jp.kentan.studentportalplus.data.parser.NoticeParser
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethClient
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider
import org.jetbrains.anko.defaultSharedPreferences


class PortalRepository(private val context: Context, shibbolethDataProvider: ShibbolethDataProvider) {

    private companion object {
        const val TAG = "PortalRepository"
    }

    private val shibbolethClient = ShibbolethClient(context, shibbolethDataProvider)

    private val preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    private val noticeParser        = NoticeParser()
    private val lectureInfoParser   = LectureInformationParser()
    private val lectureCancelParser = LectureCancellationParser()
    private val myClassParser       = MyClassParser()

    private val noticeDao        = NoticeDao(context.database)
    private val lectureInfoDao   : LectureInformationDao
    private val lectureCancelDao : LectureCancellationDao
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


    init {
        // Setup with my_class_threshold
        val threshold = context.defaultSharedPreferences.getMyClassThreshold()

        lectureInfoDao = LectureInformationDao(context.database, threshold)
        lectureCancelDao = LectureCancellationDao(context.database, threshold)

        // Update if MyClassThreshold changed
        preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
            if (key == "my_class_threshold") {
                val th = pref.getMyClassThreshold()

                lectureInfoDao.myClassThreshold = th
                lectureCancelDao.myClassThreshold = th

                postValues(
                        lectureInfoList = lectureInfoDao.getAll(),
                        lectureCancelList = lectureCancelDao.getAll()
                )
            }
        }

        context.defaultSharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun loadFromDb() {
        postValues(
                myClassDao.getAll(),
                lectureInfoDao.getAll(),
                lectureCancelDao.getAll(),
                noticeDao.getAll()
        )
    }

    fun syncWithWeb(): Pair<Boolean, String?> {
        try {
            sync()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync", e)
            return Pair(false, e.message)
        }

        return Pair<Boolean, String?>(true, null)
    }

    @Throws(Exception::class)
    fun sync(): Map<PortalDataType, List<NotifyContent>> {
        val noticeList        = noticeParser.parse(fetchDocument(PortalDataType.NOTICE))
        val lectureInfoList   = lectureInfoParser.parse(fetchDocument(PortalDataType.LECTURE_INFORMATION))
        val lectureCancelList = lectureCancelParser.parse(fetchDocument(PortalDataType.LECTURE_CANCELLATION))
        val myCLassList       = myClassParser.parse(fetchDocument(PortalDataType.MY_CLASS))

        myClassDao.updateAll(myCLassList)

        val newNoticeList        = noticeDao.updateAll(noticeList)
        val newLectureInfoList   = lectureInfoDao.updateAll(lectureInfoList)
        val newLectureCancelList = lectureCancelDao.updateAll(lectureCancelList)

        loadFromDb()

        return mapOf(
                PortalDataType.NOTICE to newNoticeList,
                PortalDataType.LECTURE_INFORMATION to newLectureInfoList,
                PortalDataType.LECTURE_CANCELLATION to newLectureCancelList
        )
    }

    fun getNoticeById(id: Long) = _noticeList.value?.find { it.id == id } ?: noticeDao.get(id)

    fun getMyClassById(id: Long) = _myClassList.value?.find { it.id == id }

    fun getMyClassSubjectList() = myClassDao.getSubjectList()

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

    fun deleteAll(): Boolean {
        val success = context.deleteDatabase(context.database.databaseName)

        loadFromDb()

        return success
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
        result.addSource(source) { result.value = it }
        return result
    }

    private fun SharedPreferences.getMyClassThreshold(): Float {
        return (this.getString("my_class_threshold", "80").toIntOrNull() ?: 80) / 100f
    }
}