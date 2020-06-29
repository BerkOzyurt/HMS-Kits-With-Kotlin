package com.huawei.appdue

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huawei.appdue.awareness.AwarenessActivity
import com.huawei.appdue.fido.BioauthnActivity
import com.huawei.appdue.locationkit.LocationActivity
import com.huawei.appdue.mapkit.MapActivity
import com.huawei.appdue.mlkit.BankCardActivity
import com.huawei.appdue.mlkit.SpeechRecognitionActivity
import com.huawei.appdue.mlkit.TranslateActivity
import com.huawei.appdue.pushkit.PushActivity
import com.huawei.appdue.safetydetect.SafetyActivity
import com.huawei.appdue.scankit.ScanKitActivity
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        push.setOnClickListener {
            val intent = Intent(this, PushActivity::class.java)
            startActivity(intent)
        }


        location.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }

        map.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        translae.setOnClickListener {
            val intent = Intent(this, TranslateActivity::class.java)
            startActivity(intent)
        }

        recognation.setOnClickListener {
            val intent = Intent(this, SpeechRecognitionActivity::class.java)
            startActivity(intent)
        }

        bankCard.setOnClickListener {
            val intent = Intent(this, BankCardActivity::class.java)
            startActivity(intent)
        }

        fido.setOnClickListener {
            val intent = Intent(this, BioauthnActivity::class.java)
            startActivity(intent)
        }

        scan.setOnClickListener {
            val intent = Intent(this, ScanKitActivity::class.java)
            startActivity(intent)
        }

        awaresness.setOnClickListener {
            val intent = Intent(this, AwarenessActivity::class.java)
            startActivity(intent)
        }

        safety.setOnClickListener {
            val intent = Intent(this, SafetyActivity::class.java)
            startActivity(intent)
        }
    }
}