package sushengbuyu.maptodemo.sys.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import sushengbuyu.maptodemo.aop.MapTo;

import java.util.List;
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

    @MapTo(targetClass = SysPermission.class
            , sql = "SELECT p.* FROM sys_permission p " +
            "LEFT JOIN sys_role_permission rp ON p.id = rp.perm_id " +
            "WHERE rp.role_id = ${id}")
    @TableField(exist = false)
    private List<SysPermission> permissionList;

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

    public List<SysPermission> getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(List<SysPermission> permissionList) {
        this.permissionList = permissionList;
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
