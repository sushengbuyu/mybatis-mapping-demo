package sushengbuyu.maptodemo.sys.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import sushengbuyu.maptodemo.aop.MapTo;

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

    @MapTo(targetClass = SysRole.class
            , doDeep = true
            , sql = "SELECT * FROM sys_role WHERE user_id=${id}")
    @TableField(exist = false)
    private SysRole sysRole;

    @MapTo(targetClass = SysRole.class
            , doDeep = true
            , sql = "SELECT * FROM sys_role WHERE user_id=${id}")
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
