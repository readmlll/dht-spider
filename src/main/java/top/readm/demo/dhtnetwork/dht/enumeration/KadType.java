package top.readm.demo.dhtnetwork.dht.enumeration;

public enum KadType{
    //我们只关注这四个消息过来 进行处理 因为并非是自身一个完整的dht节点 我们的目的只是获取信息
    PING_RESPONSE,  //当主动ping了对方可以收到 ping的回复
    GET_PEERS_QUERY,      //由别人主动发过来查找info节点的请求 我们需要回应
    FIND_NODE_RESPONSE, //当主动发送 find_node 消息会收到回复
    ANNOUNCE_PEER_QUERY  //当别人找到了对应的文件 反馈给我们的信息 需要进行收集
    ,IGNORE  //不关注的其他消息 或者 无效的消息
    ,PING_QUERY //ping的请求 需要我们回复 以便别人建立路由表
    ,FIND_NODE_QUERY
}
