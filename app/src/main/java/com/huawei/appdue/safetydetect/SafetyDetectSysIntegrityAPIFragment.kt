package com.huawei.appdue.safetydetect

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.app.Fragment;
import com.huawei.appdue.R
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import org.json.JSONException
import org.json.JSONObject


class SafetyDetectSysIntegrityAPIFragment: Fragment(), View.OnClickListener {

    val TAG = "SafetyDetectSysIntegrityAPIFragment"

    private val APP_ID = "xxx"

    private var mButton1: Button? = null

    private var basicIntegrityTextView: TextView? = null

    private var adviceTextView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fg_sysintegrity, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mButton1 =
            activity!!.findViewById(R.id.fg_button_sys_integrity_go)
        mButton1!!.setOnClickListener(this)
        basicIntegrityTextView = activity!!.findViewById(R.id.fg_payloadBasicIntegrity)
        adviceTextView = activity!!.findViewById(R.id.fg_payloadAdvice)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.fg_button_sys_integrity_go) {
            processView()
            invokeSysIntegrity()
        }
    }

    private fun invokeSysIntegrity() {
        Log.e(TAG, "Started DETECT")
        val nonce =
            ("Sample" + System.currentTimeMillis()).toByteArray()
        SafetyDetect.getClient(activity)
            .sysIntegrity(nonce, APP_ID)
            .addOnSuccessListener { response ->
                Log.e(TAG, "onSuccess Listener")
                val jwsStr = response.result
                val jwsSplit = jwsStr.split("\\.").toTypedArray()
                val jwsPayloadStr = jwsSplit[0]
                val payloadDetail =
                    String(Base64.decode(jwsPayloadStr.toByteArray(), Base64.URL_SAFE))
                try {
                    val jsonObject = JSONObject(payloadDetail)
                    val basicIntegrity: Boolean = jsonObject.getBoolean("basicIntegrity")
                    mButton1!!.setText("Rerun detection")
                    val isBasicIntegrity = basicIntegrity.toString()
                    val basicIntegrityResult =
                        "Basic Integrity: $isBasicIntegrity"
                    basicIntegrityTextView!!.text = basicIntegrityResult
                    Log.e(TAG, "BASIC INTEFRITITY : " + basicIntegrityResult)
                    if (!basicIntegrity) {
                        val advice = "Advice: " + jsonObject.getString("advice")
                        adviceTextView!!.text = advice
                        Log.e(TAG, "ADVICE : " + advice)
                    }
                } catch (e: JSONException) {
                    val errorMsg = e.message
                    Log.e(TAG, errorMsg ?: "unknown error")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "onFailure DETECT")
                val errorMsg: String?
                errorMsg = if (e is ApiException) {
                    val apiException = e
                    SafetyDetectStatusCodes.getStatusCodeString(apiException.statusCode) +
                            ": " + apiException.message
                } else {
                    e.message
                }
                Log.e(TAG, errorMsg)
                Toast.makeText(
                    activity!!.applicationContext,
                    errorMsg,
                    Toast.LENGTH_SHORT
                ).show()
                mButton1!!.setText("Rerun detection")
                Log.e(TAG, "ERROR : ")
            }
    }

    private fun processView() {
        basicIntegrityTextView!!.text = ""
        adviceTextView!!.text = ""
        (activity!!.findViewById<View>(R.id.fg_textView_title) as TextView).text = ""
        mButton1!!.setText("Detecting")
    }
}