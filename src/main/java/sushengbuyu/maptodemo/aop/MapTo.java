package sushengbuyu.maptodemo.aop;

import java.lang.annotation.*;

/**
 * @author victor
 * @desc 自定义多表关联映射注解
 * @date 2022/5/23
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapTo {
    /**
     * 映射目标
     */
    Class<?> targetClass();

    /**
     * 执行SQL
     */
    String sql();

    /**
     * 为true时，如果映射的对象类中有映射字段，也执行映射操作
     */
    boolean doDeep() default false;
}
