package com.sihai.rete_limiter.enums;

/**
 * 限流的类型
 */
public enum LimitType {

    /**
     * 默认的限流策略， 针对某一个接口进行限流
     */
    DEFAULT,

    /**
     * 针对某一个 IP 限流
     */
    IP
}
