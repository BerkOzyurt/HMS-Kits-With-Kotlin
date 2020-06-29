package com.huawei.appdue.scankit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.appdue.R
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions


class ScanKitActivity: AppCompatActivity() {

    val DEFAULT_VIEW = 0x22

    val REQUEST_CODE_SCAN = 0X01

    private var tvResult: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_kit)
        tvResult = findViewById(R.id.tv_result)
    }

    fun newViewBtnClick(view: View?) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            DEFAULT_VIEW
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions == null || grantResults == null || grantResults.size < 2 || grantResults[0] !== PackageManager.PERMISSION_GRANTED || grantResults[1] !== PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (requestCode === DEFAULT_VIEW) {
            ScanUtil.startScan(
                this@ScanKitActivity,
                REQUEST_CODE_SCAN,
                HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create()
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        if (requestCode == REQUEST_CODE_SCAN) {
            val obj: Any = data.getParcelableExtra(ScanUtil.RESULT)
            if (obj is HmsScan) {
                if (!TextUtils.isEmpty(obj.getOriginalValue())) {
                    tvResult!!.text = obj.getOriginalValue()
                }
                return
            }
        }
    }
}