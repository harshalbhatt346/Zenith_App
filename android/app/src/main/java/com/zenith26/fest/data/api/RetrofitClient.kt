package com.zenith26.fest.data.api

import com.google.gson.Gson
import com.zenith26.fest.data.model.ApiErrorResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Primary default: User's PC Wi-Fi LAN IP (for physical Android phones on the same Wi-Fi)
    const val WIFI_HOST_URL = "http://172.16.56.152:8001/"

    // Android Emulator special loopback alias pointing to host machine
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8001/"

    // Localhost for adb reverse tcp:8001 tcp:8001
    const val LOCALHOST_URL = "http://127.0.0.1:8001/"

    // Default to the user's actual PC Wi-Fi IP so physical devices work immediately out of the box!
    private var currentBaseUrl: String = WIFI_HOST_URL
    private var apiServiceInstance: ZenithApiService? = null

    val gson: Gson = Gson()

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun getBaseUrl(): String = currentBaseUrl

    fun updateBaseUrl(newUrl: String) {
        val formattedUrl = if (!newUrl.endsWith("/")) "$newUrl/" else newUrl
        currentBaseUrl = formattedUrl
        apiServiceInstance = createService(formattedUrl)
    }

    private fun createService(baseUrl: String): ZenithApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ZenithApiService::class.java)
    }

    fun getApiService(): ZenithApiService {
        if (apiServiceInstance == null) {
            apiServiceInstance = createService(currentBaseUrl)
        }
        return apiServiceInstance!!
    }

    fun parseErrorDetail(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Unknown server error"
        return try {
            val errorResponse = gson.fromJson(errorBody, ApiErrorResponse::class.java)
            errorResponse.detail ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}
