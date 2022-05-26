package sushengbuyu.maptodemo.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sushengbuyu.maptodemo.aop.DoMap;
import sushengbuyu.maptodemo.sys.mapper.SysUserMapper;
import sushengbuyu.maptodemo.sys.po.SysUser;

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
    @DoMap(targetClass = SysUser.class, spel = "#records")
    public Page<SysUser> page() {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        Page<SysUser> p = new Page<>(1, 10);
        return baseMapper.selectPage(p, wrapper);
    }

    public SysUser getByUsernameAndPassword(String username, String password) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getPassword, password);
        return baseMapper.selectOne(wrapper);
    }
}
