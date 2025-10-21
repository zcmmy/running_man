package com.example.campusrunner.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // TODO: 替换为实际的后端服务器地址
    private const val BASE_URL = "https://your-api-server.com/api/"

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

            // TODO: 添加认证token（需要先实现用户登录功能）
            /**
             * 功能：为每个请求自动添加认证token
             * 实现步骤：
             * 1. 用户登录成功后，将token保存到本地存储（如SharedPreferences）
             * 2. 在这里从本地存储读取token并添加到请求头
             * 3. 如果token过期，可以在这里添加token刷新逻辑
             */
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

    // TODO: 实现具体的token获取逻辑
    private fun getAuthToken(): String? {
        /**
         * 功能：从本地存储获取认证token
         * 实现示例：
         * val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
         * return sharedPref.getString("auth_token", null)
         */
        // 从SharedPreferences或其他存储中获取token
        // 示例：return SharedPreferencesManager.getAuthToken()
        return null
    }

    // TODO: 在用户登录成功后调用此方法保存token
    fun setAuthToken(token: String) {
        /**
         * 功能：保存认证token到本地存储
         * 实现示例：
         * val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
         * sharedPref.edit().putString("auth_token", token).apply()
         */
        // 保存token到SharedPreferences或其他存储
        // 示例：SharedPreferencesManager.saveAuthToken(token)
    }
}