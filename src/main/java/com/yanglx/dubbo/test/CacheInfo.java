package com.yanglx.dubbo.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yanglx.dubbo.test.dubbo.DubboMethodEntity;
import com.yanglx.dubbo.test.utils.Json;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CacheInfo implements Serializable {

    /**
     * Interface name
     */
    private String interfaceName;

    /**
     * Method name
     */
    private String methodName;

    /**
     * Version
     */
    private String version;

    /**
     * Group
     */
    private String group;

    /**
     * Method type
     */
    private String methodTypeJson;

    /**
     * Param obj
     */
    private String paramObjJson;

    /**
     * Address
     */
    private String address;

    private String name;

    private String id;

    private Date date;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getMethodTypeJson() {
        return methodTypeJson;
    }

    public void setMethodTypeJson(String methodTypeJson) {
        this.methodTypeJson = methodTypeJson;
    }

    public String getParamObjJson() {
        return paramObjJson;
    }

    public void setParamObjJson(String paramObjJson) {
        this.paramObjJson = paramObjJson;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public static CacheInfo of(String id, String name, DubboMethodEntity dubboMethodEntity) {
        CacheInfo cacheInfo = new CacheInfo();
        cacheInfo.setId(id);
        cacheInfo.setName(name);
        cacheInfo.setInterfaceName(dubboMethodEntity.getInterfaceName());
        cacheInfo.setMethodName(dubboMethodEntity.getMethodName());
        cacheInfo.setVersion(dubboMethodEntity.getVersion());
        cacheInfo.setGroup(dubboMethodEntity.getGroup());
        cacheInfo.setMethodTypeJson(Json.stringify(dubboMethodEntity.getMethodType()));
        cacheInfo.setParamObjJson(Json.stringify(dubboMethodEntity.getParam()));
        cacheInfo.setAddress(dubboMethodEntity.getAddress());
        cacheInfo.setDate(new Date());
        return cacheInfo;
    }

    /**
     * 将CacheInfo转换DubboMethodEntity并返回
     *
     * @return
     */
    public DubboMethodEntity getDubboMethodEntity() {
        DubboMethodEntity dubboMethodEntity = new DubboMethodEntity();
        dubboMethodEntity.setId(getId());
        dubboMethodEntity.setInterfaceName(getInterfaceName());
        dubboMethodEntity.setMethodName(getMethodName());
        dubboMethodEntity.setVersion(getVersion());
        dubboMethodEntity.setGroup(getGroup());

        if (StringUtils.isNotBlank(getMethodTypeJson())) {
            List<String> stringList = Json.fromJson(getMethodTypeJson(), new TypeReference<>() {
            });
            String[] methodTypes = new String[stringList.size()];
            for (int i = 0; i < stringList.size(); i++) {
                methodTypes[i] = stringList.get(i);
            }
            dubboMethodEntity.setMethodType(methodTypes);
        }

        if (StringUtils.isNotBlank(getParamObjJson())) {
            Object[] array = Json.fromJson(getParamObjJson(), Object[].class);
            dubboMethodEntity.setParam(array);
        }
        dubboMethodEntity.setAddress(getAddress());
        return dubboMethodEntity;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheInfo)) return false;
        CacheInfo cacheInfo = (CacheInfo) o;
        return id.equals(cacheInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
