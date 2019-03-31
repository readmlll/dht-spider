package top.readm.demo.dhtnetwork;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import top.readm.demo.dhtnetwork.dht.Utils.BencodeUtils;
import top.readm.demo.dhtnetwork.dht.Utils.KadProtocalUtils;
import top.readm.demo.dhtnetwork.dht.enumeration.KadType;
import top.readm.demo.dhtnetwork.dht.pojo.BtInfo;
import top.readm.demo.dhtnetwork.dht.pojo.DhtNode;
import top.readm.demo.dhtnetwork.dht.pojo.KadMsg;
import top.readm.demo.dhtnetwork.dht.pojo.StringPtr;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


//@RunWith(SpringRunner.class)
//@SpringBootTest
public class DhtNetworkApplicationTests {


    @Test
    public void test001() throws UnsupportedEncodingException {

        Map<String,Object> map1= BencodeUtils.strDictSortMap();
        map1.put("map1", "map11");
        Map<String,Object> map2=BencodeUtils.strDictSortMap();
        map2.put("map2", "map22");

        Map<String,Object> map3=BencodeUtils.strDictSortMap();
        map3.put("map3", "map33");


        List<Object> list1=new ArrayList<>();
        list1.add("list1");
        list1.add(map1);
        list1.add(map2);

        List<Object> list2=new ArrayList<>();
        list1.add(list2);
        list2.add("list2");

        List<Object> list3=new ArrayList<>();
        list3.add("list3");

        map3.put("map1", map1);
        map3.put("list1",list1);
        map3.put("list2",list2);
        map3.put("list3",list3);

        String enstr=BencodeUtils.encode(map3);
        System.out.println(enstr);

       /* Object obj=BencodeUtils.decode(enstr, null);
        if(obj!=null && obj instanceof Map){
            System.out.println( ((Map)obj) );
        }
        if(obj!=null && obj instanceof List){
            System.out.println(((List<Object>)obj));
        }*/

    }

    @Test
    public  void test004() throws UnknownHostException {

        //String
        String s="d1:rd2:id20:< rsH���p���A\u001B8i�H\u0013!5:nodes208:��������<�\f>�$^\n" +
                "\u001C\u0006��Mx.V-\u0010��������<�\f>�$^\n" +
                "\u001C\u0006��Mx.V-\u0010��������<�\f>�$^\n" +
                "\u001C\u0006��Mx.V-\u0010��������<�\f>�$^\n" +
                "\u001C\u0006��Mx.V-\u0010��������<�\f>�$^\n" +
                "\u001C\u0006��Mx.V-\u0010��������<�\f>�$^\n" +
                "\u001C\u0006��Mx.V-\u0010��������<�\f>�$^\n" +
                "\u001C\u0006��Mx.V-\u0010��������<�\f>�$^\n" +
                "\u001C\u0006��Mx.V-\u0010e1:t2:aa1:v4:JB  1:y1:re";

        /*s="d1:rd2:id20:< rsH���p���A\u001B8i�H\u0013!e1:t2:aa1:v4:JB  1:y1:re";
        Map<String,Object> Object=(Map)BencodeUtils.decode(s,null);
        System.out.println(Object);


        System.out.println();*/



        String string = DigestUtils.sha1Hex("text".getBytes());
        System.out.println(string);
        //372ea08cab33e71c02c651dbc83a474d32c676ea
        //372ea08cab33e71c02c651dbc83a474d32c676ea
        DhtNode dhtNode=new DhtNode();
        dhtNode.setNodeId(string);
        dhtNode.setNodePort("12580");
        dhtNode.setNodeIp("255.255.255.0");
        dhtNode.nodeInfoEncode();
        System.out.println(Arrays.toString( dhtNode.getNodeInfo()));
        //[-22, 118, -58, 50, 77, 71, 58, -56, -37, 81, -58, 2, 28, -25, 51, -85, -116, -96, 46, 55, -1, -1, -1, 0, 49, 36]

        byte[] bytes={55, 46, -96, -116, -85, 51, -25, 28, 2, -58, 81, -37, -56, 58, 71, 77, 50, -58, 118, -22, -1, -1, -1, 0, 49, 36};
        DhtNode testNode=new DhtNode().setNodeInfo(bytes).nodeInfoDecode();
        System.out.println(testNode);




    }

    @Test
    public void test006() throws UnsupportedEncodingException {


        int[] findQ={100,49,58,97,100,50,58,105,100,50,48,58,119,205,254,216,214,63,205,244,
                123,167,208,122,120,58,178,65,29,156,217,181,54,58,116,97,114,103,101,116,50,48,
                58,119,205,254,216,214,63,7,157,0,0,40,1,0,0,43,65,0,0,100,255,101,49,58,113,57,58,
                102,105,110,100,95,110,111,100,101,49,58,116,52,58,225,89,0,0,49,58,118,52,58,85,84,
                176,90,49,58,121,49,58,113,101};

        byte[] findBytes=new  byte[findQ.length];
        for(int i=0;i<findQ.length;i++){
            findBytes[i]=Integer.valueOf(findQ[i]).byteValue();
        }

        int[] getpeerQ={100,49,58,97,100,50,58,105,100,50,48,58,143,249,104,56,248,146,99,
                15,156,99,50,189,124,241,104,177,89,174,193,4,57,58,105,110,102,
                111,95,104,97,115,104,50,48,58,143,249,104,56,248,146,115,5,77,36,
                100,145,51,62,72,162,164,81,185,226,101,49,58,113,57,58,103,101,116,
                95,112,101,101,114,115,49,58,116,50,58,136,35,49,58,118,52,58,76,84,1,
                1,49,58,121,49,58,113,101};

        byte[] getpeerBytes=new  byte[getpeerQ.length];
        for(int i=0;i<getpeerQ.length;i++){
            getpeerBytes[i]=Integer.valueOf(getpeerQ[i]).byteValue();
        }
        String rightHash="8FF96838F89273054D246491333E48A2A451B9E2";

        int[] pingQ={100, 49,58,97,100,50,58,105,100,50,48,58,204,222,57,103,77,182,222,46,24,58,229,159,113,108,215,
                114,65,98,1,12,101,49,58,113,52,58,112,105,110,103,49,58,116,56,58,148,204,156,247,236,58,94,70,
                49,58,121,49,58,113,101};
        byte[] pingBytes=new  byte[pingQ.length];
        for(int i=0;i<pingQ.length;i++){
            pingBytes[i]=Integer.valueOf(pingQ[i]).byteValue();
        }

        KadMsg kadMsg=new KadMsg();
        kadMsg.parseMsg(getpeerBytes);
        System.out.println(kadMsg.getMsgMap());

        if(kadMsg.type== KadType.PING_QUERY){
            System.out.println("PING_QUERY");
        }

        if(kadMsg.type==KadType.GET_PEERS_QUERY){
            System.out.println("GET_PEERS");
            StringPtr lpstr=new StringPtr();
            String infoHash=KadProtocalUtils.handleGetPeers(kadMsg, lpstr);
            System.out.println("infohash="+infoHash);
            System.out.println("infohash是否解析正确"+infoHash.toUpperCase().equals(rightHash));
        }

    }


    @Test
    public void test007() throws UnsupportedEncodingException {

        int[] find_node_right={0x64,0x31,0x3a,0x61,0x64,0x32,
                0x3a,0x69,0x64,0x32,0x30,0x3a,0xb5,0xe4,0x1a,0xbf,0x96,0xca,0xfa,0x6e,0x01,0x62,
                0x87,0xb5,0xca,0xb0,0x79,0x81,0x8e,0xe3,0xf9,0xc6,0x36,0x3a,0x74,0x61,0x72,0x67,
                0x65,0x74,0x32,0x30,0x3a,0xc7,0x7b,0xce,0x9b,0xb2,0xfb,0xb9,0xd0,0x21,0xfd,0x35,
                0x8b,0x75,0x08,0x01,0x8e,0x76,0xe8,0x4b,0xb4,0x65,0x31,0x3a,0x71,0x39,0x3a,0x66,
                0x69,0x6e,0x64,0x5f,0x6e,0x6f,0x64,0x65,0x31,0x3a,0x74,0x32,0x30,0x3a,0x5b,0xda,
                0x1d,0xd8,0x31,0xcd,0x87,0xb8,0x0f,0xf5,0x42,0xff,0xa6,0x10,0x2b,0xa9,0xb8,0x85,
                0xda,0x48,0x31,0x3a,0x79,0x31,0x3a,0x71,0x65};

        byte[] find_node_bts=new byte[find_node_right.length];
        for(int i=0;i<find_node_right.length;i++){
            find_node_bts[i]=Integer.valueOf(find_node_right[i]).byteValue();
        }

        Object object = BencodeUtils.decode(new String(find_node_bts,"iso8859-1"), null);
        System.out.println(object);
        Map<String,Object> map =(Map<String,Object>) object;
        System.out.println(((String)map.get("t")).getBytes("iso8859-1").length);

        int[] find_node_my={0x64,0x31,0x3a,0x61,0x64,0x32,
                0x3a,0x69,0x64,0x32,0x30,0x3a,0x06,0xdb,0x40,0xdf,0x50,0xc1,0x14,0x85,0xeb,0xc7,
                0x90,0x5e,0x3d,0xec,0xa1,0x7c,0x3a,0xe3,0xb5,0x72,0x36,0x3a,0x74,0x61,0x72,0x67,
                0x65,0x74,0x32,0x30,0x3a,0x9d,0x22,0xb1,0xc7,0x64,0xcb,0xc4,0x9f,0xf0,0xd3,0xb1,
                0xe5,0x34,0xd9,0x73,0x57,0x22,0xb0,0xee,0x24,0x65,0x31,0x3a,0x71,0x39,0x3a,0x66,
                0x69,0x6e,0x64,0x5f,0x6e,0x6f,0x64,0x65,0x31,0x3a,0x74,0x32,0x3a,0x65,0x30,0x31,
                0x3a,0x79,0x31,0x3a,0x71,0x65};

        byte[] find_node_bts_my=new byte[find_node_my.length];
        for(int i=0;i<find_node_my.length;i++){
            find_node_bts_my[i]=Integer.valueOf(find_node_my[i]).byteValue();
        }

        object = BencodeUtils.decode(new String(find_node_bts_my,"iso8859-1"), null);
        System.out.println();
        System.out.println("==================");
        System.out.println();
        System.out.println(object);

    }


    @Test
    public void test008() throws IOException {

        //C:\Users\Readm\Desktop
        /*File file =new File("C:\\Users\\Readm\\Desktop\\info.txt");
        char buf[]=new char[1500];
        byte[] bytes=new byte[1500];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bytes);

        for (int i=0;i<1500;i+=20){
            byte[] bts=new byte[20];
            System.arraycopy(bytes, i, bts, 0, 20);
            String info= DhtNode.sha1Bytes2HexStr(bts);
            System.out.println(info);
        }*/

    }

    @Test
    public void test009() throws JsonProcessingException {

        String baseUrl="https://www.zhongziso.la/info-";
        String info="6f3ed527e69efa04c618d2896ef9f75283cec591";


        System.out.println( BtInfo.parseBtInfo(info, null));

    }
}






