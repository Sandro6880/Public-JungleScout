package at.htl.android_frontend.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import at.htl.android_frontend.network.Checkpoint
import at.htl.android_frontend.network.Path
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import at.htl.android_frontend.databinding.FragmentMapsBinding
import at.htl.android_frontend.network.DataBaseHandler
import com.google.android.gms.maps.model.*
import java.io.ByteArrayOutputStream

import at.htl.android_frontend.network.DataBaseHandlerSaveMapInstance
import kotlin.math.roundToInt


class MapsFragment : Fragment(), GoogleMap.OnMyLocationClickListener ,
    GoogleMap.OnMyLocationButtonClickListener{

    var property: Path? = null
    var convertedLatLng: MutableList<LatLng> = ArrayList()
    var photo: String? = null
    var databaseHandler: DataBaseHandler? = null
    var instanceSaver: DataBaseHandlerSaveMapInstance? = null
    private var db: SQLiteDatabase? = null
    var theImage: Bitmap? = null
    var cp: Checkpoint? = null
    private var permissionDenied = false
    private lateinit var map: GoogleMap
    var index = 0
    var markers: ArrayList<Marker> = ArrayList(emptyList())
    var moveCamera: LatLng? = null
    var cursor: Cursor? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_REQUEST = 1888
        const val DISTANCE_PICTURE_TAKEN = 100;
        const val TABLE_NAME = "data"
        const val KEY_IMG_URL = "ImgFavourite"
        const val F_KEY_ID = "cp_id"
        //for saving map instance
        const val TABLE_NAME_LAT_LNG = "TBL_LAT_LNG"
        const val TABLE_NAME_INDEX = "TBL_INDEX"
        const val KEY_LAT = "lat"
        const val KEY_LNG = "lng"
        const val KEY_INDEX = "in_num"
    }

    private val backButtonCallback = object: OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            onSaveInstanceState(Bundle())
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        map = googleMap
        var num = (property?.checkpoints?.size?.minus(convertedLatLng.size))
        if(moveCamera == null && convertedLatLng.isEmpty()){
            moveCamera = property?.checkpoints?.get(0)?.gpsLng?.let {
                LatLng(property?.checkpoints!![0].gpsLat.toDouble(), it.toDouble())
            }
            property?.checkpoints?.forEach { checkpoint: Checkpoint ->
                run {
                    val latlng = LatLng(checkpoint.gpsLat.toDouble(),
                        checkpoint.gpsLng.toDouble())
                    markers.add(map.addMarker(MarkerOptions()
                        .position(latlng)
                        .title(checkpoint.name)))
                    if(index == 0){
                        markers.get(index).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    }
                    convertedLatLng.add(latlng)
                }
            }

        }else{
            /**
             * Here all the checkpoints are converted into Markers and have their title set to their name.
             * The first marker will be set to Green colour.
             * It is important to keep track of the overall number of each marker because google maps allow you to remove a marker from the map,
             * but it doesn't remove it from the list of markers that we use in the map.
             * Thus knowing the position of the next marker is vital for implementing a colour concept.
             */
            convertedLatLng?.forEach { it ->
                run {
                    val latlng = it
                    markers.add(map.addMarker(MarkerOptions()
                        .position(latlng)
                        .title(property?.checkpoints?.get(num!!)?.name)))
                    num?.inc()
                    if(index == 0){
                        markers.get(index).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    }
                }
            }

        }
        map.getUiSettings().setZoomControlsEnabled(true)
        if(property?.checkpoints?.size != 0) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(moveCamera!!, 15f)
            )
            drawRoute(map, convertedLatLng)
        }
        map.setOnMyLocationClickListener(this)
        enableMyLocation()
    }
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        instanceSaver = getContext()?.let { DataBaseHandlerSaveMapInstance(it) }
        if (moveCamera == null && convertedLatLng.isEmpty()) {
            setInstanceData()
        }
        val application = requireNotNull(activity).application
        val binding = FragmentMapsBinding.inflate(inflater)
        binding.lifecycleOwner = this
        MapsFragmentArgs.fromBundle(requireArguments()).runCatching {  }
        property = MapsFragmentArgs.fromBundle(requireArguments()).selectedProperty
        val viewModelFactory = MapViewModelFactory(property!!, application)
        binding.viewModel = ViewModelProvider(
                this, viewModelFactory).get(MapViewModel::class.java)
        databaseHandler = getContext()?.let { DataBaseHandler(it) }

        //Camera Request
        if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA),200)
        }
        Toast.makeText(context,"You are only able to take One Picture per Checkpoint.", Toast.LENGTH_SHORT).show()
        binding.buttonCapturePhoto.setOnClickListener( View.OnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if(context?.let { it1 -> cameraIntent.resolveActivity(it1.packageManager) } != null){
                if(convertedLatLng.size != 0){
                    val dist = FloatArray(1)
                    Location.distanceBetween(
                        map.myLocation.latitude,
                        map.myLocation.longitude,
                        convertedLatLng.get(0).latitude,
                        convertedLatLng.get(0).longitude,
                        dist
                    )
                    if (dist[0] < DISTANCE_PICTURE_TAKEN) {
                        cp = property!!.checkpoints.get(index)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST)
                    }
                    else{
                        Toast.makeText(context,"You are not within the range of the Checkpoint. You need to be: " +
                                ((dist[0]) - DISTANCE_PICTURE_TAKEN).roundToInt() + " m closer", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(context,"You have reached the end of the Journey.",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context,"Camera could not be opened.", Toast.LENGTH_SHORT).show()
            }
        })
        return binding.root
    }
    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (this.context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) }
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_PERMISSION_REQUEST_CODE)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(savedInstanceState == null){
            val mapFragment = childFragmentManager.findFragmentById(at.htl.android_frontend.R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
            if (mapFragment != null) {
                requireActivity().onBackPressedDispatcher.addCallback(mapFragment.viewLifecycleOwner,backButtonCallback)
            }
        }
    }

    /**
     * This method configures the route that the client will follow.
     */
    fun drawRoute(map: GoogleMap, latLngCheckpoints: List<LatLng>){
        // Getting URL to the Google Directions API
        //val url: String = getUrl(latLngCheckpoints)
        //val FetchUrl = FetchUrl()

        // Start downloading json data from Google Directions API
        //FetchUrl.execute(url)
        val options: PolylineOptions = PolylineOptions()
            .addAll(latLngCheckpoints)
            .geodesic(true)
        //val options2: PolylineOptions = FetchUrl.getRoute()
        //map.addPolyline(options2)
        map.addPolyline(options)
    }

    /**
     * This method is used for displaying a precise path on the map. It calls the maps api from
     * Google and sends all the checkpoint geo location data with it to receive a Polyline in the
     * end.
     */
    private fun getUrl(latLngCheckpoints: List<LatLng>): String {
        var waypoints: String = ""
        for (item in latLngCheckpoints.subList(0,latLngCheckpoints.lastIndex)){
            waypoints += "via:" +item.latitude + "," + item.longitude +"|"
        }
        return ("https://maps.googleapis.com/maps/api/directions/json?origin="
                + latLngCheckpoints.first().latitude) +
                ","+ latLngCheckpoints.first().longitude +
                "&destination=" + latLngCheckpoints.get(latLngCheckpoints.lastIndex).latitude +
                "," + latLngCheckpoints.get(latLngCheckpoints.lastIndex).longitude +
                "&waypoints=" + waypoints + "&key=example_google_maps_directions_api_key"
    }

    /**
     * This method sets the captured photo data to the database.
     */
    private fun setDataToDataBase() {
        db = databaseHandler?.getWritableDatabase()
        val cv = ContentValues()
        cv.put(KEY_IMG_URL, photo)
        cv.put(F_KEY_ID, cp?.id)
        val id = db!!.insert(TABLE_NAME, null, cv)
        if (id < 0) {
            Toast.makeText(
                getContext(),
                "Something went wrong. Please try again later...",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(getContext(), "Add successful", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * This method saves the key fields needed to keep the relevant Google Map data.
     */
    private fun saveInstanceToDataBase() {
        db = instanceSaver?.getWritableDatabase()
        val cv = ContentValues()
        for (item in convertedLatLng){
            cv.put(KEY_LAT,item.latitude)
            cv.put(KEY_LNG,item.longitude)
            cv.put(KEY_INDEX,0)
            db!!.insert(TABLE_NAME_LAT_LNG, null, cv)
            cv.clear()
        }
        cv.put(KEY_LAT, moveCamera?.latitude)
        cv.put(KEY_LNG, moveCamera?.longitude)
        cv.put(KEY_INDEX,1)
        db!!.insert(TABLE_NAME_LAT_LNG, null, cv)
        cv.clear()
        //cv.put(KEY_INDEX,index)
        //db!!.insert(TABLE_NAME_INDEX, null, cv)
    }

    /**
     * Sets the data of the map should the local database already have a instance saved to it.
     */
    private fun setInstanceData() {
        db = instanceSaver?.readableDatabase
        var columns = arrayOf(DataBaseHandlerSaveMapInstance.KEY_ID,
            DataBaseHandlerSaveMapInstance.KEY_LAT,
            DataBaseHandlerSaveMapInstance.KEY_LNG,
            DataBaseHandlerSaveMapInstance.KEY_INDEX)
        if(db!!.query(DataBaseHandlerSaveMapInstance.TABLE_NAME_LAT_LNG, columns,
                null, null, null, null, null).columnCount != 0){
            cursor = db!!.query(DataBaseHandlerSaveMapInstance.TABLE_NAME_LAT_LNG, columns,
                null, null, null, null, null)
            while (cursor?.moveToNext() == true) {
                val index1: Int? = cursor?.getColumnIndex(DataBaseHandlerSaveMapInstance.KEY_LAT)
                val index2: Int? = cursor?.getColumnIndex(DataBaseHandlerSaveMapInstance.KEY_LNG)
                val index3: Int? = cursor?.getColumnIndex(DataBaseHandlerSaveMapInstance.KEY_INDEX)

                val number = index3?.let { cursor?.getInt(it) }
                if(number == 1){
                    moveCamera = LatLng(index1?.let { cursor?.getDouble(it) }!!,index2?.let { cursor?.getDouble(it) }!!)
                }else{
                    convertedLatLng.add(LatLng(index1?.let { cursor?.getDouble(it) }!!,index2?.let { cursor?.getDouble(it) }!!))
                }
            }
            columns = arrayOf(DataBaseHandlerSaveMapInstance.KEY_ID,
                DataBaseHandlerSaveMapInstance.KEY_INDEX)
            cursor = db!!.query(DataBaseHandlerSaveMapInstance.TABLE_NAME_INDEX, columns,
                null, null, null, null, null)
            while (cursor?.moveToNext() == true) {
                val index1: Int? = cursor?.getColumnIndex(DataBaseHandlerSaveMapInstance.KEY_INDEX)
                index = index1?.let { cursor?.getInt(it) }!!
            }

        }
    }

    /**
     * Start an activity for result.
     * This method provides and saves the picture taken by the user to the local database and
     * reorganizes the Google Map to show progress.
     * @param requestCode
     * @param resultCode
     * @param data The picture taken by the user.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            theImage = data?.extras!!["data"] as Bitmap?
            photo = getEncodedString(theImage)
            if(photo != null){
                markers.get(index).remove()
                convertedLatLng.removeAt(0)
                index++
                if(index != markers.size){
                    markers.get(index).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                }
            }
            setDataToDataBase()
        }
    }

    /**
     * This method processes a bitmap of a picture and compresses it into a Base64 string for easy usage.
     */
    private fun getEncodedString(bitmap: Bitmap?): String {
        val os = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
        //bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, os);
        val imageArr: ByteArray = os.toByteArray()
        return Base64.encodeToString(imageArr, Base64.URL_SAFE)
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this.context, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this.context, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        db = instanceSaver?.writableDatabase
        instanceSaver?.onUpgrade(db!!,1,1)
        saveInstanceToDataBase()
    }
}
