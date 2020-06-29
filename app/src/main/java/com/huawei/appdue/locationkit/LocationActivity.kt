package com.huawei.appdue.locationkit

import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.appdue.R
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*


class LocationActivity : AppCompatActivity() {

    private val TAG = "LocationKitActivity"

    private val PERMISSIONS_REQUEST_LOCATION = 9097
    private var mLocationRequest: LocationRequest? = null
    private var settingsClient: SettingsClient? = null
    private var locationManager:LocationManager? = null
    private var tvLocationActual:TextView? = null

    private val fusedLocation: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        initViews()
        if(checkfPermission()) {
            initSettings()
        }

    }


    private fun initViews(){
        tvLocationActual = findViewById(R.id.tv_location_actual)
    }


    private fun initSettings(){

        settingsClient = LocationServices.getSettingsClient(this)
        mLocationRequest = LocationRequest()
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        requestLocationUpdatesWithCallback()
    }


    private fun isGPSEnabled():Boolean{
        val providers = locationManager?.allProviders ?: return false
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)?:false
                && providers.contains(LocationManager.GPS_PROVIDER)
    }

    private fun checkfPermission():Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                ActivityCompat.requestPermissions(this, strings, PERMISSIONS_REQUEST_LOCATION)
                return false
            }
        }
        return true
    }


    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locations = locationResult.locations
            if (locations.isNotEmpty()) {
                val location = locations[0]
                tvLocationActual?.text = getString(
                    R.string.location_lat_long,
                    location.latitude.toString(),location.longitude.toString())
            }
        }
        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            if(!locationAvailability.isLocationAvailable) {
                Toast.makeText(applicationContext,"Please check GPS",
                    Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun requestLocationUpdatesWithCallback() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        settingsClient?.checkLocationSettings(locationSettingsRequest)
            ?.addOnSuccessListener {
                Log.i(TAG, "location settings success")
                fusedLocation
                    .requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback,
                        Looper.getMainLooper())
                    ?.addOnSuccessListener {
                        Log.i(TAG, "requestLocationUpdatesWithCallback onSuccess")
                    }
                    ?.addOnFailureListener { e ->
                        Log.e(TAG, "requestLocationUpdatesWithCallback onFailure:" + e.message)
                    }
            }
            ?.addOnFailureListener { e ->
                Log.e(TAG, "checkLocationSetting onFailure:" + e.message)
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this@LocationActivity, 0)
                    } catch (sie: IntentSender.SendIntentException) {
                        Log.e(TAG, "Err.")
                    }
                }
            }
    }

    private fun removeLocationUpdatesWithCallback() {
        fusedLocation.removeLocationUpdates(mLocationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.size > 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                initSettings()
                //Toast.makeText(this,"Permited", Toast.LENGTH_LONG).show()
            } else {
                //Toast.makeText(this,"Not permited", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        removeLocationUpdatesWithCallback()
        super.onDestroy()
    }
}