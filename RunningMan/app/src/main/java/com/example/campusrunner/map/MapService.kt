package com.example.campusrunner.map

import com.example.campusrunner.model.Location

/**
 * 地图服务接口
 * 定义所有地图相关操作，便于后续切换地图服务提供商
 */
interface MapService {

    /**
     * 初始化地图服务
     * TODO: 需要在Activity或Fragment中调用
     */
    fun initialize()

    /**
     * 显示实时位置追踪
     */
    fun showOrderTracking(
        orderId: String,
        runnerLocation: Location,
        pickupLocation: Location,
        deliveryLocation: Location
    )

    /**
     * 更新跑腿员位置
     */
    fun updateRunnerLocation(orderId: String, newLocation: Location)

    /**
     * 绘制配送路线
     */
    fun drawRoute(points: List<Location>)

    /**
     * 清除地图标记和路线
     */
    fun clearMap()

    /**
     * 获取当前位置
     * TODO: 需要处理权限请求
     */
    fun getCurrentLocation(callback: (Location) -> Unit)

    /**
     * 地址转坐标
     * TODO: 需要高德地图地理编码服务
     */
    fun geocodeAddress(address: String, callback: (Location?) -> Unit)

    /**
     * 坐标转地址
     * TODO: 需要高德地图逆地理编码服务
     */
    fun reverseGeocode(location: Location, callback: (String?) -> Unit)

    /**
     * 清理资源
     */
    fun cleanup()
}