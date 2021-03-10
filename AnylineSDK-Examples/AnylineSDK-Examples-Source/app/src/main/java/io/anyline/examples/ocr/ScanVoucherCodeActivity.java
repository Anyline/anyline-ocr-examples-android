
package io.anyline.examples.ocr;
import android.os.Bundle;
import android.view.ViewGroup;

import java.util.HashMap;

import io.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanVoucherCodeActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanVoucherCodeActivity.class.getSimpleName();
    private ScanView scanView;

    @Override
    protected ScanView getScanView() {
        return null;
    }
@Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.VOUCHER;
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

        try {
            scanView.init("voucher_code_view_config.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        OcrScanViewPlugin scanViewPlugin = (OcrScanViewPlugin) scanView.getScanViewPlugin();

        scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
            @Override
            public void onResult(OcrScanResult anylineOcrResult) {

                String result = anylineOcrResult.getResult();

                setFeedbackViewActive(false);

                //set the path for the image which will be shown in the result screen
                String path = setupImagePath(anylineOcrResult.getCutoutImage());
                //start the resultScanView activity
                startScanResultIntent(getResources().getString(R.string.title_voucher), getVoucherResult(result), path);

                setupScanProcessView(ScanVoucherCodeActivity.this, anylineOcrResult, getScanModule());
            }

        });
        scanViewPlugin.setDebugListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        scanView.start();
        createFeedbackView(scanView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanView.stop();
    }

    private HashMap<String, String> getVoucherResult (String voucherResult){
        HashMap<String, String> voucherCodeHashMap = new HashMap<>();

        voucherCodeHashMap.put(getResources().getString(R.string.voucher_reading_result) , (voucherResult == null || voucherResult.isEmpty()) ?  getResources().getString(R.string.not_available) : voucherResult);

        return voucherCodeHashMap;
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