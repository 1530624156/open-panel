package com.mavis.mypanel.logic;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.mavis.mypanel.entity.*;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.entity.vo.TServiceNodeNumVo;
import com.mavis.mypanel.entity.vo.TServiceNodeVo;
import com.mavis.mypanel.entity.vo.TServiceVo;
import com.mavis.mypanel.service.TServiceGroupService;
import com.mavis.mypanel.service.TServiceNodeService;
import com.mavis.mypanel.service.TServiceService;
import com.mavis.mypanel.service.TUnitService;
import com.mavis.mypanel.util.MyDockerUtil;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 服务模块逻辑层
 */
@Component
public class ServiceLogic {

    @Resource
    private TUnitService unitService;

    @Resource
    private TServiceService serviceService;

    @Resource
    private TServiceGroupService serviceGroupService;

    @Resource
    private TServiceNodeService serviceNodeService;

    @Resource
    private ServerDockerLogic serverDockerLogic;

    @Resource
    private RegistryLogic registryLogic;

    public JsonReturn getUnits(TUnit unit) {
        LambdaQueryChainWrapper<TUnit> qw = unitService.lambdaQuery();
        if(StringUtils.isNotBlank(unit.getName())){
            qw.like(TUnit::getName, unit.getName());
        }
        if(StringUtils.isNotBlank(unit.getAliasName())){
            qw.like(TUnit::getAliasName, unit.getAliasName());
        }
        if(StringUtils.isNotBlank(unit.getTag())){
            qw.like(TUnit::getTag, unit.getTag());
        }
        if(StringUtils.isNotBlank(unit.getProvince())){
            qw.eq(TUnit::getProvince, unit.getProvince());
        }
        if(unit.getType() != null){
            qw.eq(TUnit::getType, unit.getType());
        }
        List<TUnit> units = qw.list();
        return JsonReturn.success(units);
    }

    public JsonReturn addUnit(TUnit unit) {
        Long c = unitService.lambdaQuery().eq(TUnit::getName, unit.getName()).count();
        if(c > 0){
            return JsonReturn.errorMsg("该单位已存在");
        }
        return unitService.save(unit) ? JsonReturn.successMsg("添加成功") : JsonReturn.errorMsg("添加失败");
    }

    public JsonReturn updateUnit(TUnit unit) {
        return unitService.updateById(unit) ? JsonReturn.successMsg("修改成功") : JsonReturn.errorMsg("修改失败");
    }

    public JsonReturn deleteUnit(Integer id) {
        return unitService.removeById(id) ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    public JsonReturn getServices(TService service) {
        List<TServiceVo> tServiceVos = serviceService.selectServiceVoList(service.getName(), service.getAlias(),service.getUnitId(), service.getGroupId());
        //获取各服务节点数量
        List<TServiceNodeNumVo> serviceNodeNum = serviceNodeService.getServiceNodeNum();
        Map<Integer, Integer> serviceNodeNumMap = serviceNodeNum.stream().collect(Collectors.toMap(TServiceNodeNumVo::getServiceId, TServiceNodeNumVo::getNum));
        tServiceVos.forEach(tServiceVo -> {
            Integer num = serviceNodeNumMap.get(tServiceVo.getId());
            tServiceVo.setNodeNum(num == null ? 0 : num);
        });

        return JsonReturn.success(tServiceVos);
    }

    public JsonReturn addService(TService service) {
        //判断web服务端口是否冲突
        LambdaQueryChainWrapper<TService> qw = serviceService.lambdaQuery();
        Long c = qw.eq(TService::getName, service.getName()).count();
        if(c > 0){
            return JsonReturn.errorMsg("该服务名称已存在");
        }
        return serviceService.save(service) ? JsonReturn.successMsg("添加成功") : JsonReturn.errorMsg("添加失败");
    }

    public JsonReturn getServiceGroup(TServiceGroup serviceGroup) {
        LambdaQueryChainWrapper<TServiceGroup> qw = serviceGroupService.lambdaQuery();
        if(StringUtils.isNotBlank(serviceGroup.getName())){
            qw.like(TServiceGroup::getName, serviceGroup.getName());
        }
        if(StringUtils.isNotBlank(serviceGroup.getAlias())){
            qw.like(TServiceGroup::getAlias, serviceGroup.getAlias());
        }
        if(StringUtils.isNotBlank(serviceGroup.getRemark())){
            qw.like(TServiceGroup::getRemark, serviceGroup.getRemark());
        }
        List<TServiceGroup> list = qw.list();
        return JsonReturn.success(list);
    }

    public JsonReturn addServiceGroup(TServiceGroup serviceGroup) {
        LambdaQueryChainWrapper<TServiceGroup> qw = serviceGroupService.lambdaQuery();
        Long c = qw.eq(TServiceGroup::getName, serviceGroup.getName()).count();
        if(c > 0){
            return JsonReturn.errorMsg("该服务组名称已存在");
        }
        return serviceGroupService.save(serviceGroup) ? JsonReturn.successMsg("添加成功") : JsonReturn.errorMsg("添加失败");
    }

    //修改服务组
    public JsonReturn updateServiceGroup(TServiceGroup serviceGroup) {
        boolean f = serviceGroupService.updateById(serviceGroup);
        return f ? JsonReturn.successMsg("修改成功") : JsonReturn.errorMsg("修改失败");
    }

    /**
     * 删除服务组
     * @param id
     * @return
     */
    public JsonReturn deleteServiceGroup(Integer id) {
        return serviceGroupService.removeById(id) ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    public JsonReturn deleteService(Integer id) {
        //判断服务有没有节点
        LambdaQueryChainWrapper<TServiceNode> qw = serviceNodeService.lambdaQuery();
        Long c = qw.eq(TServiceNode::getServiceId, id).count();
        if(c > 0){
            return JsonReturn.errorMsg("该服务下有节点，不能删除");
        }
        return serviceService.removeById(id) ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    /**
     * 修改服务
     * @param service
     * @return
     */
    public JsonReturn updateService(TService service) {
        return serviceService.updateById(service) ? JsonReturn.successMsg("修改成功") : JsonReturn.errorMsg("修改失败");
    }

    /**
     * 获取service节点
     * @return
     */
    public JsonReturn getServiceNode(TServiceNodeVo serviceNodeVo) {
        List<TServiceNodeVo> serviceNodeVos = serviceNodeService.getServiceNodeVo(serviceNodeVo.getUnitName(),serviceNodeVo.getServiceGroupId(),serviceNodeVo.getServiceAlias());
        //获取每个服务节点的状态
        List<Integer> dockerIds = serviceNodeVos.stream().map(TServiceNodeVo::getDockerId).collect(Collectors.toList());
        List<TServerDocker> dockerNodes = serverDockerLogic.getByIds(dockerIds);
        Map<Integer, TServerDocker> dockerId_dockerNode_map = dockerNodes.stream().collect(Collectors.toMap(TServerDocker::getId, Function.identity()));
        for (TServiceNodeVo serviceNode : serviceNodeVos) {
            //获取这个服务节点，是哪个docker节点的
            TServerDocker tServerDocker = dockerId_dockerNode_map.get(serviceNode.getDockerId());
            DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(tServerDocker);

        }


        return JsonReturn.success(serviceNodeVos);
    }

    public JsonReturn getServiceName() {
        LambdaQueryChainWrapper<TService> qw = serviceService.lambdaQuery().select(TService::getName);
        List<TService> list = qw.list();
        return JsonReturn.success(list);
    }

    public JsonReturn getUnitName() {
        List<TUnit> units = unitService.lambdaQuery().select(TUnit::getName).list();
        return JsonReturn.success(units);
    }

    /**
     * 手动发布服务节点
     * @param portMap {"外","内"}
     * @return
     */
    public JsonReturn deployServiceNodeByHand(Integer serviceId,Integer dockerId,String portMap) {
        //前期准备========================================
        //获取服务
        TService service = serviceService.getById(serviceId);
        if(service == null){
            return JsonReturn.errorMsg("未找到该服务");
        }
        //获取发布docker节点
        TServerDocker dockerNode = serverDockerLogic.getById(dockerId);
        if(dockerNode == null){
            return JsonReturn.errorMsg("未找到该docker节点");
        }
        //判断docker节点状态
        if(dockerNode.getStatus() != 1){
            return JsonReturn.errorMsg("docker节点状态异常");
        }
        //判断仓库存不存在
        String imageName = service.getImageName();
        String registry_url = imageName.split("/")[0];
        TRegistry registry = registryLogic.getByHost(registry_url);
        if(registry == null){
            return JsonReturn.errorMsg(String.format("未找到该仓库 %s", registry_url));
        }

        //开始发布========================================
        DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(dockerNode);
        //拉取镜像
        boolean f = MyDockerUtil.pullImage(config, service.getImageName(), registry.getUsername(), registry.getPassword());
        if(!f){
            return JsonReturn.errorMsg("拉取镜像失败");
        }
        //随机容器名
        String uuid = UUID.randomUUID().toString();
        String containerName = service.getAlias()+"_"+uuid.substring(uuid.length()-8);
        //端口映射
        JSONObject portMap_json = JSON.parseObject(portMap);
        HashMap<Integer, Integer> portMapObj = new HashMap<>();
        for (String in_port : portMap_json.keySet()) {
            portMapObj.put(Integer.parseInt(in_port), Integer.parseInt(portMap_json.getString(in_port)));
        }
        //挂载映射
        HashMap<String, String> mountMap = new HashMap<>();
        if(StringUtils.isNotBlank(service.getMountMap())){
            JSONArray mount_list = JSON.parseArray(service.getMountMap());
            for (Object o : mount_list) {
                JSONObject jsonObject = (JSONObject) o;
                mountMap.put(jsonObject.getString("hostPath"),jsonObject.getString("containerPath"));
            }
        }
        //创建镜像
        String containerId = MyDockerUtil.addDockerContainerWithoutStart(config, containerName, service.getImageName(), portMapObj,mountMap, service.getMaxCpu());
        if(containerId == null){
            return JsonReturn.errorMsg("创建容器失败");
        }

        //记录数据库
        TServiceNode tServiceNode = new TServiceNode();
        tServiceNode.setContainerId(containerId);
        tServiceNode.setContainerName(containerName);
        tServiceNode.setDockerId(dockerId);
        tServiceNode.setServiceId(serviceId);
        tServiceNode.setPortMap(portMap);
        tServiceNode.setPortWeb(service.getPortWeb());
        tServiceNode.setMaxCpu(service.getMaxCpu());
        //计算实际外部端口
        Map<Integer, Integer> port_map_temp = portMapObj.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        tServiceNode.setPortWebOut(port_map_temp.get(service.getPortWeb()));
        if(serviceNodeService.save(tServiceNode)){
            return JsonReturn.successMsg("发布成功");
        }
        return JsonReturn.errorMsg("发布失败");
    }

    public JsonReturn deleteServiceNode(Integer id) {
        //查询这个服务节点，是哪个docker节点的
        TServiceNode serviceNode = serviceNodeService.getById(id);
        JsonReturn jr = deleteServiceNode(serviceNode);
        if(!jr.isSuccess()){
            return jr;
        }
        return serviceNodeService.removeById(id) ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    public JsonReturn deleteServiceNode(TServiceNode serviceNode) {
        if(serviceNode == null){
            return JsonReturn.errorMsg("未找到该服务节点");
        }
        TServerDocker dockerNode = serverDockerLogic.getById(serviceNode.getDockerId());
        //先移除容器
        DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(dockerNode);
        boolean f = MyDockerUtil.removeDockerContainerById(config, serviceNode.getContainerId());
        if(!f){
            return JsonReturn.errorMsg("移除容器失败");
        }
        return JsonReturn.successMsg("移除容器成功");

    }

    /**
     * 停止服务节点
     * @param id
     * @return
     */
    public JsonReturn killServiceNode(Integer id) {
        //查询这个服务节点，是哪个docker节点的
        TServiceNode serviceNode = serviceNodeService.getById(id);
        return killServiceNode(serviceNode);
    }

    public JsonReturn killServiceNode(TServiceNode serviceNode) {
        if(serviceNode == null){
            return JsonReturn.errorMsg("未找到该服务节点");
        }
        TServerDocker dockerNode = serverDockerLogic.getById(serviceNode.getDockerId());
        DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(dockerNode);
        boolean f = MyDockerUtil.killDockerContainerById(config, serviceNode.getContainerId());
        if(!f){
            return JsonReturn.errorMsg("停止容器失败");
        }
        return JsonReturn.successMsg("停止成功");
    }

    public JsonReturn startServiceNode(Integer id) {
        //查询这个服务节点，是哪个docker节点的
        TServiceNode serviceNode = serviceNodeService.getById(id);
        return startServiceNode(serviceNode);
    }

    public JsonReturn startServiceNode(TServiceNode serviceNode) {
        if(serviceNode == null){
            return JsonReturn.errorMsg("未找到该服务节点");
        }
        TServerDocker dockerNode = serverDockerLogic.getById(serviceNode.getDockerId());
        DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(dockerNode);
        boolean f = MyDockerUtil.startDockerContainerById(config, serviceNode.getContainerId());
        if(!f){
            return JsonReturn.errorMsg("启动容器失败");
        }
        return JsonReturn.successMsg("启动成功");
    }

    public JsonReturn restartServiceNode(Integer id) {
        //查询这个服务节点，是哪个docker节点的
        TServiceNode serviceNode = serviceNodeService.getById(id);
        if(serviceNode == null){
            return JsonReturn.errorMsg("未找到该服务节点");
        }
        TServerDocker dockerNode = serverDockerLogic.getById(serviceNode.getDockerId());
        DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(dockerNode);
        boolean f = MyDockerUtil.restartDockerContainerById(config, serviceNode.getContainerId());
        if(f){
            return JsonReturn.successMsg("重启成功");
        }
        return JsonReturn.errorMsg("重启失败");
    }

    /**
     * 自动发布服务
     * @param serviceId
     * @param deploySortFun 优先方式 1-容器优先 2-内存有限
     * @return
     */

    public JsonReturn deployServiceNodeByAuto(Integer serviceId ,Integer deployNum, Integer deploySortFun) {
        //先刷新一下容器统计情况
        serverDockerLogic.spanDockerNodeInfo();
        //判断节点分配方式
        if(deploySortFun == 1){
            //容器数量优先
            HashMap<Integer, Integer> serviceDockerNumMap = calcServiceDockerNumMap(deployNum);
            Integer c = 0;
            for (Integer dockerNodeId : serviceDockerNumMap.keySet()) {
                //各个服务器创建对应数量的容器
                for (int i = 0; i < serviceDockerNumMap.get(dockerNodeId); i++) {
                    Boolean f = deployService2DockerNodeByAuto(serviceId, dockerNodeId);
                    if(f){
                        c++;
                    }
                }
            }
            if(c == 0){
                return JsonReturn.errorMsg("发布失败");
            }else if(c == deployNum){
                return JsonReturn.successMsg("发布成功");
            }else {
                return JsonReturn.successMsg(String.format("发布成功，部分节点发布失败，成功数:%s", c));
            }
        }
        return JsonReturn.errorMsg("发布失败");
    }

    /**
     * 将某个服务部署到某个docker节点上，自动分配端口
     * @param serviceId
     * @param dockerNodeId
     */
    private Boolean deployService2DockerNodeByAuto(Integer serviceId, Integer dockerNodeId) {
        TService service = serviceService.getById(serviceId);
        if(service == null){
            return false;
        }
        TServerDocker dockerNode = serverDockerLogic.getById(dockerNodeId);
        if(dockerNode == null){
            return false;
        }
        //创建端口绑定关系
        HashMap<String, String> portMap = new HashMap<>();
        if(StringUtils.isNotBlank(service.getPortList())){
            String[] ports = service.getPortList().split(",");
            ArrayList<String> usedPort = (ArrayList<String>) serverDockerLogic.getDockerNodeUsedPort(dockerNodeId).getData();
            Integer maxPort = 8000;
            if(CollectionUtil.isNotEmpty(usedPort)){
                maxPort = Integer.valueOf(usedPort.get(usedPort.size() - 1));
            }
            for (String port : ports) {
                maxPort++;
                portMap.put(maxPort.toString(),port);
            }
        }
        JsonReturn jr = deployServiceNodeByHand(serviceId, dockerNodeId, JSON.toJSONString(portMap));
        if(jr.isSuccess()){
            return true;
        }
        return false;
    }

    /**
     * 根据要发布的容器数量，分配到各个docker节点
     * @return
     */
    private HashMap<Integer, Integer> calcServiceDockerNumMap(Integer deployNum) {
        //获取所有docker节点
        List<TServerDocker> dockerList = serverDockerLogic.list();
        HashMap<Integer, Integer> map = new HashMap<>();
        calcServiceDockerNumMap_pre(dockerList, deployNum, map);
        return map;
    }


    //递归分配节点
    private void calcServiceDockerNumMap_pre(List<TServerDocker> dockerList, Integer deployNum, HashMap<Integer, Integer> map) {
        //每一次，判断目前map分配够不够了
        Integer sum = map.values().stream().mapToInt(Integer::intValue).sum();
        if(sum >= deployNum){
            return;
        }
        //没有蛮。仍需要分配 每一次都判断一下谁的节点最少
        TServerDocker dockerNode = dockerList.stream().sorted((o1, o2) -> {
            Integer o1New = map.get(o1.getId());
            Integer o2New = map.get(o2.getId());
            if(o1New == null){
                o1New = 0;
            }
            if(o2New == null){
                o2New = 0;
            }
            int o1Num = o1.getContainerUpNum() + o1New;
            int o2Num = o2.getContainerUpNum() + o2New;
            return  o1Num - o2Num;
        }).collect(Collectors.toList()).get(0);
        if(map.containsKey(dockerNode.getId())){
            map.put(dockerNode.getId(), map.get(dockerNode.getId()) + 1);
        }else {
            map.put(dockerNode.getId(), 1);
        }
        //继续下一次分配
        calcServiceDockerNumMap_pre(dockerList, deployNum, map);
    }

    /**
     * 获取某个节点的日志
     * @param id
     * @param tail
     * @return
     */
    public JsonReturn getServiceNodeLogByTail(Integer id, Integer tail) {
        TServiceNode serviceNode = serviceNodeService.getById(id);
        if(serviceNode != null){
            TServerDocker dockerNode = serverDockerLogic.getById(serviceNode.getDockerId());
            if(dockerNode != null){
                DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(dockerNode);
                List<String> logs = MyDockerUtil.getLogById(config, serviceNode.getContainerId(), tail);
                return JsonReturn.success(logs);
            }
        }
        return null;
    }

    /**
     * 获取某个服务，所有节点的日志
     * @param id
     * @param tail
     * @return
     */
    public JsonReturn getServiceAllNodeLogByTail(Integer id, Integer tail) {
        List<TServiceNode> nodes = serviceNodeService.lambdaQuery().eq(TServiceNode::getServiceId, id).list();
        if(CollectionUtil.isNotEmpty(nodes)){
            List<TServerDocker> dockerNodes = serverDockerLogic.getByIds(nodes.stream().map(TServiceNode::getDockerId).collect(Collectors.toList()));
            Map<Integer, TServerDocker> map_dockerNode_id_obj = dockerNodes.stream().collect(Collectors.toMap(TServerDocker::getId, Function.identity()));
            //获取所有节点日志
            ArrayList<Object> arr = new ArrayList<>();
            for (TServiceNode node : nodes) {
                if(map_dockerNode_id_obj.get(node.getDockerId()) != null){
                    DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(map_dockerNode_id_obj.get(node.getDockerId()));
                    List<String> logs = MyDockerUtil.getLogById(config, node.getContainerId(), tail);
                    logs.forEach(e->arr.add(String.format("【%s】 | %s", node.getContainerName(),e)));
                }
            }
            return JsonReturn.success(arr);
        }
        return JsonReturn.errorMsg("获取失败");
    }

    /**
     * 获取镜像的所有tag
     * @param imageName
     * @return
     */
    public JsonReturn getImageAllTag(String imageName) {
        String[] temp = imageName.split("/");
        return registryLogic.getTagsByRegURLMirror(temp[0],temp[1].split(":")[0]);
    }

    public JsonReturn upgradeServiceToTag(Integer serviceId, String tag) {
        //先获取服务当前的信息
        TService service = serviceService.getById(serviceId);
        String oldImageName = service.getImageName();
        //修改imageName
        String newImageName = service.getImageName().substring(0,service.getImageName().lastIndexOf(":")) + ":" + tag;
        service.setImageName(newImageName);
        //该服务的所有节点
        List<TServiceNode> serviceNodes = serviceNodeService.lambdaQuery().eq(TServiceNode::getServiceId, serviceId).list();
        for (TServiceNode serviceNode : serviceNodes) {
            //停止服务
            killServiceNode(serviceNode);
            //删除容器（传id是为了删除旧记录）
            deleteServiceNode(serviceNode.getId());
            //新建容器,并启动
            createServiceNode(serviceNode, service,true);
            //删除旧镜像
            removeImage(serviceNode,oldImageName);
        }
        //更新服务信息
        serviceService.updateById(service);
        return JsonReturn.successMsg("升级成功");
    }

    public JsonReturn removeImage(TServiceNode serviceNode, String oldImageName) {
        DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(serverDockerLogic.getById(serviceNode.getDockerId()));
        boolean f = MyDockerUtil.removeImage(oldImageName, config);
        return f ? JsonReturn.successMsg("删除成功") : JsonReturn.errorMsg("删除失败");
    }

    //根据现有服务，创建容器
    private JsonReturn createServiceNode(TServiceNode serviceNode, TService service, Boolean isUp) {
        if(isUp == null){
            //默认不启动
            isUp = false;
        }
        TServerDocker dockerNode = serverDockerLogic.getById(serviceNode.getDockerId());
        DefaultDockerClientConfig config = serverDockerLogic.getDockerConfigByServerDocker(dockerNode);

        //拉取镜像
        TRegistry registry = registryLogic.getByHost(service.getImageName().split("/")[0]);
        boolean f = MyDockerUtil.pullImage(config, service.getImageName(), registry.getUsername(), registry.getPassword());
        if(!f){
            return JsonReturn.errorMsg("拉取镜像失败");
        }
        //随机容器名
        String uuid = UUID.randomUUID().toString();
        String containerName = service.getAlias()+"_"+uuid.substring(uuid.length()-8);
        //端口映射
        JSONObject portMap_json = JSON.parseObject(serviceNode.getPortMap());
        HashMap<Integer, Integer> portMapObj = new HashMap<>();
        for (String in_port : portMap_json.keySet()) {
            portMapObj.put(Integer.parseInt(in_port), Integer.parseInt(portMap_json.getString(in_port)));
        }
        //挂载映射
        HashMap<String, String> mountMap = new HashMap<>();
        if(StringUtils.isNotBlank(service.getMountMap())){
            JSONArray mount_list = JSON.parseArray(service.getMountMap());
            for (Object o : mount_list) {
                JSONObject jsonObject = (JSONObject) o;
                mountMap.put(jsonObject.getString("hostPath"),jsonObject.getString("containerPath"));
            }
        }
        //创建镜像
        String containerId = MyDockerUtil.addDockerContainerWithoutStart(config, containerName, service.getImageName(), portMapObj,mountMap, service.getMaxCpu());
        if(containerId == null){
            return JsonReturn.errorMsg("创建容器失败");
        }
        //启动
        if(isUp){
            MyDockerUtil.startDockerContainerById(config, containerId);
        }

        //记录数据库
        TServiceNode tServiceNode = new TServiceNode();
        tServiceNode.setContainerId(containerId);
        tServiceNode.setContainerName(containerName);
        tServiceNode.setDockerId(serviceNode.getDockerId());
        tServiceNode.setServiceId(service.getId());
        tServiceNode.setPortMap(serviceNode.getPortMap());
        tServiceNode.setPortWeb(service.getPortWeb());
        tServiceNode.setMaxCpu(service.getMaxCpu());
        //计算实际外部端口
        Map<Integer, Integer> port_map_temp = portMapObj.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        tServiceNode.setPortWebOut(port_map_temp.get(service.getPortWeb()));
        serviceNodeService.save(tServiceNode);
        serviceNode.setId(tServiceNode.getId());

        return JsonReturn.successMsg("创建容器成功");
    }
}
