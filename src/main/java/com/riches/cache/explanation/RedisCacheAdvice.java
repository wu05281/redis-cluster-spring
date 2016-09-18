package com.riches.cache.explanation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class RedisCacheAdvice {

	@Pointcut("@annotation(com.riches.cache.explanation.RedisCache)")
	public void getPonitcut(){
		
	}
	
	@Around("getPonitcut()")
    public Object cacheGetSingle(final ProceedingJoinPoint pjp) throws Throwable {
		System.out.println("RedisCacheAdvice。cacheGetSingle");
        return pjp.proceed();
    }
}
