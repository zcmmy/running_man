package com.example.campusrunner.navigation

object Destinations {
    const val Home = "home"
    const val Post = "post"
    const val Messages = "messages"
    const val Profile = "profile"
    const val Detail = "detail"
    const val Chat = "chat"
    const val OrderTracking = "orderTracking"
    const val Search = "search"
    const val Login = "login"
    const val OrderHistory = "orderHistory" // 添加订单历史页面路由

    // 带参数的路径
    object DetailWithArgs {
        const val Route = "detail"
        const val TaskId = "taskId"
        const val RouteWithArgs = "$Route/{$TaskId}"
    }

    object ChatWithArgs {
        const val Route = "chat"
        const val OrderId = "orderId"
        const val RouteWithArgs = "$Route/{$OrderId}"
    }

    object OrderTrackingWithArgs {
        const val Route = "orderTracking"
        const val OrderId = "orderId"
        const val RouteWithArgs = "$Route/{$OrderId}"
    }
}