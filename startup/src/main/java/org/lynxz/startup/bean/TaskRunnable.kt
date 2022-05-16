package org.lynxz.startup.bean

import android.content.Context
import org.lynxz.startup.TaskManager

class TaskRunnable(val context: Context?, val task: Task<*>, val taskManager: TaskManager) :
    Runnable {
    override fun run() {
        val data = task.callExecute(context)
        taskManager.setResultOf(task, data)
        with(taskManager) {
            setResultOf(task, data)
            countdownChildrenOf(task)
        }
    }
}