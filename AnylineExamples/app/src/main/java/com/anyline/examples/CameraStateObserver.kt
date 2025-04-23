package com.anyline.examples

import android.util.Size
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.anyline2.view.CameraPermissionState
import io.anyline2.view.CameraState
import io.anyline2.view.ScanView
import kotlin.math.max
import kotlin.math.min

/**
 * A helper class for observing the CameraState and notify the user when the expected is not available
 */
class CameraStateObserver(
    lifecycleOwner: LifecycleOwner,
    private val scanView: ScanView): Observer<CameraState> {

    var expectedViewConfigCameraSize: Size? = null
        set(value) {
            field = value
            value?.let {
                compareSizesAndNotify()
            }
        }

    private var actualCameraSize: Size? = null
        set(value) {
            field = value
            value?.let {
                compareSizesAndNotify()
            }
        }

    init {
        scanView.cameraState.observe(lifecycleOwner, this)
    }

    override fun onChanged(value: CameraState) {
        when (value) {
            is CameraState.NotReady -> {
                //waiting for camera
            }
            is CameraState.Ready -> {
                actualCameraSize = value.getFrameSize()
            }
            is CameraState.Error -> {
                if (value.cameraPermissionState is CameraPermissionState.Denied) {
                    notifyWithToast("Camera permission was denied.")
                } else {
                    notifyWithToast("Error opening camera: ${value.exception?.message}")
                }
            }
        }
    }

    private fun compareSizesAndNotify() {
        actualCameraSize?.let { actualSize ->
            expectedViewConfigCameraSize?.let { expectedSize ->
                if (max(actualSize.width, actualSize.height) != max(expectedSize.width, expectedSize.height)
                    || min(actualSize.width, actualSize.height) != min(expectedSize.width, expectedSize.height)) {
                    scanView.post {
                        notifyWithToast("Expected camera resolution not available. " +
                                    "Using ${actualSize.width}x${actualSize.height} instead.")
                    }
                }
            }
        }
    }

    private fun notifyWithToast(message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast
            .makeText(scanView.context, message, duration)
            .show()
    }

    fun reset() {
        expectedViewConfigCameraSize = null
        actualCameraSize = null
    }
}