package com.huawei.appdue.rest

import com.huawei.appdue.models.AccessTokenModel
import retrofit2.Call
import retrofit2.http.*

interface AccessTokenInterface {

    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=UTF-8")
    @POST("oauth2/v3/token")
    fun createAccessToken(
        @Field("grant_type") grant_type : String,
        @Field("client_secret") client_secret : String,
        @Field("client_id") client_id : String) : Call<AccessTokenModel>

}