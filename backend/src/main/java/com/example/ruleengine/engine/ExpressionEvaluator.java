package com.example.ruleengine.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 表达式计算器
 * <p>
 * 基于 Spring Expression Language (SpEL) 实现，支持：
 * <ul>
 *   <li>数学运算：+, -, *, /, %</li>
 *   <li>比较运算：==, !=, <, >, <=, >=</li>
 *   <li>逻辑运算：and, or, not</li>
 *   <li>聚合函数：SUM, AVG, MAX, MIN, COUNT</li>
 *   <li>数据访问：#data['fieldName'] 或 #data.fieldName</li>
 * </ul>
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 简单表达式
 * evaluate("#data['q1'] + #data['q2'] + #data['q3']", data)
 *
 * // 反向计分
 * evaluate("6 - #data['q2']", data)
 *
 * // 条件判断
 * evaluate("#data['totalScore'] >= 17 and #data['otherMax'] < 8", data)
 * </pre>
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class ExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 计算表达式
     *
     * @param expression 表达式字符串
     * @param context    数据上下文
     * @return 计算结果
     */
    public Object evaluate(String expression, Map<String, Object> context) {
        try {
            StandardEvaluationContext evalContext = new StandardEvaluationContext();
            evalContext.setVariable("data", context);

            // 注册聚合函数
            registerAggregateFunctions(evalContext, context);

            Expression exp = parser.parseExpression(expression);
            return exp.getValue(evalContext);
        } catch (Exception e) {
            log.error("表达式计算失败: expression={}, context={}", expression, context, e);
            return null;
        }
    }

    /**
     * 计算表达式并返回数值结果
     *
     * @param expression 表达式字符串
     * @param context    数据上下文
     * @return 数值结果，失败返回 0
     */
    public double evaluateAsNumber(String expression, Map<String, Object> context) {
        Object result = evaluate(expression, context);
        if (result instanceof Number) {
            return ((Number) result).doubleValue();
        }
        return 0.0;
    }

    /**
     * 计算表达式并返回布尔结果
     *
     * @param expression 表达式字符串
     * @param context    数据上下文
     * @return 布尔结果，失败返回 false
     */
    public boolean evaluateAsBoolean(String expression, Map<String, Object> context) {
        Object result = evaluate(expression, context);
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return false;
    }

    /**
     * 计算聚合函数
     *
     * @param function 聚合函数类型 (SUM, AVG, MAX, MIN, COUNT)
     * @param fields   字段列表
     * @param context  数据上下文
     * @return 聚合结果
     */
    public double evaluateAggregate(String function, List<String> fields, Map<String, Object> context) {
        if (fields == null || fields.isEmpty()) {
            return 0.0;
        }

        double result = 0.0;
        int count = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (String field : fields) {
            Object value = context.get(field);
            if (value instanceof Number) {
                double numValue = ((Number) value).doubleValue();
                count++;
                switch (function.toUpperCase()) {
                    case "SUM":
                    case "AVG":
                        result += numValue;
                        break;
                    case "MAX":
                        max = Math.max(max, numValue);
                        break;
                    case "MIN":
                        min = Math.min(min, numValue);
                        break;
                    case "COUNT":
                        // count already incremented
                        break;
                    default:
                        log.warn("不支持的聚合函数: {}", function);
                }
            }
        }

        switch (function.toUpperCase()) {
            case "SUM":
                return result;
            case "AVG":
                return count > 0 ? result / count : 0.0;
            case "MAX":
                return max == Double.MIN_VALUE ? 0.0 : max;
            case "MIN":
                return min == Double.MAX_VALUE ? 0.0 : min;
            case "COUNT":
                return count;
            default:
                return 0.0;
        }
    }

    /**
     * 验证表达式语法
     *
     * @param expression 表达式字符串
     * @return 验证结果，null表示通过，否则返回错误信息
     */
    public String validate(String expression) {
        try {
            parser.parseExpression(expression);
            return null;
        } catch (Exception e) {
            return "表达式语法错误: " + e.getMessage();
        }
    }

    /**
     * 注册聚合函数到上下文
     */
    private void registerAggregateFunctions(StandardEvaluationContext context, Map<String, Object> data) {
        try {
            // 注册 SUM 函数
            context.registerFunction("sum",
                    ExpressionEvaluator.class.getDeclaredMethod("sum", Object[].class));

            // 注册 AVG 函数
            context.registerFunction("avg",
                    ExpressionEvaluator.class.getDeclaredMethod("avg", Object[].class));

            // 注册 MAX 函数
            context.registerFunction("max",
                    ExpressionEvaluator.class.getDeclaredMethod("max", Object[].class));

            // 注册 MIN 函数
            context.registerFunction("min",
                    ExpressionEvaluator.class.getDeclaredMethod("min", Object[].class));

            // 注册 COUNT 函数
            context.registerFunction("count",
                    ExpressionEvaluator.class.getDeclaredMethod("count", Object[].class));

        } catch (NoSuchMethodException e) {
            log.error("注册聚合函数失败", e);
        }
    }

    /**
     * SUM 聚合函数
     */
    public static double sum(Object... values) {
        double result = 0.0;
        for (Object value : values) {
            if (value instanceof Number) {
                result += ((Number) value).doubleValue();
            }
        }
        return result;
    }

    /**
     * AVG 聚合函数
     */
    public static double avg(Object... values) {
        if (values.length == 0) {
            return 0.0;
        }
        return sum(values) / values.length;
    }

    /**
     * MAX 聚合函数
     */
    public static double max(Object... values) {
        double result = Double.MIN_VALUE;
        for (Object value : values) {
            if (value instanceof Number) {
                result = Math.max(result, ((Number) value).doubleValue());
            }
        }
        return result == Double.MIN_VALUE ? 0.0 : result;
    }

    /**
     * MIN 聚合函数
     */
    public static double min(Object... values) {
        double result = Double.MAX_VALUE;
        for (Object value : values) {
            if (value instanceof Number) {
                result = Math.min(result, ((Number) value).doubleValue());
            }
        }
        return result == Double.MAX_VALUE ? 0.0 : result;
    }

    /**
     * COUNT 聚合函数
     */
    public static int count(Object... values) {
        int result = 0;
        for (Object value : values) {
            if (value != null) {
                result++;
            }
        }
        return result;
    }
}
