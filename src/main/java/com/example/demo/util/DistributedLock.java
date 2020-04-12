package com.example.demo.util;

import com.example.demo.config.ZKCustor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;

public class DistributedLock {

    private CuratorFramework client = null;

    final static Logger log = LoggerFactory.getLogger(DistributedLock.class);

    // 分布式锁的总节点名
    private static final String ZK_LOCK_PROJECT = "zk-locks";
    // 分布式锁节点
    private static final String DISTRIBUTED_LOCK = "distributed_lock";

    //计数器
    private CountDownLatch zkCountDownLatch = new CountDownLatch(1);

    public DistributedLock(CuratorFramework client) {
        this.client = client;
    }

    public void init(){

        //使用命名空间
        client = client.usingNamespace("ZKLocks-Namespace");

        try {
            //创建zk锁的总节点
            if(client.checkExists().forPath("/" + ZK_LOCK_PROJECT) == null){
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath("/" + ZK_LOCK_PROJECT);
            }
            //针对zk的分布式锁节点，创建相应的watcher事件监听
            addWatcherToLock("/" + ZK_LOCK_PROJECT);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addWatcherToLock(String path) throws Exception {
        final PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                if(pathChildrenCacheEvent.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
                    String path = pathChildrenCacheEvent.getData().getPath();
                    log.info("上一个会话已经释放锁或者该会话已经断开，节点路径为:" + path);
                    if(path.contains(DISTRIBUTED_LOCK)){
                        log.info("释放计数器, 让当前请求来获得分布式锁...");
                        zkCountDownLatch.countDown();
                    }
                }
            }
        });
    }

    public void getZkLock(){
        while (true){
            // 使用死循环，当且仅当上一个锁释放并且当前请求获得锁成功后才会跳出
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath("/" + ZK_LOCK_PROJECT + "/" + DISTRIBUTED_LOCK);
                log.info("获得分布式锁成功...");
                return;
            } catch (Exception e) {
                log.info("获得分布式锁失败...");
                try {
                    // 如果没有获取到锁，需要重新设置同步资源值
                    if(zkCountDownLatch.getCount() <= 0){
                        zkCountDownLatch = new CountDownLatch(1);
                    }
                    zkCountDownLatch.await();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean releaseZkLock(){
        try {
            if(client.checkExists().forPath("/" + ZK_LOCK_PROJECT + "/" + DISTRIBUTED_LOCK) != null){
                client.delete().forPath("/" + ZK_LOCK_PROJECT + "/" + DISTRIBUTED_LOCK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        log.info("分布式锁释放完毕...");
        return true;
    }

}
