package io.anyline.examples.barcode

import android.view.View

/**
 * Keeps a [VisibleBarcode] tied to its [View]
 *
 * Contains
 * - [visibleBarcode], as the main data point
 * - [View], so we know what view belongs to this VisibleBarcode
 * - [removalCounter], which indicates a view should be removed if it reaches 0. This prevents
 *   unnecessary removing & adding of the view when it just didn't appear in a result
 */
data class BarcodeOverlayView(
    var visibleBarcode: VisibleBarcode,
    val onCreate: () -> View,
    val onDraw: (View, BarcodeOverlayView) -> Unit,
    val onRemove: (View, BarcodeOverlayView) -> Unit,
    var removalCounter: Int = 0
) {
    private var view: View? = null
    fun getView(): View {
        if (view == null) {
            view = onCreate.invoke()
        }
        return view as View
    }

    /**
     * A [ScanResult] is not guaranteed to contain every Barcode currently visible on screen.
     * This workaround gives some leeway to decide if it should be decrease the counter or indicate
     * that it needs to be removed.
     *
     * Returns
     * - [false], if the [removalCounter] is greater than 0
     * - [true], if the [removalCounter] reaches 0
     */
    fun shouldBeRemovedOrDecreasesCounter(): Boolean = if (removalCounter <= 0) {
        true
    } else {
        removalCounter--
        false
    }

}