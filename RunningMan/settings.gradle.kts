pluginManagement {
    repositories {
        google()                  // Android 插件在 Google 仓库中
        mavenCentral()            // 其他依赖
        gradlePluginPortal()      // Kotlin 插件
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RunningMan"
include(":app")
