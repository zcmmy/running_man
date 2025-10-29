package com.example.campusrunner.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {

    // 定义权限组
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // 检查权限是否已授予
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 检查存储权限
    fun hasStoragePermissions(context: Context): Boolean {
        return hasPermissions(context, STORAGE_PERMISSIONS)
    }

    // 解释为什么需要权限（用于向用户说明）
    fun getPermissionExplanation(permissions: Array<String>): String {
        return when {
            permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) ->
                "需要位置权限来追踪跑腿员实时位置和显示附近任务"
            permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE) ->
                "需要存储权限来缓存地图数据和保存应用文件"
            else -> "此功能需要相关权限才能正常使用"
        }
    }
}