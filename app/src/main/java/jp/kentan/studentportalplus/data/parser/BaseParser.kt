package jp.kentan.studentportalplus.data.parser

import org.jsoup.nodes.Document


abstract class BaseParser {

    @Throws(Exception::class)
    abstract fun parse(document: Document): List<Any>
}