@file:Suppress("DEPRECATION")

package jp.kentan.studentportalplus.data.shibboleth

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import jp.kentan.studentportalplus.data.model.User
import kotlinx.coroutines.experimental.launch
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal


class ShibbolethDataProvider(
        private val context: Context
) {

    private companion object {
        const val TAG = "ShibbolethDataProvider"

        const val KEY_ALIAS = "student_portal_plus_shibboleth_key"
        const val CIPHER_TYPE = "RSA/ECB/PKCS1Padding"
        const val CIPHER_PROVIDER = "AndroidOpenSSL"

        const val KEY_NAME     = "name"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"

        val UTF_8: Charset = Charset.forName("UTF-8")
    }

    private val preferences: SharedPreferences = context.getSharedPreferences("shibboleth", Context.MODE_PRIVATE)
    private val userLiveData = MutableLiveData<User>()
    private lateinit var keyStore: KeyStore

    init {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore") ?: throw ShibbolethException("KeyStore is null")
            keyStore.load(null)

            createKeyIfNeed(keyStore)

            this.keyStore = keyStore
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init KeyStore", e)
        }
    }

    @Throws(Exception::class)
    private fun createKeyIfNeed(keyStore: KeyStore) {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return
        }

        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 100)

        val spec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyGenParameterSpec.Builder(
                    KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setUserAuthenticationRequired(false)
                    .setCertificateSubject(X500Principal("CN=Student Portal Plus, O=K2 Studio"))
                    .setCertificateSerialNumber(BigInteger.ONE)
                    .setKeyValidityStart(start.time)
                    .setKeyValidityEnd(end.time)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .build()
        } else {
            KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEY_ALIAS)
                    .setSubject(X500Principal("CN=Student Portal Plus, O=K2 Studio"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()
        }

        val generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
        generator.initialize(spec)
        generator.generateKeyPair()
    }

    private fun encrypt(text: String, enableRetry: Boolean = true): String? {
        if (text.isEmpty()) {
            Log.e(TAG, "Empty decrypt text")
            return null
        }

        try {
            val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey

            val cipher = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            val bytes = cipher.doFinal(text.toByteArray(UTF_8))

            return Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            if (enableRetry) {
                Log.i(TAG, "Recreate key entry", e)

                keyStore.deleteEntry(KEY_ALIAS)
                createKeyIfNeed(keyStore)

                return encrypt(text, false)
            } else {
                Log.e(TAG, "Failed to encrypt", e)
            }
        }

        return null
    }

    private fun decrypt(text: String?): String? {
        if (text.isNullOrEmpty()) {
            Log.e(TAG, "Empty encrypt text")
            return null
        }

        try {
            val privateKey = keyStore.getKey(KEY_ALIAS, null)

            val cipher = Cipher.getInstance(CIPHER_TYPE)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)

            val bytes = cipher.doFinal(Base64.decode(text, Base64.DEFAULT))

            return String(bytes, UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt", e)
        }

        return null
    }

    fun save(name: String, data: ShibbolethData) {
        preferences.edit {
            putString(KEY_NAME    , encrypt(name))
            putString(KEY_USERNAME, encrypt(data.username))
            putString(KEY_PASSWORD, encrypt(data.password))
        }

        // may be call in background thread
        userLiveData.postValue(User(name, data.username))
    }

    fun getUsername() = decrypt(preferences.getString(KEY_USERNAME, null))

    fun getUser(): LiveData<User> {
        val result = MediatorLiveData<User>()

        result.addSource(userLiveData) {
            result.value = it
        }

        launch {
            result.postValue(
                    User(
                            name = decrypt(preferences.getString(KEY_NAME, null)) ?: "",
                            username = decrypt(preferences.getString(KEY_USERNAME, null)) ?: "")
            )
        }

        return result
    }

    @Throws(ShibbolethAuthenticationException::class)
    fun get() = ShibbolethData(
            username = decrypt(preferences.getString(KEY_USERNAME, null))
                    ?: throw ShibbolethAuthenticationException("ユーザー名の復号に失敗しました"),
            password = decrypt(preferences.getString(KEY_PASSWORD, null))
                    ?: throw ShibbolethAuthenticationException("パスワードの復号に失敗しました")
    )

}