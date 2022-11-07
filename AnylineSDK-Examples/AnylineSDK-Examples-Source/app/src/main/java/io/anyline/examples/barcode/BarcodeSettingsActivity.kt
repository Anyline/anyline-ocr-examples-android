package io.anyline.examples.barcode

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import androidx.core.content.ContextCompat
import io.anyline.examples.R
import io.anyline.examples.baseactivities.BaseToolbarActivity

class BarcodeSettingsActivity: BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.barcode_settings_activity)
        supportFragmentManager.beginTransaction().replace(R.id.settings_frame, BarcodeSettingsFragment()).commit();

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val s = SpannableString("Settings")
        s.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s.length, 0)
        mToolbar.title = s
        val backIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)!!
            .mutate()
        backIcon.setColorFilter(
            ContextCompat.getColor(this, R.color.black_100),
            PorterDuff.Mode.SRC_ATOP
        )
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(backIcon)
        }
    }

    override fun onBackPressed() {
        //result is always saved
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }
}