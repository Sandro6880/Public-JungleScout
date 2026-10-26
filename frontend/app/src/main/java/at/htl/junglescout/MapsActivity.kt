package at.htl.junglescout

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var mapOptions: GoogleMapOptions
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private var points: ArrayList<LatLng>? = null
    private var lineOptions: PolylineOptions? = null
    private var plantedBasicRoute: Boolean  = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
        private const val AUTOCOMPLETE_REQUEST_CODE = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        Places.initialize(applicationContext, "example_google_maps_api_key")

        // Create a new Places client instance.

        // Create a new Places client instance.
        //val placesClient: PlacesClient = Places.createClient(this)
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener {
            startNavigation()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                placeOwnMarker(currentLatLng)
            }
        }
        createLocationRequest()
    }
    fun startNavigation(){
        if(lineOptions != null && plantedBasicRoute == false) {
            val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            lineOptions!!.add(currentLatLng)
            lineOptions!!.addAll(points);
            lineOptions!!.width(5F);
            lineOptions!!.color(Color.DKGRAY);
            plantedBasicRoute = true
            map.addPolyline(lineOptions);
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapOptions = GoogleMapOptions()
        points = ArrayList()
        lineOptions = PolylineOptions()
        //val gpx = DD = degree + (min/60) + (sec/3600) - Umrechnung auf LatLng
        val htl_leonding = LatLng(48.26849979040852, 14.251785868110227)
        // Kürnberghalle: 48.26604170489488, 14.25447972882903
        val kuernberghalle = LatLng(48.26604170489488, 14.25447972882903)
        val poi3 = LatLng(48.26555941035152, 14.258278347224346)
        val poi4 = LatLng(48.26520084993149, 14.260706005618397)
        points!!.add(htl_leonding)
        points!!.add(kuernberghalle)
        points!!.add(poi3)
        points!!.add(poi4)
        //placeMarkerOnMap(htl_leonding)
        /*mapOptions.camera(CameraPosition(htl_leonding,5F,5F,5F))
        mapOptions.tiltGesturesEnabled(true)
        mapOptions.rotateGesturesEnabled(true)
        mapOptions.run { googleMap }*/
        // 48°16'6"N   14°15'7"E
        // Add a marker on Htl-Leonding and move the camera
        //val htl_leonding = LatLng(48.6, 14.7)
        placeMarkerPOIOnMap(htl_leonding, "POI: HTL-Leonding Station 1","This is the best school you can visit")
        placeMarkerPOIOnMap(kuernberghalle, "POI: Kuernberghalle Station 2","A nice play to stumble upon and enjoy festivities together")
        placeMarkerPOIOnMap(poi3, "POI: Pfarre Leonding-Hart-St Johannes Station 3","The Church with a very nice Interior")
        placeMarkerPOIOnMap(poi4, "POI: DELFIN WELLNESS GMBH Station 4","A place where you can relax yourself and enjoy the hot water")
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(htl_leonding,12.0F))
        //map.moveCamera(CameraUpdateFactory.scrollBy(3F,3F))
        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)
        setUpMap()
    }
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        // 1
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                placeOwnMarker(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }
    private fun placeOwnMarker(location: LatLng){
        val markerOptions = MarkerOptions().position(location)
        markerOptions.title("Your current position!")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        map.addMarker(markerOptions)
    }
    private fun placeMarkerPOIOnMap(location: LatLng, title: String,description:String) {
        val markerOptions = MarkerOptions().position(location)
        /*markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)))
        //val titleStr = getAddress(location) //  add these two lines*/
        markerOptions.title(title)
        markerOptions.snippet(description)
        map.addMarker(markerOptions)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        /*if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!);
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng!!)
            }
        }*/
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
    override fun onMarkerClick(p0: Marker?) = false
    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null /* Looper */
        )
    }
    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                            this@MapsActivity,
                            REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    /*private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(
                        i
                    )
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }*/


    /*private fun loadPlacePicker() {
        // Set the fields to specify which types of place data to return.
        // Set the fields to specify which types of place data to return.
        val fields: List<Place.Field> = Arrays.asList(Place.Field.ID, Place.Field.NAME)
        // Start the autocomplete intent.
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        )
            .build(this@MapsActivity)
        try {
            startActivityForResult(intent, PLACE_PICKER_REQUEST)
            //startActivityForResult(builder.build(this@MapsActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }*/
}