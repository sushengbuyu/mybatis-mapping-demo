package sushengbuyu.maptodemo.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author victor
 * @desc 标注该需要执行映射处理的方法
 * @date 2022/5/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DoMap {

    /**
     * 需要处理映射的类
     * @return Class
     */
    Class<?> targetClass();

    /**
     * spel表达式
     * 默认为空
     * @return String
     */
    String spel() default "";
}
