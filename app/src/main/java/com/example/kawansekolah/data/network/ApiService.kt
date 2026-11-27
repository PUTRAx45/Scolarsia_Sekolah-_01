package com.example.kawansekolah.data.network

import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.data.model.AiInsightRequest
import com.example.kawansekolah.data.model.AiInsightResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.IOException
import java.util.concurrent.TimeUnit

interface ApiService {

    @POST("api/v1/ai-analytics/insight")
    suspend fun getAiInsight(
        @Header("Authorization") token: String,
        @Body request: AiInsightRequest
    ): AiInsightResponse

    companion object {
        fun create(): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            }

            val retryInterceptor = Interceptor { chain ->
                val request = chain.request()
                var response: Response? = null
                var exception: IOException? = null

                try {
                    response = chain.proceed(request)
                } catch (e: IOException) {
                    exception = e
                }

                if (response == null || (!response.isSuccessful && response.code >= 500)) {
                    try {
                        response?.close()
                        response = chain.proceed(request)
                    } catch (e: IOException) {
                        throw exception ?: e
                    }
                }

                if (response != null) {
                    return@Interceptor response
                }
                throw exception ?: IOException("Gagal terhubung ke server AI Scolarsia")
            }

            val dynamicHostInterceptor = Interceptor { chain ->
                var request = chain.request()
                try {
                    val currentBaseUrl = com.example.kawansekolah.utils.DevHost.getBackendBaseUrl().trimEnd('/')
                    if (currentBaseUrl.isNotBlank() && currentBaseUrl != "http://localhost/") {
                        val hostClean = currentBaseUrl.replace("https://", "").replace("http://", "").substringBefore(":").substringBefore("/")
                        val portClean = currentBaseUrl.substringAfterLast(":", "80").substringBefore("/").toIntOrNull() ?: 80
                        val schemeClean = if (currentBaseUrl.startsWith("https")) "https" else "http"

                        if (hostClean.isNotBlank()) {
                            val urlBuilder = request.url.newBuilder()
                                .scheme(schemeClean)
                                .host(hostClean)
                                .port(portClean)
                            request = request.newBuilder().url(urlBuilder.build()).build()
                        }
                    }
                } catch (_: Exception) {
                }
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(dynamicHostInterceptor)
                .addInterceptor(retryInterceptor)
                .addInterceptor(logging)
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build()

            val baseUrl = com.example.kawansekolah.utils.DevHost.getBackendBaseUrl()

            return Retrofit.Builder()
                .baseUrl(if (baseUrl.isNotBlank()) baseUrl else "http://localhost/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
