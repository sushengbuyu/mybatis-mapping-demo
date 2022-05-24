# mybatis-mapping-demo
> mybatis 多表关联自定义注解实现
### 一、需求

目前使用的ORM框架是Mybatis Plus，是Mybatis的增强框架，基础的CRUD的方法都集成了，开发起来很是方便。但是项目中总是需要多表关联查询，而Mybatis的多表关联是需要在xml文件中配置对应的resultMap和关联标签，使用起来很不方便。JPA倒是有多表关联的注解实现，但是不想再引入另一个ORM框架。

目前的需求是增强现有的查询，使用简单的注解即可实现多表关联。

### 二、核心代码

GitHub[：https://github.com/sushengbuyu/mybatis-mapping-demo](https://github.com/sushengbuyu/mybatis-mapping-demo)

实现该功能总共需要四个文件

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2d00dea776fe46e889da7a7e56fd7911~tplv-k3u1fbpfcp-watermark.image?)

两个自定义注解，一个虚拟Mapper，一个切面处理类

> 源码

##### MapTo
> 自定义映射注解，标注需要映射处理的字段

```java
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
     * 嵌套处理
     * 为true时，如果映射的对象类中有映射字段，也执行映射操作
     * TODO
     */
    boolean doDeep() default false;
}
```

##### DoMap
> 自定义映射处理注解，标注需要执行映射的方法

```java
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
```
##### DualMapper
> 虚拟Mapper，用来执行自定义SQL

```java
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * @author victor
 * @desc 虚拟Mapper
 * @date 2022/5/23
 */
@Mapper
public interface DualMapper {

    /**
     * 执行自定义SQL
     * @param sql sql
     * @return List<Map<String, Object>>
     */
    @Select("${sql}")
    List<Map<String, Object>> executeSql(@Param("sql") String sql);
}
```
##### DoMapAspect
> 切面处理类，核心代码，执行映射操作
```java
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

    private final DualMapper dualMapper;

    public DoMapAspect(DualMapper dualMapper) {
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
            if (relObj instanceof Collection) {
                // 集合
                Collection<?> co = (Collection<?>) relObj;
                for (Object o : co) {
                    doMapping(c, o);
                }
            } else {
                // 单个对象
                doMapping(c, relObj);
            }
        } catch (Exception e) {
            log.error("映射异常", e);
        }
        log.info("返回对象：{}", obj);
        return obj;
    }

    private void doMapping(Class<?> c, Object obj) throws Exception {
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
                List<String> res = ReUtil.findAll("\$\{(.*?)}", sql, 0);
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
                                .map(r -> mapToBean(results.get(0), mapTo.targetClass()))
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
```
### 三、使用方法

> 测试类

##### SysUser
```java
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author victor
 * @desc 系统用户
 * @date 2022/5/17
 */
public class SysUser implements Serializable {
    private static final long serialVersionUID = 4855472141572371097L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickName;

    @MapTo(targetClass = SysRole.class, sql = "SELECT * FROM sys_role WHERE user_id=${id}")
    @TableField(exist = false)
    private SysRole sysRole;

    @MapTo(targetClass = SysRole.class, sql = "SELECT * FROM sys_role WHERE user_id=${id}")
    @TableField(exist = false)
    private List<SysRole> roleList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public SysRole getSysRole() {
        return sysRole;
    }

    public void setSysRole(SysRole sysRole) {
        this.sysRole = sysRole;
    }

    public List<SysRole> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<SysRole> roleList) {
        this.roleList = roleList;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SysUser.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("username='" + username + "'")
                .add("password='" + password + "'")
                .add("nickName='" + nickName + "'")
                .add("sysRole=" + sysRole)
                .add("roleList=" + roleList)
                .toString();
    }
}
```
##### SysRole
```java
import com.baomidou.mybatisplus.annotation.TableId;
import java.util.StringJoiner;

/**
 * @author victor
 * @desc 系统角色
 * @date 2022/5/23
 */
public class SysRole {

    @TableId
    private Long id;
    private Long userId;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SysRole.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("userId=" + userId)
                .add("name='" + name + "'")
                .toString();
    }
}
```
##### SysUserService
> 测试用例就常见的三种, 查单个，查列表，查分页
```java
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.io.Serializable;
import java.util.List;

/**
 * @author victor
 * @desc 说明
 * @date 2022/5/17
 */
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    @DoMap(targetClass = SysUser.class)
    @Override
    public SysUser getById(Serializable id) {
        return super.getById(id);
    }

    @DoMap(targetClass = SysUser.class)
    public List<SysUser> listAll() {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        return baseMapper.selectList(wrapper);
    }

    /**
     * 从Page中取records作为处理对象
     */
    @DoMap(targetClass = SysUser.class, spel = "records")
    public Page<SysUser> page() {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        Page<SysUser> p = new Page<>(1, 10);
        return baseMapper.selectPage(p, wrapper);
    }
}
```

##### DoMapTests
```java
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DoMapTests {

    @Autowired
    private SysUserService service;

    @Test
    void single() {
        System.out.println(JSONUtil.toJsonPrettyStr(service.getById(1)));
    }

    @Test
    void list() {
        System.out.println(JSONUtil.toJsonPrettyStr(service.listAll()));
    }

    @Test
    void page() {
        System.out.println(JSONUtil.toJsonPrettyStr(service.page()));
    }
}
```

##### 测试数据

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/558cc5cc380a463595bc0285f63ad360~tplv-k3u1fbpfcp-watermark.image?)

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/19d6b5fd0f1248fe8dbc751a207f0e53~tplv-k3u1fbpfcp-watermark.image?)

###### 测试结果
single

```json
{
    "nickName": "aa11",
    "roleList": [
        {
            "userId": 1,
            "name": "r1",
            "id": 11
        },
        {
            "userId": 1,
            "name": "r1",
            "id": 11
        }
    ],
    "password": "123456",
    "id": 1,
    "username": "a1"
}
```
list

```json
[
    {
        "nickName": "aa11",
        "roleList": [
            {
                "userId": 1,
                "name": "r1",
                "id": 11
            },
            {
                "userId": 1,
                "name": "r1",
                "id": 11
            }
        ],
        "password": "123456",
        "id": 1,
        "username": "a1"
    },
    {
        "sysRole": {
            "userId": 2,
            "name": "r3",
            "id": 13
        },
        "nickName": "aa22",
        "roleList": [
            {
                "userId": 2,
                "name": "r3",
                "id": 13
            }
        ],
        "password": "123456",
        "id": 2,
        "username": "a2"
    }
]
```
page

```json
{
    "optimizeCountSql": true,
    "records": [
        {
            "nickName": "aa11",
            "roleList": [
                {
                    "userId": 1,
                    "name": "r1",
                    "id": 11
                },
                {
                    "userId": 1,
                    "name": "r1",
                    "id": 11
                }
            ],
            "password": "123456",
            "id": 1,
            "username": "a1"
        },
        {
            "sysRole": {
                "userId": 2,
                "name": "r3",
                "id": 13
            },
            "nickName": "aa22",
            "roleList": [
                {
                    "userId": 2,
                    "name": "r3",
                    "id": 13
                }
            ],
            "password": "123456",
            "id": 2,
            "username": "a2"
        }
    ],
    "searchCount": true,
    "total": 0,
    "current": 1,
    "size": 10,
    "orders": [
    ]
}
```