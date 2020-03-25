package jp.kentan.studentportalplus.data.source

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.Calendar
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

class ShibbolethDataSource(
    private val context: Context
) {

    private companion object {
        const val TAG = "ShibbolethDataSource"

        const val KEY_ALIAS = "student_portal_plus_shibboleth_key"

        const val PRINCIPAL_NAME = "CN=Student Portal Plus, O=K2 Studio"

        const val CIPHER_TYPE = "RSA/ECB/PKCS1Padding"
        const val CIPHER_PROVIDER = "AndroidOpenSSL"

        const val KEY_NAME = "name"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"

        val UTF_8: Charset = Charset.forName("UTF-8")
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences("shibboleth", Context.MODE_PRIVATE)

    private val keyStore = KeyStore.getInstance("AndroidKeyStore")?.apply {
        load(null)
        createKeyIfNeed()
    } ?: throw ShibbolethDecryptException("KeyStore is null")

    val name: String
        get() = decrypt(preferences.getString(KEY_NAME, null))
            ?: throw ShibbolethDecryptException("名前の復号に失敗しました")

    val username: String
        get() = decrypt(preferences.getString(KEY_USERNAME, null))
            ?: throw ShibbolethDecryptException("ユーザー名の復号に失敗しました")

    val password: String
        get() = decrypt(preferences.getString(KEY_PASSWORD, null))
            ?: throw ShibbolethDecryptException("パスワードの復号に失敗しました")

    fun save(name: String, username: String, password: String) {
        preferences.edit {
            putString(KEY_NAME, encrypt(name))
            putString(KEY_USERNAME, encrypt(username))
            putString(KEY_PASSWORD, encrypt(password))
        }
    }

    private fun KeyStore.createKeyIfNeed(): KeyStore {
        if (containsAlias(KEY_ALIAS)) {
            return this
        }

        val start = Calendar.getInstance()
        val end = Calendar.getInstance().apply {
            add(Calendar.YEAR, 100)
        }

        val spec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyGenParameterSpec.Builder(
                KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setUserAuthenticationRequired(false)
                .setCertificateSubject(X500Principal(PRINCIPAL_NAME))
                .setCertificateSerialNumber(BigInteger.ONE)
                .setKeyValidityStart(start.time)
                .setKeyValidityEnd(end.time)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build()
        } else {
            @Suppress("DEPRECATION")
            KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(X500Principal(PRINCIPAL_NAME))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.time)
                .setEndDate(end.time)
                .build()
        }

        KeyPairGenerator.getInstance("RSA", "AndroidKeyStore").run {
            initialize(spec)
            generateKeyPair()
        }

        return this
    }

    private fun encrypt(text: String, isEnabledRetry: Boolean = true): String? {
        if (text.isEmpty()) {
            Log.e(TAG, "Empty decrypt text")
            return null
        }

        try {
            val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey

            val bytes = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER).run {
                init(Cipher.ENCRYPT_MODE, publicKey)
                return@run doFinal(text.toByteArray(UTF_8))
            }

            return Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            if (isEnabledRetry) {
                Log.i(TAG, "Recreate key entry", e)

                keyStore.run {
                    deleteEntry(KEY_ALIAS)
                    createKeyIfNeed()
                }

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

            val bytes = Cipher.getInstance(CIPHER_TYPE).run {
                init(Cipher.DECRYPT_MODE, privateKey)
                return@run doFinal(Base64.decode(text, Base64.DEFAULT))
            }

            return String(bytes, UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt", e)
        }

        return null
    }
}
