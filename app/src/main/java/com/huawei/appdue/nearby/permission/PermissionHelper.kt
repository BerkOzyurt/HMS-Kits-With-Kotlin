package com.huawei.appdue.nearby.permission

import android.app.Activity
import android.content.pm.PackageManager
import com.huawei.appdue.nearby.NearbyActivity


class PermissionHelper {


    private var mActivity: Activity? = null
    private var mPermissionInterface: PermissionInterface? = null
    private val permUt: PermissionUtil? = null

    constructor(
        activity: Activity,
        permissionInterface: PermissionInterface
    ) {
        mActivity = activity
        mPermissionInterface = permissionInterface
    }

    fun requestPermissions() {
        val deniedPermissions: Array<String?>? =
            permUt!!.getDeniedPermissions(this!!.mActivity!!, mPermissionInterface!!.getPermissions())
        if (deniedPermissions != null && deniedPermissions.size > 0) {
            permUt.requestPermissions(
                this!!.mActivity!!, deniedPermissions, mPermissionInterface!!.getPermissionsRequestCode()
            )
        } else {
            mPermissionInterface!!.requestPermissionsSuccess()
        }
    }

    fun requestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ): Boolean {
        if (requestCode == mPermissionInterface!!.getPermissionsRequestCode()) {
            var isAllGranted = true
            for (result in grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    isAllGranted = false
                    break
                }
            }
            if (isAllGranted) {
                mPermissionInterface!!.requestPermissionsSuccess()
            } else {
                mPermissionInterface!!.requestPermissionsFail()
            }
            return true
        }
        return false
    }


}