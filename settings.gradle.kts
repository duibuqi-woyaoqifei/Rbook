pluginManagement {
    repositories {
        // 本地 Nexus 判定：仅“非 CI 且本机 localhost:8081 可达”时启用。
        // GitHub Actions 中 localhost 指向 Runner 自身，因此 CI 绝不能访问 localhost。
        fun localNexusAvailable(): Boolean {
            if (System.getenv("CI")?.isNotBlank() == true) return false
            if (System.getenv("GITHUB_ACTIONS")?.isNotBlank() == true) return false
            return try {
                java.net.Socket("localhost", 8081).use { true }
            } catch (e: Exception) {
                false
            }
        }

        // 增加这行：直接从官方插件门户找
        gradlePluginPortal()
        // 1. 阿里云镜像（国内高速，覆盖 Google + Central + JCenter）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        // 2. 官方源（TUN 模式直连兜底）
        google()
        mavenCentral()
        gradlePluginPortal()
        // 3. 本地 Nexus（仅本地开发且 Nexus 可达时；CI 绝不访问 localhost）
        if (localNexusAvailable()) {
            maven {
                url = uri("http://localhost:8081/repository/maven-group/")
                isAllowInsecureProtocol = true
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        fun localNexusAvailable(): Boolean {
            if (System.getenv("CI")?.isNotBlank() == true) return false
            if (System.getenv("GITHUB_ACTIONS")?.isNotBlank() == true) return false
            return try {
                java.net.Socket("localhost", 8081).use { true }
            } catch (e: Exception) {
                false
            }
        }

        // 1. 阿里云 Google 镜像（androidx、hilt、material 等）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // 2. 阿里云 Public 镜像（central + jcenter 库）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 3. JitPack（pdfium-android 等 GitHub 开源库；CI 中同样需要）
        maven { url = uri("https://jitpack.io") }
        // 4. 官方原厂兜底（TUN 模式代理）
        google()
        mavenCentral()
        // 5. 本地 Nexus（仅本地开发且 Nexus 可达时；CI 绝不访问 localhost）
        if (localNexusAvailable()) {
            maven {
                url = uri("http://localhost:8081/repository/maven-group/")
                isAllowInsecureProtocol = true
            }
        }
    }
}

rootProject.name = "RBook"
include(":app")
