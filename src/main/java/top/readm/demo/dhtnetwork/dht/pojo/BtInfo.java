package top.readm.demo.dhtnetwork.dht.pojo;

import lombok.*;
import lombok.experimental.Accessors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import top.readm.demo.dhtnetwork.dht.Utils.OkHttpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
@ToString
/**
 * 种子信息实体
 */
public class BtInfo {

    private String fileTitle;  //基本信息 标题
    private int fileNum;    //文件数量
    private List<String> fileLIst; //文件列表
    private String infoHash;
    private String fileSize;    //文件大小
    private String time;        //时间


    protected BtInfo(){

    }

    /**
     * 利用别人的种子库 解析 hashinfo
     * @param infoHash
     * @param reTryCount  重试次数 为null 则默认为5
     * @return 成功解析返回BtInfo对象  否则返回null
     */
    public static BtInfo parseBtInfo(String infoHash,Integer reTryCount){

        String baseUrl="https://www.zhongziso.la/info-";

        try {
            if(reTryCount==null){
                //重试次数
                reTryCount=5;
            }

            for(int i=0;i<reTryCount;i++){
                String page=null;
                try {
                    page= OkHttpUtils.get(baseUrl+infoHash, null);

                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }

                Document doc = Jsoup.parse(page);

                Elements elements= doc.select(".panel-title .text-left");
                String name=elements.first().text();

                Element element= doc.select(".dl-horizontal.magnetmore").first();
                elements=element.children();
                String key=null,value=null;
                Map<String,Object> map = new HashMap<>();
                int count=0;
                for(Element el:elements){
                    if(++count>12){
                        break;
                    }
                    if(count%2==1){
                        key=el.text().replace(":", "");
                    }else{
                        value=el.text();
                        map.put(key, value);
                    }
                }
                map.put("标题", name);

                elements=doc.select("select.form-control option");
                List<String> fileNameList=new ArrayList<>();
                for (Element el:elements){
                    fileNameList.add(el.text());
                }
                //System.out.println(map);

                BtInfo btInfo =new BtInfo();
                btInfo.setFileLIst(fileNameList)
                        .setFileSize((String) map.get("文件大小"))
                        .setInfoHash(infoHash)
                        .setFileNum(Integer.parseInt((String) map.get("文件数量")))
                        .setFileNum(Integer.parseInt((String) map.get("文件数量")))
                        .setFileTitle(name)
                        .setTime((String)map.get("创建日期"));

                return btInfo;
            }//retry for

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


        return null;
    }

}
