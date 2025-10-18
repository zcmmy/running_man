package com.example.campusrunner

import android.app.Application
import com.example.campusrunner.map.AMapServiceImpl
import com.example.campusrunner.map.MapService

class CampusRunnerApplication : Application() {

    // 全局地图服务实例
    var mapService: MapService? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 在这里可以初始化一些全局组件
        // 但权限相关的初始化应该在MainActivity中进行
    }

    companion object {
        lateinit var instance: CampusRunnerApplication
            private set
    }
}