# dht-spider
一个java版本的dht网络爬虫，伪装dht节点获取hashinfo

导入idea 
在入口类DhtNetworkApplication 的main方法下 修改udp端口
直接运行即可。
需要放置在国外的服务器上。 


1.
磁力的hashinfo的保存暂时采用了保存到文本中 在
package top.readm.demo.dhtnetwork.dht.Utils;
HashSaveUtils类中可见保存的细节

2.
关于我写的becode编码实现类
BencodeUtils类
因为整个demo中的报文并没有用到list所以关于list的编码可能会有问题（我没有测试list）。
但对于这个demo中的需求。这个工具已经够用了。

3.
多线程 和 线程共享数据  及同步锁
所在的包
package top.readm.demo.dhtnetwork.dht.thread;




如需要测试 直接下载release下的jar包 按照描述运行即可
以下为测试的截图


开启基本的几个线程后抓取info的情况

![Image text](https://github.com/readmlll/dht-spider/blob/master/img/1111.png)

迅雷验证下可靠的hashinfo

![Image text](https://github.com/readmlll/dht-spider/blob/master/img/222.png)

