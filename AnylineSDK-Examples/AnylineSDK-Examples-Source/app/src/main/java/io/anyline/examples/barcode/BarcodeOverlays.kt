package io.anyline.examples.barcode

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import io.anyline.plugin.barcode.BarcodeScanResult
import kotlinx.coroutines.*

class BarcodeOverlays(private val lifecycleScope: CoroutineScope,
                      private var drawMode: DrawMode) {


    private var lastResultTimeMillis = 0L

    private var cleanJob: Job? = null
    private var resultJob: Job? = null

    sealed class DrawMode(open val context: Context) {
        abstract fun resetOverlay()
        abstract fun getWidth(): Int
        abstract fun getHeight(): Int
        abstract suspend fun handleVisibleBarcodes(scanResult: BarcodeScanResult)

        val currentlyVisibleBarcodeViewMap = mutableMapOf<String, BarcodeOverlayView>()

        class DrawView<T: View>(override val context: Context,
                                val frameLayout: FrameLayout,
                                val onCreate: () -> T,
                                private val onDraw: (T, BarcodeOverlayView) -> Unit,
                                private val onRemove: (T, BarcodeOverlayView) -> Unit)
        : DrawMode(context) {



            override fun getWidth(): Int {return this.frameLayout.width}
            override fun getHeight(): Int {return this.frameLayout.height}

            /**
             * Takes in a [BarcodeScanResult] and handles logic that decides what overlays to draw on screen.
             */
            override suspend fun handleVisibleBarcodes(scanResult: BarcodeScanResult) {
                val scanResultVisibleBarcodes = getVisibleBarcodes(scanResult)
                // Draw Barcode Overlays using Views

                /**
                 * scanResultVisibleBarcodes: all Barcodes found with this scanResult
                 * currentlyVisibleBarcodeViewMap: (from previous result) newlyVisibleBV + alreadyVisibleBV
                 * previouslyVisibleBarcodeViewMap: currentlyVisibleBV - scanResultVisibleB
                 * newlyVisibleBarcodeViewMap: scanResultVisibleB - currentlyVisibleBV
                 * alreadyVisibleBarcodeViewMap: scanResultVisibleB ∩ currentlyVisibleBV
                 */

                val previouslyVisibleBarcodeViewMap = filterToPreviouslyVisibleBarcodeViewMap(
                    currentlyVisibleBarcodeViewMap,
                    scanResultVisibleBarcodes
                )

                val newlyVisibleBarcodeViewMap = filterToNewlyVisibleBarcodeViewMap(
                    scanResultVisibleBarcodes,
                    currentlyVisibleBarcodeViewMap
                )

                val alreadyVisibleBarcodeViewMap = filterToAlreadyVisibleBarcodeViewMap(
                    currentlyVisibleBarcodeViewMap,
                    scanResultVisibleBarcodes
                )

                checkAndRemoveViewsFromBarcodeViewMaps(
                    previouslyVisibleBarcodeViewMap,
                    currentlyVisibleBarcodeViewMap
                )

                currentlyVisibleBarcodeViewMap.putAll(newlyVisibleBarcodeViewMap)
                currentlyVisibleBarcodeViewMap.putAll(alreadyVisibleBarcodeViewMap)

                // Draw all Barcodes that we want visible at this point
                drawBarcodeViewOverlay(currentlyVisibleBarcodeViewMap.values.toList())
            }

            /**
             * Take a Map<String, BarcodeOverlayView> and return those values from it, that are not present in
             * a given List<VisibleBarcode>.
             *
             * previouslyVisibleBarcodeViewMap = currentlyVisibleBarcodeViewMap - scanResultVisibleBarcodes
             */
            private fun filterToPreviouslyVisibleBarcodeViewMap(
                currentlyVisibleBarcodeViewMap: Map<String, BarcodeOverlayView>,
                scanResultVisibleBarcodes: List<VisibleBarcode>
            ): MutableMap<String, BarcodeOverlayView> {
                return currentlyVisibleBarcodeViewMap.filterNot { currentlyVisible ->
                    // Filter all from currentlyVisibleBVM that are NOT in scanResultVisibleBarcodes
                    scanResultVisibleBarcodes.any { it.value == currentlyVisible.value.visibleBarcode.value }
                }.toMutableMap()
            }

            /**
             * Take a List<VisibleBarcode> and from the values that are not present in a given
             * Map<String, BarcodeOverlayView> create a Map<String, BarcodeOverlayView>. Creates Views for the
             * VisibleBarcodes in the process.
             *
             * newlyVisibleBarcodeViewMap = scanResultVisibleBarcodes - currentlyVisibleBarcodeViewMap
             */
            private fun filterToNewlyVisibleBarcodeViewMap(
                scanResultVisibleBarcodes: List<VisibleBarcode>,
                currentlyVisibleBarcodeViewMap: Map<String, BarcodeOverlayView>
            ): Map<String, BarcodeOverlayView> {
                return scanResultVisibleBarcodes.filterNot { visibleBarcode ->
                    // Filter all from scanResultVisibleBarcodes that are NOT in currentlyVisibleBVM
                    currentlyVisibleBarcodeViewMap.any { it.key == visibleBarcode.value }
                }.map { newBarcode ->
                    // Create new Views for every VisibleBarcode that's new
                    val barcodeView = BarcodeOverlayView(
                        newBarcode,
                        {   val newView = onCreate.invoke()
                            frameLayout.addView(newView)
                            newView
                        },
                        onDraw as (View, BarcodeOverlayView) -> Unit,
                        onRemove as (View, BarcodeOverlayView) -> Unit
                    )
                    barcodeView
                }.associateBy { barcodeView ->
                    // Create Map <String, VisibleBarcode>
                    barcodeView.visibleBarcode.value
                }
            }


            /**
             * Take a Map<String, BarcodeOverlayView> and return those values from it that are present in a given
             * List<VisibleBarcode>. Update the VisibleBarcode with the value from the List in the process.
             *
             * alreadyVisibleBarcodeViewMap = scanResultVisibleB ∩ currentlyVisibleBV
             */
            private fun filterToAlreadyVisibleBarcodeViewMap(
                currentlyVisibleBarcodeViewMap: Map<String, BarcodeOverlayView>,
                scanResultVisibleBarcodes: List<VisibleBarcode>
            ): Map<String, BarcodeOverlayView> {
                return currentlyVisibleBarcodeViewMap.filter { currentlyVisibleBarcodeView ->
                    // Filter all from currentlyVisibleBVM that are in scanResultVisibleBarcodes
                    scanResultVisibleBarcodes.any { it.value == currentlyVisibleBarcodeView.key }
                }.map { oldEntry ->
                    // Get the matching existing View and update the VisibleBarcode with the new one
                    oldEntry.value.visibleBarcode = scanResultVisibleBarcodes.find {
                        it.value == oldEntry.key
                    } ?: oldEntry.value.visibleBarcode

                    oldEntry.value
                }.associateBy { barcodeView ->
                    // Create Map <String, VisibleBarcode>
                    barcodeView.visibleBarcode.value
                }
            }

            /**
             * Checks if Views need to be removed from previouslyVisibleBarcodeViewMap and
             * currentlyVisibleBarcodeViewMap and removes the entries and the View if necessary.
             */
            private fun checkAndRemoveViewsFromBarcodeViewMaps(
                previouslyVisibleBarcodeViewMap: MutableMap<String, BarcodeOverlayView>,
                currentlyVisibleBarcodeViewMap: MutableMap<String, BarcodeOverlayView>
            ) {
                // Check & remove Views from previouslyVisibleBVM & currentlyVisibleBVM

                previouslyVisibleBarcodeViewMap.entries.removeIf { entry ->
                    val remove = entry.value.shouldBeRemovedOrDecreasesCounter()
                    if (remove) {
                        // Remove Entry from currentlyVisibleBarcodeViewMap
                        currentlyVisibleBarcodeViewMap.remove(entry.key)
                        // Remove ViewOverlay
                        removeViewOverlay(mutableMapOf(entry.toPair()))
                    }
                    // Remove Entry from previouslyVisibleBarcodeViewMap
                    remove
                }
            }

            /**
             * Overlays using Views: Remove the given Views from Layout
             */
            private fun removeViewOverlay(viewsToRemove: MutableMap<String, BarcodeOverlayView>) {
                viewsToRemove.forEach {
                    it.value.onRemove.invoke(it.value.getView(), it.value)
                    frameLayout.removeView(it.value.getView())
                }
            }

            /**
             * Overlays using Views: Clear everything related to Overlays using Views
             */
            override fun resetOverlay() {
                currentlyVisibleBarcodeViewMap.clear()
                frameLayout.removeAllViews()
            }

            /**
             * Overlays using Views: Update Views (Position, etc) when showing Overlays using Views
             */
            private fun drawBarcodeViewOverlay(barcodeViewsList: List<BarcodeOverlayView>) {
                barcodeViewsList.forEach { barcodeView ->
                    var normalizedTop: Double = barcodeView.visibleBarcode.normalizedCoordinates[0].y
                    var normalizedBottom: Double = barcodeView.visibleBarcode.normalizedCoordinates[0].y
                    var normalizedLeft: Double = barcodeView.visibleBarcode.normalizedCoordinates[0].x
                    var normalizedRight: Double = barcodeView.visibleBarcode.normalizedCoordinates[0].x
                    barcodeView.visibleBarcode.normalizedCoordinates.forEach { point ->
                        normalizedTop = point.y.coerceAtMost(normalizedTop)
                        normalizedBottom = point.y.coerceAtLeast(normalizedBottom)
                        normalizedLeft = point.x.coerceAtMost(normalizedLeft)
                        normalizedRight = point.x.coerceAtLeast(normalizedRight)
                    }

                    val normalizedWidth = normalizedRight - normalizedLeft
                    val normalizedHeight = normalizedBottom - normalizedTop

                    val width = (normalizedWidth * getWidth()).toInt()
                    val height = (normalizedHeight * getHeight()).toInt()

                    val left = (normalizedLeft * getWidth()).toInt()
                    val top = (normalizedTop * getHeight()).toInt()

                    barcodeView.onDraw.invoke(barcodeView.getView(), barcodeView)

                    barcodeView.getView().layoutParams = FrameLayout.LayoutParams(width, height).apply {
                        leftMargin = left
                        topMargin = top
                    }
                }
            }
        }


        class DrawCanvas(override val context: Context,
                         val imageView: ImageView): DrawMode(context) {

            override fun getWidth(): Int {return this.imageView.width}
            override fun getHeight(): Int {return this.imageView.height}

            /**
             * Takes in a [BarcodeScanResult] and handles logic that decides what overlays to draw on screen.
             */
            override suspend fun handleVisibleBarcodes(scanResult: BarcodeScanResult) {
                val scanResultVisibleBarcodes = getVisibleBarcodes(scanResult)
                // Draw Barcode Overlays using a Canvas
                drawCanvasOverlay(scanResultVisibleBarcodes)
            }
            /**
             * Overlays using Canvas: Clear Canvas
             */
            override fun resetOverlay() {
                val drawImageView = imageView
                val bitmap = Bitmap.createBitmap(drawImageView.width, drawImageView.height, Bitmap.Config.ARGB_8888)
                drawImageView.setImageDrawable(BitmapDrawable(context.resources, bitmap))
            }

            /**
             * Overlays using Canvas: Draw Overlays as Paths (Polygons) on Canvas
             */
            private fun drawCanvasOverlay(visibleBarcodesList: List<VisibleBarcode>) {
                val drawImageView = imageView
                val bitmap = Bitmap.createBitmap(drawImageView.width, drawImageView.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val bluePaint = Paint().apply { color = Color.BLUE }
                val whitePaint = Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }

                visibleBarcodesList.forEachIndexed { barcodeIndex, visibleBarcode ->
                    val scaledCoordinates = visibleBarcode.normalizedCoordinates.map { point ->
                        io.anyline.opencv.core.Point(
                            point.x * drawImageView.width,
                            point.y * drawImageView.height
                        )
                    }
                    val drawPath = drawPolygon(scaledCoordinates)
                    canvas.drawPath(drawPath, whitePaint)
                }

                // Draw Circles in the Edges (so we can see the Frame Edges on Screen and confirm it's correct))
                val edgeRadius = 50F
                canvas.drawCircle(0F, 0F, edgeRadius, bluePaint)
                canvas.drawCircle(0F, drawImageView.height.toFloat(), edgeRadius, bluePaint)
                canvas.drawCircle(drawImageView.width.toFloat(), 0F, edgeRadius, bluePaint)
                canvas.drawCircle(drawImageView.width.toFloat(), drawImageView.height.toFloat(), edgeRadius, bluePaint)

                drawImageView.setImageDrawable(BitmapDrawable(context.resources, bitmap))
            }

            /**
             * Takes a list of [io.anyline.opencv.core.Point] and returns a [Path] based on these Points.
             */
            private fun drawPolygon(points: List<io.anyline.opencv.core.Point>): Path {
                val drawPath = Path()
                points.forEachIndexed { pointIndex, point ->
                    when (pointIndex) {
                        0 -> drawPath.moveTo(point.x.toFloat(), point.y.toFloat())
                        else -> drawPath.lineTo(point.x.toFloat(), point.y.toFloat())
                    }
                }
                val lastPoint = points[0]
                drawPath.lineTo(lastPoint.x.toFloat(), lastPoint.y.toFloat())
                return drawPath
            }
        }

        /**
         * Build the list of [VisibleBarcode] from a [BarcodeScanResult]
         */
        fun getVisibleBarcodes(scanResult: BarcodeScanResult): List<VisibleBarcode> {
            val visibleBarcodesList = mutableListOf<VisibleBarcode>()
            scanResult.result.forEach { barcode ->
                val visibleBarcode = VisibleBarcode.fromOriginalBarcode(
                    barcode,
                    scanResult
                )
                visibleBarcodesList.add(visibleBarcode)
            }
            return visibleBarcodesList
        }
    }

    fun setDrawMode(newMode: DrawMode) {
        if (newMode != drawMode) {
            drawMode.resetOverlay()
            drawMode = newMode
        }
    }

    fun postResult(scanResult: BarcodeScanResult, onFinish: (Map<String, BarcodeOverlayView>) -> Unit) {
        resultJob = lifecycleScope.launch {
            lastResultTimeMillis = System.currentTimeMillis()
            drawMode.handleVisibleBarcodes(scanResult)
            onFinish.invoke(drawMode.currentlyVisibleBarcodeViewMap)
        }
    }

    fun start() {
        /* Launch a coroutine that checks in periodic time intervals if new results came in,
         * and if there were no new results for a specified time frame, it clears the overlays. */
        cleanJob = lifecycleScope.launch {
            while(true) {
                delay(500)
                if (System.currentTimeMillis() - lastResultTimeMillis > 600L) {
                    drawMode.resetOverlay()
                }
            }
        }
    }

    fun stop() {
        drawMode.resetOverlay()
        resultJob?.cancel()
        cleanJob?.cancel()
    }









}