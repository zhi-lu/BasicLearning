package com.luzhi.lock;

/**
 * @author zhilu
 *
 * AQS一个用来构建锁和同步器的基础框架{
 *   one   -> 主要思想是FIFO(先来先服务).
 *   two   -> 实现算法是CLH队列算法.
 *   three -> 底层数据结构是双端队列.
 * }
 * CLH基于SMP对称多处理器架构.是指服务器中多个CPU对称工作,无主次或从属关系.
 * 等待队列是“CLH”（Craig、Landin 和 Hagersten）锁定队列的变体. CLH锁通常用于自旋锁.
 * 相反,我们将它们用于阻塞同步器,但使用相同的基本策略,即在其节点的前身中保存有关线程的一些控制信息.
 */
public abstract class AbstractQueueSynchronizerKnowledge {
    public static void main(String[] args) {

    }
}
