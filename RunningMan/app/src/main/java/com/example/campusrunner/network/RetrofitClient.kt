package com.example.campusrunner.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // TODO: 替换为实际的后端服务器地址
    private const val BASE_URL = "http://118.190.106.81/"

    /**
     * 功能：配置HTTP日志拦截器，方便调试网络请求
     * 说明：在生产环境中应该移除或设置为Level.NONE
     */

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
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

    /**
     * 功能：创建Retrofit实例，配置基础URL和JSON转换器
     * 说明：这里使用Gson进行JSON序列化和反序列化
     */

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // 添加 token 管理（需要 Context）
    private var authToken: String? = null

    // TODO: 实现具体的token获取逻辑
    fun setAuthToken(token: String) {
        authToken = token
        // 可选：保存到 SharedPreferences
        // val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // sharedPref.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        // 先从内存获取，如果没有则从 SharedPreferences 读取
        return authToken ?: run {
            // val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            // sharedPref.getString("auth_token", null)
            null
        }
    }
}