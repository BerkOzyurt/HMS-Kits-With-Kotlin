package com.huawei.appdue.safetydetect

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast
import android.app.Fragment;
import com.huawei.appdue.R
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.safetydetect.MaliciousAppsData
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import com.huawei.hms.support.api.entity.core.CommonCode;


class SafetyDetectAppsCheckAPIFragment: Fragment(), View.OnClickListener {

    val TAG = "SafetyDetectAppsCheckAPIFragment"

    private var maliciousAppListView: ListView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fg_appscheck, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity!!.findViewById<View>(R.id.fg_enable_appscheck)
            .setOnClickListener(this)
        activity!!.findViewById<View>(R.id.fg_verify_appscheck)
            .setOnClickListener(this)
        activity!!.findViewById<View>(R.id.fg_get_malicious_apps)
            .setOnClickListener(this)
        maliciousAppListView = activity!!.findViewById(R.id.fg_list_app)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.fg_enable_appscheck) {
            enableAppsCheck()
        } else if (id == R.id.fg_verify_appscheck) {
            verifyAppsCheckEnabled()
        } else if (id == R.id.fg_get_malicious_apps) {
            getMaliciousApps()
        }
    }

    private fun verifyAppsCheckEnabled() {
        SafetyDetect.getClient(activity)
            .isVerifyAppsCheck
            .addOnSuccessListener { appsCheckResp ->
                val isVerifyAppsEnabled = appsCheckResp.result
                if (isVerifyAppsEnabled) {
                    val text = "The AppsCheck feature is enabled."
                    Toast.makeText(
                        activity!!.applicationContext,
                        text,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                val errorMsg: String?
                errorMsg = if (e is ApiException) {
                    val apiException = e
                    SafetyDetectStatusCodes.getStatusCodeString(apiException.statusCode) +
                            ": " + apiException.message
                } else {
                    e.message
                }
                val msg = "Verify AppsCheck Enabled failed! Message: $errorMsg"
                Log.e(TAG, msg)
                Toast.makeText(activity!!.applicationContext, msg, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun enableAppsCheck() {
        SafetyDetect.getClient(activity)
            .enableAppsCheck()
            .addOnSuccessListener { appsCheckResp ->
                val isEnableAppsCheck = appsCheckResp.result
                if (isEnableAppsCheck) {
                    val text = "The AppsCheck feature is enabled."
                    Toast.makeText(
                        activity!!.applicationContext,
                        text,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                val errorMsg: String?
                errorMsg = if (e is ApiException) {
                    val apiException = e
                    SafetyDetectStatusCodes.getStatusCodeString(apiException.statusCode) +
                            ": " + apiException.message
                } else {
                    e.message
                }
                val msg = "Enable AppsCheck failed! Message: $errorMsg"
                Toast.makeText(activity!!.applicationContext, msg, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun getMaliciousApps() {
        SafetyDetect.getClient(activity)
            .maliciousAppsList
            .addOnSuccessListener { maliciousAppsListResp ->
                val appsDataList: List<MaliciousAppsData> =
                    maliciousAppsListResp.maliciousAppsList
                if (maliciousAppsListResp.rtnCode == CommonCode.OK) {
                    if (appsDataList.isEmpty()) {
                        val text =
                            "No known potentially malicious apps are installed."
                        Toast.makeText(
                            activity!!.applicationContext,
                            text,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        for (maliciousApp in appsDataList) {
                            Log.e(TAG, "Information about a malicious app:")
                            Log.e(
                                TAG,
                                "  APK: " + maliciousApp.apkPackageName
                            )
                            Log.e(TAG, "  SHA-256: " + maliciousApp.apkSha256)
                            Log.e(
                                TAG,
                                "  Category: " + maliciousApp.apkCategory
                            )
                        }
                        val maliciousAppAdapter: ListAdapter = MaliciousAppsDataListAdapter(
                            appsDataList,
                            activity!!.applicationContext
                        )
                        maliciousAppListView!!.adapter = maliciousAppAdapter
                    }
                } else {
                    val msg =
                        "Get malicious apps list failed! Message: " + maliciousAppsListResp.errorReason
                    Log.e(TAG, msg)
                    Toast.makeText(
                        activity!!.applicationContext,
                        msg,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                val errorMsg: String?
                errorMsg = if (e is ApiException) {
                    val apiException = e
                    SafetyDetectStatusCodes.getStatusCodeString(apiException.statusCode) +
                            ": " + apiException.message
                } else {
                    e.message
                }
                val msg = "Get malicious apps list failed! Message: $errorMsg"
                Log.e(TAG, msg)
                Toast.makeText(activity!!.applicationContext, msg, Toast.LENGTH_SHORT)
                    .show()
            }
    }


}