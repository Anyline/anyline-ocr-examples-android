/*
 * Anyline
 * SettingsActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by clemens at 2015-09-22
 */
package io.anyline.examples.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import io.anyline.examples.BuildConfig;
import io.anyline.examples.R;

public class SettingsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean isTheTitleContainerVisible = true;

    LinearLayout titleContainer;
    View contactButton;
    AppBarLayout appBarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleContainer = (LinearLayout) findViewById(R.id.splash_page);
        appBarLayout = (AppBarLayout) findViewById(R.id.contact_appbar);
        contactButton = findViewById(R.id.contact_us);

        appBarLayout.addOnOffsetChangedListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
    }


    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (isTheTitleContainerVisible) {
                startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                startAlphaAnimation(contactButton, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                isTheTitleContainerVisible = false;
            }

        } else {

            if (!isTheTitleContainerVisible) {
                startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                startAlphaAnimation(contactButton, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                isTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }


//    public void resetData() {
//        ScanHistoryDbHelper.getInstance(this).deleteCompleteScanHistory();
//
//        Intercom.client().logEvent("OCR Scanner Demo Data Reset");
//
//        SingleMessageDialog dialog = new SingleMessageDialog(this, R.string.done, R.drawable.blue_round_checkmark);
//        dialog.setAutoDismissMs(1000);
//        dialog.show();
//
//    }

    public String getVersionInfo() {
        return String.format("App: %s (%s), %s\nSDK: %s (%s)",
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                BuildConfig.FLAVOR,
                at.nineyards.anyline.BuildConfig.VERSION_NAME, at.nineyards.anyline.BuildConfig.VERSION_CODE);
    }


    public void onContactAnyline(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.anyline_email)});
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);

        }
    }
}
