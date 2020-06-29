package com.huawei.appdue.nearby.permission

interface PermissionInterface {

    fun getPermissionsRequestCode(): Int

    fun getPermissions(): Array<String?>?

    fun requestPermissionsSuccess()

    fun requestPermissionsFail()
}