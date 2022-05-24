package sushengbuyu.maptodemo.sys.po;

import com.baomidou.mybatisplus.annotation.TableId;

import java.util.StringJoiner;

/**
 * @author victor
 * @desc 说明
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
