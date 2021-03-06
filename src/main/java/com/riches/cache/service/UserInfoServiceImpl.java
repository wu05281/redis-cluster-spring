package com.riches.cache.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.riches.cache.client.RedisClient;
import com.riches.cache.explanation.CacheAble;
import com.riches.cache.explanation.CacheEvict;

@Service
public class UserInfoServiceImpl implements UserInfoService {
	
//	@SuppressWarnings("rawtypes")
//	@Resource
//	private RedisTemplate redisTemplate;
	
	@Resource
	private RedisClient redisClient;

	public void setUserName(final String key) {
		redisClient.setString(key, System.currentTimeMillis()+"", 300);
//		redisTemplate.execute(new RedisCallback() {
//			public Object doInRedis(RedisConnection connection){
//			              connection.set(key.getBytes(),(System.currentTimeMillis()+"").getBytes());
//			              return 1L;
//			          }
//			});
	}
	
	@CacheAble(expiration=300, key="#key")
	//@CacheEvict(key="#key")
	public String getUserName(String key) {
		System.out.println("未能命中");
//		String name = redisClient.get(key, String.class);
//		String name = (String) redisTemplate.execute(new RedisCallback() {
//			public Object doInRedis(RedisConnection connection){
//				final String name = new String(connection.get(key.getBytes()));
//			              return name;
//			          }
//			});
		return "zhangsan";
	}

}
