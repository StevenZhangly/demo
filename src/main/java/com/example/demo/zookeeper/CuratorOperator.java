package com.example.demo.zookeeper;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class CuratorOperator {

    public CuratorFramework client = null;

    public  static final String zkServerPath = "192.168.163.128";

    public CuratorOperator(){
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000);

        client = CuratorFrameworkFactory.builder().connectString(zkServerPath).sessionTimeoutMs(10000).retryPolicy(retryPolicy).namespace("workspace").build();

    }

    public CuratorFramework startZk(){
        client.start();

        return client;
    }

    public void stopZk(){
        client.close();
    }

    public static void main(String[] args) throws Exception {
        CuratorOperator curatorOperator = new CuratorOperator();
        CuratorFramework client = curatorOperator.startZk();
        System.out.println("成功连接到zookeeper服务端:"+ client.isStarted());

        //创建节点
        //client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath("/zly/test", "123".getBytes());

        //修改节点
        //client.setData().withVersion(0).forPath("/zly/test", "abc".getBytes());

        //删除节点
        //client.delete().guaranteed().deletingChildrenIfNeeded().withVersion(1).forPath("/zly/test");

        //读取节点数据
        /*Stat stat = new Stat();
        byte[] data = client.getData().storingStatIn(stat).forPath("/zly/test");
        System.out.println("节点数据为:" + new String(data) + ",节点版本号:" + stat.getVersion());*/

        //读取子节点
        /*List<String> nodes = client.getChildren().forPath("/zly/test");
        for(String node : nodes){
            System.out.println(node);
        }*/

        //判断节点是否存在
        Stat s = client.checkExists().forPath("/zly/test/a");
        System.out.println(s);

        //注册watcher监听事件,触发一次后就销毁
        //client.getData().usingWatcher(new MyCuratorWatcher()).forPath("/zly/test");

        //NodeCache事件监听,可多次触发
        /*final NodeCache nodeCache = new NodeCache(client, "/zly/test");
        nodeCache.start(true);
        if(nodeCache.getCurrentData() != null){
            System.out.println("节点初始化数据为：" + new String(nodeCache.getCurrentData().getData()));
        } else {
            System.out.println("节点初始化数据为空");
        }
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                String data = new String(nodeCache.getCurrentData().getData());
                System.out.println("监听到节点数据变化:" + data);
            }
        });*/

        final PathChildrenCache childrenCache = new PathChildrenCache(client, "/zly/test", true);
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        List<ChildData> childDataList = childrenCache.getCurrentData();
        System.out.println("当前数据节点的子节点数据列表:");
        for (ChildData childData : childDataList) {
            System.out.println(childData.getPath() + ":" + new String(childData.getData()));
        }
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                String path = null;
                String data = null;
                if(event.getData() != null){
                    path = event.getData().getPath();
                    data = new String(event.getData().getData());
                }
                if(event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)){
                    System.out.println("子节点初始化OK");
                } else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)){
                    System.out.println("添加子节点:" + path + ":" + data);
                } else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){
                    System.out.println("更新子节点:" + path + ":" + data);
                } else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
                    System.out.println("删除子节点:" + path + ":" + data);
                }
            }
        });
        System.in.read();
    }
}

class MyCuratorWatcher implements CuratorWatcher {

    @Override
    public void process(WatchedEvent watchedEvent) throws Exception {
        System.out.println("触发CuratorWatcher，节点路径为:" + watchedEvent.getPath());
    }
}
