package com.anyline.examples.extensions

import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private fun AppCompatActivity.handleWindowInsets(view: View) {
    // Apply insets to the root container to avoid content going behind system bars
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        // Calculate ActionBar height if present
        // When using edge-to-edge with ActionBar, we need to account for it
        // because systemBars() only includes status bar and nav bar, not ActionBar
        val actionBarHeight = if (supportActionBar != null && supportActionBar?.isShowing == true) {
            // ActionBar is visible, get its height
            var height = 0
            val tv = android.util.TypedValue()
            if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                height = android.util.TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    resources.displayMetrics
                )
            }
            height
        } else {
            0
        }

        // Apply padding to root container
        // Top padding = status bar (from systemBars) + ActionBar height
        v.setPadding(
            systemBars.left,
            systemBars.top + actionBarHeight,
            systemBars.right,
            systemBars.bottom
        )

        // IMPORTANT: Return WindowInsetsCompat.CONSUMED to prevent further propagation
        // This tells the system that we've handled the insets
        WindowInsetsCompat.CONSUMED
    }
}

fun AppCompatActivity.setContentViewUsingEdgeToEdge(view: View) {
    // Enable edge-to-edge display for immersive scanning experience
    enableEdgeToEdge()

    setContentView(view)

    // Handle window insets for edge-to-edge display
    handleWindowInsets(view)
}