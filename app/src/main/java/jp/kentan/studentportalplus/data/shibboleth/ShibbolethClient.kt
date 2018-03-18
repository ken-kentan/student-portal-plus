package jp.kentan.studentportalplus.data.shibboleth

import android.content.Context
import android.os.Build
import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import java.util.concurrent.TimeUnit
import okhttp3.TlsVersion
import okhttp3.ConnectionSpec
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import okhttp3.FormBody
import org.jetbrains.anko.doFromSdk


class ShibbolethClient(private val context: Context) {

    private companion object {
        const val TAG = "ShibbolethClient"
        const val CONNECT_TIMEOUT_SEC: Long = 30
        const val WRITE_TIMEOUT_SEC  : Long = 30
        const val READ_TIMEOUT_SEC   : Long = 60

        const val IDP_AUTH_HOST = "auth.cis.kit.ac.jp"
        const val IDP_END_POINT = "https://auth.cis.kit.ac.jp"
        const val IDP_AUTH_URL  = "https://portal.student.kit.ac.jp/ead/"

        const val INPUT_NAME_USERNAME = "j_username"
        const val INPUT_NAME_PASSWORD = "j_password"

        val SESSION_FORM_PARAMS = listOf("shib_idp_ls_supported", "_eventId_proceed")
        val LOGIN_FORM_PARAMS = listOf(INPUT_NAME_USERNAME, INPUT_NAME_PASSWORD)
    }

    private val shibbolethDataProvider = ShibbolethDataProvider(context)
    private val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
    private var httpClient: OkHttpClient? = null

    init {
        val builder = OkHttpClient.Builder()
        builder.cookieJar(cookieJar)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)

        enableTls12(builder)

        val spec = setupConnectionSpec()
        builder.connectionSpecs(spec)

        httpClient = builder.build()
    }

    private fun setupConnectionSpec(): List<ConnectionSpec> {
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .allEnabledCipherSuites()
                .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
            return listOf(spec, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT)
        }

        return listOf(spec)
    }

    private fun enableTls12(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        doFromSdk(Build.VERSION_CODES.KITKAT_WATCH) {
            return builder
        }

        try {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)

            val trustManagers = trustManagerFactory.trustManagers
            if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                throw IllegalStateException("Unexpected default trust managers: $trustManagers")
            }

            val trustManager = trustManagers[0] as X509TrustManager

            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
            builder.sslSocketFactory(Tls12SocketFactory(sslContext.socketFactory), trustManager)
        } catch (e: Exception) {
            Log.e(TAG, "Error while setting TLS 1.2", e)
        }

        return builder
    }

    fun auth(username: String, password: String): Pair<Boolean, String?> {
        Log.d(TAG, "Auth with $IDP_AUTH_URL")

        // Clear shibboleth cache
        cookieJar.clear()

        // Set auth data
        shibbolethDataProvider.set(username, password)

        try {
            fetch(IDP_AUTH_URL)
        } catch (e: Exception) {
            shibbolethDataProvider.reset()
            Log.e(TAG, "Failed to auth", e)
            return Pair(false, e.message)
        }

        // Save auth data
        shibbolethDataProvider.save()

        Log.d(TAG, "Auth success")

        return Pair(true, null)
    }

    @Throws(Exception::class)
    fun fetch(url: String): Document {
        Log.d(TAG, "Fetch from $url")

        val request = Request.Builder()
                .url(url)
                .build()

        val response = httpClient?.newCall(request)?.execute() ?: throw ShibbolethException("Empty response")
        val body     = response.body()                         ?: throw ShibbolethException("Empty response body")

        if (!response.isSuccessful) {
            throw ShibbolethException("Error HTTP status code: ${response.code()} ${response.message()}")
        }

        var document = Jsoup.parse(body.string())

        if (isRequireLogin(response)) {
            document = passLoadingSessionInformationPage(document)
            document = passLoginPage(document)
            document = passSamlResponsePage(document)
        }

        Log.d(TAG, "Fetched: ${document.title()}")

        return document
    }

    @Throws(Exception::class)
    private fun passLoadingSessionInformationPage(document: Document): Document {
        // Ignore if form not exist
        SESSION_FORM_PARAMS.forEach{
            document.selectFirst("input[name=$it]") ?: return document
        }

        val action = document.selectFirst("form").attr("action")

        val requestBody = FormBody.Builder()
                .add("shib_idp_ls_supported", "false")
                .add("_eventId_proceed"     , "")
                .build()

        val request = Request.Builder()
                .url(IDP_END_POINT + action)
                .post(requestBody)
                .build()

        val response = httpClient?.newCall(request)?.execute() ?: throw ShibbolethException("Empty response")
        val body     = response.body()                         ?: throw ShibbolethException("Empty response body")

        if (!response.isSuccessful) {
            throw ShibbolethException("Error HTTP status code: ${response.code()} ${response.message()}")
        }

        Log.d(TAG, "LoadingSessionInformationPage passed")

        return Jsoup.parse(body.string())
    }


    @Throws(Exception::class)
    private fun passLoginPage(document: Document): Document {
        // Ignore if form not exist
        LOGIN_FORM_PARAMS.forEach{
            document.selectFirst("input[name=$it]") ?: return document
        }

        val action = document.selectFirst("form").attr("action")

        val (username, password) = shibbolethDataProvider.get()

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

        val response = httpClient?.newCall(request)?.execute() ?: throw ShibbolethException("Empty response")
        val body     = response.body()                         ?: throw ShibbolethException("Empty response body")

        if (!response.isSuccessful) {
            throw ShibbolethException("Error HTTP status code: ${response.code()} ${response.message()}")
        }

        Log.d(TAG, "LoginPage passed")

        return Jsoup.parse(body.string())
    }

    @Throws(Exception::class)
    private fun passSamlResponsePage(document: Document): Document {
        // If username or password invalid
        val formErrorElem = document.selectFirst(".form-error")
        if (formErrorElem != null) {
            throw ShibbolethException(formErrorElem.text())
        }

        val formElem         = document.selectFirst("form")                     ?: throw ShibbolethException("Empty saml response form")
        val relayStateElem   = document.selectFirst("input[name=RelayState]")   ?: throw ShibbolethException("Empty relay state")
        val samlResponseElem = document.selectFirst("input[name=SAMLResponse]") ?: throw ShibbolethException("Empty saml response")

        val action       = formElem.attr("action")
        val relayState   = relayStateElem.attr("value")
        val samlResponse = samlResponseElem.attr("value")

        val requestBody = FormBody.Builder()
                .add("RelayState"  , relayState)
                .add("SAMLResponse", samlResponse)
                .build()

        val request = Request.Builder()
                .url(action)
                .post(requestBody)
                .build()

        val response = httpClient?.newCall(request)?.execute() ?: throw ShibbolethException("Empty response")
        val body     = response.body()                         ?: throw ShibbolethException("Empty response body")

        if (!response.isSuccessful) {
            throw ShibbolethException("Error HTTP status code: ${response.code()} ${response.message()}")
        }

        Log.d(TAG, "SamlResponsePage passed")

        return Jsoup.parse(body.string())
    }

    private fun isRequireLogin(response: Response): Boolean {
        val netResponse = response.networkResponse() ?: return false
        val request     = netResponse.request()      ?: return false

        return request.url().host() == IDP_AUTH_HOST
    }
}