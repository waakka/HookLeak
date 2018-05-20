package com.huawei.hookleak;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.ActivityRefWatcher;
import com.squareup.leakcanary.AndroidDebuggerControl;
import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.AndroidHeapDumper;
import com.squareup.leakcanary.AndroidWatchExecutor;
import com.squareup.leakcanary.DebuggerControl;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.GcTrigger;
import com.squareup.leakcanary.HeapDump;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.leakcanary.ServiceHeapDumpListener;

import java.lang.ref.WeakReference;
import java.security.PublicKey;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class MainHook implements IXposedHookLoadPackage {

    public int count = 0;

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


                        RefWatcher refWatcher = LeakCanary.install(application);
                        refWatcher.watch(activity);

                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });










    }
}
