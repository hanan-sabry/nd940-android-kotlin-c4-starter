package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null
    private var selectedLocationStr: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectLocationBinding.inflate(layoutInflater)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.save.setOnClickListener{
            if (selectedLocation == null) {
                Toast.makeText(context, "You must select location", Toast.LENGTH_SHORT).show()
            } else {
                onLocationSelected()
            }
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMapStyle(googleMap)
        setMapLongClick(googleMap)
        setPoiClick(googleMap)
        requestForegroundPermissions()
    }

    private fun getUserLastLocation() {
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
//                    requestForegroundPermissions()
                } else {
                    selectedLocationStr = String.format(
                        Locale.getDefault(),
                        "Lat: %1$.5f, Long: %2$.5f",
                        location.latitude,
                        location.longitude
                    )
                    selectedLocation = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15f))
                }
            }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            selectedLocation = latLng
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Selected Location")
                    .snippet(snippet)
            )
            selectedLocationStr = snippet
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            selectedLocation = poi.latLng
            selectedLocationStr = poi.name
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun requestForegroundPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            -> {
                Toast.makeText(
                    context,
                    "You can use the API that requires the permission.",
                    Toast.LENGTH_SHORT
                ).show()
                checkDeviceLocationSettingsIsEnabled()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(
                    requireView(),
                    "Access device location is required for the app, Allow now?",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }.show()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }

    private fun checkDeviceLocationSettingsIsEnabled(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsIsEnabled()
                }.show()
                mMap.isMyLocationEnabled = false
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                getUserLastLocation()
            }
        }
    }

    private fun onLocationSelected() {
        _viewModel.latitude.value = selectedLocation?.latitude
        _viewModel.longitude.value = selectedLocation?.longitude
        _viewModel.reminderSelectedLocationStr.value = selectedLocationStr
        findNavController().popBackStack()
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                Toast.makeText(context, "Permissions is granted", Toast.LENGTH_SHORT).show()
                checkDeviceLocationSettingsIsEnabled()
            } else {
                Toast.makeText(context, "Permissions is not granted", Toast.LENGTH_SHORT).show()
                Snackbar.make(
                    requireView(),
                    "User location can't be shown\ndisable my location button in google maps",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
                mMap.isMyLocationEnabled = false
            }
        }
}

private const val TAG = "SelectLocationFragment"
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 9