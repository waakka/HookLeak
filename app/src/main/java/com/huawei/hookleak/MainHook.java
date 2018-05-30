package com.huawei.hookleak;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.huawei.hookleak.leakcarany.ReftWatcher;

import java.util.UUID;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class MainHook implements IXposedHookLoadPackage {

    public int count = 0;

    public ReftWatcher watcher = new ReftWatcher();

    private ConfigBeen configBeen;







    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_LOGIN = 1;


    private long lastClickTime = -1;
    private String lastActivityName = "";

    private String curActivityName;
    private long curTime;
    /**
     * 常规界面或登录界面
     * 0：常规界面
     * 1：登录界面
     */
    private int type;

    private String loginActivityName;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        configBeen = FileUtil.getConfigBeen();
        if (!lpparam.packageName.equalsIgnoreCase(configBeen.getPackageName())){
//            XposedBridge.log("包名不符，配置包名："+ configBeen.getPackageName() +"当前包名："+lpparam.packageName);
            return;
        }


        findAndHookMethod("android.app.Activity",lpparam.classLoader,
                "onDestroy", new XC_MethodHook(){
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {


                        Activity activity = (Activity) param.thisObject;
                        Context context = activity;
                        String activityName = activity.getClass().getName();

                        XposedBridge.log("hook_"+(++count)+"==onDestroy==>activityName:" + activityName);

                        watcher.watch(context,activityName);

                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });







        /**
         * 暂时kook view的dispatchTouchEvent
         * 正式hook traverTest的deviceClick
         */
        findAndHookMethod("android.view.View",lpparam.classLoader,
                "dispatchTouchEvent", MotionEvent.class,new XC_MethodHook(){
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        MotionEvent event = (MotionEvent) param.args[0];
                        int action = event.getAction();
                        switch(action) {
                            case MotionEvent.ACTION_UP:
                                //默认为常规界面跳转
                                type = TYPE_NORMAL;

                                //TODO 增加resoureName的判断，如果符合解析的登录button则本次操作为登录操作
                                View view = (View) param.thisObject;

                                Resources resources = view.getResources();
                                String resourceName = resources.getResourceName(view.getId());
                                if (!TextUtils.isEmpty(configBeen.getLoginRecId()) && !TextUtils.isEmpty(resourceName)){
                                    if (configBeen.getLoginRecId().equalsIgnoreCase(resourceName)){
                                        XposedBridge.log("本次操作控件符合登录按钮特征，属登录操作，resourceName = " + resourceName);
                                        type = TYPE_LOGIN;
                                        //如果当前操作控件是登录控件，则当前界面是登录界面
                                        loginActivityName = lastActivityName;
                                    }
                                }

                                TextView tv = (TextView) view;
                                //强转view为TextView获取控件的text及description
                                if(tv!=null){
                                    CharSequence contentDescription = tv.getContentDescription();
                                    if(!TextUtils.isEmpty(contentDescription) && !contentDescription.equals("null")){
                                        XposedBridge.log("本次操作控件的contentDescription = " + contentDescription);
                                    }
                                    CharSequence text = tv.getText();
                                    if (!TextUtils.isEmpty(configBeen.getLoginDesc()) && text != null){
                                        XposedBridge.log("本次操作控件符合登录按钮特征，属登录操作，text = " + text);
                                        if (configBeen.getLoginDesc().equalsIgnoreCase(text.toString())){
                                            type = TYPE_LOGIN;
                                            //如果当前操作控件是登录控件，则当前界面是登录界面
                                            loginActivityName = lastActivityName;
                                        }
                                    }
                                }
                                lastClickTime = System.currentTimeMillis();
                                break;
                        }
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });

        findAndHookMethod("android.app.Activity",lpparam.classLoader,
                "onStart", new XC_MethodHook(){
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        String activityName = activity.getClass().getName();
                        XposedBridge.log("****************" + ConsumeUtil.getCurTimeStr() + "***"
                                + activityName+"调用onStart方法 ");
                        // 记录当前时间
                        curTime = System.currentTimeMillis();
                        // 记录当前ActivityName
                        curActivityName = activity.getClass().getName();
                        // 判断当前失去焦点的activity是否是lastActivityName
                        if (!curActivityName.equals(lastActivityName)){
                            // 不是则说明发生跳转,生成Consume类并打印
                            if (lastClickTime == -1){
                                //当前新界面不是通过deviceClick方法触发新界面
                                //adb启动acctivity或app内部自动跳转或back、home等造成
                                //暂不处理
                                Consume consume = new Consume();
                                // 起始时间及起始activityName暂不记录
                                consume.setStartActivity("");
                                consume.setStartTime(0);
                                consume.setStoptActivity(curActivityName);
                                consume.setStopTime(curTime);
                                consume.setType(-1);
                                XposedBridge.log("当前界面"+activityName+"发生onStart，未记录点击信息，为back、home等未知操作导致");
                                ConsumeUtil.showLog(consume.toString());
                            }else{
                                Consume consume = new Consume();
                                consume.setStartActivity(lastActivityName);
                                consume.setStartTime(lastClickTime);
                                consume.setStoptActivity(curActivityName);
                                consume.setStopTime(curTime);
                                consume.setType(type);
                                //再次根据界面判断当前是否登录数据
                                if (!TextUtils.isEmpty(lastActivityName)
                                        &&!TextUtils.isEmpty(loginActivityName)
                                        &&lastActivityName.equals(loginActivityName)){
                                    //如果上个界面是登录界面并且当前界面不等于登录界面则认为当前属于登录数据
                                    XposedBridge.log("上个界面是登录界面并且当前界面不等于登录界面则认为当前属于登录数据");
                                    consume.setType(TYPE_LOGIN);
                                }
                                ConsumeUtil.showLog(consume.toString());
                            }
                            lastActivityName = curActivityName;
                        }
                        //归零点击记录
                        lastClickTime = -1;
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });











    }
}
