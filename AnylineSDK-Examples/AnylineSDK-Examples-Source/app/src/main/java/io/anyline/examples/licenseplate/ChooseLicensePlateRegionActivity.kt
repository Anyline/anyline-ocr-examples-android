package io.anyline.examples.licenseplate

import android.graphics.Rect
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import io.anyline.examples.databinding.ActivityChooseLicensePlateRegionBinding
import io.anyline.examples.util.px

class ChooseLicensePlateRegionActivity : AppCompatActivity() {

    lateinit var binding: ActivityChooseLicensePlateRegionBinding
    lateinit var viewModel: ChooseLicensePlateRegionViewModel
    lateinit var adapter: LicensePlateRegionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseLicensePlateRegionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[ChooseLicensePlateRegionViewModel::class.java]

        adapter = LicensePlateRegionAdapter(::onRegionClicked)

        binding.licensePlates.apply {
            layoutManager = GridLayoutManager(this@ChooseLicensePlateRegionActivity, 2, VERTICAL, false)
            adapter = this@ChooseLicensePlateRegionActivity.adapter
            addItemDecoration(ChooseLicensePlateItemDecoration())
        }

        viewModel.regions().observe(this) {
            adapter.regions = it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onRegionClicked(region: Region) {
        val intent = ScanLicensePlateActivity.newIntent(this, region)
        startActivity(intent)
    }

    inner class ChooseLicensePlateItemDecoration : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

            val position = parent.getChildAdapterPosition(view)

            if (position % 2 == 0) {
                outRect.right = 2.px(parent.context)
            } else {
                outRect.left = 2.px(parent.context)
            }
        }
    }
}
