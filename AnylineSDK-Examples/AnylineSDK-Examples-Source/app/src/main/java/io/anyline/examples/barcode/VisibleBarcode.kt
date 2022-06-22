package io.anyline.examples.barcode

import io.anyline.opencv.core.Point
import io.anyline.plugin.barcode.Barcode
import io.anyline.plugin.barcode.BarcodeScanResult

/**
 * Represents one currently visible Barcode on screen.
 *
 * Contains
 * - [value] to identify the Barcode
 * - [coordinates], the original barcodes as reported in the BarcodeScanResult: List of 4 Points,
 *   x & y value ranging 0-imageSize (Integer)
 * - [normalizedCoordinates], the coordinates normalized to the image size: List of 4 Points,
 *   x & y value ranging 0-1 (Double)
 * - [imageSize], size of the image: 1 Point, x & y value contain width & height
 * - [originalBarcode], the original Barcode this VisibleBarcode has been constructed from
 * - [originalScanResult], the original BarcodeScanResult that contains the Barcode
 *
 * Use [fromOriginalBarcode] to construct [VisibleBarcode] from [Barcode] and [BarcodeScanResult].
 */
data class VisibleBarcode(
    val value: String,
    val coordinates: List<Point>,
    val normalizedCoordinates: List<Point>,
    val imageSize: Point,
    val originalBarcode: Barcode,
    val originalScanResult: BarcodeScanResult
) {
    companion object {
        fun fromOriginalBarcode(
            barcode: Barcode,
            scanResult: BarcodeScanResult
        ): VisibleBarcode {
            val coordinates = barcode.coordinates

            val originalWidth: Double = scanResult.cutoutImage?.width?.toDouble() ?: 0.0
            val originalHeight: Double = scanResult.cutoutImage?.height?.toDouble() ?: 0.0
            val normalizedCoordinates = coordinates.map { point ->
                Point(point.x / originalWidth, point.y / originalHeight)
            }
            val imageSize = Point(originalWidth, originalHeight)
            return VisibleBarcode(
                barcode.value,
                coordinates,
                normalizedCoordinates,
                imageSize,
                barcode,
                scanResult
            )
        }
    }
}