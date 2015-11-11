package com.sinpo.xnfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sinpo.xnfc.adpter.TagAdpter;
import com.sinpo.xnfc.mail.EmailService;
import com.sinpo.xnfc.model.TagModel;
import com.sinpo.xnfc.nfc.ShellUtils;
import com.sinpo.xnfc.nfc.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: UUXIA
 * Date: 2015-10-15 15:45
 * Description:
 */
public class HetDoorActivity extends Activity {
    private String FilePath = Environment.getExternalStorageDirectory().getPath() + "/libnfc-brcm-20791b05.conf";
    private String SystemRoot = "/system/etc/";
    private String SystemFile = SystemRoot+"libnfc-brcm-20791b05.conf";
    private String tagIdStr;
    private String startString = "NFA_DM_START_UP_CFG";
    private TextView TvLog;
    private ScrollView sv;
    public static String Root = Environment.getExternalStorageDirectory().getPath() + "/nfc/";
    public static String tagIdFilePath = Root+"nfc.txt";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.het_door);
        sv = (ScrollView) findViewById(R.id.sr);
        TvLog = (TextView) findViewById(R.id.log);
        tagIdStr = getIntent().getStringExtra("TAGID");
        if(tagIdStr != null && !"".equals(tagIdStr)){
//            initTagId();
            showLog("当前门禁卡ID编号：" + tagIdStr);
        }
    }

    private void showLog(String msg){
        String log = TvLog.getText().toString();
        StringBuffer sb = new StringBuffer();
        sb.append(log);
        sb.append("\r\n");
        sb.append(msg);
        sv.fullScroll(ScrollView.FOCUS_DOWN);
        TvLog.setText(sb.toString());
    }

    public void onClear(View view){
        TvLog.setText("");
    }

    public void onChangeFile(View view){
        if (isEx()){
            String readStr = null;
            try {
                readStr = Util.readFileSdcardFile(SystemFile);
                if (readStr != null && !"".equals(readStr)){
                    showLog("成功读取文件...");
                }
                String[] lines = readStr.split("\n");
                showLog("文件行数："+lines.length);
                StringBuffer sb = new StringBuffer();
                for (int i = 0;i < lines.length; i++){
                    if (lines[i].startsWith(startString)){
                        System.out.println("~~~" + lines[i]);
                        showLog("原NFC标签代码：" + lines[i]);
                        String afterNfc = chgStrings(lines[i]);
                        lines[i] = afterNfc;//"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~xxxxxxxxxxxxxxxxxxxxxxxx";
                        showLog("修改后NFC标签代码：" + afterNfc);
                    }
                    sb.append(lines[i]);
                    sb.append("\n");
                }
                Util.writeFileSdcardFile(FilePath, sb.toString());
                showLog("数据写入成功...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readNFCInfo(View view){
        if (isEx()){
            String readStr = null;
            try {
                readStr = Util.readFileSdcardFile(SystemFile);
                String[] lines = readStr.split("\n");
                StringBuffer sb = new StringBuffer();
                for (int i = 0;i < lines.length; i++){
                    if (lines[i].startsWith(startString)){
                        showLog("修改后NFC标签代码："+lines[i]);
                    }
                    sb.append(lines[i]);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cp(View view){
        showLog("copy “" + FilePath + "” 至 “" + SystemRoot + "”");
//        String[] commands = new String[] { "mount -o rw,remount /system", "cp "+ FilePath +" " + SystemRoot };
//        ShellUtils.CommandResult result = ShellUtils.execCommand(commands, true);
//        showLog("数据copy完毕..."+result.toString());

        String[] commands = new String[] { "mount -o rw,remount /system", "cp /mnt/sdcard/libnfc-brcm-20791b05.conf /system/etc/" };
        ShellUtils.CommandResult result = ShellUtils.execCommand(commands, true);
        showLog("数据copy完毕..."+result.toString());
    }

    private void reboot(){
        new AlertDialog.Builder(this)
                .setPositiveButton("重启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] commands = new String[]{"/system/bin/su","-c","reboot now"};
                        ShellUtils.CommandResult result = ShellUtils.execCommand(commands, true);
                        showLog("重启手机...");
                    }
                })
                .setNegativeButton("不重启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    private void sendEmail(String tagIdStr,String path){
        Intent mm = new Intent(this,EmailService.class);
        mm.putExtra(EmailService.HETID, tagIdStr);
        mm.putExtra(EmailService.HETPATH, path);
        startService(mm);
    }

    private List<TagModel> getData(){
        List<TagModel> mData = new ArrayList<TagModel>();
        String readStr = null;
        try {
            readStr = Util.readFileSdcardFile(tagIdFilePath);
            if (readStr != null && !"".equals(readStr)){
//                showLog("成功读取文件...");
//                sendEmail(readStr,tagIdFilePath);
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

    public void onReadLocal(View view){
        final List<TagModel> nData = getData();
        TagAdpter adpter = new TagAdpter(this,nData,R.layout.item);
        adpter.notifyDataSetChanged();
        new AlertDialog.Builder(this)
                .setAdapter(adpter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final TagModel data = nData.get(which);
                        final EditText input = new EditText(HetDoorActivity.this);
                        input.setText(data.getName());
                        new AlertDialog.Builder(HetDoorActivity.this).setTitle("请输入").
                                setIcon(android.R.drawable.ic_dialog_info).
                                setView(input).
                                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String fileName = input.getText().toString();
                                        if (!fileName.equalsIgnoreCase(data.getName())){
                                            data.setName(fileName);
                                            data.setSave("false");
                                        }
                                        if (data != null) {
                                            String tagetStr = data.getTag();
                                            if (tagetStr != null && !tagetStr.equals("")) {
                                                tagIdStr = tagetStr;
                                                showLog("当前门禁卡ID编号：" + tagIdStr);
                                            }
                                        }

                                        System.out.println(nData.toString());
                                        saveAndSendEmail(nData);
                                    }
                                }).
                                show();
                    }
                })
                .show();

    }


    private void saveAndSendEmail(List<TagModel> nListDta){
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

            showLog(sb.toString());
            if (sendEmail){
                sendEmail(sb.toString(), tagIdFilePath);
            }
        }
    }


    public void onReboot(View view){
        reboot();
    }

    private String chgStrings(String str){
        //???????70   ??????76
//NFA_DM_START_UP_CFG={45:CB:01:01:A5:01:01:CA:17:00:00:00:00:06:00:00:00:00:0F:00:00:00:00:E0:67:35:00:14:01:00:00:10:B5:03:01:02:FF:80:01:01:C9:03:03:0F:AB:5B:01:00:B2:04:E8:03:00:00:CF:02:02:08:B1:06:00:20:00:00:00:12:C2:02:00:C8}
        String chgStr = null;
        StringBuffer sb = new StringBuffer();
        if (str != null && !"".equals(str)){
            int startPos = startString.length()+2;
            int endPos = str.length()-1;
            int tagIdLen = tagIdStr.split(":").length;

            chgStr = str.substring(startPos,endPos);
            System.out.println("subString:" + chgStr);
            int len = chgStr.length();
            String[] line = chgStr.split(":");
            if (line == null || tagIdStr == null)
                return null;
            int nCount = line.length;
            showLog("NFC标签数据长度："+nCount);
            System.out.println("cur len:"+nCount);
            sb.append("NFA_DM_START_UP_CFG={");

            if (nCount == 70){
                //第一个字节代表数据长度，但是本身不在计算范围内
                int conCount = nCount + tagIdLen + 1;
                showLog("NFC数据第一字节："+conCount);
                String firstVale = Integer.toHexString(conCount);
                firstVale = firstVale.toUpperCase();
                System.out.println("first value:" + firstVale);
                for (int i = 0;i < line.length; i++){
                    if (i == 0){
                        line[i] = firstVale;
                    }
                    sb.append(line[i]);
                    sb.append(":");
                }
                sb.append("33");
                sb.append(":");

                String addLenStr = Integer.toHexString(tagIdLen);
                if (addLenStr.length() == 1) {
                    addLenStr = "0" + addLenStr;
                }
                sb.append(addLenStr);
                sb.append(":");

                sb.append(tagIdStr);

                sb.append("}");

            }else if(nCount == 76){
                //第一个字节代表数据长度，但是本身不在计算范围内
                String firstVale = Integer.toHexString(nCount-1);
                firstVale = firstVale.toUpperCase();
                showLog("NFC数据第一字节："+firstVale);
                System.out.println("first value:" + firstVale);
                for (int i = 0;i < line.length; i++){
                    if (i == 0){
                        line[i] = firstVale;
                    }else if (i == 70){
                        break;
                    }
                    sb.append(line[i]);
                    sb.append(":");
                }
                sb.append("33");
                sb.append(":");

                String addLenStr = Integer.toHexString(tagIdLen);
                if (addLenStr.length() == 1) {
                    addLenStr = "0" + addLenStr;
                }
                sb.append(addLenStr);
                sb.append(":");

                sb.append(tagIdStr);

                sb.append("}");
            }else{
                System.err.println("error len:"+nCount);
            }
        }
        chgStr = sb.toString();
        System.out.println("after chg:" + chgStr);
        return chgStr;
    }

//    private void initTagId(){
////        String tagIdStr = "B76850F6";
//        StringBuffer sb = new StringBuffer();
//        for (int j = 0;j < tagIdStr.length()/2; j++){
//            sb.append(tagIdStr.substring(j*2, j*2 + 2));
//            if(j < tagIdStr.length() / 2 -1 ){
//                sb.append(":");
//            }
//        }
//        tagIdStr = sb.toString();
//        System.out.println(sb.toString());
//    }

    public static void main(String[] args){
//        tagIdSetting();
    }
    private boolean isEx(){
        if (Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)){
            FilePath = Environment.getExternalStorageDirectory().getPath() + "/libnfc-brcm-20791b05.conf";
//            FilePath = "/system/etc/libnfc-brcm-20791b05.conf";
//            Runtime runtime = Runtime.getRuntime();
//            try {
//                runtime.exec("chmod 777 /system/etc/libnfc-brcm-20791b05.conf");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            System.out.println("~~~~~" + FilePath);
            return true;
        }
        return false;
    }

}