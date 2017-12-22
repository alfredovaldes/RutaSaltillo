package com.map.develop.rutasaltillov2.Kotlin

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.map.develop.rutasaltillov2.JSonParsers.CustomOnItemSelectedListener
import com.map.develop.rutasaltillov2.JSonParsers.UIUpdater
import com.map.develop.rutasaltillov2.JSonParsers.jsonParseRutas
import com.map.develop.rutasaltillov2.JSonParsers.jsonParseRutas.getListaRutas
import com.map.develop.rutasaltillov2.R
import com.map.develop.rutasaltillov2.SearchRoute.DirectionFinder
import com.map.develop.rutasaltillov2.SearchRoute.DirectionFinderListener
import com.map.develop.rutasaltillov2.SearchRoute.Route
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection

class MapsActivity :AppCompatActivity(), OnMapReadyCallback, DirectionFinderListener, AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d("BRO","HEY")
    }

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
    //lateinit var textViewCompleteText: AutoCompleteTextView
    lateinit var textViewCompleteText: Spinner
    var markers:ArrayList<Marker> = ArrayList()
    //val markers: MutableSet<Marker> = hashSetOf()
    var rutaSeleccionada = " "
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        /*val process = jsonParseRutas()
        process.execute(applicationContext)
*/
        llenarACT();
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnFindPath = findViewById(R.id.btnFindPath) as Button
        etOrigin = findViewById(R.id.etOrigin) as EditText
        etDestination = findViewById(R.id.etDestination) as EditText

        btnFindPath!!.setOnClickListener({ sendRequest() })
    }

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

        private fun llenarACT() {
            var rutas = ArrayList<String>()
            rutas.add(" ")
            rutas.add("Periferico");
            rutas.add("8 Morelos");
            rutas.add("Zaragoza Directo");
            rutas.add("Ramos");
            rutas.add("Arteaga");
            rutas.add("Inter");
            rutas.add("Mirasierra");
            rutas.add("Lomalinda");
            rutas.add("4-B");
            rutas.add("Mision");
            rutas.add("Express");
            rutas.add("Zapaliname");
            rutas.add("17");
            rutas.add("Vista I.M.M.S");
            rutas.add("7A Directo");
            rutas.add("ROMA");
            rutas.add("Guayulera");
            rutas.add("8 Amp Morelos");
            rutas.add("Nuevo Mierasierra");
            rutas.add("18 Herradura");
            rutas.add("18 Colonias");
            rutas.add("13-A");
            rutas.add("10-Lomas");
            var adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, rutas)
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            adapter2.notifyDataSetChanged()
            textViewCompleteText = findViewById(R.id.autocomplete_rutas)
            textViewCompleteText.adapter = adapter2
            textViewCompleteText.onItemSelectedListener= object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val selectedItem = parent.getItemAtPosition(position).toString()
                    mMap!!.clear()
                    rutaSeleccionada=selectedItem
                    if (selectedItem == "Periferico") {
                        Log.d("Fierro",selectedItem)
                    }
                    else{
                        Log.d("pariente","pirata de culiacan")
                    }
                } // to close the onItemSelected

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
        }


    var mUIUpdater: UIUpdater? = null

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mUIUpdater = UIUpdater(Runnable { setUpMap() })

        mUIUpdater!!.startUpdates()

        try {
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.425166,-101.0094829), 13.0F))
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled = true
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun setUpMap() {
        // Retrieve the city data from the web service
        // In a worker thread since it's a network operation.
        Thread(Runnable {
            try {
                retrieveAndAddCities()
            } catch (e: IOException) {
                Log.e("FAIL", "Cannot retrive cities", e)
                return@Runnable
            }
        }).start()
    }
    private fun contieneId(list:MutableSet<Marker>, id:Int):Boolean {
        return list.any { it.tag === id }
    }
    private fun updatePoss(location: Location?) {
        if (location != null) {
            mMap!!.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)))
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


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @Throws(IOException::class)
    private fun retrieveAndAddCities() {
        Log.d("SUP","ENTRE")
        var conn: HttpURLConnection? = null
        try
        {
            var parser = JSONParser()
            var file = File("/storage/emulated/0/Android/data/com.map.develop.rutasaltillov2/files/jwt.token")
            var fileReader = FileReader(file)
            var obj = parser.parse(fileReader)
            var jsonObject = JSONObject(obj.toString())
            var token = jsonObject.get("token").toString()
            var client = OkHttpClient()
            var request = Request.Builder()
                    .url("https://busmia.herokuapp.com/posicion")
                    .addHeader("authorization","Bearer "+token)
                    .get()
                    .build()
            var response = client.newCall(request).execute()
            var json = JSONObject(response.body()!!.string())
            for(i in  json.keys()) {
                var json1 = json.getJSONObject(i)
                runOnUiThread(object : Runnable {
                    override fun run() {
                        try {
                            createMarkersFromJson(json1)
                        } catch (e: JSONException) {
                            Log.e("WTF", "Error processing JSON", e)
                        }
                    }
                })
            }
        }
        catch (e:IOException) {
            Log.e("WTF", "Error connecting to service", e)
            throw IOException("Error connecting to service", e)
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(JSONException::class)
    fun createMarkersFromJson(json:JSONObject) {
        when(json.get("descripcion")) {
            rutaSeleccionada ->
            {
                if (markers.isEmpty()) {
                    val marker = mMap!!.addMarker(MarkerOptions()
                            .title(json.getString("descripcion"))
                            .position(LatLng(
                                    json.getDouble("lat"),
                                    json.getDouble("lng")
                            ))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))
                    )
                    marker.tag = json.getInt("id")
                    markers.add(marker)
                } else {
                    /*
                    var index = -1
                    markers.forEach({ u ->
                        if (u.tag == json.getInt("id")) {
                            index = (u.tag as Int) - 1
                        }
                    }
                    )
                    if (index == -1) {
                        val marker = mMap!!.addMarker(MarkerOptions()
                                .title(json.getString("descripcion"))
                                .position(LatLng(
                                        json.getDouble("lat"),
                                        jccson.getDouble("lng")
                                ))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)))
                        marker.tag = json.getInt("id")
                        markers.add(marker)
                    } else {
                        markers[index].position = LatLng(
                                json.getDouble("lat"),
                                json.getDouble("lng")
                        )
                        }
                        */
                    var results = ArrayList<Int>()
                    var resultsDesc = ArrayList<Int>()
                    for (i in 0 until markers.size)
                    {
                        if (json.getInt("id") === markers.get(i).tag)
                        {
                            // found value at index i
                            results.add(i)
                        }
                        else{
                            val marker = mMap!!.addMarker(MarkerOptions()
                                    .title(json.getString("descripcion"))
                                    .position(LatLng(
                                            json.getDouble("lat"),
                                            json.getDouble("lng")
                                    ))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)))
                            marker.tag = json.getInt("id")
                            markers.add(marker)
                        }
                    }
                    if(results.isNotEmpty()){
                        for (j in 0 until markers.size){
                            if (json.getString("desc") === markers.get(j).title)
                            {
                                resultsDesc.add(j)
                            }
                        }
                        markers[resultsDesc[0]].position= LatLng(
                                json.getDouble("lat"),
                                json.getDouble("lng")
                        )
                    }
                }

            }
        }
    }
}