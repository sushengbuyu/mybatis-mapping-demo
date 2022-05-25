package sushengbuyu.maptodemo.aop;

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
