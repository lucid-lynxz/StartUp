看到一些app启用优化DAG, 任务依赖管理的资料, 自己不跟着动手敲一遍始终理解不够深刻

## 问题点拆解
1. task功能定义
2. task拓扑排序
3. countdownlatch 实现task等待前置任务执行完成
4. 多线程, 线程池执行, 主线程task阻塞子线程task问题处理
5. 自动配置执行--contentProvider