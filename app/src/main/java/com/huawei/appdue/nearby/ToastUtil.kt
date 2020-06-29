package com.huawei.appdue.nearby

import android.view.Gravity
import android.widget.Toast


class ToastUtil {

    private var toast: Toast? = null
    val myApp = MyApplication()

    fun showShortToastTop(msg: String?) {
        if (myApp.getInstance() != null) {
            if (toast == null) {
                toast = Toast.makeText(myApp.getInstance(), msg, Toast.LENGTH_SHORT)
                toast!!.setGravity(Gravity.TOP, 0, 0)
            } else {
                toast!!.setText(msg)
            }
            toast!!.show()
        }
    }
}