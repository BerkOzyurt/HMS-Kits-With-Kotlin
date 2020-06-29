package com.huawei.appdue.safetydetect

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.app.Fragment;
import com.huawei.appdue.R
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes


class SafetyDetectWifiDetectAPIFragment: Fragment(), View.OnClickListener {

    val TAG = "SafetyDetectWifiDetectAPIFragment"

    private var mButton1: Button? = null

    private var wifiDetectStatusView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fg_wifidetect, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mButton1 = activity!!.findViewById(R.id.fg_get_wifidetect_status)
        mButton1!!.setOnClickListener(this)
        wifiDetectStatusView = activity!!.findViewById(R.id.fg_wifidetecttextView)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.fg_get_wifidetect_status) {
            getWifiDetectStatus()
        }
    }

    private fun getWifiDetectStatus() {
        SafetyDetect.getClient(activity)
            .wifiDetectStatus
            .addOnSuccessListener { wifiDetectResponse ->
                val wifiDetectStatus = wifiDetectResponse.wifiDetectStatus
                val wifiDetectView = "WifiDetect status: $wifiDetectStatus"
                wifiDetectStatusView!!.text = wifiDetectView
            }
            .addOnFailureListener { e ->
                val errorMsg: String?
                errorMsg = if (e is ApiException) {
                    val apiException = e
                    (SafetyDetectStatusCodes.getStatusCodeString(apiException.statusCode) + ": "
                            + apiException.message)
                } else {
                    e.message
                }
                val msg = "Get wifiDetect status failed! Message: $errorMsg"
                Log.e(TAG, msg)
                wifiDetectStatusView!!.text = msg
            }
    }

}