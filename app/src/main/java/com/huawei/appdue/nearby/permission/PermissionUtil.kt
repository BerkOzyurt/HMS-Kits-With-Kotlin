package com.huawei.appdue.nearby.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build


class PermissionUtil {

    fun hasPermission(context: Context, permission: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(permission!!) !== PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun requestPermissions(
        activity: Activity,
        permissions: Array<String?>?,
        requestCode: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(permissions!!, requestCode)
        }
    }

    fun getDeniedPermissions(
        context: Context,
        permissions: Array<String?>?
    ): Array<String?>? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val deniedPermissionList: ArrayList<String> = ArrayList()
            for (permission in permissions!!) {
                if (context.checkSelfPermission(permission!!) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionList.add(permission!!)
                }
            }
            val size: Int = deniedPermissionList.size
            if (size > 0) {
                return deniedPermissionList.toArray(arrayOfNulls<String>(deniedPermissionList.size))
            }
        }
        return arrayOfNulls(0)
    }
}