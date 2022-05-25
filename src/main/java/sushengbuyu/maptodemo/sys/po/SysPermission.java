package sushengbuyu.maptodemo.sys.po;

import java.util.StringJoiner;

/**
 * @author victor
 * @desc 说明
 * @date 2022/5/25
 */
public class SysPermission {

    private Long id;

    private String name;

    private Integer type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SysPermission.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("type=" + type)
                .toString();
    }
}
