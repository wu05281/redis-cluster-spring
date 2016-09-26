package com.riches.cache.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Service(value="redisClient")
public class RedisClient {
	
	protected  Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private RedisTemplate redisTemplate;

	/**
	 * 设置一个对象 可以是string 
	 * @param key
	 * @param obj
	 * @param clazz 
	 * @param seconds 超时时间 0 代表永不过期
	 */
	public <T> void set(final String key,final T obj,final int seconds){
		Assert.isTrue(!StringUtils.isEmpty(key), "key is not allow empty..");
		redisTemplate.execute(new RedisCallback<T>() {
			public T doInRedis(RedisConnection connection)
					throws DataAccessException {
				byte[] byteKey = getBytesKey(key);
				connection.set(byteKey, toBytes(obj));
				if (seconds !=0 ) {
					connection.expire(byteKey, seconds);
				}
				logger.debug("set object key {} = {} ", key,obj);
				return null;
			}
		});
	}
	
	/**
	 * 设置一个对象 可以是string 
	 * @param key
	 * @param obj
	 * @param clazz 
	 */
	public <T> T get(final String key,Class<T> clazz){
		Assert.isTrue(!StringUtils.isEmpty(key), "key is not allow empty..");
		return (T) redisTemplate.execute(new RedisCallback<T>() {
			public T doInRedis(RedisConnection connection)
					throws DataAccessException {
				T returnVaue = null;
				byte[] byteKey = getBytesKey(key);
				if (connection.exists(byteKey)) {
					returnVaue = (T)toObject(connection.get(byteKey)) ;
					logger.debug("get object key {} = {} ", key,returnVaue);
				}
				return returnVaue;
			}
		});
	}
	
	/**
	 * 取一个对象测试
	 */
	public <T> void set(final String key,final T obj){
		this.set(key, obj, 0);
	}
	
	/**
	 * 设置字符串
	 * @param key
	 * @param value
	 * @param seconds 超时时间 0 代表永不过期
	 */
	public void setString(final String key,final String value ,final int seconds){
		this.set(key, value, seconds);
	}
	
	/**
	 * 设置字符串
	 * @param key
	 * @param value
	 */
	public void setString(final String key,final String value){
		this.set(key, value);
	}
	
	/**
	 * 检查给定 key 是否存在。
	 * @param key
	 * @return 若 key 存在，返回 true ，否则返回 false 。
	 */
	public Boolean exists(final String key){
		Assert.isTrue(!StringUtils.isEmpty(key), "key is not allow empty..");
		return (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				return	connection.exists(getBytesKey(key));
			}
		});
	}
	
	/**
	 * 删除给定的一个或多个 key 。
	 * 不存在的 key 会被忽略。
	 * @param keys
	 * @return 被删除 key 的数量。
	 */
	public long del(final String... keys){
		Assert.notEmpty(keys,"keys is not allow empty..");
		return (Long) redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection)
					throws DataAccessException {
				List<byte[]> list = new ArrayList<byte[]>();
				for (String key :keys) {
					list.add(getBytesKey(key));
				}
				long returnValue = connection.del((byte[][])list.toArray());
				logger.debug("del key  {} amount {} ", keys,returnValue);
				return returnValue;
			}
		});
	}
	
	/**
	 * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
	 * @param key
	 * @param seconds 单位秒
	 * @return 设置成功返回true  ，反之false
	 */
	public Boolean expire(final String key,final int seconds){
		Assert.isTrue(!StringUtils.isEmpty(key), "key is not allow empty..");
		return (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				logger.debug("set key {} expire {} ",key,seconds);
				return connection.expire(getBytesKey(key), seconds);
			}
		});
	}
	
	/**
	 * 移除给定 key 的生存时间
	 * @param key
	 * @return 当生存时间移除成功时true  如果 key 不存在或 key 没有设置生存时间，返回 false
	 */
	public Boolean persist(final String key){
		Assert.isTrue(!StringUtils.isEmpty(key), "key is not allow empty..");
		return (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				logger.debug("persist key {} ",key);
				return connection.persist(getBytesKey(key));
			}
		});
	}
	
	/**
	 * 以秒为单位，返回给定 key 的剩余生存时间
	 * @param key
	 * @return 当 key 不存在时，返回 -2 。
	 *		        当 key 存在但没有设置剩余生存时间时，返回 -1 。
	 *		      否则，以秒为单位，返回 key 的剩余生存时间。
	 */
	public Long ttl(final String key){
		Assert.isTrue(!StringUtils.isEmpty(key), "key is not allow empty..");
		return (Long) redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection)
					throws DataAccessException {
				long returnValue = 0;
				byte[] byteKey = getBytesKey(key);
				returnValue = connection.ttl(byteKey);
				logger.debug("ttl  key {} = {} ", key,returnValue);
				return returnValue;
			}
		});
	}
	
	/**
	 * 获取byte[]类型Key
	 * @param key
	 * @return
	 */
	private byte[] getBytesKey(Object object){
		if(object instanceof String){
    		try {
				return ((String) object).getBytes("CHARSET");
			} catch (UnsupportedEncodingException e) {
				return ((String) object).getBytes();
			}
    	}else{
    		return serialize(object);
    	}
	}
	/**
	 * Object转换byte[]类型
	 * @param key
	 * @return
	 */
	private byte[] toBytes(Object object){
    	return serialize(object);
	}

	/**
	 * byte[]型转换Object
	 * @param key
	 * @return
	 */
	private Object toObject(byte[] bytes){
		return unserialize(bytes);
	}
	
	/**
	 * 序列化对象
	 * @param object
	 * @return
	 */
	private byte[] serialize(Object object) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			if (object != null){
				baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				oos.writeObject(object);
				return baos.toByteArray();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 反序列化对象
	 * @param bytes
	 * @return
	 */
	private Object unserialize(byte[] bytes) {
		ByteArrayInputStream bais = null;
		try {
			if (bytes != null && bytes.length > 0){
				bais = new ByteArrayInputStream(bytes);
				ObjectInputStream ois = new ObjectInputStream(bais);
				return ois.readObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
