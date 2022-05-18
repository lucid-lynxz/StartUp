package org.lynxz.startup.bean

import android.content.Context
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 定义任务, 通过 [execute] 执行操作
 * 假设 task1 执行完成后 task2 才能执行 则:
 * task2 的 dependencies task 是: task1, 对应于DAG的 "入度"
 * task1 的 children task 是: task2, 对应DAG的 "出度"
 *
 * <pre>
 *     val task = Task("taskName")  // 指定task名称
 *                  .addDependency(otherTask1) // 可选, 添加依赖的task, 可添加多个
 *                  .addDependency(otherTask2)
 * <pre>
 */
abstract class Task<R>(
    val name: String = "",   // task名称
    val runOnMainThread: Boolean = true // 是否运行在 "主线程"(执行TaskManager的线程)
) {
    private var dependenciesList: MutableList<Task<*>>? = null
    private val running = AtomicBoolean(false)
    private val waitCount by lazy { AtomicInteger(getDependenciesCount()) }
    private val cdl: CountDownLatch by lazy { CountDownLatch(waitCount.get()) }

    /**
     * taskManager中触发, 在实际执行前先wait dependency task执行
     */
    fun callExecute(context: Context?): R? {
        running.getAndSet(true)
        cdl.await()
        return execute(context)
    }

    /**
     * 每个 dependency task执行完成后, 触发本方法一次, 尝试唤醒本task
     */
    fun notifyCountDown() {
        if (running.get()) {
            cdl.countDown()
        } else {
            waitCount.decrementAndGet()
        }
    }

    /**
     * 当前任务实际的逻辑,子类按需进行重写
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
}