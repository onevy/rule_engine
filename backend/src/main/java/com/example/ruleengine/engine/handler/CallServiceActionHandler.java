package com.example.ruleengine.engine.handler;

import com.example.ruleengine.engine.ActionHandler;
import com.example.ruleengine.engine.RuleContext;
import com.example.ruleengine.engine.RuleResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 调用服务动作处理器
 */
@Slf4j
@Component
public class CallServiceActionHandler implements ActionHandler {

    public static final String ACTION_TYPE = "CALL_SERVICE";

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public String getActionType() {
        return ACTION_TYPE;
    }

    @Override
    public RuleResult.ActionResult execute(RuleContext context, RuleResult result, Map<String, Object> params) {
        try {
            if (params == null) {
                return new RuleResult.ActionResult(ACTION_TYPE, false, "参数不能为空");
            }

            String beanName = (String) params.get("beanName");
            String methodName = (String) params.get("methodName");
            Object[] args = (Object[]) params.get("args");

            if (beanName == null || methodName == null) {
                return new RuleResult.ActionResult(ACTION_TYPE, false, "beanName和methodName不能为空");
            }

            // 获取Spring Bean
            Object bean = applicationContext.getBean(beanName);

            // 调用方法
            Method method = findMethod(bean.getClass(), methodName, args);
            if (method == null) {
                return new RuleResult.ActionResult(ACTION_TYPE, false, "方法不存在: " + methodName);
            }

            Object serviceResult;
            if (args != null && args.length > 0) {
                serviceResult = method.invoke(bean, args);
            } else {
                serviceResult = method.invoke(bean);
            }

            log.info("调用服务成功: {}.{}", beanName, methodName);

            return new RuleResult.ActionResult(ACTION_TYPE, true, serviceResult);
        } catch (Exception e) {
            log.error("调用服务失败", e);
            return new RuleResult.ActionResult(ACTION_TYPE, false, e.getMessage());
        }
    }

    private Method findMethod(Class<?> clazz, String methodName, Object[] args) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                if (args == null || args.length == 0) {
                    if (method.getParameterCount() == 0) {
                        return method;
                    }
                } else if (method.getParameterCount() == args.length) {
                    return method;
                }
            }
        }
        return null;
    }
}
