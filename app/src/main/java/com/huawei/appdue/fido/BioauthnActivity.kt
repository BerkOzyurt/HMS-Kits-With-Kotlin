package com.huawei.appdue.fido

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.View
import android.widget.TextView
import com.huawei.appdue.R
import com.huawei.hms.support.api.fido.bioauthn.*
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


class BioauthnActivity: Activity() {

    private var fingerprintManager: FingerprintManager? = null

    private var resultTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bioauth)
        resultTextView = findViewById(R.id.resultTextView)
        fingerprintManager = createFingerprintManager()
    }

    private fun createFingerprintManager(): FingerprintManager? {
        val callback: BioAuthnCallback = object : BioAuthnCallback() {
            override fun onAuthError(errMsgId: Int, errString: CharSequence) {
                showResult("Authentication error. errorCode=$errMsgId,errorMessage=$errString")
            }

            override fun onAuthSucceeded(result: BioAuthnResult) {
                showResult("Authentication succeeded. CryptoObject=" + result.cryptoObject)
            }

            override fun onAuthFailed() {
                showResult("Authentication failed.")
            }
        }
        return FingerprintManager(
            this,
            Executors.newSingleThreadExecutor(),
            callback
        )
    }

    fun btnFingerAuthenticateWithoutCryptoObjectClicked(view: View?) {
        val errorCode = fingerprintManager!!.canAuth()
        if (errorCode != 0) {
            resultTextView!!.text = ""
            showResult("Can not authenticate. errorCode=$errorCode")
            return
        }
        resultTextView!!.text =
            "Start fingerprint authentication without CryptoObject.\nAuthenticating......\n"
        fingerprintManager!!.auth()
    }

    fun btnFingerAuthenticateWithCryptoObjectClicked(view: View?) {
        val errorCode = fingerprintManager!!.canAuth()
        if (errorCode != 0) {
            resultTextView!!.text = ""
            showResult("Can not authenticate. errorCode=$errorCode")
            return
        }

        val cipher: Cipher = HwBioAuthnCipherFactory(
            "hw_test_fingerprint",
            true
        ).getCipher()!!
        if (cipher == null) {
            showResult("Failed to create Cipher object.")
            return
        }
        val crypto = CryptoObject(cipher)
        resultTextView!!.text =
            "Start fingerprint authentication with CryptoObject.\nAuthenticating......\n"
        fingerprintManager!!.auth(crypto)
    }

    fun btnFaceAuthenticateWithoutCryptoObjectClicked(view: View?) {
        var permissionCheck = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck = checkSelfPermission(Manifest.permission.CAMERA)
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            showResult("The camera permission is not enabled. Please enable it.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            }
            return
        }

        val callback: BioAuthnCallback = object : BioAuthnCallback() {
            override fun onAuthError(errMsgId: Int, errString: CharSequence) {
                showResult(
                    "Authentication error. errorCode=" + errMsgId + ",errorMessage=" + errString
                            + if (errMsgId == 1012) " The camera permission may not be enabled." else ""
                )
            }

            override fun onAuthHelp(helpMsgId: Int, helpString: CharSequence) {
                resultTextView
                    ?.append("Authentication help. helpMsgId=$helpMsgId,helpString=$helpString\n")
            }

            override fun onAuthSucceeded(result: BioAuthnResult) {
                showResult("Authentication succeeded. CryptoObject=" + result.cryptoObject)
            }

            override fun onAuthFailed() {
                showResult("Authentication failed.")
            }
        }

        val cancellationSignal = CancellationSignal()
        val faceManager = FaceManager(this)

        val errorCode = faceManager.canAuth()
        if (errorCode != 0) {
            resultTextView!!.text = ""
            showResult("Can not authenticate. errorCode=$errorCode")
            return
        }

        val flags = 0

        val handler: Handler? = null

        val crypto: CryptoObject? = null
        resultTextView!!.text = "Start face authentication.\nAuthenticating......\n"
        faceManager.auth(crypto, cancellationSignal, flags, callback, handler)
    }

    private fun showResult(msg: String) {
        runOnUiThread {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@BioauthnActivity)
            builder.setTitle("Authentication Result")
            builder.setMessage(msg)
            builder.setPositiveButton("OK", null)
            builder.show()
            resultTextView!!.append(
                """
                    $msg
                    
                    """.trimIndent()
            )
        }
    }

}

internal class HwBioAuthnCipherFactory{


    private val TAG = "HwBioAuthnCipherFactory"

    private var storeKey: String? = null

    private var keyStore: KeyStore? = null

    private var keyGenerator: KeyGenerator? = null

    private var defaultCipher: Cipher? = null

    private var isUserAuthenticationRequired = false

    constructor( storeKey: String?,
                 isUserAuthenticationRequired: Boolean){
        this.storeKey = storeKey
        this.isUserAuthenticationRequired = isUserAuthenticationRequired
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                initDefaultCipherObject()
            } catch (e: Exception) {
                defaultCipher = null
                Log.e(TAG, "Failed to init Cipher. " + e.message)
            }
        } else {
            defaultCipher = null
            Log.e(TAG, "Failed to init Cipher.")
        }
    }

    private fun initDefaultCipherObject() {
        keyStore = try {
            KeyStore.getInstance("AndroidKeyStore")
        } catch (e: KeyStoreException) {
            throw RuntimeException(
                "Failed to get an instance of KeyStore(AndroidKeyStore). " + e.message,
                e
            )
        }
        keyGenerator = try {
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(
                "Failed to get an instance of KeyGenerator(AndroidKeyStore)." + e.message,
                e
            )
        } catch (e: NoSuchProviderException) {
            throw RuntimeException(
                "Failed to get an instance of KeyGenerator(AndroidKeyStore)." + e.message,
                e
            )
        }
        createSecretKey(storeKey!!, true)
        defaultCipher = try {
            Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC
                        + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get an instance of Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get an instance of Cipher", e)
        }
        initCipher(defaultCipher!!, storeKey.toString())
    }

    private fun initCipher(
        cipher: Cipher,
        storeKeyName: String
    ) {
        try {
            keyStore!!.load(null)
            val secretKey: SecretKey = keyStore!!.getKey(storeKeyName, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        } catch (e: KeyStoreException) {
            throw java.lang.RuntimeException("Failed to init Cipher. " + e.message, e)
        } catch (e: CertificateException) {
            throw java.lang.RuntimeException("Failed to init Cipher. " + e.message, e)
        } catch (e: UnrecoverableKeyException) {
            throw java.lang.RuntimeException("Failed to init Cipher. " + e.message, e)
        } catch (e: IOException) {
            throw java.lang.RuntimeException("Failed to init Cipher. " + e.message, e)
        } catch (e: NoSuchAlgorithmException) {
            throw java.lang.RuntimeException("Failed to init Cipher. " + e.message, e)
        } catch (e: InvalidKeyException) {
            throw java.lang.RuntimeException("Failed to init Cipher. " + e.message, e)
        }
    }

    private fun createSecretKey(
        storeKeyName: String,
        isInvalidatedByBiometricEnrollment: Boolean
    ) {
        try {
            keyStore!!.load(null)
            var keyParamBuilder: KeyGenParameterSpec.Builder? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyParamBuilder = KeyGenParameterSpec.Builder(
                    storeKeyName,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC) // This key is authorized to be used only if the user has been authenticated.
                    .setUserAuthenticationRequired(isUserAuthenticationRequired)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                keyParamBuilder!!.setInvalidatedByBiometricEnrollment(
                    isInvalidatedByBiometricEnrollment
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator!!.init(keyParamBuilder!!.build())
            }
            keyGenerator!!.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw java.lang.RuntimeException("Failed to create secret key. " + e.message, e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw java.lang.RuntimeException("Failed to create secret key. " + e.message, e)
        } catch (e: CertificateException) {
            throw java.lang.RuntimeException("Failed to create secret key. " + e.message, e)
        } catch (e: IOException) {
            throw java.lang.RuntimeException("Failed to create secret key. " + e.message, e)
        }
    }

    fun getCipher(): Cipher? {
        return defaultCipher
    }


}