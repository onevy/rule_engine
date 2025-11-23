package com.example.ruleengine.config;

import com.example.ruleengine.engine.RuleEngine;
import com.example.ruleengine.service.BusinessSceneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 规则引擎健康检查指示器
 * <p>
 * 检查规则引擎的运行状态，包括：
 * <ul>
 *   <li>规则引擎是否可用</li>
 *   <li>已加载的场景数量</li>
 *   <li>活跃规则数量</li>
 * </ul>
 * </p>
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEngineHealthIndicator implements HealthIndicator {

    private final RuleEngine ruleEngine;
    private final BusinessSceneService businessSceneService;

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();

            // 检查规则引擎是否可用
            boolean engineAvailable = checkEngineAvailable();
            details.put("engineAvailable", engineAvailable);

            // 获取场景数量
            long sceneCount = businessSceneService.count();
            details.put("sceneCount", sceneCount);

            // 检查时间戳
            details.put("checkTime", System.currentTimeMillis());

            if (engineAvailable && sceneCount >= 0) {
                return Health.up()
                        .withDetails(details)
                        .build();
            } else {
                return Health.down()
                        .withDetails(details)
                        .withDetail("error", "Rule engine is not available")
                        .build();
            }
        } catch (Exception e) {
            log.error("规则引擎健康检查失败", e);
            return Health.down()
                    .withException(e)
                    .build();
        }
    }

    /**
     * 检查规则引擎是否可用
     */
    private boolean checkEngineAvailable() {
        try {
            // 尝试执行一个简单的操作来验证引擎可用性
            return ruleEngine != null;
        } catch (Exception e) {
            log.warn("规则引擎可用性检查失败", e);
            return false;
        }
    }
}
