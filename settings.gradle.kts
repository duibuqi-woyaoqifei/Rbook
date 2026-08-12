pluginManagement {
    repositories {
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
        // 3. 本地 Nexus 兜底（最后尝试，避免锁定问题）
        maven {
            url = uri("http://localhost:8081/repository/maven-group/")
            isAllowInsecureProtocol = true
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 1. 阿里云 Google 镜像（androidx、hilt、material 等）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // 2. 阿里云 Public 镜像（central + jcenter 库）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 3. JitPack（pdfium-android 等 GitHub 开源库）
        maven { url = uri("https://jitpack.io") }
        // 4. 官方原厂兜底（TUN 模式代理）
        google()
        mavenCentral()
        // 5. 本地 Nexus 兜底（最后尝试，避免锁定问题）
        maven {
            url = uri("http://localhost:8081/repository/maven-group/")
            isAllowInsecureProtocol = true
        }
    }
}

rootProject.name = "RBook"
include(":app")
