package com.ruizburga.denunciasapp.data.remote

import com.ruizburga.denunciasapp.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    const val BASE_URL = "http://10.0.2.2:8080/"
    private const val DEBUG_BASE_URL = "http://127.0.0.1:8080/"

    // Debug uses `adb reverse tcp:8080 tcp:8080`; this reaches the host through ADB's loopback tunnel.
    private val activeBaseUrl = if (BuildConfig.DEBUG) DEBUG_BASE_URL else BASE_URL

    val denunciasApi: DenunciasApi by lazy {
        Retrofit.Builder()
            .baseUrl(activeBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DenunciasApi::class.java)
    }
}
