package jp.kentan.student_portal_plus.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.data.component.News;
import jp.kentan.student_portal_plus.notification.NotificationController;
import jp.kentan.student_portal_plus.util.LinkTransformationMethod;
import jp.kentan.student_portal_plus.util.StringUtils;

public class NewsActivity extends AppCompatActivity {

    private News mInfo;

    private boolean isNotificationMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);


        /*
        Intent
         */
        Intent intent = getIntent();

        isNotificationMode = intent.getBooleanExtra("notification", false);
        try {
            if(isNotificationMode){
                new NotificationController(this).cancel(intent.getIntExtra("notify_id", -9));

                mInfo = PortalDataProvider.getNewsByHash(this, intent.getStringExtra("hash"));
            }else{
                mInfo = PortalDataProvider.getNewsById(intent.getIntExtra("id", 1));
            }
        } catch (Exception e) {
            failedLoad();
            return;
        }

        mInfo.setRead(true);


        /*
        UI Initialize
         */
        final Context context = this;
        final boolean isFavorite = mInfo.isFavorite();
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setImageDrawable(AppCompatResources.getDrawable(context, (isFavorite) ? R.drawable.ic_star : R.drawable.ic_star_borde));
        fab.setRotation((isFavorite) ? 144.0f : 0.0f);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean isFavorite = !mInfo.isFavorite();
                mInfo.setFavorite(isFavorite);

                String msg = (mInfo.isFavorite()) ? getString(R.string.snackbar_star_set) : getString(R.string.snackbar_star_reset);
                Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                fab.setImageDrawable(AppCompatResources.getDrawable(context, (isFavorite) ? R.drawable.ic_star : R.drawable.ic_star_borde));
                fab.animate().rotation((isFavorite) ? 144.0f : 0.0f).setInterpolator(new OvershootInterpolator()).setDuration(800).start();
            }
        });


        final String strTitle = mInfo.getTitle();

        setTitle(strTitle);

        ((TextView)findViewById(R.id.title)            ).setText(strTitle);
        ((TextView)findViewById(R.id.toolbar_in_charge)).setText(mInfo.getInCharge());
        ((TextView)findViewById(R.id.toolbar_category) ).setText(mInfo.getCategory());
        ((TextView)findViewById(R.id.date)           ).setText(("掲載日: " + mInfo.getDate()));


        //詳細情報がなければ GONE
        final TextView detail   = (TextView)findViewById(R.id.detail);

        final String strDetail = mInfo.getDetail();
        if (StringUtils.isEmpty(strDetail)) {
            findViewById(R.id.header_detail).setVisibility(View.GONE);
            detail.setVisibility(View.GONE);
        } else {
            detail.setText(StringUtils.fromHtml(strDetail));
            detail.setTransformationMethod(new LinkTransformationMethod(this));
        }


        //リンクがなければ GONE
        final TextView link     = (TextView)findViewById(R.id.link);

        final String strLink = mInfo.getLink();
        if (StringUtils.isEmpty(strLink)) {
            findViewById(R.id.header_link).setVisibility(View.GONE);
            findViewById(R.id.link).setVisibility(View.GONE);
        } else {
            link.setText(strLink);
            link.setTransformationMethod(new LinkTransformationMethod(this));
        }
    }

    @Override
    public void onBackPressed() {
        if (isNotificationMode) {
            Intent home = new Intent(getApplicationContext(), HomeActivity.class);
            home.putExtra("view_mode", HomeActivity.VIEW_MODE.NEWS.ordinal());
            startActivity(home);
        }

        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share_and_delete, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInfo.getTitle());
                intent.putExtra(Intent.EXTRA_TEXT, createShareText());
                startActivity(intent);
                break;
            case R.id.action_delete:
                final Context context = this;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.delete));
                builder.setMessage(StringUtils.fromHtml(getString(R.string.msg_delete_latest_info).replaceFirst("\\{title\\}", mInfo.getTitle())));
                builder.setNegativeButton(getString(R.string.cancel), null);
                builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mInfo.delete()) {
                            finish();
                            Toast.makeText(context, getString(R.string.msg_deleted), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, getString(R.string.err_msg_failed_to_delete), Toast.LENGTH_LONG).show();
                        }
                    }

                }).show();
                break;
            default:
                if (isNotificationMode) {
                    Intent home = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(home);
                } else {
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String createShareText() {
        String detail = mInfo.getDetail();
        String link = mInfo.getLink();

        StringBuilder shareText = new StringBuilder("タイトル:");
        shareText.append(mInfo.getTitle());

        if (detail != null) {
            shareText.append("\n内容:");
            shareText.append(detail);
        }

        if (link != null) {
            shareText.append("\nリンク:");
            shareText.append(link);
        }

        shareText.append("\n掲載日:");
        shareText.append(mInfo.getDate());

        return shareText.toString();
    }

    private void failedLoad() {
        Toast.makeText(this, getString(R.string.err_msg_failed_to_load), Toast.LENGTH_LONG).show();
        finish();
    }
}
