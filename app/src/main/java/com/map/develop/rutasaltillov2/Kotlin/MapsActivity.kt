package com.map.develop.rutasaltillov2.Kotlin

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.*
import com.google.android.gms.maps.model.*
import com.map.develop.rutasaltillov2.JSonParsers.*
import com.map.develop.rutasaltillov2.JSonParsers.jsonParseRutas.getListaRutas
import com.map.develop.rutasaltillov2.SearchRoute.DirectionFinder
import com.map.develop.rutasaltillov2.SearchRoute.DirectionFinderListener
import com.map.develop.rutasaltillov2.SearchRoute.Route
import com.map.develop.rutasaltillov2.R
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity :AppCompatActivity(), OnMapReadyCallback, DirectionFinderListener {

    private var mMap: GoogleMap? = null
    private var marcador: Marker? = null
    internal var lat = 0.0
    internal var lng = 0.0
    private val TAG = MapsActivity::class.java.simpleName


    //Variable para seleccion
    lateinit var selectionRutas:String get

    //Variables de mapa por Red
    private var btnFindPath: Button? = null
    private var etOrigin: EditText? = null
    private var etDestination: EditText? = null
    private var originMarkers: MutableList<Marker>? = ArrayList()
    private var destinationMarkers: MutableList<Marker>? = ArrayList()
    private var polylinePaths: MutableList<Polyline>? = ArrayList()
    private var progressDialog: ProgressDialog? = null

    //Variables para AutoCompleteText
    lateinit var textViewCompleteText: AutoCompleteTextView

    val UI_HANDLER = Handler()
    val UI_UPDTAE_RUNNABLE = object : Runnable {

        override fun run() {
            //drawAllMarker()//Method that will get employee location and draw it on map
            Log.i("TIMER","Hola")
            var points:List<LatLng> = ArrayList<LatLng>()
            val process = parsePosiciones()
            process.execute(applicationContext,points)
            Log.d("SUP","Pirata de culiacan")
            for (a in 0 until process.points.size)
            {
                addMarker(process.points[a].latitude, process.points[a].longitude)
            }
            UI_HANDLER.postDelayed(this, 30000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        llenarACT()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnFindPath = findViewById(R.id.btnFindPath) as Button
        etOrigin = findViewById(R.id.etOrigin) as EditText
        etDestination = findViewById(R.id.etDestination) as EditText

        btnFindPath!!.setOnClickListener({ sendRequest() })

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        myPoss()
        UI_HANDLER.postDelayed(UI_UPDTAE_RUNNABLE, 3000)
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
        //Position the map's camera near Saltillo,Coahuila.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.425166,-101.0094829), 13.0F))
        //autoCompletText()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled = true
    }

    private fun addMarker(lat: Double, lng: Double) {
        val coordenadas = LatLng(lat, lng)
        val miUbicaion = CameraUpdateFactory.newLatLngZoom(coordenadas, 16f)

        if (marcador != null) {
            marcador!!.remove()
        }
        marcador = mMap!!.addMarker(MarkerOptions()
                .position(coordenadas)
                .title("Mi Ubicacion")
                .snippet("Paradas cerca a ti.")
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.persona)));
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap!!.animateCamera(miUbicaion)
        myCircle(lat,lng)
    }

    private fun updatePoss(location: Location?) {
        if (location != null) {
            lat = location.latitude
            lng = location.longitude
            addMarker(lat,lng)

        }
    }

    internal var locationListener: android.location.LocationListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            updatePoss(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {

        }

        override fun onProviderDisabled(provider: String) {

        }
    }

    private fun myPoss() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        updatePoss(location)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0f, locationListener)
    }

    private fun myCircle(lat: Double, lng: Double) {
        val center = LatLng(lat, lng)
        val r = 200
        val circleOptions = CircleOptions()

                .center(center)
                .radius(r.toDouble())
                .strokeColor(Color.parseColor("#0D47A1"))
                .strokeWidth(2f)
                .fillColor(Color.argb(32, 33, 150, 243))

        mMap!!.addCircle(circleOptions)
        mMap!!.clear()
    }

    //Metodos para encontrar ruta
    private fun sendRequest() {
        val origin = etOrigin!!.getText().toString()
        val destination = etDestination!!.getText().toString()
        if (origin.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa direccion de origen!", Toast.LENGTH_SHORT).show()
            return
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa direccion e destino!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            DirectionFinder(this, origin, destination).execute()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

    }

    override fun onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Por favor espera.",
                "Buscando Ruta...!", true)

        if (originMarkers != null) {
            for (marker in originMarkers!!) {
                marker.remove()
            }
        }

        if (destinationMarkers != null) {
            for (marker in destinationMarkers!!) {
                marker.remove()
            }
        }

        if (polylinePaths != null) {
            for (polyline in polylinePaths!!) {
                polyline.remove()
            }
        }
    }

    override fun onDirectionFinderSuccess(routes: List<Route>) {
        progressDialog!!.dismiss()
        polylinePaths = ArrayList()
        originMarkers = ArrayList()
        destinationMarkers = ArrayList()

        for (route in routes) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16f))
            (findViewById(R.id.tvDuration) as TextView).text = route.duration.text
            (findViewById(R.id.tvDistance) as TextView).text = route.distance.text

            (originMarkers as ArrayList<Marker>).add(mMap!!.addMarker(MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title(route.startAddress)
                    .position(route.startLocation)))
            (destinationMarkers as ArrayList<Marker>).add(mMap!!.addMarker(MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(route.endAddress)
                    .position(route.endLocation)))

            val polylineOptions = PolylineOptions().geodesic(true).color(Color.BLUE).width(10f)

            for (i in 0 until route.points.size)
                polylineOptions.add(route.points[i])

            (polylinePaths as ArrayList<Polyline>).add(mMap!!.addPolyline(polylineOptions))
        }
    }

    //Metodo para llenar AutoCompleteText

    fun llenarACT()
    {
        val process = jsonParseRutas()
        process.execute(applicationContext)

        textViewCompleteText = findViewById(R.id.autocomplete_rutas)
        val rutas = getListaRutas()
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_list_item_1, rutas as ArrayList<String>)
        textViewCompleteText.setAdapter<ArrayAdapter<String>>(adapter2)


        //Metodo para Obtener Ruta de CompleteTextView
        obtenerSeleccionRuta()

    }

    fun obtenerSeleccionRuta()
    {
        textViewCompleteText.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            selectionRutas = parent.getItemAtPosition(position) as String
            println(selectionRutas)
        }
    }

}
