package com.huawei.appdue.mapkit

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.appdue.R
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions


class MapActivity : AppCompatActivity(), OnMapReadyCallback, HuaweiMap.OnMapLongClickListener, HuaweiMap.OnMapClickListener {

    private var hMap: HuaweiMap? = null

    private var mMapView: MapView? = null

    private val PERMISSIONS_REQUEST_MAPS = 9897

    private val RUNTIME_PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val MAPVIEW_BUNDLE_KEY = "xxx"

    private var marker: Marker? = null
    private var draggableMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.huawei.appdue.R.layout.activity_map)
        initViews(savedInstanceState)

    }

    override fun onMapLongClick(point: LatLng) {
        Log.w("MAP", "long pressed : $point")
        addMarker(point)
    }

    private fun addMarker(latLng: LatLng) {
        marker = hMap!!.addMarker(
            MarkerOptions().position(latLng)
                .anchor(0.5f, 0.9f)
                .title("New Marker")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .snippet(latLng.toString())
        )
    }

    override fun onMapClick(p0: LatLng?) {
        Toast.makeText(this,"Location : $p0", Toast.LENGTH_LONG).show()
        Log.w("MAP", "map pressed : $p0")
    }

    private fun initViews(savedInstanceState: Bundle?){
        if (!hasPermissions(this, RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, PERMISSIONS_REQUEST_MAPS)
        }

        mMapView = findViewById(R.id.mapView)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView?.onCreate(mapViewBundle)
        mMapView?.getMapAsync(this)

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==PERMISSIONS_REQUEST_MAPS){
            if(grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Permission OK", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this,"Not Permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onMapReady(huaweiMap: HuaweiMap?) {
        hMap = huaweiMap
        hMap?.isMyLocationEnabled = true
        hMap?.uiSettings?.isMyLocationButtonEnabled = true
        hMap?.setOnMapClickListener(this);
        hMap?.setOnMapLongClickListener(this);

    }

    override fun onStart() {
        super.onStart()
        mMapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
    }

    override fun onPause() {
        mMapView?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }
}
