package top.readm.demo.dhtnetwork.dht.Utils;

import org.apache.commons.codec.digest.DigestUtils;
import top.readm.demo.dhtnetwork.dht.enumeration.KadType;
import top.readm.demo.dhtnetwork.dht.pojo.StringPtr;
import top.readm.demo.dhtnetwork.dht.pojo.DhtNode;
import top.readm.demo.dhtnetwork.dht.pojo.KadMsg;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * kad协议报文工具
 */
public class KadProtocalUtils{

    //ping，find_node，get_peer，announce_peer

    /**
     * 得到发送ping请求的bencode编码
     * @param selfId
     * @return
     */
    public static String pingQuery(byte[] selfId){

        Map map=null;
        String query="";
        try {

            if(selfId==null){
                selfId= DigestUtils.sha1("test".getBytes("iso8859-1"));
            }
            /**
             * 报文包例子 Example Packets
             * ping Query = {"t":"aa", "y":"q", "q":"ping", "a":{"id":"abcdefghij0123456789"}}
             * bencoded = d1:ad2:id20:abcdefghij0123456789e1:q4:ping1:t2:aa1:y1:qe
             *            d1:ad2:id20:�J������s�����/��e1:q4:ping1:t2:aa1:y1:qe
             */
            map= BencodeUtils.strDictSortMap();
            map.put("t",new String(DigestUtils.sha1(UUID.randomUUID().toString().getBytes()),"iso8859-1"));
            map.put("y","q");
            map.put("q","ping");
            Map param=BencodeUtils.strDictSortMap();
            param.put("id",new String(selfId,"iso8859-1"));
            map.put("a",param);
            query=BencodeUtils.encode(map);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return query;
    }


    /**
     * 解析 ping消息的响应 得到对方节点id hexstr形式40字符
     * @param kadMsg
     * @return
     */
    public static String handlePingResponse(KadMsg kadMsg){

        /**
         *          * Response = {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
         *          * bencoded = d1:rd2:id20:mnopqrstuvwxyz123456e1:t2:aa1:y1:re
         */
        //KadMsg kadMsg=new KadMsg();
        //kadMsg.parseMsg(data);
        if(!kadMsg.type.equals(KadType.PING_RESPONSE))
            return "";

        Map<String,Object> map=null;
        Object obj= null;

        map=kadMsg.getMsgMap();
        obj=map.get("r");
        if(obj==null && ! (obj instanceof Map)){
            return "";
        }
        map=(Map<String,Object> )obj;
        String id=(String) map.get("id");
        //接受到的是网络字节序  需要转换成主机字节序 然后 转成hex字符串
        byte[] nodeIdBytes= new byte[0];
        try {
            nodeIdBytes = DhtNode.bytesOrderReverse(id.getBytes("iso8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String nodeId=DhtNode.sha1Bytes2HexStr(nodeIdBytes);

        return nodeId;
    }

    public static String pingResponse(KadMsg kadMsg,byte[] selfId){

        try {
            if(selfId==null){
                selfId=DigestUtils.sha1("test".getBytes("iso8859-1"));
            }

            String res="";
            if(!kadMsg.type.equals(KadType.PING_QUERY)){
                return res;
            }
            //得到事务id  t字段
            String t =(String) kadMsg.getMsgMap().get("t");
            //Response = {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
            //开始构造响应
            Map<String,Object> map = BencodeUtils.strDictSortMap();
            Map<String,Object> param = BencodeUtils.strDictSortMap();
            map.put("t",t);
            map.put("y","r");
            param.put("id", new String(selfId,"iso8859-1"));
            map.put("r", param);
            res=BencodeUtils.encode(map);

            return  res;

        }catch (Exception e){
            e.printStackTrace();
            return "";
        }

    }

    /**
     find_node
     被用来查找给定 ID 的节点的联系信息。这时 KPRC 协议中的
     "q" == "find_node" 。 find_node 请求包含 2 个参数，第一个参数是 id ，包含了请求节点的ID。
     第 二个参数是 target ，包含了请求者正在查找的节点的 ID。当一个节点接收到了 find_node 的请求，他应该给出对应的回复，
     回复中包含 2 个关键字 id 和 nodes ， nodes 是 字符串类型，
     包含了被请求节点的路由表中接近目标节点的 K(8) 个接近的节点的联系信息。
     参数: {"id" : "<querying nodes id>", "target" : "<id of target node>"}
     回复: {"id" : "<queried nodes id>", "nodes" : "<compact node info>"}
     报文包例子 Example Packets
     find_node Query = {"t":"aa", "y":"q", "q":"find_node", "a": {"id":"abcdefghij0123456789", "target":"mnopqrstuvwxyz123456"}}
     bencoded = d1:ad2:id20:abcdefghij01234567896:target20:mnopqrstuvwxyz123456e1:q9:find_node1:t2:aa1:y1:qe
     //         d1:ad2:id20:�J������s�����/��6:target20:�K<P���r�)������f�e1:q9:find_node1:t2:aa1:y1:qe

     */

    /**
     * 获得find node 请求的bencode编码
     * @param node  //发送的节点信息
     * @param selfId
     * @param targetId
     * @return
     */
    public static String findNode(DhtNode node,byte[] selfId,byte[] targetId){

        Map map=null;
        String query="";

        try {

            if(selfId==null){
                selfId=DigestUtils.sha1("test".getBytes("iso8859-1"));
                selfId=DhtNode.bytesOrderReverse(selfId);
            }
            if(targetId==null){
                targetId=DigestUtils.sha1(UUID.randomUUID().toString().getBytes("iso8859-1"));
            }
            map=BencodeUtils.strDictSortMap();
            map.put("t",new String(DigestUtils.sha1(UUID.randomUUID().toString().getBytes()),"iso8859-1"));
            map.put("y","q");
            map.put("q","find_node");
            Map param=BencodeUtils.strDictSortMap();

            node.nodeInfoEncode();
            String id=neerIdGenerate(new String(node.getNodeInfo(),0,20,"iso8859-1")
                    , new String(selfId,"iso8859-1"));
           // System.out.println(id.length());
            //System.out.println(Arrays.toString(id.getBytes("iso8859-1")));
            param.put("id",id);


            param.put("target",new String(targetId,"iso8859-1"));
            map.put("a", param);
            query=BencodeUtils.encode(map);
        }catch (Exception e){
            e.printStackTrace();
        }

        return query;
    }

    /**
     * 解析find node消息的响应  得到对方返回的k个节点信息
     * @param kadMsg
     * @return  返回节点列表 其中只有ip 和 port的信息
     */
    public static List<DhtNode> findNodeResponse(KadMsg kadMsg){
        //Response = {"t":"aa", "y":"r", "r": {"id":"0123456789abcdefghij", "nodes": "def456..."}}
        //bencoded = d1:rd2:id20:0123456789abcdefghij5:nodes9:def456...e1:t2:aa1:y1:re
        //回复中包含 2 个关键字 id 和 nodes ，
        // nodes 是 字符串类型，包含了被请求节点的路由表中接近目标节点的 K(8) 个接近的节点的联系信息。

        Object obj= null;
        Map<String,Object> map=null;
        List<DhtNode> nodes=new ArrayList<>();


        //KadMsg kadMsg=new KadMsg();
        //kadMsg.parseMsg(data);
        if(!kadMsg.type.equals(KadType.FIND_NODE_RESPONSE))
            return nodes;

        map=kadMsg.getMsgMap();

        String nodesStr="";
        byte[] ipBytes=null;
        byte[] portBytes=null;
        String ipStr="";
        String portStr="";
        try {
            obj=map.get("r");
            if(obj==null && ! (obj instanceof Map)){
                return nodes;
            }
            map=(Map<String,Object> )obj;
            obj=map.get("nodes");
            //nodes 是 字符串类型，包含了被请求节点的路由表中接近目标节点的 K(8) 个接近的节点的  节点信息。
            //       20字节节点id     //联系信息6字节  前4为网络字节序ip  后2为网络字节序端口
            if(obj==null && ! (obj instanceof String)){
                return nodes;
            }
            nodesStr=(String) obj;
            byte[] nodesBytes=nodesStr.getBytes("iso8859-1");
            if(nodesBytes.length%26!=0){
                return nodes;
            }

            for(int i=0;i<nodesBytes.length;i+=26){
                byte[] bys=new byte[26];
                System.arraycopy(nodesBytes, i, bys, 0, 26);
                DhtNode dhtNode=new DhtNode().setNodeInfo(bys).nodeInfoDecode();
                nodes.add(dhtNode);
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return nodes;
    }

    /**
     *get_peers Query = {"t":"aa", "y":"q", "q":"get_peers", "a": {"id":"abcdefghij0123456789", "info_hash":"mnopqrstuvwxyz123456"}}
     * Response with closest nodes = {"t":"aa", "y":"r", "r": {"id":"abcdefghij0123456789", "token":"aoeusnth", "nodes": "def456..."}}
     */

    /**
     * 解析GetPeers消息 得到infohash信息
     * @param kadMsg
     * @param nodeid
     * @return
     */
    public static String handleGetPeers(KadMsg kadMsg, StringPtr nodeid){

        kadMsg.setType(KadType.ANNOUNCE_PEER_QUERY);
        String infoHash=handleAnnouncePeer(kadMsg,nodeid);
        return infoHash;
    }

    /**
     * 获得GetPeers消息的响应的bencode编码
     * @param kadMsg
     * @param selfId 自己的id
     * @param nodes  响应给对方的最近的k个节点
     * @return
     */
    public static String responseGetPeers(KadMsg kadMsg,byte[] selfId,List<DhtNode> nodes){

        String response="";
        Map map=BencodeUtils.strDictSortMap();
        Map param=BencodeUtils.strDictSortMap();

        //获取到事务id
        map.put("t",kadMsg.getMsgMap().get("t"));
        map.put("y","r");
        map.put("r",param);

        try {
            //获取id 然后生成回复所用的近似id
            String id = neerIdGenerate((String) ((Map<String,Object>)kadMsg.getMsgMap().get("a")).get("id")
                    ,new String(selfId,"iso8859-1"));

            param.put("id", id);
            int len=nodes.size();
            byte[] nodesByte=new byte[len*26];
            for(int i=0;i<len;i++){
                byte[] nodeBytes=nodes.get(i).nodeInfoEncode().getNodeInfo();
                System.arraycopy(nodeBytes, 0, nodesByte, i*26, 26);
            }
            param.put("nodes",new String(nodesByte,"iso8859-1"));
            param.put("token", UUID.randomUUID().toString().substring(0,2));
            response=BencodeUtils.encode(map);

        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }


    /**
     * 处理AnnouncePeer 得到较为可信的infohash
     * @param kadMsg
     * @param nodeid  nodeid的指针 获取nodeid
     * @return
     */
    public static String handleAnnouncePeer(KadMsg kadMsg,StringPtr nodeid){
        /**
         * announce_peers Query = {"t":"aa", "y":"q", "q":"announce_peer", "a": {"id":"abcdefghij0123456789", "implied_port": 1,
         * "info_hash":"mnopqrstuvwxyz123456", "port": 6881, "token": "aoeusnth"}}
         * bencoded = d1:ad2:id20:abcdefghij01234567899:info_hash20:mnopqrstuvwxyz1234564:porti6881e
         * 5:token8:aoeusnthe1:q13:announce_peer1:t2:aa1:y1:qe
         * Response = {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
         * bencoded = d1:rd2:id20:mnopqrstuvwxyz123456e1:t2:aa1:y1:re
         */
        Object obj= null;
        Map<String,Object> map=null;
        String nodesStr="";
        String infoHash="";

        //KadMsg kadMsg=new KadMsg();
        //kadMsg.parseMsg(data);
        if(!kadMsg.type.equals(KadType.ANNOUNCE_PEER_QUERY))
            return "";
        map=kadMsg.getMsgMap();

        try {
            obj=map.get("a");
            if(obj==null && ! (obj instanceof Map)){
                return infoHash;
            }
            map=(Map<String,Object> )obj;
            obj=map.get("info_hash");
            //infohash 是20字节的  需要转换成 hexstr
            if(obj==null && ! (obj instanceof String)){
                return infoHash;
            }
            nodesStr=(String) obj;
            byte[] nodesBytes=nodesStr.getBytes("iso8859-1");
            infoHash=DhtNode.sha1Bytes2HexStr(nodesBytes);

            obj=map.get("id");
            //id 是20字节的 就直接获取不转什么 hexstr了反正 除了回复之外没什么意义
            if((obj!=null && obj instanceof String)){
                nodeid.setStr((String) obj);
            }else{
                nodeid.setStr("");
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return infoHash;
    }

    /**
     * 拼接一个临近的节点  根据对方的发来的id
     * @param peerId  这个参数不是hexstr  而是直接是二十字节编码的20个字符
     * @param selfId  这个参数不是hexstr  而是直接是二十字节编码的20个字符
     * @return
     */
    public static String neerIdGenerate(String peerId,String selfId ){
        String id=peerId.substring(0,6);
        id+=selfId.substring(6);
        return id;
    }

    /**
     * 回复别人发来的find node
     * @param kadMsg
     * @param selfId  自己的网络字节id
     * @param nodes   选取的回复他的八个节点
     * @return  返回 回复的字符串
     */
    public static String handleFindNodeQuery(KadMsg kadMsg,byte[] selfId,List<DhtNode> nodes){

        String res="";
        if(kadMsg.type!=KadType.FIND_NODE_QUERY){
            return res;
        }
        // find_node 请求包含 2 个参数，第一个参数是 id ，包含了请求节点的ID。第 二个参数是 target ，
        // 包含了请求者正在查找的节点的 ID。当一个节点接收到了 find_node 的请求，
        // 他应该给出对应的回复，回复中包含 2 个关键字 id 和 nodes ， nodes 是 字符串类型，
        // 包含了被请求节点的路由表中接近目标节点的 K(8) 个接近的节点的联系信息。
        //find_node Query = {"t":"aa", "y":"q", "q":"find_node", "a": {"id":"abcdefghij0123456789", "target":"mnopqrstuvwxyz123456"}}
        //bencoded = d1:ad2:id20:abcdefghij01234567896:target20:mnopqrstuvwxyz123456e1:q9:find_node1:t2:aa1:y1:qe
        //Response = {"t":"aa", "y":"r", "r": {"id":"0123456789abcdefghij", "nodes": "def456..."}}
        //bencoded = d1:rd2:id20:0123456789abcdefghij5:nodes9:def456...e1:t2:aa1:y1:re

        Map<String,Object> map=null;
        String str="";
        String resId=null;
        Map<String,Object> resMap=BencodeUtils.strDictSortMap();
        Map<String,Object> param=BencodeUtils.strDictSortMap();
        StringBuilder nodeStr=new StringBuilder();

        try {
            map=kadMsg.getMsgMap();

            //获取事务 id
            resMap.put("t",map.get("t"));
            resMap.put("y","r");
            resMap.put("r",param);

            map=(Map<String,Object>)map.get("a");
            str=(String) map.get("id");
            resId= neerIdGenerate(str,new String(selfId,"iso8859-1"));

            param.put("id", resId);

            //生成nodes字段数据 k个节点的信息
            nodes.stream().forEach(node -> {
                try {
                    nodeStr.append(new String(node.getNodeInfo(),"iso8859-1"));
                } catch (UnsupportedEncodingException e) {
                }
            });

            param.put("nodes",nodeStr.toString());

            res=BencodeUtils.encode(resMap);


        }catch (Exception e){
            e.printStackTrace();
        }

        return res;
    }


}
