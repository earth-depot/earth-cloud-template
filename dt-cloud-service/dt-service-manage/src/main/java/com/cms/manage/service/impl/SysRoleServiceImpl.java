package com.cms.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.common.core.domain.SysSearchPage;
import com.cms.common.jdbc.config.IdGenerator;
import com.cms.common.tool.result.ResultUtil;
import com.cms.manage.entity.SysDepartmentEntity;
import com.cms.manage.entity.SysMenuEntity;
import com.cms.manage.entity.SysRoleDeptEntity;
import com.cms.manage.entity.SysRoleEntity;
import com.cms.manage.entity.SysRoleMenuEntity;
import com.cms.manage.mapper.SysRoleMapper;
import com.cms.manage.service.SysDeptService;
import com.cms.manage.service.SysRoleService;
import com.cms.manage.vo.SysRoleMenuData;
import com.cms.manage.vo.SysRoleScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.cms.common.tool.constant.ConstantCode.DATA_SCOPE_CUSTOM;

/**
 * 系统角色服务实现类
 * @date 2021/6/2 22:23
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

    @Autowired
    private SysDeptService sysDeptService;

    @Override
    public ResultUtil<IPage<SysRoleEntity>> pageSearch(SysSearchPage request) {
        Page<SysRoleEntity> page = new Page<>(request.getCurrent(),request.getSize());
        IPage<SysRoleEntity> list = this.baseMapper.pageSearch(page,request);
        if(!CollectionUtils.isEmpty(list.getRecords())) {
            list.getRecords().forEach(role -> {
                List<Long> longs = this.baseMapper.selectRoleDeptList(role.getId());
                List<Long> deptIds = new ArrayList<>();
                if(!longs.isEmpty()) {
                    // 过滤父节点id,前端显示需要
                    for (Long id : longs) {
                        // 子节点，加入集合
                        long count = sysDeptService.count(new QueryWrapper<SysDepartmentEntity>().eq("parent_id", id));
                        if (count == 0L) {
                            deptIds.add(id);
                        }
                    }
                }
                role.setDeptIds(deptIds);
            });
        }
        return ResultUtil.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultUtil<List<SysRoleEntity>> queryList(SysRoleEntity request) {
        List<SysRoleEntity> responseList = new ArrayList<>();
        QueryWrapper<SysRoleEntity> wrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(request.getName())) {
            wrapper.like("name",request.getName());
        }
        if(null != request.getStartTime() && null != request.getEndTime()) {
            wrapper.between("create_time"
                    ,DateFormatUtils.format(request.getStartTime(),"yyyy-MM-dd HH:mm:ss")
                    ,DateFormatUtils.format(request.getEndTime(),"yyyy-MM-dd HH:mm:ss"));
        }
        List<SysRoleEntity> roleEntityList = this.baseMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(roleEntityList)) {
            roleEntityList.forEach(it -> {
                // 根据角色ID查询角色权限信息
                List<SysMenuEntity> menuEntityList = this.baseMapper.getMenuListByRoleId(it.getId());
                List<SysMenuEntity> childrenList = buildTree(menuEntityList, "0");
                it.setChildren(childrenList);
                responseList.add(it);
            });
        }
        return ResultUtil.success(responseList);
    }

    public static List<SysMenuEntity> buildTree(List<SysMenuEntity> list, String pid){
        List<SysMenuEntity> children = list.stream().filter(x -> x.getChildren() != null && x.getParentId().equals(pid)).collect(Collectors.toList());
        List<SysMenuEntity> subclass = list.stream().filter(x -> x.getChildren() != null && !x.getParentId().equals(pid)).collect(Collectors.toList());
        if(children.size() > 0) {
            children.forEach(x -> {
                if(subclass.size() > 0) {
                    buildTree(subclass,x.getId()).forEach(y -> x.getChildren().add(y));
                }
            });
        }
        return children;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SysRoleEntity> saveRole(SysRoleEntity request) {
        if (null != request.getId()) {
            this.baseMapper.updateById(request);
            return ResultUtil.success();
        }
        this.baseMapper.insert(request);
        return ResultUtil.success();
    }

    @Override
    @Transactional(readOnly = true)
    public ResultUtil<SysRoleEntity> getRoleById(Long id) {
        SysRoleEntity entity = this.baseMapper.selectById(id);
        return ResultUtil.success(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SysRoleEntity> deleteRoleById(Long id) {
        this.deleteRoleRelevantTable(id);
        int delete = this.baseMapper.deleteById(id);
        return delete > 0 ? ResultUtil.success() : ResultUtil.error();
    }

    @Override
    @Transactional(readOnly = true)
    public ResultUtil<List<SysRoleEntity>> findAll() {
        List<SysRoleEntity> entityList = this.baseMapper.selectList(null);
        return ResultUtil.success(entityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<?> deleteBath(long[] ids) {
        // 查询当前是否有操作员
        List<Long> resultIds = new ArrayList<>();
        for (long id : ids) {
            Long count = this.baseMapper.selectOperotarRoleByRoleId(id);
            if(count <= 0) {
                resultIds.add(id);
            }
        }
        if(!CollectionUtils.isEmpty(resultIds)) {
            for (Long id : resultIds) {
                this.deleteRoleRelevantTable(id);
            }
        }
        // 批量删除角色
        this.baseMapper.deleteBath(resultIds);
        return ResultUtil.success();
    }

    /**
     * 删除角色相关数据表
     * @param id 角色ID
     */
    private void deleteRoleRelevantTable(Long id) {
        // 删除角色权限中间表
        List<Long> collectIds = this.baseMapper.listRoleMenuByRoleId(id);
        if(!CollectionUtils.isEmpty(collectIds)) {
            this.baseMapper.deleteRoleMenuByIds(collectIds);
        }
        // 删除角色数据权限中间表
        List<Long> longs = this.baseMapper.selectRoleDataScopeList(id);
        if(!CollectionUtils.isEmpty(longs)) {
            this.baseMapper.deleteRoleDataScopeByIds(longs);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResultUtil<SysRoleEntity> getTreeRoleMenuById(Long id) {
        SysRoleEntity sysRoleEntity = this.baseMapper.selectById(id);
        // 根据角色ID查询角色权限信息
        List<SysMenuEntity> menuEntityList = this.baseMapper.getMenuListByRoleId(id);
        List<SysMenuEntity> childrenList = buildTree(menuEntityList, "0");
        sysRoleEntity.setChildren(childrenList);
        return ResultUtil.success(sysRoleEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<?> saveRoleMenu(SysRoleMenuData sysRoleMenuData) {
        List<SysRoleMenuEntity> sysRoleMenuEntityList = this.baseMapper.selectRoleMenuList(sysRoleMenuData.getRoleId());
        if(!CollectionUtils.isEmpty(sysRoleMenuEntityList)) {
            // 删除旧数据
            List<Long> ids = sysRoleMenuEntityList.stream().map(SysRoleMenuEntity::getId).collect(Collectors.toList());
            this.baseMapper.deleteBathRoleMenu(ids);
        }
        // 新增角色权限信息
        sysRoleMenuData.getMenuIds().forEach(id -> {
            SysRoleMenuEntity roleMenuEntity = new SysRoleMenuEntity();
            roleMenuEntity.setId(IdGenerator.generateId());
            roleMenuEntity.setRoleId(sysRoleMenuData.getRoleId());
            roleMenuEntity.setMenuId(id);
            this.baseMapper.insertRoleMenu(roleMenuEntity);
        });
        return ResultUtil.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<?> saveRoleDataScope(SysRoleScope sysRoleScope) {
        // 删除旧数据
        List<Long> ids = this.baseMapper.selectRoleDataScopeList(sysRoleScope.getRoleId());
        if(!CollectionUtils.isEmpty(ids)) {
            this.baseMapper.deleteRoleDataScopeByIds(ids);
        }
        // 添加角色的数据权限
        if (!CollectionUtils.isEmpty(sysRoleScope.getDeptIds()) && sysRoleScope.getDataScope().equals(DATA_SCOPE_CUSTOM)) {
            sysRoleScope.getDeptIds().forEach(deptId -> {
                SysRoleDeptEntity roleDeptEntity = new SysRoleDeptEntity();
                roleDeptEntity.setId(IdGenerator.generateId());
                roleDeptEntity.setRoleId(sysRoleScope.getRoleId());
                roleDeptEntity.setDeptId(Long.valueOf(deptId));
                this.baseMapper.insertRoleDept(roleDeptEntity);
            });
        }
        // 更新角色信息
        SysRoleEntity sysRoleEntity = this.baseMapper.selectById(sysRoleScope.getRoleId());
        sysRoleEntity.setDataScope(sysRoleScope.getDataScope());
        this.baseMapper.updateById(sysRoleEntity);
        return ResultUtil.success();
    }
}
