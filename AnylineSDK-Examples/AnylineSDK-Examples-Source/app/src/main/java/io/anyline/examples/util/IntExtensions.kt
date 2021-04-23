package io.anyline.examples.util

import android.content.Context
import android.util.TypedValue

fun Int.px(context: Context): Int {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
    ).toInt()
}