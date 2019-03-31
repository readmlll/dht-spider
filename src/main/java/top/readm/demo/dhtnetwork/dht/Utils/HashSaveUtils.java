package top.readm.demo.dhtnetwork.dht.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 持久化可靠谱的 不可靠的 hashinfo
 * 暂时保存到文件
 */
public class HashSaveUtils {

    //没有经过探测的 来自不可靠的info信息
    private static File unreliableInfoFile=new File("unreliableInfo.txt");
    //没有经过探测的 可靠来源的info信息
    private static File reliableInfoFile=new File("reliableInfo.txt");

    private static BufferedWriter unreliableInfoFileWr=null;
    private static BufferedWriter reliableInfoFileWr=null;

    static{
        try {
            unreliableInfoFileWr = new BufferedWriter(new FileWriter(unreliableInfoFile));
            reliableInfoFileWr=new BufferedWriter(new FileWriter(reliableInfoFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public  static synchronized void unreliableInfoSave(String hashInfo){
        try {
            unreliableInfoFileWr.write(hashInfo);
            unreliableInfoFileWr.newLine();
            unreliableInfoFileWr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static synchronized void reliableInfoSave(String hashInfo){

        try {
            reliableInfoFileWr.write(hashInfo);
            reliableInfoFileWr.newLine();
            reliableInfoFileWr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
