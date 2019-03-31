package top.readm.demo.dhtnetwork.dht.thread;

import lombok.Data;
import lombok.experimental.Accessors;
import top.readm.demo.dhtnetwork.dht.pojo.DhtNode;
import top.readm.demo.dhtnetwork.dht.Utils.KadProtocalUtils;
import top.readm.demo.dhtnetwork.dht.thread.share.ThreadShareUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Data
@Accessors(chain = true)
/**
 * 发送ping请求线程
 */
public class PingThread implements Runnable{

    private DatagramSocket udpSocket=null;
    private byte[] selfId=null;

    //未探测节点队列
    private ArrayDeque<DhtNode> unSenseQueue= ThreadShareUtils.unSenseQueue;
    private ReentrantReadWriteLock unSenseQueueLock=ThreadShareUtils.unSenseQueueLock;

    //已经联系节点 路由表
    private ConcurrentHashMap<String,DhtNode> synPeerMap=ThreadShareUtils.synPeerMap;
    ReentrantReadWriteLock synPeerQueueLock=ThreadShareUtils.synPeerQueueLock;

    @Override
    public void run() {
        DatagramPacket packet=null;
        byte[] sendData=null;
        DhtNode node=null;

        while (true){

            try {
                Thread.sleep(300);
                unSenseQueueLock.writeLock().lock();
                //System.out.println("未ping节点剩余:"+synUnSenseQueue.size());

                if(unSenseQueue.size()>0) {
                    node = unSenseQueue.remove();
                }else {
                    synPeerMap.forEach((nodeId,routeNode)->{
                        unSenseQueue.add(routeNode);
                    });
                    //     System.out.println("路由表ping");
                    continue;
                }

            }catch (Exception e){

            }finally {
                unSenseQueueLock.writeLock().unlock();
            }

            try {
                //node 已经获取到了 那么进行 ping 请求 进行探测节点是否存活
                sendData= KadProtocalUtils.pingQuery(selfId).getBytes("iso8859-1");
                packet=new DatagramPacket(sendData,0,sendData.length);
                packet.setAddress(InetAddress.getByName(node.getNodeIp()));
                packet.setPort(Integer.parseInt(node.getNodePort()));
                udpSocket.send(packet);

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
            }
        }
    }
}
