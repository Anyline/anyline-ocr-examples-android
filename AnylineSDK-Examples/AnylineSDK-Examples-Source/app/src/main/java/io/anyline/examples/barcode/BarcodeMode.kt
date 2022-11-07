package io.anyline.examples.barcode

import io.anyline.camera.VisualFeedbackConfig.FeedbackStyle

enum class BarcodeMode(val text: String, val multi: Boolean, val feedbackStyle: FeedbackStyle) {
    Single("Single", false, FeedbackStyle.RECT),
    Multi("Multi", true, FeedbackStyle.RECT),
    BatchCount("Batch Count", true, FeedbackStyle.RECT)

}