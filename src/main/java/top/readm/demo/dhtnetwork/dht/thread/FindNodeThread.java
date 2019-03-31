package top.readm.demo.dhtnetwork.dht.thread;

import lombok.Data;
import lombok.experimental.Accessors;
import top.readm.demo.dhtnetwork.dht.pojo.DhtNode;
import top.readm.demo.dhtnetwork.dht.Utils.KadProtocalUtils;
import top.readm.demo.dhtnetwork.dht.thread.share.ThreadShareUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Data
@Accessors(chain = true)
/**
 * 发送findnood 请求
 */
public class FindNodeThread implements Runnable {

    private DatagramSocket udpSocket=null;
    private byte[] selfId=null;

    //存活节点
    private HashMap<String,DhtNode> survivalMap= ThreadShareUtils.survivalMap;
    private ReentrantReadWriteLock survivalMapLock=ThreadShareUtils.survivalMapLock;

    //已经联系节点 路由表
    ConcurrentHashMap<String,DhtNode> synPeerMap=ThreadShareUtils.synPeerMap;
    ReentrantReadWriteLock synPeerQueueLock=ThreadShareUtils.synPeerQueueLock;

    @Override
    public void run() {
        byte [] sendData=null;
        DatagramPacket packet=null;
        DhtNode node=null;

        while (true){
            try {
                //从存活节点map中获取一个出列一个节点
                //     System.out.println("存活节点剩余"+survivalMap.size()+"个");
                Thread.sleep(220);
                survivalMapLock.writeLock().lock();
                if(survivalMap.size()<1){
                    continue;
                }
                Set<Map.Entry<String,DhtNode>> entrySet=  survivalMap.entrySet();

                String nodeInfo="";
                for(Map.Entry<String,DhtNode> entry:entrySet){
                    nodeInfo=entry.getKey();
                    node=entry.getValue();
                    break;
                }
                survivalMap.remove(nodeInfo);
                synPeerMap.put(node.getNodeIp(),node);

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                survivalMapLock.writeLock().unlock();
            }

            //取到了存活的节点信息 发送find node请求
            try {
                sendData= KadProtocalUtils.findNode(node,selfId, null).getBytes("iso8859-1");
                packet=new DatagramPacket(sendData, sendData.length);
                packet.setPort(Integer.parseInt(node.getNodePort()));
                packet.setAddress(InetAddress.getByName(node.getNodeIp()));
                //     System.out.println("发送find node请求: "+node.getNodeIp()+":"+node.getNodePort());
                udpSocket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
