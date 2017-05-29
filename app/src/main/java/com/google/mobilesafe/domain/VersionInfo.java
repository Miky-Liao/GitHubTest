package com.google.mobilesafe.domain;

/**
 * Created by Miky on 2017/5/22.
 */

public class VersionInfo {
    private String versionCode;
    private String versionName;
    private String desc;
    private String downloadUrl;

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", desc='" + desc + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
