package io.anyline.examples;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.examples.ocr.feedback.FeedbackView;
import io.anyline.examples.scanviewresult.ScanViewResultActivity;
import io.anyline.examples.util.Constant;
import io.anyline.models.AnylineImage;
import io.anyline.view.ScanView;


abstract public class ScanActivity extends ScanningConfigurationActivity {


//    /**
//     * @return the cutout rect of the corresponding {@link AnylineBaseModuleView}
//     */
//    public abstract Rect getCutoutRect();
//
//    /**
//     * @return the actual used {@link AnylineBaseModuleView}
//     */
//    protected abstract AnylineBaseModuleView getScanView();

    /**
     * @return the module type view {@link io.anyline.examples.ScanModuleEnum.ScanModule}
     */
    protected abstract ScanModuleEnum.ScanModule getScanModule();

    protected long timeStarted;
    protected boolean hasScannedOneTime = false;
    private boolean feedbackViewActive;
    private FeedbackView feedbackView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onResume() {
        super.onResume();
        resetTime();

    }

    /**
     * resets the time used the calculate how many seconds the scan required from startScanning() until a result has been reported
     */
    protected void resetTime() {
        timeStarted = System.currentTimeMillis();
    }

    protected long milliSecondsPassedSinceStartedScanning() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - timeStarted);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
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
    protected void onPause() {
        super.onPause();

    }

    protected void createFeedbackView(final ScanView scanView) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        feedbackView = new FeedbackView(ScanActivity.this);
        RelativeLayout mainLayout;

//        if (scanView.getScanViewPlugin() instanceof LicensePlateScanViewPlugin) {
//            mainLayout = (RelativeLayout) findViewById(R.id.license_plate_main_layout);
//        } else {
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        //}

        mainLayout.addView(feedbackView, params);

        // adapt y position of feedbackView relative to cutout and watermark view
        // notice that you get both view positions only after the ScanView is drawn, therefore use postDelayed
        scanView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        if (scanView == null || scanView.getMeasuredHeight() == 0 || scanView.getMeasuredWidth() == 0) {
                            setFeedbackViewActive(false);
                        } else {
                            //int yPos = feedbackView.calculateYPosition(/**scanView.getScanViewPlugin().getCutoutImageOnSurface()**/scanView.getWatermarkRect(), scanView.getWatermarkRect(),
                            //        scanView
                            //                .getMeasuredHeight());
                            //feedbackView.setY(yPos);
                            //setFeedbackViewActive(true);
                        }
                    }
                });
    }

    protected void setFeedbackViewActive(boolean active) {
        if (feedbackView == null) {
            return;
        } else if (active) {
            feedbackViewActive = true;
        } else {
            feedbackViewActive = false;
            feedbackView.setVisibility(View.INVISIBLE);

        }
    }


    protected void handleFeedback(FeedbackType feedbackType) {
        if (feedbackView == null) {
            return;
        } else if (feedbackViewActive) {
            feedbackView.setFeedbackType(feedbackType);
        }
    }

    protected String setupImagePath(AnylineImage image) {
        String imagePath = "";
        long time = System.currentTimeMillis();
        try {
            if (this.getExternalFilesDir(null) != null) {

                imagePath = this
                        .getExternalFilesDir(null)
                        .toString() + "/results/" + "mrz_image" + time;

            } else if (this.getFilesDir() != null) {

                imagePath = this
                        .getFilesDir()
                        .toString() + "/results/" + "mrz_image" + time;

            }
            File fullFile = new File(imagePath);
            //create the directory
            fullFile.mkdirs();
            image.save(fullFile, 100);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return imagePath;
    }

    protected void startScanResultIntent(String scanMode, LinkedHashMap<String, String> scanResult, String... path) {
        // String path = setupImagePath(anylineOcrResult.getCutoutImage());

        String scanResultDataString = new JSONObject(scanResult).toString();

        Intent i = new Intent(getBaseContext(), ScanViewResultActivity.class);
        i.putExtra(Constant.SCAN_MODULE, scanMode);
        i.putExtra(Constant.SCAN_RESULT_DATA, scanResultDataString);
        if (path.length == 2) {
            i.putExtra(Constant.SCAN_FULL_PICTURE_PATH, path[0]);
            i.putExtra(Constant.SCAN_FACE_PICTURE_PATH, path[1]);
        } else if (path.length == 1) {
            i.putExtra(Constant.SCAN_FULL_PICTURE_PATH, path[0]);
        }

        overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);
        startActivity(i);
    }

}
