
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
import io.anyline.plugin.ocr.AnylineOcrConfig;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanRedBullCodeActivity extends ScanActivity implements AnylineDebugListener {

	private static final String TAG = ScanRedBullCodeActivity.class.getSimpleName();
	private ScanView scanView;

	@Override
	protected ScanView getScanView() {
		return null;
	}
	@Override
	protected ScanModuleEnum.ScanModule getScanModule() {
		return ScanModuleEnum.ScanModule.RED_BULL_CODE;
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

		// see ScanScrabbleActivity for a more detailed description
		AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
		//TODO check what to use here
		//anylineOcrConfig.setModel("rbf_jan2015_v2.traineddata");
		anylineOcrConfig.setCharWhitelist("2346789ABCDEFGHKLMNPQRTUVWXYZ");
		anylineOcrConfig.setMinCharHeight(15);
		anylineOcrConfig.setMaxCharHeight(30);
		anylineOcrConfig.setMinConfidence(75);
		anylineOcrConfig.setValidationRegex("^[0-9A-Z]{4}\n[0-9A-Z]{4}");
		anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.GRID);
		anylineOcrConfig.setCharCountX(4);
		anylineOcrConfig.setCharCountY(2);
		anylineOcrConfig.setCharPaddingXFactor(0.3);
		anylineOcrConfig.setCharPaddingYFactor(0.5);


		//init the scanViewPlugin config
		scanView.setScanConfig("rb_view_config.json");
		//init the scan vie plugin
		OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(getApplicationContext(), anylineOcrConfig, scanView.getScanViewPluginConfig(), "OCR");

		scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
			@Override
			public void onResult(OcrScanResult result) {
				String path = setupImagePath(result.getCutoutImage());

				startScanResultIntent(getResources().getString(R.string.title_redbull), getRedbullResult(result.getResult()), path);
				setupScanProcessView(ScanRedBullCodeActivity.this, result, getScanModule());
			}

		});
		scanViewPlugin.setDebugListener(this);
		scanView.setScanViewPlugin(scanViewPlugin);
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

	protected HashMap<String, String> getRedbullResult(String result) {

		HashMap<String, String> serialNumberResult = new HashMap();

		serialNumberResult.put(getResources().getString(R.string.redbull_reading_result), (result.isEmpty() || result ==null) ? getResources().getString(R.string.not_available) : result );

		return serialNumberResult;
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