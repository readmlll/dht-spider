package top.readm.demo.dhtnetwork.dht.thread;

import lombok.Data;
import lombok.experimental.Accessors;
import top.readm.demo.dhtnetwork.dht.pojo.DhtNode;
import top.readm.demo.dhtnetwork.dht.pojo.IntegerPtr;
import top.readm.demo.dhtnetwork.dht.thread.share.ThreadShareUtils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Data
@Accessors(chain = true)
/**
 * 路由表维护线程  由于我们只是伪装节点 简单的实现一个路由表即可 无需全部按照协议
 */
public class RouteTableThread implements Runnable {

    //已经联系节点 路由表
    ConcurrentHashMap<String,DhtNode> synPeerMap= ThreadShareUtils.synPeerMap;
    ReentrantReadWriteLock synPeerQueueLock=ThreadShareUtils.synPeerQueueLock;

    @Override
    public void run() {

        while (true){

            //     System.out.println("当前路由表个数 "+synPeerMap.size());
            try {
                Thread.sleep(800);
                synPeerQueueLock.writeLock().lock();
                if(synPeerMap.size()>160){
                    //基本上维持最新的160个节点  超出就过滤到大约一半
                    final IntegerPtr integerPtr=new IntegerPtr().setInteger(0);
                    HashMap<String, DhtNode> map = new HashMap<>();
                    int count=0;
                    synPeerMap.forEach( (nodeId,node) ->{
                        if(integerPtr.integer++<80)
                            map.put(nodeId, node);
                    });
                    synPeerMap.clear();
                    synPeerMap.putAll(map);
                }
            }catch (Exception e){

            }finally {
                synPeerQueueLock.writeLock().unlock();
            }
        }

    }

}
