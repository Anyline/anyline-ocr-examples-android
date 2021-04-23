package io.anyline.examples.licenseplate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.anyline.examples.R
import io.anyline.examples.databinding.ViewChooseLicensePlateRegionBinding

class LicensePlateRegionAdapter(
        private val onClickAction: (Region) -> Unit
) : RecyclerView.Adapter<LicensePlateRegionAdapter.LicensePlateRegionViewHolder>() {

    var regions: List<Region> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicensePlateRegionAdapter.LicensePlateRegionViewHolder {
        return LicensePlateRegionViewHolder(
                ViewChooseLicensePlateRegionBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                ),
                onClickAction
        )
    }

    override fun onBindViewHolder(holder: LicensePlateRegionAdapter.LicensePlateRegionViewHolder, position: Int) = holder.bind(regions[position])

    override fun getItemCount(): Int = regions.size

    inner class LicensePlateRegionViewHolder(
            private val binding: ViewChooseLicensePlateRegionBinding,
            private val onClickAction: (Region) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(region: Region) {
            val imageResource = when (region) {
                Region.Europe -> R.drawable.tile_licenseplate_eu
                Region.US -> R.drawable.tile_licenseplate_us
            }
            binding.tileImage.setImageResource(imageResource)
            binding.root.setOnClickListener {
                onClickAction.invoke(region)
            }
        }
    }
}