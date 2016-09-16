package com.riches.cache.service;

import java.io.IOException;
import java.util.HashSet;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class TestJedisCluster {

	public static void main(String[] args) {
		HashSet<HostAndPort> nodes = new HashSet<HostAndPort>();
//        nodes.add(new HostAndPort("127.0.0.1", 7001));
        nodes.add(new HostAndPort("127.0.0.1", 7002));
//        nodes.add(new HostAndPort("127.0.0.1", 7003));
//        nodes.add(new HostAndPort("127.0.0.1", 7004));
//        nodes.add(new HostAndPort("127.0.0.1", 7005));
//        nodes.add(new HostAndPort("127.0.0.1", 7006));
        JedisCluster cluster = new JedisCluster(nodes);
//        cluster.set("jedisClusterKey", "hello_world");
        String str = cluster.get("jedisClusterKey");
        System.out.println("---:"+str);
        //关闭连接
        try {
			cluster.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
