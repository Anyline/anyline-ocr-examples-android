package io.anyline.examples.document;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.models.AnylineImage;
import io.anyline.AnylineSDK;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.document.DocumentScanResultListener;
import io.anyline.plugin.document.DocumentScanViewPlugin;
import io.anyline.view.ScanView;

/**
 * Example activity for the Anyline-Document-Detection-Module
 */
public class ScanDocumentActivity extends ScanActivity implements CameraOpenListener {

	private static final String TAG = ScanDocumentActivity.class.getSimpleName();
	private ScanView documentScanView;
	private Toast notificationToast;
	private ImageView imageViewResult;
	private ProgressDialog progressDialog;
	private ImageView imageViewFull;
	private List<PointF> lastOutline;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id.scan_view_placeholder));

		imageViewFull = (ImageView) findViewById(R.id.image_result);

		imageViewResult = (ImageView) findViewById(R.id.full_image);

		documentScanView = (ScanView) findViewById(R.id.scan_view);
		// add a camera open listener that will be called when the camera is opened or an error occurred
		//  this is optional (if not set a RuntimeException will be thrown if an error occurs)
		documentScanView.setCameraOpenListener(this);
		// the view can be configured via a json file in the assets, and this config is set here
		// (alternatively it can be configured via xml, see the Energy Example for that)
//		AnylineSDK.init(getString(R.string.anyline_license_key), this);
		try {
			documentScanView.init("document_view_config.json");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// initialize Anyline with the license key and a Listener that is called if a result is found

		final DocumentScanViewPlugin scanViewPlugin = (DocumentScanViewPlugin) documentScanView.getScanViewPlugin();
		scanViewPlugin.getScanViewPluginConfig().setCancelOnResult(true);
		scanViewPlugin.setCancelOnResult(true);

		scanViewPlugin.addScanResultListener(new DocumentScanResultListener() {

			@Override
			public void onPreviewProcessingSuccess(AnylineImage anylineImage) {

				notificationToast = Toast.makeText(ScanDocumentActivity.this, "Scanning full document. Please hold " +
						"still", Toast.LENGTH_LONG);
				notificationToast.show();

				showToast(getString(R.string.document_preview_success));
			}

			@Override
			public void onPreviewProcessingFailure(DocumentScanViewPlugin.DocumentError error) {
				// this is called on any error while processing the document image
				// Note: this is called every time an error occurs in a run, so that might be quite often
				// An error message should only be presented to the user after some time
				Log.d("Callback", "onPictureProcessingFailure: " + error.name());

			}

			@Override
			public void onPictureProcessingFailure(DocumentScanViewPlugin.DocumentError error) {

				// handle an error while processing the full picture here
				// the preview will be restarted automatically
				String text = getString(R.string.document_picture_error);
				switch (error) {
					case DOCUMENT_NOT_SHARP:
						text += getString(R.string.document_error_not_sharp);
						break;
					case DOCUMENT_SKEW_TOO_HIGH:
						text += getString(R.string.document_error_skew_too_high);
						break;
					case DOCUMENT_OUTLINE_NOT_FOUND:
						text += getString(R.string.document_error_outline_not_found);
						break;
					case GLARE_DETECTED:
						text += getString(R.string.document_error_glare_detected);
						break;
					case IMAGE_TOO_DARK:
						text += getString(R.string.document_error_too_dark);
						break;
//					case UNKNOWN:
//					default:
//						text += getString(R.string.document_error_unknown);
//						break;
				}

				showToast(text);
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}

				AnylineImage image = scanViewPlugin.getCurrentFullImage();

				if (image != null) {
					File outDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "error");
					outDir.mkdir();
					File outFile = new File(outDir, "" + System.currentTimeMillis() + error.name() + ".jpg");
					Log.d(TAG, "saved image to: " + outFile.getAbsolutePath());
					try {
						image.save(outFile, 100);
						Log.d(TAG, "error image saved to " + outFile.getAbsolutePath());
					} catch (IOException e) {
						e.printStackTrace();
					}
					image.release();
				}
			}

			@Override
			public void onTakePictureSuccess() {
				progressDialog = ProgressDialog.show(ScanDocumentActivity.this, "Processing", "Processing the picture" +
						". Please wait", true);
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				if (notificationToast != null) {
					notificationToast.cancel();
				}

			}

			@Override
			public void onTakePictureError(Throwable error) {
				Log.d("Callback", "onTakePictureError: " + error.getMessage());
			}

			@Override
			public void onPictureTransformed(AnylineImage transformedImage) {
				Log.d("Callback", "onPictureTransformed");
			}

			@Override
			public void onPictureTransformError(DocumentScanViewPlugin.DocumentError error) {
				Log.d("Callback", "onPictureTransformError: " + error.name());
			}

			@Override
			public void onPictureCornersDetected(AnylineImage fullFrame, List corners) {
				Log.d("Callback", "onPictureCornersDetected");
			}

			@Override
			// TODO parameter zu Square
			public boolean onDocumentOutlineDetected(List rect, boolean documentShapeAndBrightnessValid) {

				lastOutline = rect;
				return false;

			}

			@Override
			public void onResult(ScanResult result) {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}

				// handle the result document images here
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}

				AnylineImage transformedImage = (AnylineImage) result.getResult();
				AnylineImage fullFrame = result.getFullImage();

				showToast(getString(R.string.document_picture_success));


				// show the picture on the screen
				displayPicture(transformedImage);

				/**
				 * IMPORTANT: cache provided frames here, and release them at the end of this onResult. Because
				 * keeping them in memory (e.g. setting the full frame to an ImageView)
				 * will result in a OutOfMemoryError soon. This error is reported in {@link #onTakePictureError
				 * (Throwable)}
				 *
				 * Use a DiskCache http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html#disk-cache
				 * for example
				 *
				 */
				File outDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ok");
				outDir.mkdir();
				File outFile = new File(outDir, "" + System.currentTimeMillis() + ".jpg");
				try {
					transformedImage.save(outFile, 100);
					showToast("image saved to " + outFile.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}

				// release the images
				transformedImage.release();
				fullFrame.release();
			}
		});
	}

	/**
	 * Performs an animation after the final image was successfully processed. This is just an example.
	 *
	 * @param transformedImage The transformed final image
	 */
	private void displayPicture(AnylineImage transformedImage) {

		imageViewResult.setImageBitmap(Bitmap.createScaledBitmap(transformedImage.getBitmap(), imageViewResult.getWidth(), imageViewResult.getHeight(), false));
		imageViewResult.setVisibility(View.VISIBLE);

		imageViewResult.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				resetScanning();
			}
		});

	}


	   @Override
    protected ScanView getScanView() {
        return null;
    }



	private void showToast(String text) {
		try {
			notificationToast.setText(text);
		} catch (Exception e) {
			notificationToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
		}
		notificationToast.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//start the actual scanning
		documentScanView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//stop the scanning
		documentScanView.stop();
		//release the camera (must be called in onPause, because there are situations where
		// it cannot be auto-detected that the camera should be released)
		documentScanView.releaseCameraInBackground();
	}

	@Override
	public void onCameraOpened(CameraController cameraController, int width, int height) {
		//the camera is opened async and this is called when the opening is finished
		Log.d(TAG, "Camera opened successfully. Frame resolution " + width + " x " + height);
	}

	@Override
	public void onCameraError(Exception e) {
		//This is called if the camera could not be opened.
		// (e.g. If there is no camera or the permission is denied)
		// This is useful to present an alternative way to enter the required data if no camera exists.
		throw new RuntimeException(e);
	}

	@Override
	protected ScanModuleEnum.ScanModule getScanModule() {
		return ScanModuleEnum.ScanModule.DOCUMENT;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			if(imageViewResult.getVisibility() == View.VISIBLE){
				resetScanning();
			}else {
				onBackPressed();
			}
			return true;
		}

		return false;
	}

	@Override
	public void onBackPressed() {
		if(imageViewResult.getVisibility() == View.VISIBLE){
			resetScanning();
		}else {
			super.onBackPressed();
			overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
		}
	}

	private void resetScanning(){
		if(imageViewResult.getVisibility() == View.VISIBLE){
			imageViewResult.setVisibility(View.GONE);
			//if(documentScanView.getScanViewPlugin() != null && !documentScanView.getScanViewPlugin().isRunning() ){
				documentScanView.start();
			//}
		}
	}
}
