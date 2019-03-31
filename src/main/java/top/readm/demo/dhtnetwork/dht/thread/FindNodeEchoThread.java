package top.readm.demo.dhtnetwork.dht.thread;

import lombok.Data;
import lombok.experimental.Accessors;
import top.readm.demo.dhtnetwork.dht.pojo.DhtNode;
import top.readm.demo.dhtnetwork.dht.pojo.KadMsg;
import top.readm.demo.dhtnetwork.dht.Utils.KadProtocalUtils;
import top.readm.demo.dhtnetwork.dht.thread.share.ThreadShareUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Data
@Accessors(chain = true)
/**
 * 回复别人的findnood请求
 */
public class FindNodeEchoThread implements Runnable {

    private DatagramSocket udpSocket=null;
    private byte[] selfId=null;

    //待我们响应的find node 请求的节点队列
    Queue<DhtNode> findNodeQueryWaitQueue= ThreadShareUtils.findNodeQueryWaitQueue;
    ReentrantReadWriteLock findNodeQueryWaitQueueLock=ThreadShareUtils.findNodeQueryWaitQueueLock;

    //已经联系节点 路由表
    ConcurrentHashMap<String,DhtNode> synPeerMap=ThreadShareUtils.synPeerMap;
    ReentrantReadWriteLock synPeerQueueLock=ThreadShareUtils.synPeerQueueLock;

    @Override
    public void run() {
        //获取 需要回复的节点
        DhtNode node=null;
        DatagramPacket packet=null;
        byte [] sendbytes=null;
        byte [] nodeidBytes=null;
        List<DhtNode> nodes=new ArrayList<>();

        while (true){
            try {
                Thread.sleep(300);
                findNodeQueryWaitQueueLock.writeLock().lock();
                //先判断已经联系的队列是否有k个节点  也就是路由表
                if(synPeerMap.size()<8 || findNodeQueryWaitQueue.size()<1){
                    continue;
                }
                node=findNodeQueryWaitQueue.remove();

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                findNodeQueryWaitQueueLock.writeLock().unlock();
            }


            //得到节点信息进行回复操作
            //从存活的节点中取出 八个节点来  因为我们并不是真正的节点 只是一个伪装节点 就随机取了
            if(nodes.size()>0){
                nodes.clear();
            }
            try {
                synPeerQueueLock.writeLock().lock();
                Enumeration<DhtNode> nodeEnumeration=synPeerMap.elements();
                for(int i=0;i<8;i++){
                    nodes.add(nodeEnumeration.nextElement());
                }

                //发送findnode请求的响应报文
                KadMsg msg = (KadMsg) node.getExtData();
                sendbytes= KadProtocalUtils.handleFindNodeQuery(msg, selfId, nodes).getBytes("iso8859-1");
                packet=new DatagramPacket(sendbytes,sendbytes.length);
                packet.setAddress(InetAddress.getByName(node.getNodeIp()));
                packet.setPort(Integer.parseInt(node.getNodePort()));
                udpSocket.send(packet);
                System.out.println("主动发送findnode的响应成功");

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                synPeerQueueLock.writeLock().unlock();
            }

        }
    }
}
