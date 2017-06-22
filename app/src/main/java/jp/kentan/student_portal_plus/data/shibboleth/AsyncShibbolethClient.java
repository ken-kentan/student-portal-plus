package jp.kentan.student_portal_plus.data.shibboleth;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.util.StringUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;


public class AsyncShibbolethClient {

    public enum FAILED_STATUS {ERROR, ERROR_UNKNOWN, FAILED_TO_SCRAPING, INVALID_USERNAME, INVALID_PASSWORD, FAILED_TO_DECRYPT, FAILED_TO_ACCESS_IDP}

    private final static String TAG = "AsyncShibbolethClient";

    private final static SimpleDateFormat LOGIN_DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.JAPAN);

    private final static String IDP_END_POINT = "https://auth.cis.kit.ac.jp";

    private final static String INVALID_USERNAME_MSG = "The username you entered cannot be identified.";
    private final static String INVALID_PASSWORD_MSG = "The password you entered was incorrect.";

    private final static int CONNECT_TIMEOUT_SEC = 30, WRITE_TIMEOUT_SEC = 30, READ_TIMEOUT_SEC = 60;

    private final ShibbolethData mShibbolethData;
    private final OkHttpClient mClient;
    private final AuthCallback mCallback;
    private final Context mContext;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final boolean useCookies;//trueの場合、passとusernameが更新される


    public AsyncShibbolethClient(final Context context, final boolean useCookies, @NonNull AuthCallback callback) {
        this.mContext = context;
        this.mCallback = callback;

        this.useCookies = useCookies;

        this.mShibbolethData = new ShibbolethData(mContext);


        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        CookieJar cookieJar;
        if (useCookies) {
            cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(mContext));
        } else {
            cookieJar = new CookieJar() {
                private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<Cookie>();
                }
            };
        }

        builder.cookieJar(cookieJar)
                .followSslRedirects(true)
                .followRedirects(true)
                .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS);


        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){

            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256)
                    .build();

            builder.connectionSpecs(Collections.singletonList(spec));

        }else{//Support TLS v1.2 on Android 4.+ (~19)

            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:"
                            + Arrays.toString(trustManagers));
                }
                X509TrustManager trustManager = (X509TrustManager) trustManagers[0];


                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, new TrustManager[] { trustManager }, null);
                builder.sslSocketFactory(new Tls12SocketFactory(sslContext.getSocketFactory()), trustManager);

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                builder.connectionSpecs(specs);
            } catch (Exception e) {
                Log.e(TAG, "Error while setting TLS 1.2", e);
            }
        }

        mClient = builder.build();
    }

    public void fetchDocument(final String targetUrl) {
        authenticate(targetUrl, IDP_END_POINT, mShibbolethData.getUsername(), mShibbolethData.getPassword());
    }

    private void updateStatus(final String status) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCallback.updateStatus(status);
            }
        });
    }

    private void failed(final FAILED_STATUS status, final Exception e) {
        Log.w(TAG, e);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case ERROR:
                        mCallback.failed(status, e.getLocalizedMessage(), e);
                        break;
                    case ERROR_UNKNOWN:
                        mCallback.failed(status, mContext.getString(R.string.err_msg_unknown), null);
                        break;
                    case FAILED_TO_SCRAPING:
                        mCallback.failed(status, mContext.getString(R.string.err_msg_failed_to_scraping), null);
                        break;
                    case INVALID_USERNAME:
                        mCallback.failed(status, mContext.getString(R.string.error_invalid_username), null);
                        break;
                    case INVALID_PASSWORD:
                        mCallback.failed(status, mContext.getString(R.string.error_incorrect_password), null);
                        break;
                    case FAILED_TO_DECRYPT:
                        mCallback.failed(status, mContext.getString(R.string.error_failed_to_decrypt), null);
                        break;
                    case FAILED_TO_ACCESS_IDP:
                        mCallback.failed(status, mContext.getString(R.string.error_failed_to_access_idp), null);
                        break;
                }
            }
        });
    }

    public void authenticate(final String spEndPoint, final String idpEndPoint, final String username, final String password) {
        Log.d(TAG, "GET: " + spEndPoint);

        updateStatus(spEndPoint + " に接続中...");

        Request request = new Request.Builder()
                .url(spEndPoint)
                .get()
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                updateStatus("レスポンスを処理中...");

                final Document document;

                final Element idpFormElement;
                final String formAction;


                try {
                    document = Jsoup.parse(response.body().string());

                    final String title = document.title();

                    Log.d(TAG, title);

                    if (title.contains("京都工芸繊維大学 学務課ホームページ") || title.contains("京都工芸繊維大学 学生情報ポータル")) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCallback.success(spEndPoint, document);
                            }
                        });
                        return;
                    }

                    idpFormElement = document.select("form").get(0);
                    formAction = idpFormElement.attr("action");
                } catch (Exception e) {
                    failed(FAILED_STATUS.FAILED_TO_ACCESS_IDP, e);
                    return;
                }


                if (formAction.equals("https://portal.student.kit.ac.jp/Shibboleth.sso/SAML2/POST")) {
                    redirectSAMLResponse(spEndPoint, response);
                } else { //need login
                    if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){ //復号化失敗
                        failed(FAILED_STATUS.FAILED_TO_DECRYPT, null);
                        return;
                    }

                    RequestBody requestParams = new FormBody.Builder()
                            .add("j_username", username)
                            .add("j_password", password)
                            .add("_eventId_proceed", "")
                            .build();

                    Log.d(TAG, "POST: " + idpEndPoint + formAction);

                    updateStatus(idpEndPoint + formAction + " にリクエストを送信中...");

                    Request request = new Request.Builder()
                            .url(idpEndPoint + formAction)
                            .post(requestParams)
                            .build();

                    mClient.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(!useCookies){
                                mShibbolethData.setUsername(username);
                                mShibbolethData.setPassword(password);
                            }


                            redirectSAMLResponse(spEndPoint, response);
                        }

                        @Override
                        public void onFailure(Call call, final IOException e) {
                            Log.e(TAG, "onFailure 192:" + e.toString());
                            failed(FAILED_STATUS.ERROR, e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e(TAG, "onFailure 201:" + e.toString());
                failed(FAILED_STATUS.ERROR, e);
            }
        });
    }

    private void redirectSAMLResponse(final String spEndPoint, Response response) throws IOException {
        Log.d(TAG, "redirectSAMLResponse");

        updateStatus("SAMLResponseを処理中...");

        final String body;
        Element authResponseFormElement, relayStateElement, SAMLResponseElement;

        try {
            body = response.body().string();
            Document document = Jsoup.parse(body);

            authResponseFormElement = document.select("form" ).get(0);
            relayStateElement       = document.select("input").get(0);
            SAMLResponseElement     = document.select("input").get(1);
        } catch (Exception e) {
            failed(FAILED_STATUS.FAILED_TO_ACCESS_IDP, null);
            return;
        }

        String action = "", relayStateValue = null, SAMLResponseValue = null;
        if (authResponseFormElement != null) {
            action = authResponseFormElement.attr("action");
        }
        if (relayStateElement != null) {
            relayStateValue = relayStateElement.attr("value");
        }
        if (SAMLResponseElement != null) {
            SAMLResponseValue = SAMLResponseElement.attr("value");
        }

        if (StringUtils.isEmpty(relayStateValue) || StringUtils.isEmpty(SAMLResponseValue)) {
            Log.e(TAG, "invalid username or password.");

            if (body.contains(INVALID_USERNAME_MSG)) {
                failed(FAILED_STATUS.INVALID_USERNAME, null);
            } else if (body.contains(INVALID_PASSWORD_MSG)) {
                failed(FAILED_STATUS.INVALID_PASSWORD, null);
            } else {
                failed(FAILED_STATUS.ERROR_UNKNOWN, null);
            }
        } else {
            RequestBody requestParams = new FormBody.Builder()
                    .add("RelayState", relayStateValue)
                    .add("SAMLResponse", SAMLResponseValue)
                    .build();

            Log.d(TAG, "POST: " + action);
            updateStatus("SAMLResponseを送信中...");

            Request request = new Request.Builder()
                    .url(action)
                    .post(requestParams)
                    .build();

            mClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    final Document document = Jsoup.parse(response.body().string());
                    final Element element = document.getElementById("user_info");

                    if(element != null){
                        mShibbolethData.setName(element.text());
                    }

                    if(!useCookies){
                        mShibbolethData.save();
                    }

                    SharedPreferences.Editor editor = mContext.getSharedPreferences("IDP", Context.MODE_PRIVATE).edit();
                    editor.putString("last_login_date", LOGIN_DATE_FORMAT.format(Calendar.getInstance().getTime()));
                    editor.apply();

                    updateStatus(mContext.getString(R.string.login_progress_done));

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "End SAMLResponse (" + response.code() + ")");
                            mCallback.success(spEndPoint, document);
                        }
                    });
                }

                @Override
                public void onFailure(Call call, final IOException e) {
                    Log.e(TAG, "onFailure 295:" + e.toString());
                    failed(FAILED_STATUS.ERROR, e);
                }
            });
        }
    }


    public interface AuthCallback {
        void updateStatus(final String status);

        void failed(final FAILED_STATUS status, final String errorMessage, Throwable error);

        void success(String url, Document document);
    }
}
