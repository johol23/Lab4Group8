package edu.msudenver.cs3013.lab04

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class DetailFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null
    private lateinit var viewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        viewModel.parkUpdate.observe(viewLifecycleOwner, { latLng ->
            updateText(latLng)
            updateMap(latLng)
        })

        // Initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.detail_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun updateText(latLng: LatLng) {
        view?.findViewById<TextView>(R.id.detail_fragment)?.text = "Latitude: ${latLng.latitude}, Longitude: ${latLng.longitude}"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun updateMap(latLng: LatLng) {
        if (::mMap.isInitialized) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            if (marker == null) {
                marker = mMap.addMarker(MarkerOptions().position(latLng).title("I'm parked here").icon(getBitmapDescriptorFromVector(R.drawable.star, Color.BLUE)))
            } else {
                marker?.position = latLng
            }
        }
    }

    private fun getBitmapDescriptorFromVector(vectorResId: Int, color: Int): BitmapDescriptor {
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(requireContext(), vectorResId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val wrappedDrawable = DrawableCompat.wrap(vectorDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, color)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        wrappedDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
