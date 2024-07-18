package edu.msudenver.cs3013.lab04


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class myViewModel : ViewModel() {

    private val _parkingLocation = MutableLiveData<LatLng>()
    val parkUpdate: LiveData<LatLng> get() = _parkingLocation

    fun setParkingLocation(latLng: LatLng) {
        _parkingLocation.value = latLng
    }
}




