package org.lynxz.startup

import android.content.Context
import android.os.Looper
import org.lynxz.startup.bean.Task
import org.lynxz.startup.bean.TaskResult
import org.lynxz.startup.bean.TaskRunnable
import org.lynxz.startup.util.TopologicalSort
import java.util.concurrent.ConcurrentHashMap

/**
 * <pre>
 *   val taskManager = TaskManager.Builder()
 *                       .addTask(task1)
 *                      .addTask(task2)
 *                      .build()
 *                      .start(context)
 *
 *   val taskResult = taskManager.getResultOf(task1)
 * </pre>
 */
class TaskManager private constructor() {
    // 所有task
    private var tasks: List<Task<*>>? = null

    // parentTask -> childrenTasks
    private val childrenTaskMap = mutableMapOf<Task<*>, MutableList<Task<*>>>()

    // 缓存执行结果
    private val resultCacheMap = ConcurrentHashMap<Task<*>, TaskResult<*>>()
    private fun isMainThread() = Thread.currentThread() == Looper.getMainLooper().thread

    /**
     * 对tasks进行拓扑排序后执行
     */
    fun start(context: Context?): TaskManager {
        if (!isMainThread()) {
            throw RuntimeException("please invoke in main thread")
        }

        tasks?.forEach { task ->
            val runnable = TaskRunnable(context, task, this)
            if (task.runOnMainThread()) {
                runnable.run()
            } else {
                // TODO: 在线程池中执行
            }
        }
        return this
    }

    /**
     * 获取指定task的执行的结果, 若为null,则表示task未执行完成
     */
    fun getResultOf(task: Task<*>) = resultCacheMap[task]

    /**
     * 缓存task执行结果
     */
    fun setResultOf(task: Task<*>, data: Any?) {
        resultCacheMap[task] = TaskResult(data)
    }

    /**
     * task执行完成后, 尝试触发child task执行
     */
    fun countdownChildrenOf(task: Task<*>) =
        childrenTaskMap[task]?.forEach { child -> child.notifyCountDown() }

    class Builder {
        private val bTasks = mutableListOf<Task<*>>()

        fun addTask(task: Task<*>) = this.apply {
            if (bTasks.indexOf(task) == -1) {
                bTasks.add(task)
            }
        }

        fun build() = TaskManager().apply {
            // 得到child task map
            bTasks.forEach { child ->
                child.getDependencies()?.forEach { dependency ->
                    val list = childrenTaskMap[dependency] ?: mutableListOf()
                    if (list.indexOf(child) == -1) {
                        list.add(child)
                    }
                    childrenTaskMap[dependency] = list
                }
            }

            // 对task进行拓扑排序
            tasks = TopologicalSort.sort(bTasks)
        }
    }
}