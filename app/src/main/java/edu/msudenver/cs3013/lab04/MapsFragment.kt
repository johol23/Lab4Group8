package edu.msudenver.cs3013.lab04

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.msudenver.cs3013.lab04.databinding.ActivityMapsBinding

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var marker: Marker? = null

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMapsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getLocation()
                } else {
                    showPermissionRationale {
                        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                    }
                }
            }

        val viewModel = ViewModelProvider(requireActivity()).get(myViewModel::class.java)
        val onParkedHereButton = view.findViewById<Button>(R.id.Parking)

        onParkedHereButton.setOnClickListener {
            marker?.let {//check if marker not null
                val markerLocation = it.position//marker location get position  lat lon from marker
                viewModel.setParkingLocation(markerLocation) //call method inside viewmodel pass marker location
                updateText(markerLocation)// send markerlocation to update text method
            }
        }



        prepareViewModel()

    }
    // each time we open the activity and the value will be displayed. The require activity replaced the this key word to make sure that a new instance isnt created


    private fun prepareViewModel() {

        val totalsViewModel =
            ViewModelProvider(requireActivity()).get(myViewModel::class.java)
        totalsViewModel.parkUpdate.observe(viewLifecycleOwner, { updateText(it) })

    }
    private fun updateText(latLng: LatLng) {

        view?.findViewById<TextView>(R.id.maps_text_view)?.text =
            "${latLng.latitude}, ${latLng.longitude}"
    }



    private fun addOrMoveMarker(latLng: LatLng) {
        if (marker == null) {
            marker = addMarkerAtLocation(latLng, "I'm parked here",
                getBitmapDescriptorFromVector(R.drawable.car_pin)
            )
        } else { marker?.position=latLng }
        }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap.apply {
            setOnMapClickListener { latLng ->
                //addOrMoveSelectedPositionMarker(latLng)
                addOrMoveMarker(latLng)
            }
        }

        when {
            hasLocationPermission() -> getLocation()
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        //fused location last location with addOnFailureListener and addOnCanceledListener listeners added
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    updateMapLocation(currentLocation)
                    addMarkerAtLocation(currentLocation, "Current Location")
                    //zoom in
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
                }
            }
    }




    private fun getLocation() {
//        Log.d("MapsActivity", "getLocation() called.")
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
              val userLocation = LatLng(location.latitude, location.longitude)
                updateMapLocation(userLocation)
               addMarkerAtLocation(userLocation, "Jessica")
               // val Colorado = LatLng(39.7,-104.7)
                //updateMapLocation(Colorado)
               // addMarkerAtLocation(Colorado, "Beautiful Colorado")
            }}
    }


    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 11f))
    }

    private fun getBitmapDescriptorFromVector(@DrawableRes
                                              vectorDrawableResourceId: Int): BitmapDescriptor? {
        val bitmap = ContextCompat.getDrawable(requireContext(),
            vectorDrawableResourceId)?.let { vectorDrawable ->
            vectorDrawable.setBounds(0, 0,
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight)
            val drawableWithTint = DrawableCompat
                .wrap(vectorDrawable)
            DrawableCompat.setTint(drawableWithTint, Color.RED)
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawableWithTint.draw(canvas)
            bitmap
        }?: return null
        return BitmapDescriptorFactory.fromBitmap(bitmap)
            .also { bitmap?.recycle() }
    }


    //this function is not used in this version of the code
    private fun addOrMoveSelectedPositionMarker(latLng: LatLng) {
        if (marker == null) {
            marker = addMarkerAtLocation(latLng, "Deploy here",
                getBitmapDescriptorFromVector(R.drawable.car_pin)
            )
        } else { marker?.apply { position = latLng }
            }
    }



    private fun addMarkerAtLocation(
        location: LatLng, title: String, markerIcon: BitmapDescriptor? = null
    ) = mMap.addMarker(
        MarkerOptions().title(title).position(location)
            .apply { markerIcon?.let { icon(markerIcon) } }
    )


    private fun hasLocationPermission() =
        //check if ACCESS_FINE_LOCATION permission is granted
        ContextCompat.checkSelfPermission(
            requireContext(),
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun showPermissionRationale(
        positiveAction: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Location permission")
            .setMessage("We need your permission to find your current position")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                positiveAction()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

}