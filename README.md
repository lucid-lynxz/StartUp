[![](https://jitpack.io/v/lucid-lynxz/startup.svg)](https://jitpack.io/#lucid-lynxz/startup)

看到一些app启用优化DAG, 任务依赖管理的资料, 自己不跟着动手敲一遍始终理解不够深刻

## 问题点拆解

1. [x] task功能定义
2. [x] task拓扑排序
3. [x] countdownlatch 实现task等待前置任务执行完成
4. [x] 多线程, 线程池执行, 主线程task阻塞子线程task问题处理
5. [ ] 自动配置执行--contentProvider

## 使用说明

1. 添加依赖

```groovy
// 1.1 添加仓库
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
// 或者 settings.gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

// 1.2 在使用的module中添加依赖
dependencies {
    implementation 'com.github.lucid-lynxz:startup:V1.0'
}
```

2. 定义task

```kotlin
// runOnMainThread 表示是否在taskManager的当前线程中执行, false-会在线程池中运行本task
val task1 = object : Task<Unit>(name = "taskName1", runOnMainThread = false) {
    override fun execute(context: Context?) {
        println("execute task1 ${Thread.currentThread().name}")
    }
}

val task2 = object : Task<Unit>(name = "taskName2", runOnMainThread = false) {
    override fun execute(context: Context?) {
        println("execute task2 ${Thread.currentThread().name}")
    }
}

// 设置依赖, 如下, 则 task2 执行完成后才会执行task1
task1.addDependency(task2)
```

3. 发起执行

```kotlin
TaskManager.Builder()
            .addTask(task1)
            .addTask(task2)
            .build()
            .start(context)
```

