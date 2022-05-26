package sushengbuyu.maptodemo.sys.mapping;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import sushengbuyu.maptodemo.aop.AbstractDoMapAspect;
import sushengbuyu.maptodemo.aop.DoMap;
import sushengbuyu.maptodemo.aop.IDualMapper;

/**
 * @author victor
 * @desc 自定义关联映射切面
 * @date 2022/5/23
 */
@Component
@Aspect
public class DoMapAspect extends AbstractDoMapAspect {

    public DoMapAspect(IDualMapper dualMapper) {
        super(dualMapper);
    }

    @Override
    public String getMapScan() {
        return "sushengbuyu.maptodemo";
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
        obj = doMap(obj, doMap);
        return obj;
    }
}
