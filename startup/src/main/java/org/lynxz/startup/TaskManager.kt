package org.lynxz.startup

import org.lynxz.startup.util.TopologicalSort

class TaskManager private constructor() {
    // 所有task
    private var tasks: List<Task<*>>? = null

    /**
     * 对tasks进行拓扑排序后执行
     */
    fun start() {
        tasks?.forEach { it.execute(null) }
    }

    class Builder {
        private val bTasks = mutableListOf<Task<*>>()

        fun addTask(task: Task<*>) = this.apply { bTasks.add(task) }

        fun build() = TaskManager().apply { tasks = TopologicalSort.sort(bTasks) }
    }
}