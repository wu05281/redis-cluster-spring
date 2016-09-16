package com.riches.cache.service;

import javax.annotation.Resource;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {
	
	@SuppressWarnings("rawtypes")
	@Resource
	private RedisTemplate redisTemplate;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setUserName(final String key) {
		redisTemplate.execute(new RedisCallback() {
			public Object doInRedis(RedisConnection connection){
			              connection.set(key.getBytes(),(System.currentTimeMillis()+"").getBytes());
			              return 1L;
			          }
			});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getUserName(final String key) {
		String name = (String) redisTemplate.execute(new RedisCallback() {
			public Object doInRedis(RedisConnection connection){
				final String name = new String(connection.get(key.getBytes()));
			              return name;
			          }
			});
		return name;
	}

}
