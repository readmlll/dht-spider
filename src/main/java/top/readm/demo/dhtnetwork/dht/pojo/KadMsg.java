package top.readm.demo.dhtnetwork.dht.pojo;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;
import top.readm.demo.dhtnetwork.dht.enumeration.KadType;
import top.readm.demo.dhtnetwork.dht.Utils.BencodeUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class KadMsg{

    public KadType type=KadType.IGNORE;

    private Map msgMap;

    public Object parseMsg(byte[] data){

        Object obj= null;
        Map<String,Object> map=null;
        Map<String,Object> mapBack=null;

        try {
            obj = BencodeUtils.decode(new String(data,"iso8859-1"),null);
            if(obj==null && ! (obj instanceof Map)){
                return null;
            }
            map=(Map<String,Object> )obj;

            System.out.println("解析map:"+map);
            String q=(String) map.get("q");
            System.out.println("=====================================解析消息q字段为"+q);
            if(q!=null && !StringUtils.isEmpty(q)){
                //存在q字段 announce_peer 或者  get_peers
                this.msgMap=map;

                switch (q){

                    case "announce_peer":
                        System.out.println("parseMsg解析到announce_peer请求");
                        this.type=KadType.ANNOUNCE_PEER_QUERY;
                        break;

                    case "get_peers":
                        System.out.println("parseMsg解析到get_peers请求");
                        this.type=KadType.GET_PEERS_QUERY;
                        break;

                    case "ping":
                        System.out.println("parseMsg解析到PING_QUERY请求");
                        this.type=KadType.PING_QUERY;
                        break;

                    case "find_node":
                        System.out.println("parseMsg解析到find_node请求");
                        this.type=KadType.FIND_NODE_QUERY;
                        break;

                }
                return map;

            }

            Object r= map.get("r");
           // System.out.println("解析消息r字段为"+r);
            if(r !=null && (r instanceof Map)){
                //不存在q字段 但存在r字段看看是不是我们关注的其他两类消息
                //find_node的响应  ping的响应
                // "r": {"id":"0123456789abcdefghij", "nodes": "def456..."}}
                // * Response = {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
                mapBack=(Map<String, Object>) r;

                String nodes=(String) mapBack.get("nodes");
                if(nodes!=null && !StringUtils.isEmpty(nodes)){
                    this.type=KadType.FIND_NODE_RESPONSE;
                    this.msgMap=map;
                    return map;
                }
                String nodeId=(String) mapBack.get("id");
                if(nodeId!=null && !StringUtils.isEmpty(nodeId)){
                    this.type=KadType.PING_RESPONSE;
                    this.msgMap=map;
                    return map;
                }
            }



        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return map;
    }

}
