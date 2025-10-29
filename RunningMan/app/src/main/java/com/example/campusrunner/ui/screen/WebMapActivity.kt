package com.example.campusrunner.ui.screens

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URISyntaxException

class WebMapActivity : ComponentActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL) ?: "about:blank"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "地图"

        setContent {
            MaterialTheme {
                WebMapScreen(
                    title = title,
                    url = url,
                    onClose = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebMapScreen(title: String, url: String, onClose: () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    WebView(it).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                if (url == null) return false
                                val context = view?.context ?: return false

                                val uri: Uri
                                try {
                                    uri = Uri.parse(url)
                                } catch (e: Exception) {
                                    return false
                                }

                                return when (uri.scheme) {
                                    "http", "https" -> {
                                        // [!!] 关键修复 [!!]
                                        // 告诉系统让 WebView 自己处理 http/https 链接，
                                        // 而不是我们手动调用 loadUrl 再返回 false 导致混乱。
                                        return false
                                    }

                                    "amap", "baidumap", "qqmap" -> {
                                        // 处理地图 App 的自定义 Scheme
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            intent.addCategory(Intent.CATEGORY_BROWSABLE)
                                            context.startActivity(intent)
                                            true // 返回 true 表示我们已经处理了这个 URL
                                        } catch (e: ActivityNotFoundException) {
                                            Toast.makeText(context, "未安装对应的地图应用", Toast.LENGTH_SHORT).show()
                                            true
                                        }
                                    }

                                    "intent" -> {
                                        // 处理 intent:// 协议
                                        try {
                                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                            intent.addCategory(Intent.CATEGORY_BROWSABLE)
                                            intent.setPackage(null) // 移除包名限制，让系统选择

                                            if (intent.resolveActivity(context.packageManager) != null) {
                                                context.startActivity(intent)
                                            } else {
                                                // 尝试打开备用 URL (通常是网页版)
                                                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                                if (fallbackUrl != null) {
                                                    view?.loadUrl(fallbackUrl)
                                                } else {
                                                    Toast.makeText(context, "未找到可处理的应用", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            true
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "启动应用失败", Toast.LENGTH_SHORT).show()
                                            true
                                        }
                                    }

                                    else -> {
                                        // 处理其他未知的 Scheme
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            intent.addCategory(Intent.CATEGORY_BROWSABLE)
                                            context.startActivity(intent)
                                            true
                                        } catch (e: ActivityNotFoundException) {
                                            Toast.makeText(context, "未找到可处理的应用: ${uri.scheme}", Toast.LENGTH_SHORT).show()
                                            true
                                        }
                                    }
                                }
                            }
                        }

                        webChromeClient = WebChromeClient()
                        loadUrl(url)
                    }
                }
            )
        }
    }
}

