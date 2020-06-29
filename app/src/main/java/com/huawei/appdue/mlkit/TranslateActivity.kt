package com.huawei.appdue.mlkit

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.huawei.appdue.R
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslateSetting
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslator
import kotlinx.android.synthetic.main.activity_translate.*
import java.util.*
import kotlin.collections.ArrayList


class TranslateActivity : AppCompatActivity(){

    private val TAG = "TranslateActivity"
    private var mTextView: TextView? = null
    private var mEditText: EditText? = null

    var fromLanguageList = arrayOf("Chinese", "English", "French", "Arabic", "Thai", "Spanish", "Turkish", "Portuguese", "Japanese", "German", "Italian", "Russian")
    private var fromLanguage: String? = null
    var toLanguageList = arrayOf("Chinese", "English", "French", "Arabic", "Thai", "Spanish", "Turkish", "Portuguese", "Japanese", "German", "Italian", "Russian")
    private var toLanguage: String? = null
    private var selectedLanguageCode: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        this.mTextView = this.findViewById(R.id.tv_output);
        this.mEditText = this.findViewById(R.id.et_input);

        val fromSpinner = findViewById<Spinner>(R.id.fromSpinner)
        val adapterFrom = ArrayAdapter(this, R.layout.spinner_item, fromLanguageList)
        fromSpinner.adapter = adapterFrom
        fromSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                setLanguage(fromLanguageList[position], "From")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val toSpinner = findViewById<Spinner>(R.id.toSpinner)
        val adapterTo = ArrayAdapter(this, R.layout.spinner_item, toLanguageList)
        toSpinner.adapter = adapterTo
        toSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,view: View, position: Int, id: Long) {
                setLanguage(toLanguageList[position], "To")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        btn_translator.setOnClickListener {
            translateText(mEditText?.text!!, fromLanguage!!, toLanguage!!)
        }
    }

    fun setLanguage(selectedLanguage: String, currentSpinner: String){
        if(currentSpinner.equals("From")){
            if(selectedLanguage.equals("Chinese")){
                fromLanguage = "zh"
            }else if(selectedLanguage.equals("English")){
                fromLanguage = "en"
            }else if(selectedLanguage.equals("French")){
                fromLanguage = "fr"
            }else if(selectedLanguage.equals("Arabic")){
                fromLanguage = "ar"
            }else if(selectedLanguage.equals("Thai")){
                fromLanguage = "th"
            }else if(selectedLanguage.equals("Spanish")){
                fromLanguage = "es"
            }else if(selectedLanguage.equals("Turkish")){
                fromLanguage = "tr"
            }else if(selectedLanguage.equals("Portuguese")){
                fromLanguage = "pt"
            }else if(selectedLanguage.equals("Japanese")){
                fromLanguage = "ja"
            }else if(selectedLanguage.equals("German")){
                fromLanguage = "de"
            }else if(selectedLanguage.equals("Italian")){
                fromLanguage = "it"
            }else if(selectedLanguage.equals("Russian")){
                fromLanguage = "ru"
            }
        }else {
            if(selectedLanguage.equals("Chinese")){
                toLanguage = "zh"
            }else if(selectedLanguage.equals("English")){
                toLanguage = "en"
            }else if(selectedLanguage.equals("French")){
                toLanguage = "fr"
            }else if(selectedLanguage.equals("Arabic")){
                toLanguage = "ar"
            }else if(selectedLanguage.equals("Thai")){
                toLanguage = "th"
            }else if(selectedLanguage.equals("Spanish")){
                toLanguage = "es"
            }else if(selectedLanguage.equals("Turkish")){
                toLanguage = "tr"
            }else if(selectedLanguage.equals("Portuguese")){
                toLanguage = "pt"
            }else if(selectedLanguage.equals("Japanese")){
                toLanguage = "ja"
            }else if(selectedLanguage.equals("German")){
                toLanguage = "de"
            }else if(selectedLanguage.equals("Italian")){
                toLanguage = "it"
            }else if(selectedLanguage.equals("Russian")){
                toLanguage = "ru"
            }
        }
    }

    fun translateText(sourceText: Editable, fromLang: String, toLang: String) {
        val setting: MLRemoteTranslateSetting =
            MLRemoteTranslateSetting.Factory()
                .setSourceLangCode(fromLang)
                .setTargetLangCode(toLang)
                .create()
        val mlRemoteTranslator: MLRemoteTranslator =
            MLTranslatorFactory.getInstance().getRemoteTranslator(setting)

        if(!TextUtils.isEmpty(sourceText)){
            val task: Task<String> =
                mlRemoteTranslator.asyncTranslate(sourceText.toString())
            task.addOnSuccessListener {
                mTextView?.text = it
            }.addOnFailureListener {
                mTextView?.text = "Error"
            }
        } else {
            mTextView?.text = "null"
        }
    }
}