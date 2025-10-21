package com.example.campusrunner.map

import com.example.campusrunner.model.Location

/**
 * 地图服务接口
 * 定义所有地图相关操作，便于后续切换地图服务提供商
 *
 * 后端接入说明：
 * 地图服务主要依赖第三方SDK（高德地图），不直接调用后端API
 * 但地图数据需要与后端服务配合使用：
 * 1. 位置数据需要同步到后端数据库
 * 2. 路线规划可能需要后端算法支持
 * 3. 实时位置需要WebSocket推送
 */
interface MapService {

    /**
     * 初始化地图服务
     * 后端接入步骤：
     * 1. 申请高德地图API Key
     * 2. 在AndroidManifest.xml中配置API Key
     * 3. 添加地图SDK依赖
     * 4. 初始化定位和地图服务
     *
     * 注意事项：
     * - 需要在Application或MainActivity中调用
     * - 需要处理权限申请
     * - 需要配置混淆规则
     */
    fun initialize()

    /**
     * 显示实时位置追踪
     * 后端接入说明：
     * 需要与后端实时位置API配合：
     * 1. 调用 GET /api/orders/{orderId}/tracking 获取初始位置
     * 2. 使用WebSocket连接 /ws/orders/{orderId}/tracking 获取实时更新
     * 3. 在地图上显示跑腿员位置、取货点和送货点
     *
     * 实现步骤：
     * 1. 添加地图标记（Marker）
     * 2. 绘制路线（Polyline）
     * 3. 设置地图显示范围
     */
    fun showOrderTracking(
        orderId: String,
        runnerLocation: Location,
        pickupLocation: Location,
        deliveryLocation: Location
    )

    /**
     * 更新跑腿员位置
     * 后端接入说明：
     * 位置更新流程：
     * 1. 跑腿员App定期上传位置到 PUT /api/orders/{orderId}/location
     * 2. 后端通过WebSocket推送给用户
     * 3. 用户App接收更新并调用此函数
     *
     * 请求体示例：
     * {
     *   "latitude": 30.5163,
     *   "longitude": 114.4204,
     *   "timestamp": "2024-01-20T10:45:00Z"
     * }
     */
    fun updateRunnerLocation(orderId: String, newLocation: Location)

    /**
     * 绘制配送路线
     * 后端接入说明：
     * 路线规划可以：
     * 1. 使用高德地图SDK的路线规划功能（前端实现）
     * 2. 或调用后端路线规划API GET /api/routes/plan
     *
     * 后端路线规划API示例：
     * GET /api/routes/plan?origin=30.5150,114.4180&destination=30.5175,114.4220
     * 响应示例：
     * {
     *   "distance": 2000,
     *   "duration": 900,
     *   "points": [
     *     {"latitude": 30.5150, "longitude": 114.4180},
     *     {"latitude": 30.5155, "longitude": 114.4190},
     *     ...
     *   ]
     * }
     */
    fun drawRoute(points: List<Location>)

    /**
     * 清除地图标记和路线
     * 调用时机：
     * - 订单完成时
     * - 页面销毁时
     * - 切换订单时
     */
    fun clearMap()

    /**
     * 获取当前位置
     * 后端接入说明：
     * 获取的位置可用于：
     * 1. 发布任务时自动填充位置
     * 2. 搜索附近任务
     * 3. 路线规划起点
     *
     * 权限要求：
     * - android.permission.ACCESS_FINE_LOCATION
     * - android.permission.ACCESS_COARSE_LOCATION
     *
     * 注意事项：
     * - 需要动态申请位置权限
     * - 处理定位失败情况
     * - 考虑定位精度和电量消耗
     */
    fun getCurrentLocation(callback: (Location) -> Unit)

    /**
     * 地址转坐标（地理编码）
     * 后端接入说明：
     * 两种实现方式：
     * 1. 使用高德地图SDK的地理编码服务（推荐，响应快）
     * 2. 调用后端地理编码API POST /api/geocode/forward
     *
     * 后端API示例：
     * POST /api/geocode/forward
     * 请求体：{"address": "华中科技大学南大门"}
     * 响应：{"latitude": 30.5163, "longitude": 114.4204, "address": "详细地址"}
     */
    fun geocodeAddress(address: String, callback: (Location?) -> Unit)

    /**
     * 坐标转地址（逆地理编码）
     * 后端接入说明：
     * 两种实现方式：
     * 1. 使用高德地图SDK的逆地理编码服务（推荐）
     * 2. 调用后端逆地理编码API POST /api/geocode/reverse
     *
     * 后端API示例：
     * POST /api/geocode/reverse
     * 请求体：{"latitude": 30.5163, "longitude": 114.4204}
     * 响应：{"address": "湖北省武汉市洪山区珞喻路1037号"}
     */
    fun reverseGeocode(location: Location, callback: (String?) -> Unit)

    /**
     * 清理资源
     * 重要：避免内存泄漏
     * 清理内容：
     * - 停止定位服务
     * - 清除地图监听器
     * - 释放地图资源
     */
    fun cleanup()
}