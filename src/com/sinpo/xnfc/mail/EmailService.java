package com.sinpo.xnfc.mail;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.sinpo.xnfc.HetDoorActivity;
import com.sinpo.xnfc.model.TagModel;
import com.sinpo.xnfc.nfc.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Android Studio.
 * Author: UUXIA
 * Date: 2015-10-26 19:57
 * Description:
 */
public class EmailService extends Service {
    public final static String HETID = "HETID";
    public final static String HETPATH = "PATH";
    public static Email mEmail = null;
    private NetWorkBroadcast netWorkBroadcast;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        demonThread();
        registerWifiListener(this);
        mEmail = Email.create("smtp.qq.com", "263996097@qq.com", "19809175@qq.com", "xxl2475431305.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterWifiListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String id = intent.getStringExtra(HETID);
            String path = intent.getStringExtra(HETPATH);
            String title = "This is H&T Tags  " + Util.getTime();
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(id) && !TextUtils.isEmpty(id)){
                sendEmail(title,id,path);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    public static void main(String[] s){
        System.out.println(Util.getTime());
        String id = "夏小力=B7:68:50:F6=false=2015-10-26 23:08:40";
        System.out.println(id);
        id = id.replace("false", "true");
        System.out.println(id);
    }

    private void registerWifiListener(Context context) {
        netWorkBroadcast = new NetWorkBroadcast();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentfilter.addAction("android.net.conn.WIFI_STATE_CHANGED");
        intentfilter.addAction("android.net.conn.STATE_CHANGE");
        intentfilter.addAction("android.net.wifi.STATE_CHANGE");
        intentfilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        context.registerReceiver(netWorkBroadcast, intentfilter);
    }

    private void unregisterWifiListener(Context context) {
        if (netWorkBroadcast != null && context != null) {
            context.unregisterReceiver(netWorkBroadcast);
        }
    }

    public void sendEmail(final String titile,final String content,final String path) {
        System.out.println("Email="+titile+" "+content+" "+path);
        if (mEmail == null) {
            System.out.println("mail error...");
            return;
        }

        if (!Util.isNetworkAvailable(this)){
            changeFile(content,false);
            System.err.println("..........没有网络。。。。。。。。。。。。。。。");
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                Class clazz = mEmail.getClass();
                try {
                    String sendcontent = new String(content.getBytes(), "ISO-8859-1");
                    Method subJect = clazz.getDeclaredMethod(MethondName.setSubject, String.class);
                    Method conTent = clazz.getDeclaredMethod(MethondName.setContent, String.class);
                    Method attachFile = clazz.getDeclaredMethod(MethondName.setAttachFile, String.class);
                    Method sendtextMail = clazz.getDeclaredMethod(MethondName.sendTextMail);
                    subJect.invoke(mEmail, titile);
                    conTent.invoke(mEmail, sendcontent);
//                    attachFile.invoke(mEmail, path);
                    sendtextMail.invoke(mEmail);
                    changeFile(content,true);
                } catch (NoSuchMethodException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void changeFile(String content,boolean flag){
        try {
            if (content == null || "".equals(content)){
                return;
            }
            if (flag) {
                content = content.replace("false", "true");
            }
            System.out.println("xxxxxxxxxx="+content);
            Util.writeFileSdcardFile(HetDoorActivity.tagIdFilePath, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<TagModel> getData(){
        List<TagModel> mData = new ArrayList<TagModel>();
        String readStr = null;
        try {
            readStr = Util.readFileSdcardFile(HetDoorActivity.tagIdFilePath);
            if (readStr == null && "".equals(readStr)){
                return null;
            }
            final String[] lines = readStr.split("\n");
            for (String item : lines){
                if (item != null) {
                    String[] tags = item.split("=");
                    if (item != null && item.length() >= 4) {
                        TagModel tagModel = new TagModel();
                        tagModel.setName(tags[0]);
                        tagModel.setTag(tags[1]);
                        tagModel.setSave(tags[2]);
                        tagModel.setTime(tags[3]);
                        mData.add(tagModel);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mData;
    }

    private void saveAndSendEmail(){
        List<TagModel> nListDta = getData();
        if (nListDta != null && nListDta.size() > 0){
            boolean sendEmail = false;
            StringBuffer sb = new StringBuffer();
            for (TagModel item : nListDta){
                sb.append(item.getName());
                sb.append("=");
                sb.append(item.getTag());
                sb.append("=");
                sb.append(item.isSave());
                sb.append("=");
                sb.append(item.getTime());
                sb.append("\n");
                if (item.isSave().equals("false")){
                    sendEmail = true;
                }
            }
            if (sendEmail){
                String title = "This is H&T Tags  " + Util.getTime();
                sendEmail(title,sb.toString(), null);
            }
        }
    }

    static class MethondName{
        public static String setSubject = "setSubject";
        public static String setContent = "setContent";
        public static String setAttachFile = "setAttachFile";
        public static String sendTextMail = "sendTextMail";
    }


    private Thread demon = null;
    private byte[] lock = new byte[0];
    private boolean netWorkChg = true;

    private void demonThread(){
        if (demon == null) {
            demon = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        while (true) {
                            while (netWorkChg) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (Util.isNetworkAvailable(EmailService.this)){
                                saveAndSendEmail();
                                netWorkChg = true;
                            }
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            demon.start();
        }
    }

    private void nitifyDemon(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock){
                    netWorkChg = false;
                    lock.notifyAll();
                }
            }
        }).start();
    }



    public class NetWorkBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
                Log.i("EmailService", intent.getAction());
            } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {// 锟斤拷锟较匡拷锟斤拷锟斤拷
                    Log.i("EmailService","NetWorkBroadcast.uuuuixa. wifi disconnected... ");
                }
                if (info.getState().equals(NetworkInfo.State.CONNECTING)) {
                    Log.i("EmailService", "NetWorkBroadcast.uuuuixa. wifi connected...");
                    nitifyDemon();
                }
            } else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                // WIFI锟斤拷锟斤拷
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {// 锟斤拷锟截憋拷
                    Log.i("EmailService","NetWorkBroadcast.uuuuixa. wifi closed...");
                }

                if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    Log.i("EmailService","NetWorkBroadcast.uuuuixa. wifi opened...");
                }
            }
        }  //锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟絘ctiveInfo为null

    }
}
