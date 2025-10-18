package com.example.campusrunner.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // TODO: 替换为实际的后端服务器地址
    private const val BASE_URL = "https://your-api-server.com/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")

            // TODO: 添加认证token（需要先实现用户登录功能）
            getAuthToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // TODO: 实现具体的token获取逻辑
    private fun getAuthToken(): String? {
        // 从SharedPreferences或其他存储中获取token
        // 示例：return SharedPreferencesManager.getAuthToken()
        return null
    }

    // TODO: 在用户登录成功后调用此方法保存token
    fun setAuthToken(token: String) {
        // 保存token到SharedPreferences或其他存储
        // 示例：SharedPreferencesManager.saveAuthToken(token)
    }
}