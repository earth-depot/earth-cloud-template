package com.cms.manage.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cms.common.core.domain.SysSearchPage;
import com.cms.common.log.annotation.Log;
import com.cms.common.log.enums.BusinessType;
import com.cms.common.tool.result.ResultUtil;
import com.cms.manage.entity.SysRoleEntity;
import com.cms.manage.service.SysRoleService;
import com.cms.manage.vo.SysRoleScope;
import com.cms.manage.vo.SysRoleMenuData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @date 2021/6/5 0:32
 */
@Api(tags = "角色管理API")
@RestController
@RequestMapping(value = "/role")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @ApiOperation(value = "分页查询角色列表")
    @RequestMapping(value = "/page",method = RequestMethod.GET)
    public ResultUtil<IPage<SysRoleEntity>> page(SysSearchPage request) {
        return sysRoleService.pageSearch(request);
    }

    @ApiOperation(value = "查询角色权限列表")
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public ResultUtil<List<SysRoleEntity>> list(SysRoleEntity request){
        return sysRoleService.queryList(request);
    }

    @Log(title = "编辑系统角色日志记录", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "添加系统角色")
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    public ResultUtil<SysRoleEntity> save(@RequestBody SysRoleEntity request) {
        return sysRoleService.saveRole(request);
    }

    @ApiOperation(value = "根据id查询角色")
    @RequestMapping(value = "/getById/{id}",method = RequestMethod.GET)
    public ResultUtil<SysRoleEntity> getById(@PathVariable Long id){
        return sysRoleService.getRoleById(id);
    }

    @Log(title = "删除系统角色日志记录", businessType = BusinessType.DELETE)
    @ApiOperation(value = "删除系统角色")
    @RequestMapping(value = "/delete/{id}",method = RequestMethod.DELETE)
    public ResultUtil<SysRoleEntity> delete(@PathVariable Long id){
        return sysRoleService.deleteRoleById(id);
    }

    @Log(title = "批量删除系统角色日志记录", businessType = BusinessType.DELETE)
    @ApiOperation(value = "批量删除角色")
    @RequestMapping(value = "/delete_bath",method = RequestMethod.DELETE)
    public ResultUtil<?> deleteBath(@RequestBody long[] ids) {
        return sysRoleService.deleteBath(ids);
    }

    @ApiOperation(value = "查询所有角色")
    @RequestMapping(value = "/findAll",method = RequestMethod.GET)
    public ResultUtil<List<SysRoleEntity>> findAll(){
        return sysRoleService.findAll();
    }

    @ApiOperation(value = "根据角色ID获取角色菜单权限信息")
    @RequestMapping(value = "/getTreeRoleMenuById/{id}",method = RequestMethod.GET)
    public ResultUtil<SysRoleEntity> getTreeRoleMenuById(@PathVariable Long id) {
        return sysRoleService.getTreeRoleMenuById(id);
    }

    @Log(title = "添加角色菜单权限日志记录", businessType = BusinessType.INSERT)
    @ApiOperation(value = "添加角色菜单权限")
    @RequestMapping(value = "/saveRoleMenu",method = RequestMethod.POST)
    public ResultUtil<?> saveRoleMenu(@RequestBody SysRoleMenuData sysRoleMenuData) {
        return sysRoleService.saveRoleMenu(sysRoleMenuData);
    }

    @Log(title = "添加角色数据权限日志记录", businessType = BusinessType.INSERT)
    @ApiOperation(value = "添加角色数据权限")
    @RequestMapping(value = "/saveRoleDataScope",method = RequestMethod.POST)
    public ResultUtil<?> saveRoleDataScope(@RequestBody SysRoleScope sysRoleScope) {
        return sysRoleService.saveRoleDataScope(sysRoleScope);
    }
}
