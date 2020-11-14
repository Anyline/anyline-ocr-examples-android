
package io.anyline.examples.ocr;
import android.os.Bundle;
import android.view.ViewGroup;
import java.util.HashMap;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;

public class ScanIbanActivity extends ScanActivity implements AnylineDebugListener {

	private static final String TAG = ScanIsbnActivity.class.getSimpleName();
	private ScanView scanView;

	   @Override
    protected ScanView getScanView() {
        return null;
    }


	@Override
	protected ScanModuleEnum.ScanModule getScanModule() {
		return ScanModuleEnum.ScanModule.IBAN;
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
			scanView.init("iban_view_config.json");
		} catch (Exception e) {
			e.printStackTrace();
		}
		OcrScanViewPlugin scanViewPlugin = (OcrScanViewPlugin) scanView.getScanViewPlugin();

		scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
			@Override
			public void onResult(OcrScanResult result) {

				setFeedbackViewActive(false);

				String ibanResult = result.getResult();

				String path = setupImagePath(result.getCutoutImage());
				startScanResultIntent(getResources().getString(R.string.title_iban), getIbanResult(ibanResult), path);

				setupScanProcessView(ScanIbanActivity.this, result, getScanModule());
			}

		});

		scanViewPlugin.setDebugListener(this);
	}

	private HashMap<String, String> getIbanResult (String ibanResult){
		HashMap<String, String> ibanHashMap = new HashMap<>();

		ibanHashMap.put(getResources().getString(R.string.iban_reading_result) , (ibanResult == null || ibanResult.isEmpty()) ?  getResources().getString(R.string.not_available) : ibanResult);

		return ibanHashMap;
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

	@Override
	protected void onRestart() {
		super.onRestart();
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