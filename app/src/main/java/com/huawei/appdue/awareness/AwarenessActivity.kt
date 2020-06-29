package com.huawei.appdue.awareness

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.huawei.appdue.R
import com.huawei.hms.kit.awareness.Awareness
import com.huawei.hms.kit.awareness.status.BeaconStatus
import com.huawei.hms.kit.awareness.status.BluetoothStatus
import com.huawei.hms.kit.awareness.status.HeadsetStatus
import java.util.*


class AwarenessActivity: AppCompatActivity(), View.OnClickListener {

    private val TAG = "CaptureActivity"

    private var mScrollView: ScrollView? = null

    var logView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_awareness)
        mScrollView = findViewById(R.id.root_scrollView)
        logView = findViewById(R.id.logView)
        findViewById<View>(R.id.capture_getTimeCategories).setOnClickListener(this)
        findViewById<View>(R.id.capture_getHeadsetStatus).setOnClickListener(this)
        findViewById<View>(R.id.capture_getLocation).setOnClickListener(this)
        findViewById<View>(R.id.capture_getBehaviorStatus).setOnClickListener(this)
        findViewById<View>(R.id.capture_getLightIntensity).setOnClickListener(this)
        findViewById<View>(R.id.capture_getWeatherStatus).setOnClickListener(this)
        findViewById<View>(R.id.capture_getBluetoothStatus).setOnClickListener(this)
        findViewById<View>(R.id.capture_getBeaconStatus).setOnClickListener(this)
        findViewById<View>(R.id.clear_log).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.capture_getTimeCategories -> getTimeCategories()
            R.id.capture_getHeadsetStatus -> getHeadsetStatus()
            R.id.capture_getLocation -> getLocation()
            R.id.capture_getBehaviorStatus -> getBehaviorStatus()
            R.id.capture_getLightIntensity -> getLightIntensity()
            R.id.capture_getWeatherStatus -> getWeatherStatus()
            R.id.capture_getBluetoothStatus -> getBluetoothStatus()
            R.id.capture_getBeaconStatus -> getBeaconStatus()
            R.id.clear_log -> logView!!.setText("")
            else -> {
            }
        }
    }

    private fun getTimeCategories() {
        Awareness.getCaptureClient(this).timeCategories
            .addOnSuccessListener { timeCategoriesResponse ->
                val timeCategories = timeCategoriesResponse.timeCategories
                val stringBuilder = StringBuilder()
                for (timeCode in timeCategories.timeCategories) {
                    stringBuilder.append(ConstantAwareness.TIME_DESCRIPTION_MAP.get(timeCode))
                }
                logView!!.setText(stringBuilder.toString())
                Log.e(TAG, "Succeeessss : " + stringBuilder.toString())
                scrollToBottom()
            }
            .addOnFailureListener { e ->
                logView!!.setText("Failed to get time categories." + e)
                Log.e(TAG, "Failed to get time categories.", e)
            }
    }

    private fun getHeadsetStatus() {
        Awareness.getCaptureClient(this)
            .headsetStatus
            .addOnSuccessListener { headsetStatusResponse ->
                val headsetStatus = headsetStatusResponse.headsetStatus
                val status = headsetStatus.status
                val stateStr = "Headsets are " +
                        if (status == HeadsetStatus.CONNECTED) "connected" else "disconnected"
                logView!!.setText(stateStr)
                scrollToBottom()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get the headset capture.", e)
                logView!!.setText("Failed to get the headset capture."+ e)
            }
    }

    private fun getLocation() {
        Awareness.getCaptureClient(this).location
            .addOnSuccessListener { locationResponse ->
                val location: Location = locationResponse.location
                logView!!.setText("Longitude:" + location.getLongitude().toString() + ",Latitude:" + location.getLatitude())
                scrollToBottom()
            }
            .addOnFailureListener { e ->
                logView!!.setText("Failed to get the location." + e)
                Log.e(TAG, "Failed to get the location.", e)
            }
    }

    private fun getBehaviorStatus() {
        Awareness.getCaptureClient(this).behavior
            .addOnSuccessListener { behaviorResponse ->
                val behaviorStatus = behaviorResponse.behaviorStatus
                val mostLikelyBehavior =
                    behaviorStatus.mostLikelyBehavior
                val str = "Most likely behavior is " +
                        ConstantAwareness.BEHAVIOR_DESCRIPTION_MAP[mostLikelyBehavior.type] +
                        ",the confidence is " + mostLikelyBehavior.confidence

                logView!!.setText(str)
                scrollToBottom()
            }
            .addOnFailureListener { e ->
                logView!!.setText("Failed to get the behavior." + e)
                Log.e(TAG, "Failed to get the behavior.", e)
            }
    }

    private fun getLightIntensity() {
        Awareness.getCaptureClient(this).lightIntensity
            .addOnSuccessListener { ambientLightResponse ->
                val ambientLightStatus =
                    ambientLightResponse.ambientLightStatus

                logView!!.setText("Light intensity is " + ambientLightStatus.lightIntensity + " lux")
                scrollToBottom()
            }
            .addOnFailureListener { e ->
                logView!!.setText("Failed to get the light intensity." + e)
                Log.e(TAG, "Failed to get the light intensity.", e)
            }
    }

    private fun getWeatherStatus() {
        Awareness.getCaptureClient(this).weatherByDevice
            .addOnSuccessListener { weatherStatusResponse ->
                val weatherStatus = weatherStatusResponse.weatherStatus
                val weatherSituation = weatherStatus.weatherSituation
                val situation = weatherSituation.situation
                // For more weather information, please refer to the development guide.
                val weatherInfoStr =
                    """
                    City:${weatherSituation.city.provinceName}
                    Weather id is ${situation.weatherId}
                    CN Weather id is ${situation.cnWeatherId}
                    Temperature is ${situation.temperatureC}℃,${situation.temperatureF}℉
                    Wind speed is ${situation.windSpeed}km/h
                    Wind direction is ${situation.windDir}
                    Humidity is ${situation.humidity}%
                    """.trimIndent()

                logView!!.setText(weatherInfoStr)
                scrollToBottom()
            }
            .addOnFailureListener {
                logView!!.setText("Failed to get weather information.")
                Log.e(TAG, "Failed to get weather information.")
            }
    }

    private fun getBluetoothStatus() {
        val deviceType = 0 
        Awareness.getCaptureClient(this).getBluetoothStatus(deviceType)
            .addOnSuccessListener { bluetoothStatusResponse ->
                val bluetoothStatus =
                    bluetoothStatusResponse.bluetoothStatus
                val status = bluetoothStatus.status
                val stateStr = "The Bluetooth car stereo is " +
                        if (status == BluetoothStatus.CONNECTED) "connected" else "disconnected"
                logView!!.setText(stateStr)
                scrollToBottom()
            }
            .addOnFailureListener { e ->
                logView!!.setText("Failed to get Bluetooth status." + e)
                Log.e(TAG, "Failed to get Bluetooth status.", e)
            }
    }

    private fun getBeaconStatus() {
        val namespace = "sample namespace"
        val type = "sample type"
        val content = byteArrayOf(
            's'.toByte(),
            'a'.toByte(),
            'm'.toByte(),
            'p'.toByte(),
            'l'.toByte(),
            'e'.toByte()
        )
        val filter = BeaconStatus.Filter.match(namespace, type, content)
        Awareness.getCaptureClient(this).getBeaconStatus(filter)
            .addOnSuccessListener { beaconStatusResponse ->
                val beaconDataList = beaconStatusResponse
                    .beaconStatus.beaconData
                if (beaconDataList != null && beaconDataList.size != 0) {
                    var i = 1
                    val builder = java.lang.StringBuilder()
                    for (beaconData in beaconDataList) {
                        builder.append("Beacon Data ").append(i)
                        builder.append(" namespace:").append(beaconData.namespace)
                        builder.append(",type:").append(beaconData.type)
                        builder.append(",content:")
                            .append(Arrays.toString(beaconData.content))
                        builder.append(". ")
                        i++
                    }
                    logView!!.setText(builder.toString())
                } else {
                    logView!!.setText("No beacon matches filters nearby.")
                    Log.e(TAG, "No beacon matches filters nearby.")
                }
                scrollToBottom()
            }
            .addOnFailureListener { e ->

                logView!!.setText("Failed to get beacon status." + e)
                Log.e(TAG, "Failed to get beacon status.", e)
            }
    }

    private fun scrollToBottom() {
        mScrollView!!.postDelayed({
            mScrollView!!.smoothScrollTo(
                0,
                mScrollView!!.bottom
            )
        }, 200)
    }

}