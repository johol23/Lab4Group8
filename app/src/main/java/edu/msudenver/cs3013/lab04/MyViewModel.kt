package edu.msudenver.cs3013.lab04

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MyViewModel : ViewModel() {
    private val _parkUpdate = MutableLiveData<LatLng>()
    val parkUpdate: LiveData<LatLng> get() = _parkUpdate

    fun setParkingLocation(latLng: LatLng) {
        _parkUpdate.value = latLng
    }
}
