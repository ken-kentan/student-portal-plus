@file:Suppress("DEPRECATION")

package jp.kentan.studentportalplus.data.shibboleth

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import jp.kentan.studentportalplus.data.component.ShibbolethData
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.security.auth.x500.X500Principal


class ShibbolethDataProvider(private val context: Context) {

    private companion object {
        const val TAG = "ShibbolethDataProvider"

        const val KEY_ALIAS = "student_portal_plus_shibboleth_key"
        const val CIPHER_TYPE = "RSA/ECB/PKCS1Padding"
        const val CIPHER_PROVIDER = "AndroidOpenSSL"

        const val KEY_NAME     = "name"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
    }

    private val preferences: SharedPreferences = context.getSharedPreferences("shibboleth", Context.MODE_PRIVATE)
    private val userLiveData = MutableLiveData<Pair<String, String>>() // (name, username)
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

    private fun encryptString(text: String): String? {
        if (text.isEmpty()) {
            Log.e(TAG, "Empty decrypt text")
            return null
        }

        try {
            val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
            val publicKey = privateKeyEntry.certificate.publicKey as RSAPublicKey

            val cipher = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            val outputStream = ByteArrayOutputStream()
            val cipherOutputStream = CipherOutputStream(outputStream, cipher)
            cipherOutputStream.write(text.toByteArray(StandardCharsets.UTF_8))
            cipherOutputStream.close()

            val bytes = outputStream.toByteArray()

            return Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt", e)
        }

        return null
    }

    private fun decryptString(text: String?): String? {
        if (text.isNullOrEmpty()) {
            Log.e(TAG, "Empty encrypt text")
            return null
        }

        try {
            val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry

            val cipher = Cipher.getInstance(CIPHER_TYPE)
            cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)

            val cipherInputStream = CipherInputStream(ByteArrayInputStream(Base64.decode(text, Base64.DEFAULT)), cipher)

            val bytes = cipherInputStream.readBytes()

            return String(bytes, 0, bytes.size, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt", e)
        }

        return null
    }

    fun save(name: String, username: String, password: String) {
        preferences.edit {
            putString(KEY_NAME    , encryptString(name))
            putString(KEY_USERNAME, encryptString(username))
            putString(KEY_PASSWORD, encryptString(password))
        }

        // may be call in background thread
        userLiveData.postValue(Pair(name, username))
    }

    fun getUsername() = decryptString(preferences.getString(KEY_USERNAME, null))

    fun getUser(): LiveData<Pair<String, String>> {
        val result = MediatorLiveData<Pair<String, String>>()

        result.addSource(userLiveData) {
            result.value = it
        }

        bg {
            result.postValue(
                    Pair(
                            decryptString(preferences.getString(KEY_NAME,     null)) ?: "",
                            decryptString(preferences.getString(KEY_USERNAME, null)) ?: "")
            )
        }

        return result
    }

    @Throws(Exception::class)
    fun get() = ShibbolethData(
            username = decryptString(preferences.getString(KEY_USERNAME, null))
                    ?: throw ShibbolethAuthenticationException("ユーザー名の復号に失敗しました"),
            password = decryptString(preferences.getString(KEY_PASSWORD, null))
                    ?: throw ShibbolethAuthenticationException("パスワードの復号に失敗しました")
    )

}