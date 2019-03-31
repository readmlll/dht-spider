package top.readm.demo.dhtnetwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.readm.demo.dhtnetwork.dht.DhtApp;

import java.io.IOException;

//@SpringBootApplication
public class DhtNetworkApplication {

    public static void main(String[] args) throws IOException {

        //SpringApplication.run(DhtNetworkApplication.class, args);

        DhtApp dhtApp=new DhtApp();
        dhtApp.setPORT(9120);
        dhtApp.run();
    }

}
