package com.huawei.appdue.mlkit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.appdue.R
import com.huawei.hms.mlplugin.asr.MLAsrCaptureActivity
import com.huawei.hms.mlplugin.asr.MLAsrCaptureConstants
import kotlinx.android.synthetic.main.activity_text_recognition.*


class SpeechRecognitionActivity: AppCompatActivity(), View.OnClickListener {

    private val TAG = "SpeechRecognitionActivity"
    private val HANDLE_CODE = 0
    private val HANDLE_KEY = "text"
    private val AUDIO_PERMISSION_CODE = 1
    private val ML_ASR_CAPTURE_CODE = 2
    private var mTextView: TextView? = null

    var handler: Handler = Handler(object : Handler.Callback {
        override fun handleMessage(message: Message): Boolean {
            when (message.what) {
                HANDLE_CODE -> {
                    val text: String = message.getData().getString(HANDLE_KEY)!!
                    mTextView!!.text = """ $text """.trimIndent()
                    Log.e(TAG, text)
                }
                else -> { }
            }
            return false
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_text_recognition)
        this.mTextView = this.findViewById(R.id.textView);
        voice_input.setOnClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            this.requestAudioPermission();
        }
    }

    private fun requestAudioPermission() {
        val permissions =
            arrayOf(Manifest.permission.RECORD_AUDIO)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, permissions, AUDIO_PERMISSION_CODE )
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode != AUDIO_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
    }

    private fun displayResult(str: String) {
        val msg = Message()
        val data = Bundle()
        data.putString(HANDLE_KEY, str)
        msg.data = data
        msg.what = HANDLE_CODE
        handler.sendMessage(msg)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.voice_input -> {
                val intent = Intent(this,MLAsrCaptureActivity::class.java)
                    .putExtra(MLAsrCaptureConstants.LANGUAGE,"en-US")
                    .putExtra(MLAsrCaptureConstants.FEATURE, MLAsrCaptureConstants.FEATURE_WORDFLUX)
                startActivityForResult(intent, ML_ASR_CAPTURE_CODE)
            }else -> { }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        var text = ""
        if (null == data) {
            displayResult("Intent data is null.")
        }
        if (requestCode == ML_ASR_CAPTURE_CODE) {
            when (resultCode) {
                MLAsrCaptureConstants.ASR_SUCCESS -> if (data != null) {
                    val bundle = data.extras
                    if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_RESULT)) {
                        text = bundle.getString(MLAsrCaptureConstants.ASR_RESULT)!!
                    }
                    if (text == null || "" == text) {
                        text = "Result is null."
                    }
                    displayResult(text)
                }
                MLAsrCaptureConstants.ASR_FAILURE -> {
                    if (data != null) {
                        val bundle = data.extras
                        if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_CODE)) {
                            text = text + bundle.getInt(MLAsrCaptureConstants.ASR_ERROR_CODE)
                        }
                        if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)) {
                            val errorMsg =
                                bundle.getString(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)
                            if (errorMsg != null && "" != errorMsg) {
                                text = "[$text]$errorMsg"
                            }
                        }
                    }
                    displayResult(text)
                    displayResult("Failure.")
                }
                else -> displayResult("Failure.")
            }
        }
    }
}