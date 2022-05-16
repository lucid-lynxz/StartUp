package org.lynxz.startup

import android.content.Context
import org.junit.Test
import org.lynxz.startup.bean.Task

class TopologicalSortTest {

    @Test
    fun sort() {
        val task1 = object : Task<Unit>("task1") {
            override fun execute(context: Context?) {
                println("execute task1")
            }
        }

        val task2 = object : Task<Unit>("task2") {
            override fun execute(context: Context?) {
                println("execute task2")
            }
        }

        val task3 = object : Task<Unit>("task3") {
            override fun execute(context: Context?) {
                println("execute task3")
            }
        }


        val task4 = object : Task<Unit>("task4") {
            override fun execute(context: Context?) {
                println("execute task4")
            }
        }

        val task5 = object : Task<Unit>("task5") {
            override fun execute(context: Context?) {
                println("execute task5")
            }
        }

        // task1 -> task3 -> task4 -> task5 -> task2
//        task2.addDependency(task5)
//        task5.addDependency(task4)
//        task4.addDependency(task3)
//        task3.addDependency(task1)

        // task2/task3 -> task1 -> task5 -> task4
        task1.addDependency(task2)
        task1.addDependency(task3)
        task5.addDependency(task1)
        task4.addDependency(task5)

        TaskManager.Builder().addTask(task5)
            .addTask(task4)
            .addTask(task3)
            .addTask(task2)
            .addTask(task1)
            .build()
            .start(null)
    }
}