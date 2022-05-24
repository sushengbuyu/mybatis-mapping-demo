package sushengbuyu.maptodemo;

import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sushengbuyu.maptodemo.sys.service.SysUserService;

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
