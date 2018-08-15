package com.huawei.hookleak;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import de.robv.android.xposed.XposedBridge;

public class FileUtil {

    public static final String TAG = "hookLog";


    public static void showLog(String msg){
        XposedBridge.log(msg);
        Log.e(TAG,msg);
    }


    public static long getTimeFromFile(){
        long result = -1;
        String path = "/data/local/tmp" + "/ClickTimeTmp.txt";
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
            FileUtil.showLog("创建文件夹成功 file=" + file.getAbsolutePath());
        }
        String timegStr = getStrFromFile(file);
        if (!TextUtils.isEmpty(timegStr)){
            result = Long.parseLong(timegStr);
        }
        FileUtil.showLog("=======lastClickTime = "+timegStr+"成功读取文件=====\n");
        return result;
    }


    public static void setTimeToFile(long time){
        String path = "/data/local/tmp" + "/ClickTimeTmp.txt";
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
            FileUtil.showLog("创建文件夹成功 file=" + file.getAbsolutePath());
        }

        FileOutputStream fos = null;
        BufferedWriter bw = null;

        try {
            fos = new FileOutputStream(file,false);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(time + "");
            bw.flush();
            FileUtil.showLog("=======lastClickTime = "+time+"成功写入文件=====\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            FileUtil.showLog("写文件时出错，文件未找到 " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            FileUtil.showLog("写文件时出错，IOException " + e.getMessage());
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                FileUtil.showLog(e.getMessage());
            }
        }
    }



    //解析配置文件
    public static ConfigBeen getConfigBeen(){
//        String path = Environment.getExternalStorageDirectory() + "/TraverseConfig.txt";
        String path = "/data/local/tmp" + "/TraverseConfig.txt";
        File file = new File(path);
        String configStr = getStrFromFile(file);
        ConfigBeen configBeen = new ConfigBeen();
        if (!TextUtils.isEmpty(configStr)){
            try {
                JSONObject jsonObject = new JSONObject(configStr);
                configBeen.setPackageName(jsonObject.getString("packageName"));
                if (!configStr.contains("\"login\":null")){
                    //需要登录
                    //账号密码信息
                    JSONObject Obj = jsonObject.getJSONObject("traverse");
                    configBeen.setAccount(Obj.getString("account"));
                    configBeen.setPassWord(Obj.getString("password"));

                    //登录配置信息
                    JSONObject loginObj = jsonObject.getJSONObject("login");
                    JSONArray loginCfgObj = loginObj.getJSONArray("loginCfg");
                    for (int i = 0 ; i < loginCfgObj.length() ; i++){
                        JSONObject o = loginCfgObj.getJSONObject(i);
                        String execSeq = o.getString("execSeq");
                        if (execSeq.equals("4")){
                            String loginBtnId = o.getString("loginBtnId");
                            String widgetType = o.getString("widgetType");
                            if (widgetType.equals("1")){
                                configBeen.setLoginRecId(unescapeJava(loginBtnId));
                            }else{
                                configBeen.setLoginDesc(loginBtnId);
                            }
                        }
                    }
                }
//                FileUtil.showLog("解析得当前配置文件：" + configBeen.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                FileUtil.showLog(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                FileUtil.showLog(e.getMessage());
            }
        }else{
//            configBeen.setPackageName("com.waakka.login");
//            XposedBridge.log("配置文件为空：" + configBeen.toString());
        }
        return configBeen;
    }

    //获取Hprof文件路径
    public static File getHprofFile(String name){
        String path1 = Environment.getExternalStorageDirectory() + "/u2test";
        String path2 = path1 + "/hprof";
        String pathHprof = path2 + "/"+name + ".hprof";
        File file1 = new File(path1);
        File file2 = new File(path2);
        File logFile = new File(pathHprof);
        if (!file1.exists()){
            file1.mkdirs();
            FileUtil.showLog("创建文件夹成功 file1=" + file1.getAbsolutePath());
        }
        if (!file2.exists()){
            file2.mkdirs();
            FileUtil.showLog("创建文件夹成功 file2=" + file2.getAbsolutePath());
        }
        if (logFile.exists()){
            logFile.delete();
        }
        try {
            logFile.createNewFile();
            FileUtil.showLog("创建文件成功 logFile=" + logFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            FileUtil.showLog("创建文件失败 " + e.getMessage());
        }
        return logFile;
    }

    //获取log日志文件路径
    public static File getLogFile(){
        String path1 = Environment.getExternalStorageDirectory() + "/u2test";
        String path2 = path1 + "/UiAutomation";
        String pathtxt = path2 + "/Consume.txt";
        File file1 = new File(path1);
        File file2 = new File(path2);
        File logFile = new File(pathtxt);
        if (!logFile.exists()){
            XposedBridge.log("文件不存在，创建文件夹及文件");
            if (!file1.exists()){
                file1.mkdirs();
                FileUtil.showLog("创建文件夹成功 file1=" + file1.getAbsolutePath());
            }
            if (!file2.exists()){
                file2.mkdirs();
                FileUtil.showLog("创建文件夹成功 file2=" + file2.getAbsolutePath());
            }
            try {
                logFile.createNewFile();
                FileUtil.showLog("创建文件成功 logFile=" + logFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                FileUtil.showLog("创建文件失败 " + e.getMessage());
            }
        }
        return logFile;
    }


    /**
     * 读取文件中string
     * @param file
     * @return
     */
    public static String getStrFromFile(File file){
        String content = "";
        if (file.exists()){
            String encouding = "UTF-8";
            BufferedReader reader = null;
            InputStreamReader isr = null;
            FileInputStream fis = null;
            String line = "";
            try {
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis,encouding);
                reader = new BufferedReader(isr);
                while ((line = reader.readLine()) != null){
                    content += line;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                FileUtil.showLog("未找到配置文件==>" + e.getMessage());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                FileUtil.showLog(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                FileUtil.showLog(e.getMessage());
            } finally {
                if (reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        FileUtil.showLog(e.getMessage());
                    }
                    reader = null;
                }
                if (isr != null){
                    try {
                        isr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        FileUtil.showLog(e.getMessage());
                    }
                    isr = null;
                }
                if (fis != null){
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        FileUtil.showLog(e.getMessage());
                    }
                    fis = null;
                }

            }
        }
        return content;
    }

    /**
     * 去转义符方法
     * @param str
     * @return
     * @throws IOException
     */
    public static String unescapeJava(String str) throws IOException
    {
        Writer out = new StringWriter();
        if (str != null)
        {
            int sz = str.length();
            StringBuilder unicode = new StringBuilder(4);
            boolean hadSlash = false;
            boolean inUnicode = false;

            for (int i = 0; i < sz; ++i)
            {
                char ch = str.charAt(i);
                if (inUnicode)
                {
                    unicode.append(ch);
                    if (unicode.length() == 4)
                    {
                        try
                        {
                            int nfe = Integer.parseInt(unicode.toString(), 16);
                            out.write((char) nfe);
                            unicode.setLength(0);
                            inUnicode = false;
                            hadSlash = false;
                        }
                        catch (NumberFormatException var9)
                        {
                        }
                    }
                }
                else if (hadSlash)
                {
                    hadSlash = false;
                    switch (ch)
                    {
                        case '\"':
                            out.write(34);
                            break;
                        case '\'':
                            out.write(39);
                            break;
                        case '\\':
                            out.write(92);
                            break;
                        case 'b':
                            out.write(8);
                            break;
                        case 'f':
                            out.write(12);
                            break;
                        case 'n':
                            out.write(10);
                            break;
                        case 'r':
                            out.write(13);
                            break;
                        case 't':
                            out.write(9);
                            break;
                        case 'u':
                            inUnicode = true;
                            break;
                        default:
                            out.write(ch);
                    }
                }
                else if (ch == 92)
                {
                    hadSlash = true;
                }
                else
                {
                    out.write(ch);
                }
            }

            if (hadSlash)
            {
                out.write(92);
            }

        }
        return out.toString();
    }


}
