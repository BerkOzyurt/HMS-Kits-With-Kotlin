package com.huawei.appdue.authservice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.appdue.HomeActivity
import com.huawei.appdue.R
import com.huawei.appdue.locationkit.LocationActivity
import com.huawei.appdue.models.User
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.api.entity.hwid.HwIDConstant
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var mAuthManager: HuaweiIdAuthService
    private lateinit var mAuthParam: HuaweiIdAuthParams
    private lateinit var mClient: HuaweiIdAuthService

    private var REQUEST_CODE_SIGNIN_HWID = 8888

    val mLoginCallbacks: List<OnLoginEventCallBack> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_signIn.setOnClickListener {
            login()
        }

    }

    fun login(){
        logout()
        val huaweiIdAuthParamsHelper =
            HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
        val scopeList: MutableList<Scope> = ArrayList()
        scopeList.add(Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE))
        huaweiIdAuthParamsHelper.setScopeList(scopeList)
        val authParams =
            huaweiIdAuthParamsHelper.setAccessToken().createParams()

        mClient = HuaweiIdAuthManager.getService(this, authParams)
        this.startActivityForResult(mClient.signInIntent, REQUEST_CODE_SIGNIN_HWID)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.w(
            "LOGIN",
            "onActivityResult: $requestCode resultCode = $resultCode"
        )
        if (requestCode == REQUEST_CODE_SIGNIN_HWID) {
            val signInHuaweiIdTask =
                HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (signInHuaweiIdTask.isSuccessful) {
                val huaweiAccount = signInHuaweiIdTask.result
                val accessToken = huaweiAccount.accessToken
                Log.w("LOGIN", "accessToken: $accessToken")
                val credential =
                    HwIdAuthProvider.credentialWithToken(accessToken)
                val provider_now = credential.provider
                Log.w("LOGIN", "provider_now: $provider_now")
                AGConnectAuth.getInstance().signIn(credential)
                    .addOnSuccessListener { signInResult ->
                        val user = AGConnectAuth.getInstance().currentUser
                        val userModel = User()
                        userModel.display_name = user.displayName
                        userModel.anonyms = user.isAnonymous
                        userModel.mail = user.email
                        userModel.provider_id = user.providerId
                        userModel.u_id = user.uid

                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()

                        for (loginEventCallBack in mLoginCallbacks) {
                            loginEventCallBack.onLogin(true, signInResult)
                        }
                    }.addOnFailureListener { e: Exception ->
                        Toast.makeText(applicationContext, "Sign In Failture", Toast.LENGTH_SHORT)
                            .show()
                        Log.w("LOGIN", "sign in for agc failed: " + e.message)
                        for (loginEventCallBack in mLoginCallbacks) {
                            loginEventCallBack.onLogOut(false)
                        }
                    }
            } else {
                Toast.makeText(applicationContext, "Sign In Failed", Toast.LENGTH_SHORT).show()
                Log.e(
                    "LOGIN",
                    "sign in failed : " + (signInHuaweiIdTask.exception as ApiException).statusCode
                )
            }
        }
    }

    fun logout(){
        val auth = AGConnectAuth.getInstance()
        auth.signOut()
    }



    interface OnLoginEventCallBack {
        fun onLogin(
            showLoginUserInfo: Boolean,
            signInResult: SignInResult?
        )

        fun onLogOut(showLoginUserInfo: Boolean)
    }

}
