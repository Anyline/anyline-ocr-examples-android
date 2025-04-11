package com.anyline.examples.barcodeOverlay


import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.anyline.examples.R
import io.anyline.plugin.config.OverlayConfig
import io.anyline.plugin.config.OverlayConfig.OverlayAnchorConfig
import io.anyline.plugin.config.OverlayDimensionConfig
import io.anyline.plugin.config.OverlayScaleConfig
import io.anyline2.viewplugin.ar.BarcodeOverlayView
import io.anyline2.viewplugin.ar.OverlayViewHolder
import io.anyline2.viewplugin.ar.VisibleBarcode
import java.util.ArrayList

sealed class OverlayType {
    abstract val small: Boolean

    data class Selected(override val small: Boolean): OverlayType()
    data class NotSelected(override val small: Boolean): OverlayType()
}

class BarcodeOverlayViewHolderList(
    context: Context,
    barcodeOverlayView: BarcodeOverlayView): ArrayList<OverlayViewHolder>() {

    val overlayType: OverlayType = getOverlayTypeFromBarcodeOverlayView(barcodeOverlayView)
    init {
        setOverlayViewHolders(context, barcodeOverlayView)
    }

    private fun setOverlayViewHolders(context: Context, barcodeOverlayView: BarcodeOverlayView) {
        clear()
        add(
            OverlayViewHolder(
                overlayView = ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    layoutParams = ViewGroup.LayoutParams(150, 150)
                    setImageResource(getImageResourceFromOverlayType(overlayType))
                    setOnClickListener {
                        val barcodeValue = barcodeOverlayView.visibleBarcode.value
                        Companion.setSelected(!isSelected(barcodeValue), barcodeValue, barcodeOverlayView.visibleBarcode)
                        /*
                         * by calling BarcodeOverlayView.invalidate() we request the BarcodeOverlayListener
                         * to re-create the OverlayViewHolder list on the next BarcodeOverlayView update
                         */
                        barcodeOverlayView.invalidate()
                    }
                },
                overlayConfig = OverlayConfig().apply {
                    anchor = OverlayAnchorConfig.CENTER
                    sizeDimension = OverlayDimensionConfig().apply {
                        scaleX = OverlayScaleConfig().apply {
                            scaleValue = 0.7
                            scaleType = OverlayScaleConfig.OverlayScaleTypeConfig.OVERLAY
                        }
                        scaleY = OverlayScaleConfig().apply {
                            scaleValue = 0.7
                            scaleType = OverlayScaleConfig.OverlayScaleTypeConfig.OVERLAY
                        }
                    }
                }
            )
        )
        if (overlayType is OverlayType.Selected) {
            add(
                OverlayViewHolder(
                    overlayView = TextView(context).apply {
                        text = barcodeOverlayView.visibleBarcode.value
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        gravity = Gravity.CENTER_HORIZONTAL + Gravity.CENTER
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        setBackgroundResource(R.drawable.selected_barcode_overlay_rounded_corner)
                    },
                    overlayConfig = OverlayConfig().apply {
                        anchor = OverlayAnchorConfig.BOTTOM_CENTER

                        sizeDimension = OverlayDimensionConfig().apply {
                            scaleX = OverlayScaleConfig().apply {
                                scaleValue = 1.0
                                scaleType = OverlayScaleConfig.OverlayScaleTypeConfig.OVERLAY
                            }
                            scaleY = OverlayScaleConfig().apply {
                                scaleValue = 0.3
                                scaleType = OverlayScaleConfig.OverlayScaleTypeConfig.OVERLAY
                            }
                        }
                        offsetDimension = OverlayDimensionConfig().apply {
                            scaleY = OverlayScaleConfig().apply {
                                scaleValue = -0.1
                                scaleType = OverlayScaleConfig.OverlayScaleTypeConfig.OVERLAY
                            }
                        }
                    }
                )
            )
        }
    }

    companion object {
        private val selectedBarcodeMap = mutableMapOf<String, VisibleBarcode>()

        private fun isSelected(barcodeValue: String): Boolean {
            return selectedBarcodeMap.containsKey(barcodeValue)
        }

        private fun setSelected(selected: Boolean, barcodeValue: String, visibleBarcode: VisibleBarcode) {
            if (selected) {
                selectedBarcodeMap[barcodeValue] = visibleBarcode
            } else {
                selectedBarcodeMap.remove(barcodeValue)
            }
        }

        fun resetSelectedBarcodes() {
            selectedBarcodeMap.clear()
        }

        private const val MAX_SMALL_SIZE: Int = 200

        fun getOverlayTypeFromBarcodeOverlayView(barcodeOverlayView: BarcodeOverlayView): OverlayType {
            val rect = barcodeOverlayView.visibleBarcode.imageRect
            val isSmall = (rect.width < MAX_SMALL_SIZE && rect.height < MAX_SMALL_SIZE)
            return when (isSelected(barcodeOverlayView.visibleBarcode.value)) {
                true -> OverlayType.Selected(isSmall)
                false -> OverlayType.NotSelected(isSmall)
            }
        }

        private fun getImageResourceFromOverlayType(overlayType: OverlayType) = when (overlayType) {
            is OverlayType.Selected -> {
                when (overlayType.small) {
                    true -> R.drawable.ic_barcode_overlay_far_green
                    false -> R.drawable.ic_barcode_overlay_checkmark_green
                }
            }
            is OverlayType.NotSelected -> {
                when (overlayType.small) {
                    true -> R.drawable.ic_barcode_overlay_far_blue
                    false -> R.drawable.ic_barcode_overlay_plus_blue
                }
            }
        }
    }
}

