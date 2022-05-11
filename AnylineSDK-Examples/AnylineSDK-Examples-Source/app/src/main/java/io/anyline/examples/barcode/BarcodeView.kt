package io.anyline.examples.barcode

import android.graphics.Color
import android.widget.TextView

/**
 * Keeps a [VisibleBarcode] tied to its [TextView]
 *
 * Contains
 * - [visibleBarcode], as the main data point
 * - [textView], so we know what view belongs to this VisibleBarcode
 * - [removalCounter], which indicates a view should be removed if it reaches 0. This prevents
 *   unnecessary removing & adding of the view when it just didn't appear in a result
 * - [mockServerCallStatus], to decide how to represent the view
 */
data class BarcodeView(
    var visibleBarcode: VisibleBarcode,
    val textView: TextView,
    var mockServerCallStatus: MockServerCallStatus = MockServerCallStatus.WAITING,
    var removalCounter: Int = 5
) {
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

    /**
     * Return the appropriate color for a MockServerCallStatus
     */
    fun getMockServerCallBackgroundColor(): Int {
        val color = when (mockServerCallStatus) {
            MockServerCallStatus.WAITING -> Color.BLUE
            MockServerCallStatus.SUCCESS -> Color.GREEN
            MockServerCallStatus.ERROR -> Color.RED
        }
        return color
    }

    companion object {
        /**
         * Turns a Barcode value into a Human-Readable String for the Murelli Drink. For testing only!
         */
        fun getMurelliFlavor(value: String): String {
            return when (value) {
                "90145322" -> "Orange-Maracuja"
                "90145339" -> "Cola-Mix"
                "90145346" -> "Raspberry"
                "9014500002878" -> "Cola"
                "9014500003004" -> "Apple-Pear"
                else -> value
            }
        }

        /**
         * Turns a Barcode value int a hex color value. For testing only!
         */
        fun getMurelliColor(value: String): String {
            return when (value) {
                "90145322" -> "#9F5690"
                "90145339" -> "#A06E3C"
                "90145346" -> "#963246"
                "9014500002878" -> "#323232"
                "9014500003004" -> "#A0AA6E"
                else -> "Unknown"
            }
        }
    }

    enum class MockServerCallStatus {
        WAITING, SUCCESS, ERROR
    }
}