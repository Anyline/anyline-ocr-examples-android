package io.anyline.examples.document;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.document.DocumentResult;
import at.nineyards.anyline.modules.document.DocumentResultListener;
import at.nineyards.anyline.modules.document.DocumentScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;

/**
 * Example activity for the Anyline-Document-Detection-Module
 */
public class ScanDocumentActivity extends ScanActivity implements CameraOpenListener {

    private static final String TAG = ScanDocumentActivity.class.getSimpleName();
    private DocumentScanView documentScanView;
    private Toast notificationToast;
    private ImageView imageViewResult;
    private ProgressDialog progressDialog;
    private ImageView imageViewFull;
    private List<PointF> lastOutline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_scan_document, (ViewGroup) findViewById(R.id.scan_view_placeholder));

        imageViewResult = (ImageView) findViewById(R.id.image_result);

        imageViewFull = (ImageView) findViewById(R.id.full_image);

        documentScanView = (DocumentScanView) findViewById(R.id.document_scan_view);
        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        documentScanView.setCameraOpenListener(this);
        // the view can be configured via a json file in the assets, and this config is set here
        // (alternatively it can be configured via xml, see the Energy Example for that)
        documentScanView.setConfigFromAsset("document_view_config.json");

        // initialize Anyline with the license key and a Listener that is called if a result is found
        documentScanView.initAnyline(getString(R.string.anyline_license_key), new DocumentResultListener() {
            @Override
            public void onResult(DocumentResult documentResult) {
                // handle the result document images here
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                AnylineImage transformedImage = documentResult.getResult();
                AnylineImage fullFrame = documentResult.getFullImage();

                showToast(getString(R.string.document_picture_success));

                // perform some animation
                performScaleOutAnimation(transformedImage);


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

            @Override
            public void onPreviewProcessingSuccess(AnylineImage anylineImage) {
                // this is called after the preview of the document is completed, and a full picture will be
                // processed automatically

                performScaleInAnimation(anylineImage);
                notificationToast = Toast.makeText(ScanDocumentActivity.this, "Scanning full document. Please hold " +
                        "still", Toast.LENGTH_LONG);
                notificationToast.show();

                showToast(getString(R.string.document_preview_success));
            }

            @Override
            public void onPreviewProcessingFailure(DocumentScanView.DocumentError documentError) {
                // this is called on any error while processing the document image
                // Note: this is called every time an error occurs in a run, so that might be quite often
                // An error message should only be presented to the user after some time
            }

            @Override
            public void onPictureProcessingFailure(DocumentScanView.DocumentError documentError) {

                // handle an error while processing the full picture here
                // the preview will be restarted automatically
                String text = getString(R.string.document_picture_error);
                switch (documentError) {
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
                    case UNKNOWN:
                    default:
                        text += getString(R.string.document_error_unknown);
                        break;
                }

                showToast(text);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                // cancel the animation on error
                imageViewFull.clearAnimation();
                imageViewFull.setVisibility(View.INVISIBLE);

                AnylineImage image = documentScanView.getCurrentFullImage();

                if (image != null) {
                    File outDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "error");
                    outDir.mkdir();
                    File outFile = new File(outDir, "" + System.currentTimeMillis() + documentError.name() + ".jpg");
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
            public boolean onDocumentOutlineDetected(List<PointF> list, boolean anglesValid) {
                // is called when the outline of the document is detected. return true if the outline is consumed by
                // the implementation here, false if the outline should be drawn by the DocumentScanView
                lastOutline = list; // saving the outline for the animations
                return false;
            }

            @Override
            public void onTakePictureSuccess() {
                // this is called after the image has been captured from the camera and is about to be processed
                progressDialog = ProgressDialog.show(ScanDocumentActivity.this, "Processing", "Processing the picture" +
                        ". Please wait", true);

                if (notificationToast != null) {
                    notificationToast.cancel();
                }
            }

            @Override
            public void onTakePictureError(Throwable throwable) {
                // This is called if the image could not be captured from the camera (most probably because of an
                // OutOfMemoryError)
                throw new RuntimeException(throwable);
            }

            @Override
            public void onPictureCornersDetected(AnylineImage anylineImage, List<PointF> list) {

            }

            @Override
            public void onPictureTransformed(AnylineImage anylineImage) {

            }

            @Override
            public void onPictureTransformError(DocumentScanView.DocumentError documentError) {

            }

        });

        // optionally stop the scan once a valid result was returned
        // documentScanView.cancelOnResult(true);

    }

    @Override
    public Rect getCutoutRect() {
        return null;
    }

    @Override
    protected AnylineBaseModuleView getScanView() {
        return documentScanView;
    }

    /**
     * Performs an animation on a successful preview. This is just an example.
     *
     * @param anylineImage The cropped successful preview image
     */
    private void performScaleInAnimation(AnylineImage anylineImage) {
        final AlphaAnimation scanPulseAnimation = new AlphaAnimation(0.05f, 0.3f);
        scanPulseAnimation.setDuration(500);
        scanPulseAnimation.setFillAfter(true);
        scanPulseAnimation.setRepeatMode(Animation.REVERSE);
        scanPulseAnimation.setRepeatCount(Animation.INFINITE);

        if (lastOutline != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams
                    .WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); //WRAP_CONTENT param can be
            // FILL_PARENT
            params.leftMargin = (lastOutline.get(0).x < lastOutline.get(3).x) ? (int) lastOutline.get(0).x :
                    (int) lastOutline.get(3).x; //XCOORD
            params.topMargin = (lastOutline.get(0).y < lastOutline.get(1).y) ? (int) lastOutline.get(0).y :
                    (int) lastOutline.get(0).y; //YCOORD
            params.width = (lastOutline.get(1).x > lastOutline.get(2).x) ? (int) (lastOutline.get(1).x -
                    params.leftMargin) : (int) (lastOutline.get(2).x - params.leftMargin);
            params.height = (lastOutline.get(2).y > lastOutline.get(3).y) ? (int) (lastOutline.get(2).y -
                    params.topMargin) : (int) (lastOutline.get(3).y - params.topMargin);
            imageViewFull.setLayoutParams(params);
        }


        imageViewFull.setImageBitmap(anylineImage.getBitmap());


        final AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setRepeatCount(0);


        float scaleWidth = (float) documentScanView.getWidth() / imageViewFull.getLayoutParams().width;
        float scaleHeight = (float) documentScanView.getHeight() / imageViewFull.getLayoutParams().height;

        float maxScale = (scaleWidth > scaleHeight) ? scaleWidth : scaleHeight;
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, maxScale, 1f, maxScale, Animation
                .RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnimation.setRepeatCount(0);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setFillEnabled(true);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageViewFull.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageViewFull
                        .getLayoutParams();
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                imageViewFull.setLayoutParams(params);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alphaAnimation);
        set.setFillAfter(true);
        set.setFillEnabled(true);
        imageViewFull.startAnimation(set);
    }

    /**
     * Performs an animation after the final image was successfully processed. This is just an example.
     *
     * @param transformedImage The transformed final image
     */
    private void performScaleOutAnimation(AnylineImage transformedImage) {
        float targetHeight = transformedImage.getHeight() * (100.0f / transformedImage.getWidth());

        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, (float) imageViewResult.getWidth() / imageViewFull
                .getWidth(), 1f, targetHeight / imageViewFull.getHeight(), Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 1f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

        AlphaAnimation animation1 = new AlphaAnimation(1f, 0.0f);
        animation1.setDuration(500);
        animation1.setFillAfter(true);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(scaleAnimation);
        set.addAnimation(animation1);

        imageViewFull.setImageBitmap(Bitmap.createScaledBitmap(transformedImage.getBitmap(), imageViewFull
                        .getLayoutParams().width,
                imageViewFull.getLayoutParams().height, false));
        imageViewFull.startAnimation(set);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageViewFull.setVisibility(View.INVISIBLE);
                imageViewResult.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageViewResult.setImageBitmap(Bitmap.createScaledBitmap(transformedImage.getBitmap(), 100, 160, false));
        imageViewResult.setVisibility(View.INVISIBLE);
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
        documentScanView.startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        documentScanView.cancelScanning();
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
}
