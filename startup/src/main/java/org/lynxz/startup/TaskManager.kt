package org.lynxz.startup

import android.content.Context
import android.os.Looper
import org.lynxz.startup.bean.Task
import org.lynxz.startup.bean.TaskResult
import org.lynxz.startup.bean.TaskRunnable
import org.lynxz.startup.util.TopologicalSort
import java.util.concurrent.*

/**
 * <pre>
 *   // 使用方法
 *   val taskManager = TaskManager.Builder()
 *                      .addTask(task1)  // 添加task, 可添加多个
 *                      .addTask(task2)
 *                      .build()  // 进行拓扑排序
 *                      .setExecutor(executorService) // 可选, 设置自定义线程池
 *                      .start(context)
 *
 *   // 获取task执行结果, 若为null,表示task尚未执行结束
 *   val taskResult = taskManager.getResultOf(task1)
 * </pre>
 */
class TaskManager private constructor() {
    // 已完成拓扑排序的待执行的task列表
    private var tasks: MutableList<Task<*>> = mutableListOf()

    // 出度表, key-value: parentTask -> childrenTasks
    private lateinit var childrenTaskMap: Map<Task<*>, List<Task<*>>>

    // 缓存执行结果
    private val resultCacheMap = ConcurrentHashMap<Task<*>, TaskResult<*>>()

    // 线程池,默认cpu密集型
    private var executorService: ExecutorService

    init {
        // 默认的线程池:cpu密集型, 最大线程数为: cpu核心数+1
        val processors = Runtime.getRuntime().availableProcessors()
        executorService = ThreadPoolExecutor(
            processors,
            processors + 1,
            60,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            Executors.defaultThreadFactory(),
            ThreadPoolExecutor.AbortPolicy()
        )
    }

    private fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()


    /**
     * 自定义线程池
     */
    fun setExecutor(executorService: ExecutorService): TaskManager {
        this.executorService = executorService
        return this
    }

    /**
     * 对tasks进行拓扑排序后执行
     */
    fun start(context: Context?): TaskManager {
//        if (!isMainThread()) {
//            throw RuntimeException("please invoke in main thread")
//        }

        // 为避免主线程task先执行阻塞子线程task的运行, 先分类, 并优先提交子线程task
        val mainThreadTasks = mutableListOf<TaskRunnable>()
        val subThreadTasks = mutableListOf<TaskRunnable>()

        tasks.forEach { task ->
            val runnable = TaskRunnable(context, task, this)
            if (task.runOnMainThread) {
                mainThreadTasks.add(runnable)
            } else {
                subThreadTasks.add(runnable)
            }
        }

        subThreadTasks.forEach { executorService.execute(it) }
        mainThreadTasks.forEach { it.run() }
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
            val (topologicalList, childrenMap) = TopologicalSort.sort(bTasks)
            childrenTaskMap = childrenMap
            tasks.clear()
            tasks.addAll(topologicalList)
            tasks.forEach { println("${it.name} main=${it.runOnMainThread}") }
        }
    }
}