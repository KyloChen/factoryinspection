package com.loohos.factoryinspection.model.local;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Set;


public class LocalSite {
    //Id
    private String siteId;
    //名称
    private String siteName;
    //描述
    private String siteDesc;
    //x坐标
    private String siteLocX;
    //y坐标
    private String siteLocY;
    //父级Id 为空代表第一级
    private String parentId;
    //无限制子集
    private Set<LocalSite> chileSite;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }


    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteDesc() {
        return siteDesc;
    }

    public void setSiteDesc(String siteDesc) {
        this.siteDesc = siteDesc;
    }
    public String getSiteLocX() {
        return siteLocX;
    }

    public void setSiteLocX(String siteLocX) {
        this.siteLocX = siteLocX;
    }
    public String getSiteLocY() {
        return siteLocY;
    }

    public void setSiteLocY(String siteLocY) {
        this.siteLocY = siteLocY;
    }
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Set<LocalSite> getChileSite() {
        return chileSite;
    }

    public void setChileSite(Set<LocalSite> chileSite) {
        this.chileSite = chileSite;
    }
}
