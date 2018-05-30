package com.huawei.hookleak;

public class ConfigBeen {

    private String packageName;
    private String loginRecId;
    private String loginDesc;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getLoginRecId() {
        return loginRecId;
    }

    public void setLoginRecId(String loginRecId) {
        this.loginRecId = loginRecId;
    }

    public String getLoginDesc() {
        return loginDesc;
    }

    public void setLoginDesc(String loginDesc) {
        this.loginDesc = loginDesc;
    }

    @Override
    public String toString() {
        return "ConfigBeen{" +
                "packageName='" + packageName + '\'' +
                ", loginRecId='" + loginRecId + '\'' +
                ", loginDesc='" + loginDesc + '\'' +
                '}';
    }
}
