package com.huawei.hookleak;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

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

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {




        findAndHookMethod("android.app.Activity",lpparam.classLoader,
                "onDestroy", new XC_MethodHook(){
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {


                        Activity activity = (Activity) param.thisObject;
                        Application application = activity.getApplication();
                        Context context = activity;
                        String activityName = activity.getClass().getName();

                        XposedBridge.log("hook_"+(++count)+"==onDestroy==>activityName:" + activityName);



                        watcher.watch(context,activityName);

                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });










    }
}
