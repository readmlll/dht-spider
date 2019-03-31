package top.readm.demo.dhtnetwork.dht.pojo;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.codec.digest.DigestUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Peers 的联系信息被编码为 6 字节的字符串。又被称为 "CompactIP-address/port info"，其中前 4 个字节是网络字节序的 IP 地址，后 2 个字节是网络字节序的端口。
 * 节点的联系信息被编码为 26 字节的字符串。又被称为 "Compactnode info"，其中前 20 字节是网络字节序的节点 ID，后面 6 个字节是 peers 的 "CompactIP-address/port info"。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class DhtNode{
    private String nodeId= DigestUtils.sha1Hex("default".getBytes()); //如f38cfe2e2facbcc742bad63f91ad55637300cb45
    private String nodeIp; //如 127.0.0.1
    private String nodePort; //如 65530
    private byte[] nodeInfo=new byte[26];
    //扩展数据
    private Object extData;

    public DhtNode nodeInfoDecode() {
        //解析nodeid
        byte[] nodeidBytes = new byte[20];
        System.arraycopy(nodeInfo, 0, nodeidBytes, 0, 20);
        //网络字节序的节点信息 需要转成小端 然后转成 十六进制字符串
        nodeidBytes = bytesOrderReverse(nodeidBytes);
        nodeId = sha1Bytes2HexStr(nodeidBytes);

        byte[] ipBytes = new byte[4];
        System.arraycopy(nodeInfo, 20, ipBytes, 0, 4);
       // ipBytes = bytesOrderReverse(ipBytes);
        //nodeIp = parseIp(ipBytes);
        try {
            nodeIp=InetAddress.getByAddress(ipBytes).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        byte[] portBytes = new byte[2];
        System.arraycopy(nodeInfo, 24, portBytes, 0, 2);
        //portBytes = bytesOrderReverse(portBytes);
        nodePort = parsePort(portBytes);

        return this;
    }

    public DhtNode nodeInfoEncode(){

        //nodeid必然是一串 sha1编码的字符串  需要转换成字节数组 然后转换成网络字节序
        // f38cfe2e2facbcc742bad63f91ad55637300cb45 两两1字节转换
        byte[] nodeidBytes=sha1hexStr2Bytes(nodeId);
        //逆序一下字节序 转成网络字节序
        nodeidBytes=bytesOrderReverse(nodeidBytes);

        //ip必然需要进行拆分  组成4个字节是网络字节序的 IP 地址  如127.0.0.1 网络字节序第一个字节为127
        String[] ipParts= nodeIp.split("\\.");
        byte[] ipBytes=new byte[4];
        for(int i=0;i<4;i++){
            byte bt=(byte)Integer.parseInt(ipParts[i]);
            ipBytes[i]=bt;
        }


        // 2 个字节是网络字节序的端口  由于jvm本身是网络字节序 所以直接得到字节数组就可
        short port=(short)Integer.parseInt(nodePort);
        ByteBuffer bf=ByteBuffer.allocate(2);
        byte[] portBytes=bf.putShort(port).array();

        System.arraycopy(nodeidBytes,0,this.nodeInfo, 0, 20);
        System.arraycopy(ipBytes,0,this.nodeInfo, 20, 4);
        System.arraycopy(portBytes,0,this.nodeInfo, 24, 2);
        return this;
    }

    public byte[] getCompactNodeInfo(){
        byte[] info=new byte[6];
        System.arraycopy(this.getNodeInfo(), 20, info, 0, 6);
        return info;
    }


    /**
     *   将16进制字符串 两两转换成正确的字节数组
     * @param hexStr
     * @return
     */
    public static byte[] sha1hexStr2Bytes(String hexStr){

        String dic="0123456789abcdef";

        if(hexStr.length()%2==1){
            return null;
        }
        int len=hexStr.length()/2;
        byte[] resBytes=new byte[len];
        for(int i=0,j=0;i<len;i++,j+=2){
            //拆分高低位
            //a94a8fe5ccb19ba61c4c0873d391e987982fbbd3
            int high= dic.indexOf(hexStr.charAt(j))*16;
            int low=dic.indexOf(hexStr.charAt(j+1));
            resBytes[i]=(byte)(high+low);
        }

        return resBytes;
    }

    /**
     * 将主机字节序的 sha1编码的bytes（20个字节）转成 十六进制字符串(40长度)
     * @param sha1bytes
     * @return
     */
    public static String sha1Bytes2HexStr(byte[] sha1bytes){
        StringBuilder sb=new StringBuilder();
        String strTemp=null;
        int ntemp=0;
        for(byte bt:sha1bytes){
            if(bt<0){
                ntemp=bt&0xff;
                //ntemp&=0xff;
                strTemp=Integer.toHexString(ntemp);
            }
            else {
                strTemp=Integer.toHexString(bt);
            }
            if(strTemp.length()==1){
                strTemp="0"+strTemp;
            }
            sb.append(strTemp);
        }
        return sb.toString();
    }


    /**
     *  字节序逆序转换
     * @param bytes
     * @return
     */
    public  static byte[] bytesOrderReverse(byte[] bytes){
        byte[] newBytes=new byte[bytes.length];
        for(int i=bytes.length-1,j=0;i>=0;i--,j++){
            newBytes[j]=bytes[i];
        }
        return newBytes;
    }

    /**
     * 按照网络字节序转换bytes到字符串的点分十进制ip形式
     * @param ipBytes
     * @return
     */
    public static String parseIp(byte[] ipBytes){
        int ntemp=0;

        String strTemp=null;
        StringBuilder sb=new StringBuilder();
        for(byte bt:ipBytes){
            if(bt<0){
                ntemp=bt&0xff;
                strTemp=String.valueOf(ntemp);
            }else{
                strTemp=String.valueOf(bt);
            }
            sb.append(strTemp);
            sb.append(".");
        }
        return sb.substring(0, sb.length()-1);
    }

    /**
     * 转换网络字节序的端口bytes到十进制字符串
     * @param portBytes
     * @return
     */
    public static String parsePort(byte[] portBytes){

        int ntemp=0;
        String strTemp=null;
        short s=ByteBuffer.wrap(portBytes).getShort();
        if(s<0){
            //ntemp&=0x0000ffff;
            ntemp=65535+1+s;
        }else{
            ntemp=s;
        }
        strTemp=Integer.toString(ntemp);
        // 0000 ‭1010        0001 0100‬


      /* String strTemp=null;
       int sum=0;
       sum+=portBytes[0]*256;
       sum+=portBytes[1];
       strTemp=Integer.toString(sum);*/
       return  strTemp;
    }

}

