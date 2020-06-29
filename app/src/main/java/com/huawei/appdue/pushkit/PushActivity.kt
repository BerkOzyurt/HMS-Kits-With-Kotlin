package com.huawei.appdue.pushkit

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.appdue.R
import com.huawei.appdue.models.AccessTokenModel
import com.huawei.appdue.models.NotificationMessageBody
import com.huawei.appdue.models.NotificationMessageModel
import com.huawei.appdue.rest.AccessTokenClient
import com.huawei.appdue.rest.AccessTokenInterface
import com.huawei.appdue.rest.NotificationClient
import com.huawei.appdue.rest.NotificationInterface
import com.huawei.hms.aaid.HmsInstanceId
import kotlinx.android.synthetic.main.activity_push.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PushActivity : AppCompatActivity() {

    private var pushToken: String? = null
    private var accessToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push)

        getToken()

        getAccessToken()

        btn_sendNotification.setOnClickListener {
            sendNotification(this!!.pushToken.toString())
        }
    }

    private fun getToken() {
        object : Thread() {
            override fun run() {
                try {
                    val appId: String =
                        AGConnectServicesConfig.fromContext(this@PushActivity).getString("client/app_id")
                    pushToken = HmsInstanceId.getInstance(this@PushActivity).getToken(appId, "HCM")

                    if (!TextUtils.isEmpty(pushToken)) {
                        Log.i("Token", "get token:$pushToken")
                    }
                } catch (e: Exception) {
                    Log.i("TokenError", "getToken failed, $e")
                }
            }
        }.start()
    }

    private fun getAccessToken() {
        AccessTokenClient.getClient().create(AccessTokenInterface::class.java)
            .createAccessToken(
                "client_credentials",
                "xxx",
                "xxx"
            )
            .enqueue(object : Callback<AccessTokenModel> {
                override fun onFailure(call: Call<AccessTokenModel>, t: Throwable) {
                    Log.d("TagFailure", "HATA " + t.message)
                }

                override fun onResponse(
                    call: Call<AccessTokenModel>,
                    response: Response<AccessTokenModel>
                ) {
                    if (response.isSuccessful) {
                        Log.d("Response", "Token " + response.body()?.access_token)
                        accessToken = response.body()?.access_token
                    }
                }
            })
    }

    private fun sendNotification(pushToken : String) {

        val notifMessageBody: NotificationMessageBody = NotificationMessageBody.Builder(
            "This is Notification Tittle", "Hi, You can write here notification body.",
            arrayOf(pushToken)
        )
            .build()

        NotificationClient.getClient().create(NotificationInterface::class.java)
            .createNotification(
                "Bearer $accessToken",
                notifMessageBody
            )
            .enqueue(object : Callback<NotificationMessageModel> {
                override fun onFailure(call: Call<NotificationMessageModel>, t: Throwable) {
                    Log.d("NotifFailure", "HATA " + t.message)
                }

                override fun onResponse(
                    call: Call<NotificationMessageModel>,
                    response: Response<NotificationMessageModel>
                ) {
                    if (response.isSuccessful) {
                        Log.d("Response", "Response " + response.body())
                    }
                }

            })
    }
}