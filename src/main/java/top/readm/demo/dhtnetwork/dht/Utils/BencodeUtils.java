package top.readm.demo.dhtnetwork.dht.Utils;

import top.readm.demo.dhtnetwork.dht.pojo.IntegerPtr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * bencode编码工具
 * 其中对于列表的解析可能会有问题
 * 整个demo中解析得消息 也没有用到列表的 于是就没有测试
 */
public class BencodeUtils{

    public static String encode(Object obj){

        StringBuilder sb=new StringBuilder();
        if(obj!=null && obj instanceof Integer){
            sb.append("i");
            sb.append( ((Integer)obj).toString() );
            sb.append("e");
            return  sb.toString();
        }

        if(obj!=null && obj instanceof String){
            sb.append(((String)obj).length());
            sb.append(":");
            sb.append( ((String)obj).toString() );
            return  sb.toString();
        }

        if(obj!=null && obj instanceof List){
            sb.append("l");
            List<Object> list=(List<Object>)obj;
            for(Object ob:list){
                sb.append(encode(ob));
            }
            sb.append("e");
            return sb.toString();
        }

        if(obj!=null && obj instanceof Map){
            sb.append("d");
            Map<String,Object> map=(Map<String,Object>)obj;
            Map<String, Object> sortMap = strDictSortMap();

            sortMap.putAll(map);

            for (Map.Entry<String, Object> entry : sortMap.entrySet()) {
                String key=entry.getKey();
                sb.append(key.length());
                sb.append(":");
                sb.append(key);
                sb.append(encode(entry.getValue()));
            }
            sb.append("e");
            return sb.toString();

        }

        return "";
    }

    public static Map strDictSortMap(){
        Map<String, Object> sortMap = new TreeMap<String, Object>(
                (str1,str2)->{
                    return str1.compareTo(str2);
                });
        return  sortMap;
    }

    public  static  Object decode(String data, IntegerPtr handleLen){
        if(handleLen==null){
            handleLen=new IntegerPtr().setInteger(0);
        }
        //d7:balancei1000e4:coin3:btc4:name5:jisene
        //字典 de  列表le 数字ie  字符 长度:字符串
        char first=data.charAt(0);
        boolean firstCome=true;
        int handleLenSum=0;
        int count=0;

        switch (first){
            case 'i':
                //数字
                handleLen.setInteger(data.indexOf('e')+1);
                data=data.substring(1, data.indexOf('e'));
                return Integer.parseInt(data);
            case 'l':
                //列表 l5:hello5:worldi123456ee
                List<Object> list=new ArrayList<>();
                firstCome=true;
                handleLenSum=0;
                while (true){
                    Object ob=null;
                    if(firstCome)
                    {
                        data=data.substring(1);
                        firstCome=false;
                        handleLenSum++;
                    }

                    if(data.charAt(0)=='l'){
                        List<Object> listTemp=(List<Object>) decode(data, handleLen);
                        ob=listTemp;
                    }else{
                        ob=decode(data,handleLen);
                    }

                    handleLenSum+=handleLen.getInteger();
                    list.add(ob);
                    data=data.substring(handleLen.getInteger());

                    /*if(data.length()<=0)
                        break;

                    if(data.charAt(0)=='e'){
                        count=0;
                        while (count<data.length() && data.charAt(count)=='e' ){
                            count++;
                            handleLenSum++;
                        }
                        break;
                    }*/

                    boolean currentEnd=false;
                    count=0;
                    while (data.length()>0 && data.charAt(count)=='e'){
                        while (count<data.length() && data.charAt(count)=='e' ){
                            count++;
                            handleLenSum++;

                        }
                        data=data.substring(count);
                        if(count>0)
                            currentEnd=true;
                        count=0;
                    }

                    if(data.length()<=0)
                        break;

                    if(!currentEnd)
                        continue;

                }
                handleLen.setInteger(handleLenSum);
                return list;

            case 'd':
                //字典 de    d7:balancei1000e4:coin3:btc4:name5:jisene
                Map map=strDictSortMap();
                firstCome=true;
                //计算已经处理的字符总个数
                handleLenSum=0;
                //获取所有的key 和所有的value  key必然是字符型
                while (true){
                    if(firstCome){
                        data=data.substring(1);
                        //因为第一次的话需要除掉标志位  就已经算处理一个字符了
                        handleLenSum++;
                        firstCome=false;
                    }

                    String key=(String) decode(data,handleLen);
                    data=data.substring(handleLen.getInteger());
                    handleLenSum+=handleLen.getInteger();

                    //value 可能还是map 或者list
                    Object value=null;
                    value=decode(data, handleLen);
                    data= data.substring(handleLen.getInteger());

                    handleLenSum+=handleLen.getInteger();

                    map.put(key, value);

                    //处理结束的判断  因为map  list 的标志结束都是e   到了这里已经处理完毕了 只剩下没有用的标志位
                    //标志位可能有多重 所有计算个数 算到总的处理字符个数上

                    boolean currentEnd=false;
                    count=0;
                    while (data.length()>0 && data.charAt(count)=='e'){
                        while (count<data.length() && data.charAt(count)=='e' ){
                            count++;
                            handleLenSum++;

                        }
                        data=data.substring(count);
                        if(count>0)
                            currentEnd=true;
                        count=0;
                    }

                    if(data.length()<=0)
                        break;

                    if(currentEnd)
                        break;
                }
                //截取第一个key的长度
                handleLen.setInteger(handleLenSum);
                return  map;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                //字符串
                int strlen=Integer.parseInt(data.substring(0,data.indexOf(':')));
                String str=data.substring(data.indexOf(':')+1, data.indexOf(':')+1+strlen);
                handleLen.setInteger(data.indexOf(':')+1+strlen);
                return str;
            default:
                return null;
        }

    }

}