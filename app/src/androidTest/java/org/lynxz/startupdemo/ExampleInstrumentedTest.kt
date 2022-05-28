package org.lynxz.startupdemo

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.lynxz.startup.TaskManager
import org.lynxz.startup.bean.Task

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.lynxz.startupdemo", appContext.packageName)
    }


    @Test
    fun sortFromApp() {
        val task1 = object : Task<Unit>("task1") {
            override fun execute(context: Context?) {
                println("execute task1 ${Thread.currentThread().name}")
            }
        }

        val task2 = object : Task<Unit>(name = "task2", runOnMainThread = false) {
            override fun execute(context: Context?) {
                println("execute task2 ${Thread.currentThread().name}")
            }
        }

        val task3 = object : Task<Unit>("task3") {
            override fun execute(context: Context?) {
                println("execute task3 ${Thread.currentThread().name}")
            }
        }


        val task4 = object : Task<Unit>("task4") {
            override fun execute(context: Context?) {
                println("execute task4 ${Thread.currentThread().name}")
            }
        }

        val task5 = object : Task<Unit>(name = "task5", runOnMainThread = false) {
            override fun execute(context: Context?) {
                println("execute task5 ${Thread.currentThread().name}")
            }
        }

//        // task1 -> task3 -> task4 -> task5 -> task2
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