package com.ywt.common.utils;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Description: spring el表达式解析
 * Author: ywt
 * Date: 2023-04-22
 */
public class SpElUtils {

    // SpEL 表达式解析器
    private static final ExpressionParser parser = new SpelExpressionParser();

    // 参数名称发现器，用于获取方法参数的名称
    private static final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 解析 SpEL 表达式并生成对应的字符串结果。
     *
     * @param method 目标方法对象
     * @param args 方法参数数组 ->参数对应的值
     * @param spEl 要解析的 SpEL 表达式
     * @return 解析后的字符串结果
     */
    public static String parseSpEl(Method method, Object[] args, String spEl) {
        // 解析方法参数名称 -> 方法名称数组
        String[] params = Optional.ofNullable(parameterNameDiscoverer.getParameterNames(method)).orElse(new String[]{});
        // 创建 SpEL 上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        // 将方法参数设置为 SpEL 上下文的变量
        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);
        }
        // 解析 SpEL 表达式并获取结果 "#uid" -> uid
        Expression expression = parser.parseExpression(spEl);
        return expression.getValue(context, String.class);
    }

    /**
     * 获取方法的唯一标识符，由类名和方法名组成。
     *
     * @param method 目标方法对象
     * @return 方法的唯一标识符
     */
    public static String getMethodKey(Method method) {
        // 将类名和方法名拼接为方法的唯一标识符
        return method.getDeclaringClass() + "#" + method.getName();
    }
}
