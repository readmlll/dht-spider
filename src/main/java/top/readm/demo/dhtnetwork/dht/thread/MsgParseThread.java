package top.readm.demo.dhtnetwork.dht.thread;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.util.Strings;
import top.readm.demo.dhtnetwork.dht.pojo.DhtNode;
import top.readm.demo.dhtnetwork.dht.pojo.KadMsg;
import top.readm.demo.dhtnetwork.dht.Utils.KadProtocalUtils;
import top.readm.demo.dhtnetwork.dht.pojo.StringPtr;
import top.readm.demo.dhtnetwork.dht.Utils.HashSaveUtils;
import top.readm.demo.dhtnetwork.dht.thread.share.ThreadShareUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Data
@Accessors(chain = true)
/**
 * 消息解析线程
 */
public class MsgParseThread implements Runnable {

    private DatagramSocket udpSocket=null;
    private byte[] selfId=null;

    //未探测节点队列
    private ArrayDeque<DhtNode> unSenseQueue= ThreadShareUtils.unSenseQueue;
    private ReentrantReadWriteLock unSenseQueueLock=ThreadShareUtils.unSenseQueueLock;

    //存活节点
    private HashMap<String,DhtNode> survivalMap=ThreadShareUtils.survivalMap;
    private ReentrantReadWriteLock survivalMapLock=ThreadShareUtils.survivalMapLock;

    //待我们响应的getpeer 请求的节点队列
    private Queue<DhtNode> getpeerWaitQueue=ThreadShareUtils.getpeerWaitQueue;
    private ReentrantReadWriteLock getPeerWaitQueueLock=ThreadShareUtils.getPeerWaitQueueLock;


    //待我们响应的find node 请求的节点队列
    private Queue<DhtNode> findNodeQueryWaitQueue=ThreadShareUtils.findNodeQueryWaitQueue;
    private ReentrantReadWriteLock findNodeQueryWaitQueueLock=ThreadShareUtils.findNodeQueryWaitQueueLock;


    @Override
    public void run() {
        byte [] recvData=null;
        DatagramPacket packet=null;
        byte[] msgBytes=null;
        byte[] sendData=null;
        KadMsg kadMsg=null;
        String strRes="";
        List<DhtNode> nodes=null;
        DhtNode node = null;

        while (true){
            try {
                Thread.sleep(200);
                recvData=new byte[4096];
                packet=new DatagramPacket(recvData, recvData.length);
                udpSocket.setSoTimeout(60*1000);
                udpSocket.receive(packet);

                msgBytes=new byte[packet.getLength()];
                System.arraycopy(packet.getData(),0,msgBytes,0,msgBytes.length);
                kadMsg=new KadMsg();

                kadMsg.parseMsg(msgBytes);

                StringPtr lpstr=new StringPtr();

                switch (kadMsg.type){

                    case PING_RESPONSE:
                        strRes= KadProtocalUtils.handlePingResponse(kadMsg);
                        if(Strings.isNotEmpty(strRes)){

                            //     System.out.println("解析线程 收到 PING_RESPONSE:"+strRes+"  "+"ip:"+packet.getAddress().getHostAddress()+":"+packet.getPort()+" 验证存活成功");
                            node=new DhtNode().setNodeIp(packet.getAddress().getHostAddress())
                                    .setNodePort(String.valueOf(packet.getPort())).setNodeId(strRes).nodeInfoEncode();

                            survivalMapLock.writeLock().lock();
                            try {
                                survivalMap.put(strRes, node);
                            }catch (Exception e){
                                e.printStackTrace();
                            }finally {
                                survivalMapLock.writeLock().unlock();
                            }
                        }
                        break;

                    case FIND_NODE_RESPONSE:
                        //收到findnode的回复 将收到的节点 放入未检测队列
                        nodes=KadProtocalUtils.findNodeResponse(kadMsg);
                        try {
                            unSenseQueueLock.writeLock().lock();
                            nodes.stream().forEach((tnode)->{
                                unSenseQueue.add(tnode);
                            });

                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            unSenseQueueLock.writeLock().unlock();
                        }
                        //     System.out.println("解析线程 收到 FIND_NODE_RESPONSE ");
                        break;

                    case GET_PEERS_QUERY:
                        strRes=KadProtocalUtils.handleGetPeers(kadMsg,lpstr);
                        System.out.println("收到 GET_PEERS_QUERY 得到不可靠info:"+strRes);
                        //得到不可靠info
                        HashSaveUtils.unreliableInfoSave(strRes);

                        //保存节点信息待我们回复的getpeer队列 此时节点id我们保存一下他 自身的id
                        try {
                            getPeerWaitQueueLock.writeLock().lock();
                            String nodeId=DhtNode.sha1Bytes2HexStr(DhtNode.bytesOrderReverse(lpstr.str.getBytes("iso8859-1")));
                            DhtNode newNode=new DhtNode().setNodeIp(packet.getAddress().getHostAddress())
                                    .setNodePort(String.valueOf(packet.getPort())).setNodeId(nodeId)
                                    .nodeInfoEncode();

                            //放入消息对象 方便回复peer消息的线程解析事务id 等信息
                            newNode.setExtData(kadMsg);
                            getpeerWaitQueue.add(newNode);

                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            getPeerWaitQueueLock.writeLock().unlock();
                        }

                        break;
                    case ANNOUNCE_PEER_QUERY:
                        strRes=KadProtocalUtils.handleAnnouncePeer(kadMsg,lpstr);
                        String nodeId=lpstr.str;
                        System.out.println("收到 ANNOUNCE_PEER_QUERY 得到较为可靠info:"+strRes);

                        //加入可靠的infos
                        HashSaveUtils.reliableInfoSave(strRes);

                        break;
                    case PING_QUERY:

                        strRes=KadProtocalUtils.pingResponse(kadMsg, selfId);
                        sendData=strRes.getBytes("iso8859-1");

                        DatagramPacket sendPacket=new DatagramPacket(sendData,0,sendData.length);
                        sendPacket.setAddress(packet.getAddress());
                        sendPacket.setPort(packet.getPort());
                        udpSocket.send(sendPacket);
                        System.out.println("收到 PING_QUERY 解析事务id 并且回复成功");
                        break;

                    case FIND_NODE_QUERY:
                        //由别人发过来的find node请求 我们需要回复
                        System.out.println("收到 FIND_NODE_QUERY 等待我们回复...");

                        try {

                            //放入消息对象 方便回复find node的线程解析事务id
                            DhtNode newNode=new DhtNode().setNodeIp(packet.getAddress().getHostAddress())
                                    .setNodePort(String.valueOf(packet.getPort())).setExtData(kadMsg)
                                    .nodeInfoEncode();

                            findNodeQueryWaitQueueLock.writeLock().lock();
                            //放入findnode等待回复队列
                            findNodeQueryWaitQueue.add(newNode);

                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            findNodeQueryWaitQueueLock.writeLock().unlock();
                        }

                        break;

                    case IGNORE:
                        System.out.println("注意！！！！！收到无用的消息");
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {

            }
        }
    }
}
