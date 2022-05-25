package sushengbuyu.maptodemo.aop;

import java.util.List;
import java.util.Map;

/**
 * @author victor
 * @desc 虚拟Mapper,具体实现由使用者实现
 * @date 2022/5/23
 */
public interface IDualMapper {

    List<Map<String, Object>> executeSql(String sql);
}
