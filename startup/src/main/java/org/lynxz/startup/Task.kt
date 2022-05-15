package org.lynxz.startup

import android.content.Context
import androidx.interpolator.view.animation.FastOutLinearInInterpolator

/**
 * 任务定义, 通过 [execute] 执行
 * 假设 task1 执行完成后 task2 才能执行 则:
 * task2 的 dependencies task 是: task1, 对应于DAG的 "入度"
 * task1 的 children task 是: task2, 对应DAG的 "出度"
 * 创建对象时, 仅设置其前置dependency即可, 即按需触发 [addDependency], 无需添加child, 在拓扑排序时会自动添加
 */
abstract class Task<R>(val name: String = "") {
    private var dependenciesList: MutableList<Task<*>>? = null
    private var childrenList: MutableList<Task<*>>? = null
    private val taskSet = mutableSetOf<Task<*>>() // 用于去重,一个task只能作为dependency或者child其中一种

    /**
     * 执行当前任务
     */
    abstract fun execute(context: Context?): R

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
        return !taskSet.contains(task) && dependenciesList!!.add(task)
    }

    /**
     * 前置task数量
     */
    fun getDependenciesCount(): Int = dependenciesList?.size ?: 0

    /**
     * 依赖于当前task的task
     * 进行拓扑排序后自动生成
     */
    fun getChildren(): List<Task<*>>? = childrenList

    /**
     * 添加一个child task
     * @return 是否添加成功
     */
    fun addChild(task: Task<*>): Boolean {
        if (childrenList == null) {
            childrenList = mutableListOf()
        }
        return !taskSet.contains(task) && childrenList!!.add(task)
    }

    /**
     * 依赖于当前task的task数量
     */
    fun getChildrenCount(): Int = childrenList?.size ?: 0

    /**
     * 当前任务是否运行在主线程
     */
    fun runOnMainThread(): Boolean = true
}