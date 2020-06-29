package com.huawei.appdue.safetydetect

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.app.Fragment;
import com.huawei.appdue.R
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.safetydetect.UserDetectResponse
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectClient
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException


class SafetyDetectUserDetectAPIFragment: Fragment(), View.OnClickListener {

    val TAG = "SafetyDetectUserDetectAPIFragment"

    private val APP_ID = xxx

    private var client: SafetyDetectClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = SafetyDetect.getClient(activity)
    }

    override fun onResume() {
        super.onResume()
        client!!.initUserDetect()
    }

    override fun onPause() {
        super.onPause()
        client!!.shutdownUserDetect()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fg_userdetect, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity!!.findViewById<View>(R.id.fg_login_btn).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.fg_login_btn) {
            detect()
        }
    }

    private fun detect() {
        Log.i(TAG, "User detection start.")
        client!!.userDetection(APP_ID.toString())
            .addOnSuccessListener { userDetectResponse ->
                Log.i(TAG, "User detection succeed, response = $userDetectResponse")
                val verifySucceed: Boolean =
                    verify(userDetectResponse.responseToken)
                if (verifySucceed) {
                    Toast.makeText(
                        activity!!.applicationContext,
                        "User detection succeed and verify succeed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    Toast.makeText(
                        activity!!.applicationContext,
                        "User detection succeed but verify fail, please replace verify url with your's server address",
                        Toast.LENGTH_SHORT
                    )
                        .show()
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
                Log.i(TAG, "User detection fail. Error info: $errorMsg")
                Toast.makeText(
                    activity!!.applicationContext,
                    errorMsg,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun verify(responseToken: String): Boolean {
        return try {
            object : AsyncTask<String?, Void?, Boolean?>() {

                override fun doInBackground(vararg params: String?): Boolean? {
                    val input = params[0]
                    val jsonObject = JSONObject()
                    return try {
                        val baseUrl = "https://www.example.com/userdetect/verify"
                        jsonObject.put("response", input)
                        val result = sendPost(baseUrl, jsonObject)
                        val resultJson = JSONObject(result)
                        val success = resultJson.getBoolean("success")
                        Log.i(TAG, "verify: result = $success")
                        success
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        false
                    }
                }
            }.execute(responseToken).get()!!
        } catch (e: ExecutionException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }

    @Throws(Exception::class)
    private fun sendPost(baseUrl: String, postDataParams: JSONObject): String? {
        val url = URL(baseUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 20000
        conn.connectTimeout = 20000
        conn.requestMethod = "POST"
        conn.doInput = true
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.outputStream.use { os ->
            BufferedWriter(
                OutputStreamWriter(
                    os,
                    StandardCharsets.UTF_8
                )
            ).use { writer ->
                writer.write(postDataParams.toString())
                writer.flush()
            }
        }
        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val `in` =
                BufferedReader(InputStreamReader(conn.inputStream))
            val sb = StringBuffer()
            var line: String?
            while (`in`.readLine().also { line = it } != null) {
                sb.append(line)
                break
            }
            `in`.close()
            return sb.toString()
        }
        return null
    }
}