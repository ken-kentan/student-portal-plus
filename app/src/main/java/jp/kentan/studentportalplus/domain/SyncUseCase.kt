package jp.kentan.studentportalplus.domain

import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.DocumentParser
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val shibbolethClient: ShibbolethClient,
    private val attendCourseRepository: AttendCourseRepository,
    private val lectureInformationRepository: LectureInformationRepository,
    private val lectureCancellationRepository: LectureCancellationRepository,
    private val noticeRepository: NoticeRepository
) {

    companion object {
        private const val ATTEND_COURSE_URL =
            "https://portal.student.kit.ac.jp/ead/?c=attend_course"
        private const val LECTURE_INFO_URL =
            "https://portal.student.kit.ac.jp/ead/?c=lecture_information"
        private const val LECTURE_CANCEL_URL =
            "https://portal.student.kit.ac.jp/ead/?c=lecture_cancellation"
        private const val NOTICE_URL = "https://portal.student.kit.ac.jp"
    }

    suspend operator fun invoke(): SyncUseCaseResult = withContext(Dispatchers.IO) {
        val attendCourseDocument = shibbolethClient.fetch(ATTEND_COURSE_URL)
        val attendCourseList = DocumentParser.parseAttendCourse(attendCourseDocument)
        attendCourseRepository.updateAll(attendCourseList)

        val lectureInfoDeferred = async {
            val document = shibbolethClient.fetch(LECTURE_INFO_URL)
            val lectureInfoList = DocumentParser.parseLectureInformation(document)
            lectureInformationRepository.updateAll(lectureInfoList)
        }

        val lectureCancelDeferred = async {
            val document = shibbolethClient.fetch(LECTURE_CANCEL_URL)
            val lectureCancelList = DocumentParser.parseLectureCancellation(document)
            lectureCancellationRepository.updateAll(lectureCancelList)
        }

        val noticeDeferred = async {
            val document = shibbolethClient.fetch(NOTICE_URL)
            val noticeList = DocumentParser.parseNotice(document)
            noticeRepository.updateAll(noticeList)
        }

        return@withContext SyncUseCaseResult(
            updatedLectureInformationList = lectureInfoDeferred.await(),
            updatedLectureCancellationList = lectureCancelDeferred.await(),
            updatedNoticeList = noticeDeferred.await()
        )
    }
}

data class SyncUseCaseResult(
    val updatedLectureInformationList: List<LectureInformation>,
    val updatedLectureCancellationList: List<LectureCancellation>,
    val updatedNoticeList: List<Notice>
)
