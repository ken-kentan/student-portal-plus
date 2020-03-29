package jp.kentan.studentportalplus.data.vo

import jp.kentan.studentportalplus.data.entity.MyCourse
import jp.kentan.studentportalplus.util.JaroWinklerDistance

enum class MyCourseType(
    val isMyCourse: Boolean,
    val canAddToMyCourse: Boolean
) {
    EDITABLE(isMyCourse = true, canAddToMyCourse = false),
    NOT_EDITABLE(isMyCourse = true, canAddToMyCourse = false),
    SIMILAR(isMyCourse = true, canAddToMyCourse = true),
    NOT_FOUND(isMyCourse = false, canAddToMyCourse = true),
    UNKNOWN(isMyCourse = false, canAddToMyCourse = false);
}

fun List<MyCourse>.resolveMyCourseType(
    subject: String,
    threshold: Float
): MyCourseType {
    filter { it.subject == subject }.takeIf { it.isNotEmpty() }?.let { filtered ->
        return if (filtered.all { it.isEditable }) MyCourseType.EDITABLE else MyCourseType.NOT_EDITABLE
    }

    if (any { JaroWinklerDistance.getDistance(it.subject, subject) >= threshold }) {
        return MyCourseType.SIMILAR
    }

    return MyCourseType.NOT_FOUND
}
