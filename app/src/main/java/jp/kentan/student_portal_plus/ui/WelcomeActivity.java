package jp.kentan.student_portal_plus.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.ui.span.CustomTitle;


public class WelcomeActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        setTitle(new CustomTitle(this, getString(R.string.title_activity_welcome)));

        mWebView = findViewById(R.id.web_view);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //オンライン取得に失敗
                if (!mWebView.getTitle().contains(getString(R.string.title_terms))) {
                    mWebView.loadUrl(getString(R.string.url_terms_local));
                }
            }
        });
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        mWebView.loadUrl(getString(R.string.url_terms));

        final CheckBox checkBoxAgree = findViewById(R.id.check_box_agree);

        Button buttonIdp = findViewById(R.id.button_shibboleth);
        buttonIdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBoxAgree.isChecked()) {
                    SharedPreferences.Editor editor = getSharedPreferences("common", MODE_PRIVATE).edit();
                    editor.putBoolean("agree_to_the_terms", true);
                    editor.apply();

                    Intent login = new Intent(getApplicationContext(), IdpLoginActivity.class);
                    login.putExtra("welcome", true);
                    startActivity(login);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.err_msg_not_agree), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        final SharedPreferences pref = getSharedPreferences("common", MODE_PRIVATE);

        if(!pref.getBoolean("first_time", true)) finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mWebView == null) return;

        ViewGroup webParent = (ViewGroup) mWebView.getParent();
        webParent.removeView(mWebView);
        mWebView.stopLoading();
        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(null);
        unregisterForContextMenu(mWebView);
        mWebView.removeAllViews();
        mWebView.destroy();
        mWebView = null;
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
