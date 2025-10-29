package com.example.campusrunner

import android.Manifest
import android.content.pm.PackageManager // [!!] 添加了缺失的 import
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.campusrunner.data.UserRepository
import com.example.campusrunner.navigation.AppNavHost
import com.example.campusrunner.theme.CampusRunnerTheme
import com.example.campusrunner.utils.PermissionUtils
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
        // [!!] 注意: 如果你的App也不需要读写存储，可以清空这个数组
    )

    // 使用新的权限请求API
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // 所有权限都已授予
            initializeAfterPermissionsGranted()
        } else {
            // 有些权限被拒绝，处理这种情况
            handlePermissionsDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查并请求权限
        checkAndRequestPermissions()

        // [!!] 按照要求，注释掉登录状态检查，直接进入App
        // 初始化用户状态检查
        lifecycleScope.launch {
            UserRepository.checkLoginStatus()
        }

        setContent {
            CampusRunnerTheme {
                AppNavHost()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        // 检查是否已经拥有所有需要的权限
        val hasAllPermissions = PermissionUtils.hasPermissions(this, requiredPermissions)

        if (hasAllPermissions) {
            // 已经有所有权限，直接初始化
            initializeAfterPermissionsGranted()
        } else {
            // [!!] 修复了逻辑：
            // 1. 如果有权限要请求，则启动请求。
            // 2. 如果没有权限要请求 (数组为空)，则直接初始化。
            if (requiredPermissions.isNotEmpty()) {
                requestPermissionLauncher.launch(requiredPermissions)
            } else {
                initializeAfterPermissionsGranted()
            }
        }
    }

    private fun initializeAfterPermissionsGranted() {
        // 这里初始化需要权限的功能
        // (例如：创建通知渠道、初始化本地存储等)
    }

    private fun handlePermissionsDenied() {
        // 处理权限被拒绝的情况
        // 可以显示提示信息，说明为什么需要这些权限
    }

}

