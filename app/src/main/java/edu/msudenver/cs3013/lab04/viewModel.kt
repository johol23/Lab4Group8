package edu.msudenver.cs3013.lab04


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class viewModel: ViewModel() {

    private val _parkingLocation = MutableLiveData<String>()
    val parkUpdate: LiveData<String> get() = _parkingLocation
    init {
        _parkingLocation.postValue(0)
    }

    fun setParkingLocation(latLng: String) {
        _parkingLocation.value = latLng
    }
}




