package com.huawei.appdue.rest

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AccessTokenClient {

    companion object{
        private const val TIMEOUT: Long = 500000

        fun getClient() : Retrofit{

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT,TimeUnit.MICROSECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://oauth-login.cloud.huawei.com/").client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}