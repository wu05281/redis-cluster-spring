package com.riches.cache.explanation;

import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.riches.cache.client.RedisClient;

@Component
@Aspect
public class RedisCacheAdvice {

	@Resource
	private RedisClient redisClient;

	@Around("@annotation(com.riches.cache.explanation.CacheAble)")
	public Object cacheAble(final ProceedingJoinPoint pjp) throws Throwable {

		Method currentMethod = parseMethod(pjp);
		CacheAble cacheable = currentMethod.getAnnotation(CacheAble.class);
		int expiration = cacheable.expiration();

		String key =parseKey(cacheable.key(), currentMethod, pjp.getArgs());
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
	
	@Around("@annotation(com.riches.cache.explanation.CacheEvict)")
	public Object cacheEvict(final ProceedingJoinPoint pjp) throws Throwable {

		Method currentMethod = parseMethod(pjp);
		CacheEvict cacheEvict = currentMethod.getAnnotation(CacheEvict.class);

		String key =parseKey(cacheEvict.key(), currentMethod, pjp.getArgs());
		System.out.println(key);
		if (redisClient.exists(key)) {
			redisClient.expire(key, 0);
		}
		return pjp.proceed();
	}

	private Method parseMethod(final ProceedingJoinPoint pjp) throws NoSuchMethodException {
		Signature sig = pjp.getSignature();

		MethodSignature msig = null;
		if (!(sig instanceof MethodSignature)) {
			throw new IllegalArgumentException("该注解只能用于方法");
		}
		msig = (MethodSignature) sig;
		Object target = pjp.getTarget();
		Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
		return currentMethod;
	}

	    /**
	     *	获取缓存的key 
	     *	key 定义在注解上，支持SPEL表达式
	     * @param pjp
	     * @return
	     */
	    private String parseKey(String key,Method method,Object [] args){
	      
	      
	      //获取被拦截方法参数名列表(使用Spring支持类库)
	      LocalVariableTableParameterNameDiscoverer u =   
	        new LocalVariableTableParameterNameDiscoverer();  
	      String [] paraNameArr=u.getParameterNames(method);
	      
	      //使用SPEL进行key的解析
	      ExpressionParser parser = new SpelExpressionParser(); 
	      //SPEL上下文
	      StandardEvaluationContext context = new StandardEvaluationContext();
	      //把方法参数放入SPEL上下文中
	      for(int i=0;i<paraNameArr.length;i++){
	        context.setVariable(paraNameArr[i], args[i]);
	      }
	      return parser.parseExpression(key).getValue(context,String.class);
	    }
}
