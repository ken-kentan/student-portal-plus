package jp.kentan.studentportalplus.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.component.NoticeQuery
import jp.kentan.studentportalplus.data.component.PortalData
import jp.kentan.studentportalplus.data.component.PortalDataSet
import jp.kentan.studentportalplus.data.dao.LectureCancellationDao
import jp.kentan.studentportalplus.data.dao.LectureInformationDao
import jp.kentan.studentportalplus.data.dao.MyClassDao
import jp.kentan.studentportalplus.data.dao.NoticeDao
import jp.kentan.studentportalplus.data.dao.database
import jp.kentan.studentportalplus.data.model.Lecture
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.data.parser.LectureCancellationParser
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.data.parser.MyClassParser
import jp.kentan.studentportalplus.data.parser.NoticeParser
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethClient
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider
import jp.kentan.studentportalplus.util.getSimilarSubjectThresholdFloat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences

class PortalRepository(
        private val context: Context,
        shibbolethDataProvider: ShibbolethDataProvider
) {
    private val client = ShibbolethClient(context, shibbolethDataProvider)

    private val similarSubjectThresholdListener: SharedPreferences.OnSharedPreferenceChangeListener

    private val noticeParser = NoticeParser()
    private val lectureInfoParser = LectureInformationParser()
    private val lectureCancelParser = LectureCancellationParser()
    private val myClassParser = MyClassParser()

    private val noticeDao = NoticeDao(context.database)
    private val lectureInfoDao: LectureInformationDao
    private val lectureCancelDao: LectureCancellationDao
    private val myClassDao = MyClassDao(context.database)

    private val _portalDataSet = MutableLiveData<PortalDataSet>()
    private val noticeList = MutableLiveData<List<Notice>>()
    private val lectureInfoList = MutableLiveData<List<LectureInformation>>()
    private val lectureCancelList = MutableLiveData<List<LectureCancellation>>()
    private val _myClassList = MutableLiveData<List<MyClass>>()

    val portalDataSet: LiveData<PortalDataSet>
        get() = _portalDataSet

    val myClassList: LiveData<List<MyClass>>
        get() = _myClassList

    val subjectList: LiveData<List<String>> by lazy {
        return@lazy MediatorLiveData<List<String>>().apply {
            addSource(lectureInfoList) { list ->
                value = list.asSequence()
                        .map { it.subject }
                        .plus(value.orEmpty())
                        .distinct().toList()
            }

            addSource(lectureCancelList) { list ->
                value = list.asSequence()
                        .map { it.subject }
                        .plus(value.orEmpty())
                        .distinct().toList()
            }

            addSource(_myClassList) { list ->
                value = list.asSequence()
                        .map { it.subject }
                        .plus(value.orEmpty())
                        .distinct().toList()
            }
        }
    }

    init {
        val threshold = context.defaultSharedPreferences.getSimilarSubjectThresholdFloat()

        lectureInfoDao = LectureInformationDao(context.database, threshold)
        lectureCancelDao = LectureCancellationDao(context.database, threshold)

        similarSubjectThresholdListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
            if (key == "similar_subject_threshold") {
                val th = pref.getSimilarSubjectThresholdFloat()

                lectureInfoDao.similarThreshold = th
                lectureCancelDao.similarThreshold = th

                GlobalScope.launch {
                    postValues(
                            lectureInfoList = lectureInfoDao.getAll(),
                            lectureCancelList = lectureCancelDao.getAll()
                    )
                }
            }
        }

        context.defaultSharedPreferences.registerOnSharedPreferenceChangeListener(similarSubjectThresholdListener)
    }

    @Throws(Exception::class)
    fun sync() = GlobalScope.async {
        val noticeList = noticeParser.parse(PortalData.NOTICE.fetchDocument())
        val lectureInfoList = lectureInfoParser.parse(PortalData.LECTURE_INFO.fetchDocument())
        val lectureCancelList = lectureCancelParser.parse(PortalData.LECTURE_CANCEL.fetchDocument())
        val myClassList = myClassParser.parse(PortalData.MY_CLASS.fetchDocument())

        myClassDao.updateAll(myClassList)

        val updatedNoticeList = noticeDao.updateAll(noticeList)
        val updatedLectureInfoList = lectureInfoDao.updateAll(lectureInfoList)
        val updatedLectureCancelList = lectureCancelDao.updateAll(lectureCancelList)

        loadFromDb().await()

        return@async mapOf(
                PortalData.NOTICE to updatedNoticeList,
                PortalData.LECTURE_INFO to updatedLectureInfoList,
                PortalData.LECTURE_CANCEL to updatedLectureCancelList
        )
    }

    fun loadFromDb() = GlobalScope.async {
        postValues(
                noticeList = noticeDao.getAll(),
                lectureInfoList = lectureInfoDao.getAll(),
                lectureCancelList = lectureCancelDao.getAll(),
                myClassList = myClassDao.getAll()
        )
    }

    fun getNoticeList(query: NoticeQuery): LiveData<List<Notice>> {
        val result = MediatorLiveData<List<Notice>>()

        result.addSource(noticeList) { list ->
            GlobalScope.launch {
                result.postValue(
                        list.filter { notice ->
                            if (query.isUnread && notice.isRead) {
                                return@filter false
                            }
                            if (query.isRead && !notice.isRead) {
                                return@filter false
                            }
                            if (query.isFavorite && !notice.isFavorite) {
                                return@filter false
                            }
                            if (query.dateRange != NoticeQuery.DateRange.ALL) {
                                return@filter notice.createdDate.time >= query.dateRange.time
                            }
                            if (query.keywordList.isNotEmpty()) {
                                return@filter query.keywordList.any { notice.title.contains(it, true) }
                            }

                            return@filter true
                        }
                )
            }
        }

        return result
    }

    fun getLectureInfoList(query: LectureQuery): LiveData<List<LectureInformation>> {
        val result = MediatorLiveData<List<LectureInformation>>()

        result.addSource(lectureInfoList) { list ->
            GlobalScope.launch {
                val filtered = list.filter { lecture ->
                    if (query.isUnread && lecture.isRead) {
                        return@filter false
                    }
                    if (query.isRead && !lecture.isRead) {
                        return@filter false
                    }
                    if (query.isAttend && !lecture.attend.isAttend()) {
                        return@filter false
                    }
                    if (query.keywordList.isNotEmpty()) {
                        return@filter query.keywordList.any {
                            lecture.subject.contains(it, true) || lecture.instructor.contains(it, true)
                        }
                    }

                    return@filter true
                }

                result.postValue(
                        if (query.order == LectureQuery.Order.ATTEND_CLASS) {
                            filtered.sortedBy { !it.attend.isAttend() }
                        } else {
                            filtered
                        }
                )
            }
        }

        return result
    }

    fun getLectureCancelList(query: LectureQuery): LiveData<List<LectureCancellation>> {
        val result = MediatorLiveData<List<LectureCancellation>>()

        result.addSource(lectureCancelList) { list ->
            GlobalScope.launch {
                val filtered = list.filter { lecture ->
                    if (query.isUnread && lecture.isRead) {
                        return@filter false
                    }
                    if (query.isRead && !lecture.isRead) {
                        return@filter false
                    }
                    if (query.isAttend && !lecture.attend.isAttend()) {
                        return@filter false
                    }
                    if (query.keywordList.isNotEmpty()) {
                        return@filter query.keywordList.any {
                            lecture.subject.contains(it, true) || lecture.instructor.contains(it, true)
                        }
                    }

                    return@filter true
                }

                result.postValue(
                        if (query.order == LectureQuery.Order.ATTEND_CLASS) {
                            filtered.sortedBy { !it.attend.isAttend() }
                        } else {
                            filtered
                        }
                )
            }
        }

        return result
    }

    fun getMyClassSubjectList() = myClassDao.getSubjectList()

    fun getNotice(id: Long): LiveData<Notice> {
        if (noticeList.value == null) {
            loadFromDb()
        }

        return noticeList.map { list -> checkNotNull(list.find { it.id == id }) }
    }

    fun getLectureInfo(id: Long): LiveData<LectureInformation> {
        if (lectureInfoList.value == null) {
            loadFromDb()
        }

        return lectureInfoList.map { list -> checkNotNull(list.find { it.id == id }) }
    }

    fun getLectureCancel(id: Long): LiveData<LectureCancellation> {
        if (lectureCancelList.value == null) {
            loadFromDb()
        }

        return lectureCancelList.map { list -> checkNotNull(list.find { it.id == id }) }
    }

    fun getMyClass(id: Long, isAllowNullOnlyFirst: Boolean = false): LiveData<MyClass> {
        val result = MediatorLiveData<MyClass>()

        var isFirst = true

        result.addSource(_myClassList) { list ->
            val data = list.find { it.id == id }

            if (!isAllowNullOnlyFirst || isFirst || data != null) {
                result.value = checkNotNull(data)
            }

            isFirst = false
        }

        return result
    }

    fun getMyClassWithSync(id: Long) = _myClassList.value?.find { it.id == id }

    fun updateNotice(data: Notice) = GlobalScope.async {
        if (noticeDao.update(data) > 0) {
            postValues(noticeList = noticeDao.getAll())
            return@async true
        }

        return@async false
    }

    fun updateLectureInfo(data: LectureInformation) = GlobalScope.async {
        if (lectureInfoDao.update(data) > 0) {
            postValues(lectureInfoList = lectureInfoDao.getAll())
            return@async true
        }

        return@async false
    }

    fun updateLectureCancel(data: LectureCancellation) = GlobalScope.async {
        if (lectureCancelDao.update(data) > 0) {
            postValues(lectureCancelList = lectureCancelDao.getAll())
            return@async true
        }

        return@async false
    }

    fun updateMyClass(data: MyClass) = GlobalScope.async {
        if (myClassDao.update(data) > 0) {
            postValues(
                    myClassDao.getAll(),
                    lectureInfoDao.getAll(),
                    lectureCancelDao.getAll())
            return@async true
        }

        return@async false
    }

    fun addMyClass(data: MyClass) = GlobalScope.async {
        if (myClassDao.insert(listOf(data)) > 0) {
            postValues(
                    myClassDao.getAll(),
                    lectureInfoDao.getAll(),
                    lectureCancelDao.getAll())
            return@async true
        }

        return@async false
    }

    fun addToMyClass(data: Lecture) = GlobalScope.async {
        try {
            val list = myClassParser.parse(data)
            myClassDao.insert(list)
        } catch (e: Exception) {
            Log.e("PortalRepository", "Failed to add to MyClass", e)
            return@async false
        }

        postValues(
                myClassDao.getAll(),
                lectureInfoDao.getAll(),
                lectureCancelDao.getAll()
        )

        return@async true
    }

    fun deleteFromMyClass(subject: String) = GlobalScope.async {
        try {
            myClassDao.delete(subject)
        } catch (e: Exception) {
            Log.e("PortalRepository", "Failed to delete from MyClass", e)
            return@async false
        }

        postValues(
                myClassDao.getAll(),
                lectureInfoDao.getAll(),
                lectureCancelDao.getAll()
        )

        return@async true
    }

    fun deleteAll() = GlobalScope.async {
        val isSuccess = context.deleteDatabase(context.database.databaseName)

        if (isSuccess) {
            postValues(emptyList(), emptyList(), emptyList(), emptyList())
        }

        return@async isSuccess
    }

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
            this._myClassList.postValue(myClassList)

            set = set.copy(myClassList = myClassList)
        }
        if (lectureInfoList != null) {
            postCount++
            this.lectureInfoList.postValue(lectureInfoList)

            set = set.copy(lectureInfoList = lectureInfoList)
        }
        if (lectureCancelList != null) {
            postCount++
            this.lectureCancelList.postValue(lectureCancelList)

            set = set.copy(lectureCancelList = lectureCancelList)
        }
        if (noticeList != null) {
            postCount++
            this.noticeList.postValue(noticeList)

            set = set.copy(noticeList = noticeList)
        }

        if (postCount > 0 && _portalDataSet.value != set) {
            _portalDataSet.postValue(set)
        }

        Log.d("PortalRepository", "posted $postCount lists")
    }

    private fun PortalData.fetchDocument() = client.fetch(url)
}