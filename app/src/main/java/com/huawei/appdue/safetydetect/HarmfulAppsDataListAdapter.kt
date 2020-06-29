package com.huawei.appdue.safetydetect

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.huawei.appdue.R
import com.huawei.hms.support.api.entity.safetydetect.MaliciousAppsData
import com.huawei.hms.support.api.safetydetect.AppsCheckConstants


class HarmfulAppsDataListAdapter: BaseAdapter {

    private val harmfulAppsData: MutableList<MaliciousAppsData> = ArrayList()
    private var context: Context? = null

    constructor(data: List<MaliciousAppsData>?,
                context: Context?) : super()  {
        harmfulAppsData.addAll(data!!)
        this.context = context
    }

    override fun getCount(): Int {
        return harmfulAppsData.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup?): View? {
        var convertView: View = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.item_list_app, parent, false)
        val txtAppPackageName: TextView = convertView.findViewById(R.id.txt_aName)
        val txtAppCategory: TextView = convertView.findViewById(R.id.txt_aCategory)
        val oneHarmfulAppsData = harmfulAppsData[position]
        txtAppPackageName.text = oneHarmfulAppsData.apkPackageName
        txtAppCategory.text = getCategory(oneHarmfulAppsData.apkCategory)
        return convertView
    }

    private fun getCategory(apkCategory: Int): String? {
        if (apkCategory == AppsCheckConstants.VIRUS_LEVEL_RISK) {
            return context!!.getString(R.string.app_type_risk)
        } else if (apkCategory == AppsCheckConstants.VIRUS_LEVEL_VIRUS) {
            return context!!.getString(R.string.app_type_virus)
        }
        return context!!.getString(R.string.app_type_unknown)
    }
}