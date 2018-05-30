package com.huawei.hookleak;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.robv.android.xposed.XposedBridge;

public class ConsumeUtil {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS");



    public static String getCurTimeStr(){
        return sdf.format(new Date());
    }


    public static void showLog(String msg){
        XposedBridge.log(msg);
        write(msg);
    }

    public static void write(String msg){
        FileOutputStream fos = null;
        BufferedWriter bw = null;

        try {
            fos = new FileOutputStream(FileUtil.getLogFile(),true);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(msg);
            bw.flush();
            XposedBridge.log("===================成功写入文件===================\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            XposedBridge.log("写文件时出错，文件未找到 " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            XposedBridge.log("写文件时出错，IOException " + e.getMessage());
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
