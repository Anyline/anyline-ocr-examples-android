package io.anyline.examples.licenseplate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChooseLicensePlateRegionViewModel : ViewModel() {

    private val regions = MutableLiveData<List<Region>>()
    fun regions(): LiveData<List<Region>> = regions

    init {
        regions.postValue(listOf(
                Region.Europe,
                Region.US
        ))
    }
}

enum class Region {
    Europe, US
}
