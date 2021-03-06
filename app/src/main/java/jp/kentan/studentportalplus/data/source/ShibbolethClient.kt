package jp.kentan.studentportalplus.data.source

import android.util.Log
import jp.kentan.studentportalplus.data.Preferences
import jp.kentan.studentportalplus.data.entity.User
import okhttp3.ConnectionSpec
import okhttp3.FormBody
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.TlsVersion
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.CookieManager
import java.util.concurrent.TimeUnit

class ShibbolethClient(
    private val shibbolethDataSource: ShibbolethDataSource,
    private val preferences: Preferences
) {

    private companion object {
        const val TAG = "ShibbolethClient"

        const val CONNECT_TIMEOUT_SEC = 30L
        const val WRITE_TIMEOUT_SEC = 30L
        const val READ_TIMEOUT_SEC = 60L

        const val IDP_AUTHENTICATION_HOST = "auth.cis.kit.ac.jp"
        const val IDP_END_POINT = "https://auth.cis.kit.ac.jp"
        const val IDP_AUTHENTICATION_URL = "https://portal.student.kit.ac.jp/ead/"

        const val INPUT_NAME_USERNAME = "j_username"
        const val INPUT_NAME_PASSWORD = "j_password"

        val SESSION_FORM_PARAMETERS = listOf("shib_idp_ls_supported", "_eventId_proceed")
        val LOGIN_FORM_PARAMETERS = listOf(
            INPUT_NAME_USERNAME,
            INPUT_NAME_PASSWORD
        )
    }

    private val cookieManager = CookieManager()

    private val client = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager))
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .connectionSpecs(createConnectionSpec())
        .build()

    @Throws(ShibbolethException::class)
    fun authenticate(username: String, password: String): User {
        Log.d(TAG, "Authenticate with $IDP_AUTHENTICATION_URL")

        // Clear cookies
        cookieManager.cookieStore.removeAll()

        val document = fetch(IDP_AUTHENTICATION_URL, username, password)

        val name = document.selectFirst("p#user_info")?.text() ?: "unknown"

        Log.d(TAG, "Successful authentication: $name")

        return User(name, username)
    }

    @Throws(ShibbolethException::class)
    fun fetch(url: String) = fetch(url, null, null)

    @Throws(ShibbolethException::class)
    private fun fetch(url: String, username: String?, password: String?): Document {
        Log.d(TAG, "Fetch from $url")

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw ShibbolethResponseException("Error HTTP status code: ${response.code()} ${response.message()}")
        }

        val body = response.body() ?: throw ShibbolethResponseException(
            "Empty response body"
        )

        val document: Document = Jsoup.parse(body.string()).run {
            if (response.isRequireLogin) {
                return@run passLoadingSessionInformationPage()
                    .passLoginPage(
                        username ?: shibbolethDataSource.username,
                        password ?: shibbolethDataSource.password
                    )
                    .passSamlResponsePage()
            }

            return@run this
        }

        Log.d(TAG, "Fetched: ${document.title()}")

        return document
    }

    private fun Document.passLoadingSessionInformationPage(): Document {
        // Ignore if form does not exist
        if (SESSION_FORM_PARAMETERS.any { selectFirst("input[name=$it]") == null }) {
            return this
        }

        val action = selectFirst("form").attr("action")

        val requestBody = FormBody.Builder()
            .add("shib_idp_ls_supported", "false")
            .add("_eventId_proceed", "")
            .build()

        val request = Request.Builder()
            .url(IDP_END_POINT + action)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw ShibbolethResponseException("Error HTTP status code: ${response.code()} ${response.message()}")
        }

        val body = response.body() ?: throw ShibbolethResponseException("Empty response body")

        Log.d(TAG, "Passed: LoadingSessionInformationPage")

        return Jsoup.parse(body.string())
    }

    private fun Document.passLoginPage(username: String, password: String): Document {
        Log.d(TAG, "passLoginPage")
        // Ignore if form does not exist
        if (LOGIN_FORM_PARAMETERS.any { selectFirst("input[name=$it]") == null }) {
            return this
        }

        val action = selectFirst("form").attr("action")

        val requestBody = FormBody.Builder()
            .add(INPUT_NAME_USERNAME, username)
            .add(INPUT_NAME_PASSWORD, password)
            .add("_eventId_proceed", "")
            .build()

        val request = Request.Builder()
            .url(IDP_END_POINT + action)
            .addHeader("Accept-Language", "ja")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw ShibbolethResponseException("Error HTTP status code: ${response.code()} ${response.message()}")
        }

        val body = response.body() ?: throw ShibbolethResponseException(
            "Empty response body"
        )

        Log.d(TAG, "Passed: LoginPage")

        return Jsoup.parse(body.string())
    }

    private fun Document.passSamlResponsePage(): Document {
        Log.d(TAG, "passSamlResponsePage")
        // Invalid username or password
        selectFirst(".form-error")?.run {
            throw ShibbolethAuthenticationException(text())
        }

        val formElem = selectFirst("form")
            ?: throw ShibbolethResponseException("Empty saml response form")
        val relayStateElem = selectFirst("input[name=RelayState]")
            ?: throw ShibbolethResponseException("Empty relay state")
        val samlResponseElem = selectFirst("input[name=SAMLResponse]")
            ?: throw ShibbolethResponseException("Empty saml response")

        val action = formElem.attr("action")
        val relayState = relayStateElem.attr("value")
        val samlResponse = samlResponseElem.attr("value")

        val requestBody = FormBody.Builder()
            .add("RelayState", relayState)
            .add("SAMLResponse", samlResponse)
            .build()

        val request = Request.Builder()
            .url(action)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw ShibbolethResponseException("Error HTTP status code: ${response.code()} ${response.message()}")
        }

        val body = response.body() ?: throw ShibbolethResponseException(
            "Empty response body"
        )

        preferences.updateShibbolethLastLoginDate()

        Log.d(TAG, "Passed: SamlResponsePage")

        return Jsoup.parse(body.string())
    }

    private fun createConnectionSpec(): List<ConnectionSpec> {
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .allEnabledCipherSuites()
            .build()

        return listOf(spec)
    }

    private val Response.isRequireLogin: Boolean
        get() {
            val request = networkResponse()?.request() ?: return false
            return request.url().host() == IDP_AUTHENTICATION_HOST
        }
}
