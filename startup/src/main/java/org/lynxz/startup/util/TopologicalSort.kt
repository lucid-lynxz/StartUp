package org.lynxz.startup.util

import org.lynxz.startup.bean.Task
import java.util.*

/**
 * 拓扑排序
 */
object TopologicalSort {

    /**
     * 将DAG task列表进行拓扑排序
     * @return (排序后的列表, 出度map)
     */
    fun sort(list: MutableList<Task<*>>): Pair<List<Task<*>>, MutableMap<Task<*>, MutableList<Task<*>>>> {
        val result = mutableListOf<Task<*>>()
        val inDegreeMap = mutableMapOf<Task<*>, Int>()

        // 入度为0的task
        val zeroInDegreeStack = Stack<Task<*>>()

        // 得到入度表 Map
        list.forEach {
//            it.getDependencies()?.forEach { dependency -> dependency.addChild(it) }
            inDegreeMap[it] = it.getDependenciesCount()
        }


        // 得到child task map
        // parentTask -> childrenTasks
        val childrenTaskMap = mutableMapOf<Task<*>, MutableList<Task<*>>>()
        list.forEach { child ->
            child.getDependencies()?.forEach { dependency ->
                val childrenList = childrenTaskMap[dependency] ?: mutableListOf()
                if (childrenList.indexOf(child) == -1) {
                    childrenList.add(child)
                }
                childrenTaskMap[dependency] = childrenList
            }
        }


        // 遍历入度表Map
        while (inDegreeMap.isNotEmpty()) {
            val iterator = inDegreeMap.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val task = entry.key
                val inDegree = entry.value

                // 将入度为0的task并从map中剔除并加入结果集, 同时将属于其的出度task的入度值减1
                if (inDegree == 0) {
                    zeroInDegreeStack.add(task)
                    iterator.remove()
//                    task.getChildren()?.forEach { inDegreeMap[it] = inDegreeMap[it]!!.minus(1) }
                    childrenTaskMap[task]?.forEach { inDegreeMap[it] = inDegreeMap[it]!!.minus(1) }
                    result.add(task) // 将入度为0的task加入到结果集中
                }
            }
        }

        return Pair(result, childrenTaskMap)
    }
}