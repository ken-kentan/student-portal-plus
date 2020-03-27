package jp.kentan.studentportalplus.domain

import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.Lecture
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.data.resolveAttendCourseType
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.vo.LectureNotificationType
import jp.kentan.studentportalplus.data.vo.NoticeNotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val shibbolethClient: ShibbolethClient,
    private val attendCourseRepository: AttendCourseRepository,
    private val lectureInformationRepository: LectureInformationRepository,
    private val lectureCancellationRepository: LectureCancellationRepository,
    private val noticeRepository: NoticeRepository,
    private val localPreferences: LocalPreferences
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
        attendCourseRepository.updateAll(
            DocumentParser.parseAttendCourse(attendCourseDocument)
        )

        val attendCourseList = attendCourseRepository.getAll()
        val similarSubjectThreshold = localPreferences.similarSubjectThreshold

        val lectureInfoDeferred = async {
            val document = shibbolethClient.fetch(LECTURE_INFO_URL)
            val lectureInfoList = DocumentParser.parseLectureInformation(document)
            lectureInformationRepository.updateAll(lectureInfoList)
                .filterWith(
                    localPreferences.lectureInformationNotificationType,
                    attendCourseList,
                    similarSubjectThreshold
                )
        }

        val lectureCancelDeferred = async {
            val document = shibbolethClient.fetch(LECTURE_CANCEL_URL)
            val lectureCancelList = DocumentParser.parseLectureCancellation(document)
            lectureCancellationRepository.updateAll(lectureCancelList)
                .filterWith(
                    localPreferences.lectureCancellationNotificationType,
                    attendCourseList,
                    similarSubjectThreshold
                )
        }

        val noticeDeferred = async {
            val document = shibbolethClient.fetch(NOTICE_URL)
            val noticeList = DocumentParser.parseNotice(document)
            noticeRepository.updateAll(noticeList)
                .filterWith(localPreferences.noticeNotificationType)
        }

        return@withContext SyncUseCaseResult(
            updatedLectureInformationList = lectureInfoDeferred.await(),
            updatedLectureCancellationList = lectureCancelDeferred.await(),
            updatedNoticeList = noticeDeferred.await()
        )
    }

    private fun <T : Lecture> List<T>.filterWith(
        notificationType: LectureNotificationType,
        attendCourseList: List<AttendCourse>,
        threshold: Float
    ): List<T> = when (notificationType) {
        LectureNotificationType.ALL -> this
        LectureNotificationType.ATTEND -> filter {
            attendCourseList.resolveAttendCourseType(it.subject, threshold).isAttend
        }
        LectureNotificationType.NOT -> emptyList()
    }

    private fun List<Notice>.filterWith(notificationType: NoticeNotificationType) =
        when (notificationType) {
            NoticeNotificationType.ALL -> this
            NoticeNotificationType.IMPORTANT -> filter {
                it.title.contains("重要") || it.title.contains("注意")
            }
            NoticeNotificationType.NOT -> emptyList()
        }
}

data class SyncUseCaseResult(
    val updatedLectureInformationList: List<LectureInformation>,
    val updatedLectureCancellationList: List<LectureCancellation>,
    val updatedNoticeList: List<Notice>
)
