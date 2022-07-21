package com.sihai.rete_limiter.controller;

import com.sihai.rete_limiter.annotation.RateLimiter;
import com.sihai.rete_limiter.enums.LimitType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class hello {

    /**
     *  限流10秒内，接口可以访问3次
     *  limitType = LimitType.IP： 限制当前IP
     */
    @GetMapping("/hello")
    @RateLimiter(time = 1000, conut = 3, limitType = LimitType.IP)
    public String hello() {
        return "Hello World";
    }
}
