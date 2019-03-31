package top.readm.demo.dhtnetwork.dht.thread.share;

import lombok.Data;
import lombok.experimental.Accessors;
import top.readm.demo.dhtnetwork.dht.pojo.DhtNode;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Data
@Accessors(chain = true)
/**
 * 各个线程共享的数据 和 对应的锁
 */
public class ThreadShareUtils {
    //未探测节点
    public static final ArrayDeque<DhtNode> unSenseQueue=new ArrayDeque<>();
    public static final ReentrantReadWriteLock unSenseQueueLock=new  ReentrantReadWriteLock();

    //存活节点
    public static final HashMap<String,DhtNode> survivalMap=new HashMap<>();
    public static final ReentrantReadWriteLock survivalMapLock=new  ReentrantReadWriteLock();

    //已经联系节点 路由表
    public static final ConcurrentHashMap<String,DhtNode> synPeerMap=new ConcurrentHashMap<>();
    public static final ReentrantReadWriteLock synPeerQueueLock=new ReentrantReadWriteLock();


    //待我们响应的getpeer 请求的节点队列
    public static final Queue<DhtNode> getpeerWaitQueue=new ArrayDeque<>();
    public static final  ReentrantReadWriteLock getPeerWaitQueueLock=new  ReentrantReadWriteLock();



    //待我们响应的find node 请求的节点队列
    public static final Queue<DhtNode> findNodeQueryWaitQueue=new ArrayDeque<>();
    public static final ReentrantReadWriteLock findNodeQueryWaitQueueLock=new  ReentrantReadWriteLock();
}
