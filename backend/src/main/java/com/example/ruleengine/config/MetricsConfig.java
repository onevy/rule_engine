package com.example.ruleengine.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控指标配置
 * <p>
 * 配置规则引擎相关的监控指标，包括：
 * <ul>
 *   <li>规则执行次数计数器</li>
 *   <li>规则执行时间计时器</li>
 *   <li>规则匹配次数计数器</li>
 *   <li>规则编译次数计数器</li>
 * </ul>
 * </p>
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Configuration
public class MetricsConfig {

    /**
     * 规则执行次数计数器
     */
    @Bean
    public Counter ruleExecutionCounter(MeterRegistry registry) {
        return Counter.builder("rule.execution.count")
                .description("规则执行总次数")
                .tag("type", "execution")
                .register(registry);
    }

    /**
     * 规则执行成功次数计数器
     */
    @Bean
    public Counter ruleExecutionSuccessCounter(MeterRegistry registry) {
        return Counter.builder("rule.execution.success")
                .description("规则执行成功次数")
                .tag("type", "success")
                .register(registry);
    }

    /**
     * 规则执行失败次数计数器
     */
    @Bean
    public Counter ruleExecutionFailureCounter(MeterRegistry registry) {
        return Counter.builder("rule.execution.failure")
                .description("规则执行失败次数")
                .tag("type", "failure")
                .register(registry);
    }

    /**
     * 规则匹配次数计数器
     */
    @Bean
    public Counter ruleMatchCounter(MeterRegistry registry) {
        return Counter.builder("rule.match.count")
                .description("规则匹配总次数")
                .tag("type", "match")
                .register(registry);
    }

    /**
     * 规则执行时间计时器
     */
    @Bean
    public Timer ruleExecutionTimer(MeterRegistry registry) {
        return Timer.builder("rule.execution.time")
                .description("规则执行耗时")
                .tag("type", "execution")
                .register(registry);
    }

    /**
     * 规则编译时间计时器
     */
    @Bean
    public Timer ruleCompileTimer(MeterRegistry registry) {
        return Timer.builder("rule.compile.time")
                .description("规则编译耗时")
                .tag("type", "compile")
                .register(registry);
    }

    /**
     * 规则加载次数计数器
     */
    @Bean
    public Counter ruleReloadCounter(MeterRegistry registry) {
        return Counter.builder("rule.reload.count")
                .description("规则重载次数")
                .tag("type", "reload")
                .register(registry);
    }

    /**
     * 缓存命中次数计数器
     */
    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("cache.hit.count")
                .description("缓存命中次数")
                .tag("type", "hit")
                .register(registry);
    }

    /**
     * 缓存未命中次数计数器
     */
    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("cache.miss.count")
                .description("缓存未命中次数")
                .tag("type", "miss")
                .register(registry);
    }
}
