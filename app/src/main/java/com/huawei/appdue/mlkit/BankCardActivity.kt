package com.huawei.appdue.mlkit

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.appdue.R
import com.huawei.hms.mlplugin.card.bcr.MLBcrCapture
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureConfig
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureFactory
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureResult


class BankCardActivity: AppCompatActivity(), View.OnClickListener {

    private val TAG: String = "BankCardActivity"
    private val CAMERA_PERMISSION_CODE = 1
    private val READ_EXTERNAL_STORAGE_CODE = 2
    private var cardResultFront = ""

    private var mTextView: TextView? = null
    private var previewImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_bank_card)

        mTextView = findViewById(R.id.text_result)
        previewImage = findViewById(R.id.Bank_Card_image)

        previewImage!!.setScaleType(ImageView.ScaleType.FIT_XY)

        findViewById<View>(R.id.detect).setOnClickListener(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
            this.requestCameraPermission()
        }
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {
            this.requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA  ) ) {
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_CODE)
        }
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE ) ) {
            ActivityCompat.requestPermissions(this, permissions, READ_EXTERNAL_STORAGE_CODE)
        }
    }

    private fun formatIdCardResult(bankCardResult: MLBcrCaptureResult): String? {
        val resultBuilder = StringBuilder()
        resultBuilder.append("Number：")
        resultBuilder.append(bankCardResult.number)
        resultBuilder.append("\r\n")
        Log.i(TAG, "front result: $resultBuilder")
        return resultBuilder.toString()
    }

    private fun displayFailure() {
        mTextView!!.text = "Failure"
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val banCallback: MLBcrCapture.Callback = object : MLBcrCapture.Callback {
        override fun onSuccess(bankCardResult: MLBcrCaptureResult) {
            Log.i(TAG, "CallBack onRecSuccess")
            Log.i(TAG, "CardNumber : " + bankCardResult.number)
            if (bankCardResult == null) {
                Log.i(TAG,"CallBack onRecSuccess idCardResult is null")
                return
            }
            val bitmap = bankCardResult.originalBitmap
            this@BankCardActivity!!.previewImage!!.setImageBitmap(bitmap)
            this@BankCardActivity!!.cardResultFront = this@BankCardActivity!!.formatIdCardResult(bankCardResult)!!
            this@BankCardActivity.mTextView!!.setText(this@BankCardActivity.cardResultFront)
        }

        override fun onCanceled() {
            Log.i(TAG, "CallBackonRecCanceled")
        }

        override fun onFailure(retCode: Int, bitmap: Bitmap) {
            this@BankCardActivity.displayFailure()
            Log.i(TAG, "CallBackonRecFailed")
        }

        override fun onDenied() {
            this@BankCardActivity.displayFailure()
            Log.i(TAG, "CallBackonCameraDenied")
        }
    }

    private fun startCaptureActivity(Callback: MLBcrCapture.Callback) {
        val config =
            MLBcrCaptureConfig.Factory()
                .setOrientation(MLBcrCaptureConfig.ORIENTATION_AUTO)
                .create()
        val bcrCapture = MLBcrCaptureFactory.getInstance().getBcrCapture(config)
        bcrCapture.captureFrame(this, Callback)
    }

    override fun onClick(v: View?) {
        this.mTextView!!.setText("");
        this.startCaptureActivity(this.banCallback);
    }
}