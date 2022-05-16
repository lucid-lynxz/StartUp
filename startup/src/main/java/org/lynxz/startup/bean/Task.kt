package org.lynxz.startup.bean

import android.content.Context
import java.util.concurrent.CountDownLatch

/**
 * 任务定义, 通过 [execute] 执行
 * 假设 task1 执行完成后 task2 才能执行 则:
 * task2 的 dependencies task 是: task1, 对应于DAG的 "入度"
 * task1 的 children task 是: task2, 对应DAG的 "出度"
 * 创建对象时, 仅设置其前置dependency即可, 即按需触发 [addDependency], 无需添加child, 在拓扑排序时会自动添加
 */
abstract class Task<R>(val name: String = "") {
    private var dependenciesList: MutableList<Task<*>>? = null
    private var cdl: CountDownLatch? = null

    fun callExecute(context: Context?): R? {
        if (cdl == null) {
            cdl = CountDownLatch(getDependenciesCount())
        }
        cdl!!.await()
        return execute(context)
    }

    fun notifyCountDown() {
        cdl?.countDown()
    }

    /**
     * 执行当前任务
     */
    abstract fun execute(context: Context?): R?

    /**
     * 当前task依赖的前置task
     * 定义task时就要进行明确
     */
    fun getDependencies(): List<Task<*>>? = dependenciesList

    /**
     * 添加依赖的task
     * @return 是否添加成功
     */
    fun addDependency(task: Task<*>): Boolean {
        if (dependenciesList == null) {
            dependenciesList = mutableListOf()
        }
        return dependenciesList!!.indexOf(task) == -1 && dependenciesList!!.add(task)
    }

    /**
     * 前置task数量
     */
    fun getDependenciesCount(): Int = dependenciesList?.size ?: 0

    /**
     * 当前任务是否运行在主线程
     */
    fun runOnMainThread(): Boolean = true
}