package com.huawei.hookleak;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.KeyEvent;
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


    private String curPackageName = "";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {



        findAndHookMethod("android.app.Activity",lpparam.classLoader,
                "onKeyDown",int.class, KeyEvent.class, new XC_MethodHook(){
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        int keyCode = (int) param.args[0];
                        FileUtil.showLog("keyCode = " + keyCode + "重置lastClickTime为-1，");

                        lastClickTime = -1;



                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });




        //当前curPackageName为空，第一次解析配置文件
        if (TextUtils.isEmpty(curPackageName)){
            FileUtil.showLog("当前curPackageName为空，开始解析配置文件");
            configBeen = FileUtil.getConfigBeen();
            curPackageName = configBeen.getPackageName();
        }
        //包名不符，配置文件可能发生变更，重新解析配置文件
        if(!lpparam.packageName.equalsIgnoreCase(curPackageName)){
            FileUtil.showLog("包名不符，配置包名："+ curPackageName +"当前事件包名："+lpparam.packageName);
            configBeen = FileUtil.getConfigBeen();
            curPackageName = configBeen.getPackageName();
        }
        else{
            FileUtil.showLog("包名符合，开始hook");
        }
        //经过重新解析配置文件后，包名依然不符合，退出当前事件流
        if (!lpparam.packageName.equalsIgnoreCase(curPackageName)){
            FileUtil.showLog("再次解析后，包名不符，配置包名："+ curPackageName +"当前事件包名："+lpparam.packageName + "退出当前事件流");
            return;
        }
        else{
            FileUtil.showLog("再次解析后，包名符合，开始hook");
        }


        findAndHookMethod("android.app.Activity",lpparam.classLoader,
                "onDestroy", new XC_MethodHook(){
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        FileUtil.showLog("本次事件===>onDestroy");

                        Activity activity = (Activity) param.thisObject;
                        Context context = activity;
                        String activityName = activity.getClass().getName();

                        FileUtil.showLog("hook_"+(++count)+"==onDestroy==>activityName:" + activityName);

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
//                        FileUtil.showLog("本次事件===>dispatchTouchEvent");
                        MotionEvent event = (MotionEvent) param.args[0];
                        int action = event.getAction();
//                        switch(action) {
//                            case MotionEvent.ACTION_UP:
                                //默认为常规界面跳转
                                type = TYPE_NORMAL;

                                //TODO 增加resoureName的判断，如果符合解析的登录button则本次操作为登录操作
                                View view = (View) param.thisObject;
                                lastClickTime = System.currentTimeMillis();
                                FileUtil.showLog("dispatchTouchEvent,action=" + action +
                                        ",view==" + view.getClass().getSimpleName() +
                                        ",lastClickTime = " + lastClickTime);
                                try {
                                    Resources resources = view.getResources();
                                    String resourceName = resources.getResourceName(view.getId());
                                    if (!TextUtils.isEmpty(configBeen.getLoginRecId()) && !TextUtils.isEmpty(resourceName)){
                                        if (configBeen.getLoginRecId().equalsIgnoreCase(resourceName)){
                                            FileUtil.showLog("本次操作控件符合登录按钮特征，属登录操作，resourceName = " + resourceName);
                                            if (TextUtils.isEmpty(configBeen.getAccount()) || TextUtils.isEmpty(configBeen.getPassWord())){
                                                FileUtil.showLog("当前应用无账号或密码，取消登录信息记录");
                                            }else{
                                                type = TYPE_LOGIN;
                                                //如果当前操作控件是登录控件，则当前界面是登录界面
                                                loginActivityName = lastActivityName;
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
//                                    FileUtil.showLog("resources获取失败:" + e.getMessage());
                                }

                                try {
                                    TextView tv = (TextView) view;
                                    //强转view为TextView获取控件的text及description
                                    if(tv!=null){
                                        CharSequence contentDescription = tv.getContentDescription();
                                        if(!TextUtils.isEmpty(contentDescription) && !contentDescription.equals("null")){
                                            XposedBridge.log("本次操作控件的contentDescription = " + contentDescription);
                                        }
                                        CharSequence text = tv.getText();
                                        if (!TextUtils.isEmpty(configBeen.getLoginDesc()) && text != null){
                                            FileUtil.showLog("本次操作控件符合登录按钮特征，属登录操作，text = " + text);
                                            if (configBeen.getLoginDesc().equalsIgnoreCase(text.toString())){
                                                if (TextUtils.isEmpty(configBeen.getAccount()) || TextUtils.isEmpty(configBeen.getPassWord())){
                                                    FileUtil.showLog("当前应用无账号或密码，取消登录信息记录");
                                                }else{
                                                    type = TYPE_LOGIN;
                                                    //如果当前操作控件是登录控件，则当前界面是登录界面
                                                    loginActivityName = lastActivityName;
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
//                                    FileUtil.showLog("控件不能强转为TextView:" + e.getMessage());
                                }

//                                break;
//                        }
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });





        findAndHookMethod("android.app.Activity",lpparam.classLoader,
                "onStart", new XC_MethodHook(){
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        FileUtil.showLog("本次事件===>onStart");
                        Activity activity = (Activity) param.thisObject;
                        String activityName = activity.getClass().getName();
                        FileUtil.showLog("onStart,activity=" + activityName);
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
//                                FileUtil.showLog("当前界面"+activityName+"发生onStart，未记录点击信息，为back、home等未知操作导致");
                                ConsumeUtil.write(consume.toString());
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
                                    FileUtil.showLog("上个界面是登录界面并且当前界面不等于登录界面则认为当前属于登录数据");
                                    consume.setType(TYPE_LOGIN);
                                }
                                ConsumeUtil.write(consume.toString());
                                //判断超时activity并打印
                                if (curTime - lastClickTime > 5000){
                                    FileUtil.showLog("本次页面切换超时==>" + curActivityName + ":" + (curTime - lastClickTime));
                                }
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
