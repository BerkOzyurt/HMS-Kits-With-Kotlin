package com.huawei.appdue.safetydetect

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.app.Fragment;
import com.huawei.appdue.R
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.safetydetect.UrlCheckResponse
import com.huawei.hms.support.api.entity.safetydetect.UrlCheckThreat
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectClient
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes


class SafetyDetectUrlCheckAPIFragment: Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    val TAG = "SafetyDetectUrlCheckAPIFragment"

    private val APP_ID = xxx

    private var client: SafetyDetectClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = SafetyDetect.getClient(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fg_urlcheck, container, false)
    }

    override fun onResume() {
        super.onResume()
        client!!.initUrlCheck()
    }

    override fun onPause() {
        super.onPause()
        client!!.shutdownUrlCheck()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity!!.findViewById<View>(R.id.fg_call_url_btn).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fg_call_url_btn) {
            callUrlCheckApi()
        }
    }

    override fun onItemSelected(
        adapterView: AdapterView<*>,
        view: View?,
        pos: Int,
        id: Long
    ) {
        val url = adapterView.getItemAtPosition(pos) as String
        val textView = activity!!.findViewById<EditText>(R.id.fg_call_urlCheck_text)
        textView.setText(url)
        val testRes = activity!!.findViewById<EditText>(R.id.fg_call_urlResult)
        testRes.setText("")
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {}

    private fun callUrlCheckApi() {
        Log.i(TAG, "Start call URL check api")
        val editText = activity!!.findViewById<EditText>(R.id.fg_call_urlCheck_text)
        val realUrl = editText.text.toString().trim { it <= ' ' }
        val testRes = activity!!.findViewById<EditText>(R.id.fg_call_urlResult)
        client!!.urlCheck(
            realUrl, APP_ID.toString(),
            UrlCheckThreat.MALWARE,
            UrlCheckThreat.PHISHING
        )
            .addOnSuccessListener { urlCheckResponse ->
                val list =
                    urlCheckResponse.urlCheckResponse
                if (list.isEmpty()) {
                    // No threats found.
                    testRes.setText("No threats found.")
                } else {
                    // Threats found!
                    testRes.setText("Threats found!")
                }
            }
            .addOnFailureListener { e ->
                val errorMsg: String?
                errorMsg = if (e is ApiException) {
                    "Error: " +
                            SafetyDetectStatusCodes.getStatusCodeString(e.statusCode) + ": " +
                            e.message
                } else {
                    e.message
                }
                Log.d(TAG, errorMsg)
                Toast.makeText(
                    activity!!.applicationContext,
                    errorMsg,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}