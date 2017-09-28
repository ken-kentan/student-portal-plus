package jp.kentan.student_portal_plus.data.shibboleth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

import jp.kentan.student_portal_plus.util.StringUtils;


public class ShibbolethData {

    private final static String TAG = "ShibbolethData";

    private static final String KEY_ALIAS = "student_portal_plus_shibboleth_key";

    private static final String CIPHER_TYPE = "RSA/ECB/PKCS1Padding";
    private static final String CIPHER_PROVIDER = "AndroidOpenSSL";

    private final Context mContext;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private KeyStore mKeyStore;


    @SuppressLint("CommitPrefEdits")
    public ShibbolethData(@NonNull Context context) {
        this.mContext = context;

        try {
            mPreferences = context.getSharedPreferences("IDP", Context.MODE_PRIVATE);
            mEditor = mPreferences.edit();

            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);

            createNewKeysIfNeed();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void encrypt(String key, String string){
        final String encrypted = encryptString(string);

        if (encrypted != null) {
            mEditor.putString(key, encrypted);
        }
    }

    private String decrypt(String key) {
        final String encrypted = mPreferences.getString(key, null);

        if (encrypted != null) {
            return decryptString(encrypted);
        } else {
            return "";
        }
    }

    void setName(String name) {
        encrypt("name", name);
    }

    void setUsername(String username) {
        encrypt("username", username);
    }

    void setPassword(String password) {
        encrypt("key", password);
        encrypt("password", UUID.randomUUID().toString());
    }

    public String getName() {
        return decrypt("name");
    }

    public String getUsername() {
        return decrypt("username");
    }

    String getPassword() {
        return decrypt("key");
    }

    void save() {
        mEditor.apply();
    }

    private void createNewKeysIfNeed() {
        try {
            if (mKeyStore.containsAlias(KEY_ALIAS)) return;

            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 100);

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setUserAuthenticationRequired(false)
                        .setCertificateSubject(new X500Principal("CN=Student Portal Plus, O=K2 Studio"))
                        .setCertificateSerialNumber(BigInteger.ONE)
                        .setKeyValidityStart(start.getTime())
                        .setKeyValidityEnd(end.getTime())
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .build();

                generator.initialize(spec);
            } else {
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mContext)
                        .setAlias(KEY_ALIAS)
                        .setSubject(new X500Principal("CN=Student Portal Plus, O=K2 Studio"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                generator.initialize(spec);
            }

            generator.generateKeyPair();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private String encryptString(String text) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(KEY_ALIAS, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            if (StringUtils.isEmpty(text)) {
                return null;
            }

            Cipher cipher = Cipher.getInstance(CIPHER_TYPE, CIPHER_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            cipherOutputStream.write(text.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte[] val = outputStream.toByteArray();

            return Base64.encodeToString(val, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    private String decryptString(String text) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(KEY_ALIAS, null);

            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(text, Base64.DEFAULT)), cipher);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i);
            }

            return new String(bytes, 0, bytes.length, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

}
