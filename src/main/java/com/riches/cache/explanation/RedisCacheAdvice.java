package com.riches.cache.explanation;

import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.riches.cache.client.RedisClient;

@Component
@Aspect
public class RedisCacheAdvice {
	
	@Resource
	private RedisClient redisClient;

	@Pointcut("@annotation(com.riches.cache.explanation.RedisCache)")
	public void getPonitcut() {

	}

	@Around("getPonitcut()")
	public Object cacheGetSingle(final ProceedingJoinPoint pjp)
			throws Throwable {
		// 得到类名、方法名和参数
		String clazzName = pjp.getTarget().getClass().getName();
		String methodName = pjp.getSignature().getName();
		Object[] args = pjp.getArgs();
		
		
		Signature sig = pjp.getSignature();
        MethodSignature msig = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        msig = (MethodSignature) sig;
        Object target = pjp.getTarget();
        Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        
        int expiration = currentMethod.getAnnotation(RedisCache.class).expiration();
        
		// 根据类名，方法名和参数生成key
		String key = genKey(clazzName, methodName, args);
		System.out.println(key);
		Object obj = null;
		if (redisClient.exists(key)) {
			obj = redisClient.get(key, Object.class);
		} else {
			obj = pjp.proceed();
			if (expiration > 0) {
				redisClient.set(key, obj, expiration);
			} else {
				redisClient.set(key, obj);
			}
		}
		return obj;
	}

	/**
	 * 根据类名、方法名和参数生成key
	 * 
	 * @param clazzName
	 * @param methodName
	 * @param args
	 *            方法参数
	 * @return
	 */
	protected String genKey(String clazzName, String methodName, Object[] args) {
		StringBuilder sb = new StringBuilder(clazzName);
		sb.append("-");
		sb.append(methodName);
		sb.append("-");

		for (Object obj : args) {
			sb.append(obj.toString());
			sb.append("-");
		}

		return sb.toString();
	}
}
