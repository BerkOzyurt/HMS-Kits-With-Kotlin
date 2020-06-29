package com.huawei.appdue.rest

import com.huawei.appdue.models.NotificationMessageBody
import com.huawei.appdue.models.NotificationMessageModel
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call

interface NotificationInterface {

    //POST, Body
    @Headers("Content-Type:application/json; charset=UTF-8")
    @POST("v1/xxx/messages:send")
    fun createNotification(
        @Header("Authorization") authorization: String?,
        @Body notifMessageBody: NotificationMessageBody) : Call<NotificationMessageModel>
}