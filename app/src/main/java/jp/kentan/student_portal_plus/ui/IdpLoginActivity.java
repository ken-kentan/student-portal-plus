package jp.kentan.student_portal_plus.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.nodes.Document;

import jp.kentan.student_portal_plus.data.shibboleth.AsyncShibbolethClient;
import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.shibboleth.ShibbolethData;
import jp.kentan.student_portal_plus.ui.span.CustomTitle;

/**
 * A login screen that offers login via email/password.
 */
public class IdpLoginActivity extends AppCompatActivity {

    private IdpLoginActivity activity;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private TextView mProgressText;
    private View mProgressView;
    private View mLoginFormView;

    private boolean isWelcome = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle(new CustomTitle(this, getString(R.string.title_activity_login)));

        this.activity = this;
        // Set up the login form.
        mUsernameView = findViewById(R.id.username);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressText = findViewById(R.id.login_progress_text);
        mProgressView = findViewById(R.id.login_progress);


        mUsernameView.setText(new ShibbolethData(this).getUsername());

        /*
        From Welcome Activity
         */
        Intent welcome = getIntent();
        this.isWelcome = welcome.getBooleanExtra("welcome", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = (focusView == null) ? mPasswordView : focusView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = (focusView == null) ? mPasswordView : focusView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, 0);

            showProgress(true);

            AsyncShibbolethClient client = new AsyncShibbolethClient(this, false, new AsyncShibbolethClient.AuthCallback() {
                @Override
                public void updateStatus(String status) {
                    updateProgress(status);
                }

                @Override
                public void failed(AsyncShibbolethClient.FAILED_STATUS status, String errorMessage, Throwable error) {
                    showProgress(false);

                    switch (status) {
                        case ERROR:
                            Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                            break;
                        case INVALID_USERNAME:
                            mUsernameView.setError(getString(R.string.error_incorrect_username));
                            mUsernameView.requestFocus();
                            break;
                        case INVALID_PASSWORD:
                            mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mPasswordView.requestFocus();
                            break;
                    }
                }

                @Override
                public void success(String url, Document document) {
                    showProgress(false);

                    if (isWelcome) {
                        SharedPreferences.Editor editor = getSharedPreferences("common", MODE_PRIVATE).edit();
                        editor.putBoolean("first_time", false);
                        editor.apply();

                        Intent home = new Intent(getApplicationContext(), HomeActivity.class);
                        home.putExtra("first_login", true);
                        startActivity(home);
                    }

                    finish();
                }
            });

            client.authenticate("https://portal.student.kit.ac.jp/ead/", "https://auth.cis.kit.ac.jp", username, password);
        }
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        mProgressText.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressText.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressText.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void updateProgress(final String text) {
        mProgressText.setText(text);
    }

    private boolean isUsernameValid(String username) {
        return username.contains("b") || username.contains("m");
    }

    private boolean isPasswordValid(String password) {
        return 8 <= password.length() && password.length() <= 24;
    }
}

