# DiskFailureLocation
diagnosis disk failure in large-scale cluster systems

本系统参考论文：《Making Problem Diagnosis Work for Large-Scale,Production Storage Systems》

一、	解决问题

  大规模分布式存储系统，性能问题都是不可避免的，难以解决。该方案目的在于诊断故障节点以及性能过低的点。

二、	数据采集

  sysstat是一个软件包，包含监测系统性能及效率的一组工具，这些工具对于我们收集系统性能数据，比如CPU使用率、硬盘和网络吞吐数据，这些数据的收集和分析，有利于我们判断系统是否正常运行，是提高系统运行效率、安全运行服务器的得力助手。使用sysstat收集如下数据：

tps：每秒I/O请求数

rd_sec：每秒读取字节数

wr_sec：每秒写字节数

avgrq-sz： Average size (in sectors) of the LUN’s I/O requests

avgqu-sz：读写请求队列平均长度

await：请求平均等待时间（包含队列等待时间和处理时间）

svctm：请求处理时间（不包含等待时间）

%util：CPU利用率


三、	主要思路
 
1.	对每一项性能指标依次进行诊断。任一项指标异常，就判定该点异常。

2.	在一个时间周期2T内，获取每个逻辑存储单元的性能指标。将每个指标收集到的数据分别构建直方图。（案例中T=15s*30=7.5m，在T内获取每个指标的30个样本每15s取一次样。先使用2T时间进行判断，当发现一个点出错时，判断间隔改为T，加快故障定位）
构建直方图是先计算样本的四分位差，然后利用Freedman-Diaconis rule计算直方图的组距。

3.	计算每块磁盘每项指标与其它磁盘对应指标之间的距离。两个节点在同一T下某个指标的距离就是对应两个直方图的距离。这种方式比取T内平均值的方式更能体现两个节点行为的不同程度。
以取CPU利用率指标的2个样本点P、Q为例，说明直方图距离计算方式，这里设样本数只有2个、分为4组，组距的宽度为25%：P点（1%，99%）、Q点（49%，51%），如果简单的使用平均值来衡量，P、Q两点距离是0，而如果使用Freedman-Diaconis rule得到的P、Q两点直方图，计算直方图的距离更更能体现两个样本的差异。大概表达如下：
d(P,Q)=|P(0%~25%) – Q(0%-25%)| + |P(25%~50%) – Q(25%~50%)| + |P(50%~75%) – Q(50%~75%)| + |P(75%~100%) – Q(75%~100%)|
=|1 – 0 | + |0 – 1 | + |0 – 1 | + |1 – 0 | = 4

4.	计算一个点与对等节点之间的某指标直方图的距离，如果与多于一半的对等节点的距离 超过一个阙值，那么就标记这个点异常。如果在2k-1个周期T内，该点超过k个周期都异常，那么就认为该点出错。（案例中k=3，即5个周期内有3个出错则判定该点出错，最少的判定时间为：15s*30*3=22.5m）

5.	确定阙值：阙值用于确定正常节点与异常节点之间的最大距离。阙值使用集群正常时的数据或经去噪的数据进行训练得到，即计算训练数据中直方图的最大距离。（案例中使用的是一天的数据）

6.	对每一个节点维护一个正计数器。如果该点在T内出错则+1，如果该点正常则且计数>0则-1。并根据计数器值排序，得到最近时间内出错最多的点。

7.	出错较多的点认定为故障节点。
