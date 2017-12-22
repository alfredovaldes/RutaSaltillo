package com.map.develop.rutasaltillov2.Kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.map.develop.rutasaltillov2.JSonParsers.UIUpdater
import com.map.develop.rutasaltillov2.R
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.net.HttpURLConnection

class MapsActivity :AppCompatActivity(), OnMapReadyCallback{

    private var mMap: GoogleMap? = null
    private val TAG = MapsActivity::class.java.simpleName
//Variable para seleccion
    lateinit var selectionRutas:String get
    var markers:ArrayList<Marker> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
    @Throws(JSONException::class)
    fun createMarkersFromJson(json:JSONObject) {
        if(markers.isEmpty()) {
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
        }
        else {
            var index = -1
            markers.forEach({ u ->
                if (u.tag == json.getInt("id")) {
                    index = (u.tag as Int)-1
                }
            }
            )
            if(index==-1){
                val marker = mMap!!.addMarker(MarkerOptions()
                        .title(json.getString("descripcion"))
                        .position(LatLng(
                                json.getDouble("lat"),
                                json.getDouble("lng")
                        ))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)))
                marker.tag=json.getInt("id")
                markers.add(marker)
            }
            else{
                markers[index].position= LatLng(
                        json.getDouble("lat"),
                        json.getDouble("lng")
                )
            }
        }
    }
}