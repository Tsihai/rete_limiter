package com.sihai.rete_limiter.aspectj;

import com.sihai.rete_limiter.annotation.RateLimiter;
import com.sihai.rete_limiter.enums.LimitType;
import com.sihai.rete_limiter.exception.RateLimitException;
import com.sihai.rete_limiter.utils.IpUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import java.lang.reflect.Method;
import java.util.Collections;

@Aspect
@Component
public class RateLimiterAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);

    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    RedisScript<Long> redisScript;

    /**
     * 加了@RateLimiter注解的方法，执行前先判断是否超过了限制
     * @param jp
     * @param rateLimiter
     */
    @Before("@annotation(rateLimiter)")
    public void before(JoinPoint jp, RateLimiter rateLimiter) throws RateLimitException {
        int time = rateLimiter.time();
        int conut = rateLimiter.conut();
        String combineKey = getCombineKey(jp, rateLimiter);
        try {
            Long number = redisTemplate.execute(redisScript, Collections.singletonList(combineKey), time, conut);
            if(number == null || number.intValue() > conut) {
                // 超过限流阈值
                logger.info("当前接口已经达到最大限流次数");
                throw new RateLimitException("访问过于频繁，请稍后访问");
            }
            logger.info("一个时间窗内请求次数:{}, 当前请求次数:{}, 缓存的 key 为: {}", number, conut, combineKey);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 接口的调用次数 缓存在 redis 的key
     *
     * 限制ip+接口
     * reta_limit: 11.11.11.11-com.sihai.rete_limiter.controller.UserController.user
     *
     * 限制接口
     * reta_limit: com.sihai.rete_limiter.controller.UserController.user
     *
     * @param jp
     * @param rateLimiter
     * @return
     */
    private String getCombineKey(JoinPoint jp, RateLimiter rateLimiter) {
        StringBuilder key = new StringBuilder(rateLimiter.key());
        if (rateLimiter.limitType() == LimitType.IP) {
            key.append(IpUtils.getIpAddr(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()))
                    .append("-");
        }
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        key.append(method.getDeclaringClass().getName())
                .append("-")
                .append(method.getName());
        return key.toString();
    }

}
