package sushengbuyu.maptodemo.aop;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author victor
 * @desc 自定义关联映射切面
 * @date 2022/5/23
 */
@Component
@Aspect
public class DoMapAspect {

    private final static Logger log = LoggerFactory.getLogger(DoMapAspect.class);
    /**
     * 保存MapTo映射关系
     * key 映射字段所在类
     * value 映射字段集合
     */
    private static final Map<Class<?>, Set<String>> MAPPING = new HashMap<>(8);

    private final IDualMapper dualMapper;

    public DoMapAspect(IDualMapper dualMapper) {
        this.dualMapper = dualMapper;
    }

    /**
     * 初始化映射关系
     * 扫描指定包下所有类，找出带有MapTo注解的字段
     * 存储映射数据
     */
    @PostConstruct
    public void initMap() {
        // 初始化所有MapTo对象
        // 扫描所有类
        Set<Class<?>> classes = ClassUtil.scanPackage("sushengbuyu.maptodemo");
        int totalField = 0;
        // 找出使用MapTo注解的对象
        for (Class<?> c : classes) {
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                if (null != f.getAnnotation(MapTo.class)){
                    log.info("找到需要映射的字段: 类名：{} - 字段名：{}", c.getName(), f.getName());
                    // 保存映射关系
                    Set<String> set;
                    if (MAPPING.containsKey(c)) {
                        set = MAPPING.get(c);
                    } else {
                        set = new HashSet<>();
                    }
                    set.add(f.getName());
                    MAPPING.put(c, set);
                    totalField++;
                }
            }
        }
        log.info("总计{}个映射类，{}个映射字段", MAPPING.size(), totalField);
    }

    /**
     * 切点
     * @param doMap 执行映射注解
     */
    @Pointcut("@annotation(doMap)")
    public void point(DoMap doMap){}

    /**
     * 处理关联映射
     * @param point 切点
     * @param doMap 映射处理配置
     * @return Object
     * @throws Throwable 异常
     */
    @Around(value = "@annotation(doMap)")
    public Object doMap(ProceedingJoinPoint point, DoMap doMap) throws Throwable {
        // 执行切面方法
        Object obj = point.proceed();
        try {
            Object relObj = obj;
            if (StringUtils.hasLength(doMap.spel())) {
                // 如果使用了SPEL表达式，则从返回值中获取处理对象
                ExpressionParser parser = new SpelExpressionParser();
                Expression expression = parser.parseExpression(doMap.spel());
                relObj = expression.getValue(obj);
            }
            // 获取映射类
            Class<?> c = doMap.targetClass();
            // 映射处理
            doMapping(c, relObj);
        } catch (Exception e) {
            log.error("映射异常", e);
        }
        log.info("返回对象：{}", obj);
        return obj;
    }

    private void doMapping(Class<?> c, Object obj) throws Exception {
        if (obj instanceof Collection) {
            // 集合
            Collection<?> co = (Collection<?>) obj;
            for (Object o : co) {
                mapping(c, o);
            }
        } else {
            // 单个对象
            mapping(c, obj);
        }
    }

    private void mapping(Class<?> c, Object obj) throws Exception {
        // 判断是否有映射关系
        if (MAPPING.containsKey(c)) {
            log.info("处理映射类：{}", c.getName());
            // 从缓存中获取映射字段名称
            Set<String> filedNames = MAPPING.get(c);
            for (String fieldName : filedNames) {
                Field f = c.getDeclaredField(fieldName);
                log.info("处理映射字段：{}", f.getName());
                // 获取映射注解
                MapTo mapTo = f.getAnnotation(MapTo.class);
                log.info("映射配置：{}", mapTo);
                // 设置私有字段访问权限
                f.setAccessible(true);
                // 执行SQL
                String sql = mapTo.sql();
                // 处理SQL变量
                List<String> res = ReUtil.findAll("\\$\\{(.*?)}", sql, 0);
                log.info("SQL变量：{}", res);
                for (String re : res) {
                    Field ff = obj.getClass().getDeclaredField(re.substring(2, re.length()-1));
                    ff.setAccessible(true);
                    Object o = ff.get(obj);
                    sql = sql.replace(re, o.toString());
                }
                log.info("最终SQL：{}", sql);
                List<Map<String, Object>> results = dualMapper.executeSql(sql);
                Object v = null;
                if (Collection.class.isAssignableFrom(f.getType())) {
                    // 集合对象
                    if (results.size() > 0) {
                        v = results.stream()
                                .map(r -> mapToBean(r, mapTo.targetClass()))
                                .collect(Collectors.toList());
                    }
                } else {
                    // 单个对象
                    if (results.size() > 1) {
                        log.error("预计返回一条数据，实际返回多条数据。执行SQL: {}", sql);
                    } else if (results.size() == 1) {
                        // 转换结果，赋值
                        v = mapToBean(results.get(0), mapTo.targetClass());
                    }
                }
                if (v != null && mapTo.doDeep()) {
                    doMapping(mapTo.targetClass(), v);
                }
                f.set(obj, v);
            }
        }
    }

    /**
     * Map对象转Bean
     * @param map map
     * @param clazz bean
     * @return bean
     */
    private Object mapToBean(Map<?, ?> map, Class<?> clazz) {
        try {
            return BeanUtil.fillBeanWithMap(map, clazz.newInstance(), true);
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("实例化异常", e);
            return null;
        }
    }
}
