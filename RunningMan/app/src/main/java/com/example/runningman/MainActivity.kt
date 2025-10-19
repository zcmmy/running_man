package com.example.campusrunner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.campusrunner.data.UserRepository
import com.example.campusrunner.navigation.AppNavHost
import com.example.campusrunner.theme.CampusRunnerTheme
import com.example.campusrunner.utils.PermissionUtils

class MainActivity : ComponentActivity() {

    // 定义需要请求的权限
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // 使用新的权限请求API
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // 所有权限都已授予，可以初始化地图等需要权限的功能
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

        // 初始化用户状态检查
        UserRepository.checkLoginStatus()

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
            // 请求缺失的权限
            requestPermissionLauncher.launch(requiredPermissions)
        }
    }

    private fun initializeAfterPermissionsGranted() {
        // 这里初始化需要权限的功能
        // 例如：初始化高德地图服务

        // TODO: 初始化地图服务
        // val mapService = AMapServiceImpl(this)
        // mapService.initialize()
        // (application as CampusRunnerApplication).mapService = mapService
    }

    private fun handlePermissionsDenied() {
        // 处理权限被拒绝的情况
        // 可以显示提示信息，说明为什么需要这些权限

        // 注意：对于Android 11+，如果用户选择了"不再询问"，
        // 可能需要引导用户到设置页面手动开启权限
    }

    // 提供一个公共方法来检查权限状态
    fun hasLocationPermissions(): Boolean {
        return PermissionUtils.hasLocationPermissions(this)
    }

    // 当从设置页面返回时，可以重新检查权限
    override fun onResume() {
        super.onResume()
        // 可以在这里检查权限状态，如果之前被拒绝但现在授予了
        if (hasLocationPermissions()) {
            initializeAfterPermissionsGranted()
        }
    }
}