package com.huawei.hookleak;

public class Consume {

    private long startTime;
    private long stopTime;

    private String startActivity;
    private String stoptActivity;
    /**
     * 常规界面或登录界面
     * 0：常规界面点击跳转
     * 1：登录界面点击跳转
     * 2：未知操作导致跳转back，home等
     */
    private int type;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public String getStartActivity() {
        return startActivity;
    }

    public void setStartActivity(String startActivity) {
        this.startActivity = startActivity;
    }

    public String getStoptActivity() {
        return stoptActivity;
    }

    public void setStoptActivity(String stoptActivity) {
        this.stoptActivity = stoptActivity;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

//    @Override
//    public String toString() {
//        return "Consume{" +
//                "startTime=" + ConsumeUtil.getTimeStr(startTime) +
//                ", startActivity='" + startActivity + '\'' +
//                ", stopTime=" + ConsumeUtil.getTimeStr(stopTime) +
//                ", stoptActivity='" + stoptActivity + '\'' +
//                ", type=" + type +
//                "}\n";
//    }
    @Override
    public String toString() {
        return getType() + "|" +
                getStartActivity() + "|" +
                getStoptActivity() + "|" +
                getStartTime() + "|" +
                getStopTime() +
                "\n";
    }
}
