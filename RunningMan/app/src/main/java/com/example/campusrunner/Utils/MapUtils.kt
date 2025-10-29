package com.example.campusrunner.utils

import android.content.Context
import android.content.Intent
import android.net.Uri // [!!] 必须导入
import android.widget.Toast
// [!!] 导入你自己的 WebMapActivity，作为备选方案
import com.example.campusrunner.ui.screens.WebMapActivity
import java.net.URLEncoder

/**
 * 包含打开地图的工具函数
 */
object MapUtils {

    // [!!] 默认地址前缀
    private const val ADDRESS_PREFIX = "华中科技大学"

    /**
     * 在地图上显示地址。
     * 优先尝试启动本地安装的地图App (如高德, 百度等)。
     * 如果没有找到本地App，则回退到应用内的 WebView 加载网页地图。
     *
     * @param context 用于启动Intent的上下文
     * @param address 要在地图上显示的地址字符串 (例如: "东区食堂")
     */
    fun showAddressOnWebMap(context: Context, address: String) {
        if (address.isBlank()) {
            Toast.makeText(context, "地址为空", Toast.LENGTH_SHORT).show()
            return
        }

        // [!!] 1. 为地址添加统一前缀
        val fullAddress = "$ADDRESS_PREFIX$address"

        try {
            // [!!] 2. 对完整地址进行编码
            val encodedAddress = URLEncoder.encode(fullAddress, "UTF-8")

            // [!!] 3. 尝试启动本地地图App (优先方案)
            // 使用 "geo:0,0?q=地址" URI，这是最通用的地图 Intent
            val geoUri = Uri.parse("geo:0,0?q=$encodedAddress")
            val nativeMapIntent = Intent(Intent.ACTION_VIEW, geoUri)

            if (nativeMapIntent.resolveActivity(context.packageManager) != null) {
                // 找到了本地地图App (高德、百度等)
                context.startActivity(nativeMapIntent)
            } else {
                // [!!] 4. 未找到本地地图App，执行备选方案：启动应用内 WebView
                // (这需要你在 AndroidManifest.xml 中添加对 'geo' scheme 的 <queries>)

                // 使用高德地图的网页URI "search" 功能
                val webUriString = "https://uri.amap.com/search?keyword=$encodedAddress"

                val webIntent = Intent(context, WebMapActivity::class.java).apply {
                    putExtra(WebMapActivity.EXTRA_URL, webUriString)
                    putExtra(WebMapActivity.EXTRA_TITLE, "查看位置: $fullAddress")
                }
                context.startActivity(webIntent)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "无法打开地图: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}