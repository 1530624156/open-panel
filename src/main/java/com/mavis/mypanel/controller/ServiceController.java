package com.mavis.mypanel.controller;

import com.mavis.mypanel.entity.TService;
import com.mavis.mypanel.entity.TServiceGroup;
import com.mavis.mypanel.entity.TUnit;
import com.mavis.mypanel.entity.anno.Permission;
import com.mavis.mypanel.entity.enums.PermissionEnum;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.entity.vo.TServiceNodeVo;
import com.mavis.mypanel.logic.ServiceLogic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 服务模块控制层
 */
@RestController
@RequestMapping("service")
public class ServiceController {
    @Resource
    private ServiceLogic serviceLogic;


    @RequestMapping("getUnits")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn getUnits(TUnit unit) {
        return serviceLogic.getUnits(unit);
    }

    @RequestMapping("addUnit")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn addUnit(TUnit unit) {
        return serviceLogic.addUnit(unit);
    }
    @RequestMapping("updateUnit")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn updateUnit(TUnit unit) {
        return serviceLogic.updateUnit(unit);
    }
    @RequestMapping("deleteUnit")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn deleteUnit(Integer id) {
        return serviceLogic.deleteUnit(id);
    }

    /**
     * 获取租户名列表
     * @return
     */
    @RequestMapping("getUnitName")
    public JsonReturn getUnitName() {
        return serviceLogic.getUnitName();
    }

    //服务组管理
    @RequestMapping("getServiceGroup")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn getServiceGroup(TServiceGroup serviceGroup) {
        return serviceLogic.getServiceGroup(serviceGroup);
    }

    //获取服务列表

    @RequestMapping("addServiceGroup")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn addServiceGroup(TServiceGroup serviceGroup) {
        return serviceLogic.addServiceGroup(serviceGroup);
    }

    //修改服务组
    @RequestMapping("updateServiceGroup")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn updateServiceGroup(TServiceGroup serviceGroup) {
        return serviceLogic.updateServiceGroup(serviceGroup);
    }

    @RequestMapping("deleteServiceGroup")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn deleteServiceGroup(Integer id) {
        return serviceLogic.deleteServiceGroup(id);
    }

    @RequestMapping("getServices")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn getServices(TService service) {
        return serviceLogic.getServices(service);
    }

    @RequestMapping("addService")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn addService(TService service) {
        return serviceLogic.addService(service);
    }

    @RequestMapping("updateService")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn updateService(TService service) {
        return serviceLogic.updateService(service);
    }
    @RequestMapping("deleteService")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn deleteService(Integer id) {
        return serviceLogic.deleteService(id);
    }

    //节点操作
    @RequestMapping("getServiceNode")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn getServiceNode(TServiceNodeVo serviceNodeVo) {
        return serviceLogic.getServiceNode(serviceNodeVo);
    }

    @RequestMapping("deleteServiceNode")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn deleteServiceNode(Integer id) {
        return serviceLogic.deleteServiceNode(id);
    }

    @RequestMapping("killServiceNode")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn killServiceNode(Integer id) {
        return serviceLogic.killServiceNode(id);
    }

    @RequestMapping("startServiceNode")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn startServiceNode(Integer id) {
        return serviceLogic.startServiceNode(id);
    }

    @RequestMapping("restartServiceNode")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn restartServiceNode(Integer id) {
        return serviceLogic.restartServiceNode(id);
    }



    /**
     * 获取服务名称列表
     * @return
     */
    @RequestMapping("getServiceName")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn getServiceName() {
        return serviceLogic.getServiceName();
    }

    /**
     * 手动发布服务
     * @return
     */
    @RequestMapping("deployServiceNodeByHand")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn deployServiceNodeByHand(Integer serviceId,Integer dockerId,String portMap) {
        return serviceLogic.deployServiceNodeByHand(serviceId,dockerId,portMap);
    }

    /**
     * 自动发布服务
     * @param serviceId
     * @param deploySortFun 优先方式 1-容器优先 2-内存有限
     * @return
     */
    @RequestMapping("deployServiceNodeByAuto")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn deployServiceNodeByAuto(Integer serviceId,Integer deployNum,Integer deploySortFun) {
        return serviceLogic.deployServiceNodeByAuto(serviceId, deployNum,deploySortFun);
    }

    @RequestMapping("getServiceNodeLogByTail")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn getServiceNodeLogByTail(Integer id,Integer tail) {
        return serviceLogic.getServiceNodeLogByTail(id,tail);
    }

    @RequestMapping("getServiceAllNodeLogByTail")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn getServiceAllNodeLogByTail(Integer id,Integer tail) {
        return serviceLogic.getServiceAllNodeLogByTail(id,tail);
    }

    @RequestMapping("getImageAllTag")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn getImageAllTag(String imageName) {
        return serviceLogic.getImageAllTag(imageName);
    }


    /**
     * 将服务升级到某个版本
     * @param serviceId
     * @param tag
     * @return
     */
    @RequestMapping("upgradeServiceToTag")
    @Permission(permission = PermissionEnum.SYSTEM_SERVICE)
    public JsonReturn upgradeServiceToTag(Integer serviceId,String tag) {
        return serviceLogic.upgradeServiceToTag(serviceId,tag);
    }
}
