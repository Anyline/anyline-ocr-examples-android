
package io.anyline.examples.ocr;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.apis.AnagramActivity;
import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.AnylineOcrConfig;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;

public class ScanScrabbleActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanScrabbleActivity.class.getSimpleName();
    private ScanView scanView;
    private io.anyline.view.AnylineViewConfig anylineViewConfig;

    @Override
    protected ScanView getScanView() {
        return null;
    }
    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.SCRABBLE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        init();

    }

    void init() {
        scanView = (ScanView) findViewById(R.id.scan_view);

        //Configure the OCR for Scrabble
        final AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        // Use the grid mode, since it's a one line grid with up to 7 characters
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.GRID);
        // set the languages used for OCR
        anylineOcrConfig.setLanguages("scrabble.traineddata");
        // allow only capital letters plus some german Umlaute
        anylineOcrConfig.setCharWhitelist("ABCDEFGHIJKLMNOPQRSTUVWXYZÄÜÖ");
        // set the height range the text can have
        anylineOcrConfig.setMinCharHeight(30);
        anylineOcrConfig.setMaxCharHeight(60);
        // the minimum confidence required to return a result, a value between 0 and 100.
        // (higher confidence means less likely to get a wrong result, but may be slower to get a result)
        anylineOcrConfig.setMinConfidence(80);
        // a simple regex for a basic validation of the scrabble characters. We require at least 4 characters, and a
        // maximum of 10 (usually it would be 7, but Umlaute may use two ASCII symbols)
        anylineOcrConfig.setValidationRegex("^[A-ZÄÜÖ]{7,10}$");
        // the character count in a row may be up to 7
        anylineOcrConfig.setCharCountX(7);
        // and we only have one row of characters
        anylineOcrConfig.setCharCountY(1);
        // the characters may be up to 1.7 times their width (horizontally) apart from each other in this example
        anylineOcrConfig.setCharPaddingXFactor(1.7);
        // the characters may be up to 0.5 times their width (vertically) apart from each other in this example
        // setting the charPaddingYFactor is not necessary in this example, since there is only one row
        anylineOcrConfig.setCharPaddingYFactor(0.5);
        // the text is dark on brither background
        anylineOcrConfig.setIsBrightTextOnDark(false);

        //init the scanViewPlugin config
        scanView.setScanConfig("scrabble_view_config_new.json");

        //init the scan view
        OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(getApplicationContext(), getString(R.string.anyline_license_key), anylineOcrConfig, scanView.getScanViewPluginConfig(), "OCR");


        scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
            @Override
            public void onResult(OcrScanResult result) {

                if (!result.getResult().toString().isEmpty()) {
                    setFeedbackViewActive(false);

                    Intent i = new Intent(ScanScrabbleActivity.this, AnagramActivity.class);
                    i.putExtra(AnagramActivity.SCRABBLE_INPUT, result.getResult().toString().trim());

                    startActivity(i);
                    overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);

                    setupScanProcessView(ScanScrabbleActivity.this, result, getScanModule());

                }
            }

        });
        scanViewPlugin.setDebugListener(this);
        scanView.setScanViewPlugin(scanViewPlugin);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //we use a postdelay for 'start scanning' to improve the user experience:
        //otherwise the scrabble result would be shown faster as the user realizes the scanning
        //process has already started
        scanView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    scanView.start();
                }
            }
        }, 1500);
        createFeedbackView(scanView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanView.stop();
        scanView.releaseCameraInBackground();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setFeedbackViewActive(true);
    }

    @Override
    public void onDebug(String name, Object value) {

        if (AnylineDebugListener.BRIGHTNESS_VARIABLE_NAME.equals(name) &&
                (AnylineDebugListener.BRIGHTNESS_VARIABLE_CLASS.equals(value.getClass()) ||
                        AnylineDebugListener.BRIGHTNESS_VARIABLE_CLASS.isAssignableFrom(value.getClass()))) {
            switch (scanView.getBrightnessFeedBack()) {
                case TOO_BRIGHT:
                    handleFeedback(FeedbackType.TOO_BRIGHT);
                    break;
                case TOO_DARK:
                    handleFeedback(FeedbackType.TOO_DARK);
                    break;
                case OK:
                    handleFeedback(FeedbackType.PERFECT);
                    break;
            }
        } else if(AnylineDebugListener.DEVICE_SHAKE_WARNING_VARIABLE_NAME.equals(name)){
            handleFeedback(FeedbackType.SHAKY);
        }
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }

}