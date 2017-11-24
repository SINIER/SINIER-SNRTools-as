package com.bluetooth.modbus.snrtools2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ab.http.AbFileHttpResponseListener;
import com.ab.http.AbHttpUtil;
import com.ab.util.AbAppUtil;
import com.ab.util.AbFileUtil;
import com.ab.view.progress.AbHorizontalProgressBar;
import com.bluetooth.modbus.snrtools2.db.DBManager;
import com.bluetooth.modbus.snrtools2.db.Main;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.db.Var;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools2.uitls.AppUtil;
import com.bluetooth.modbus.snrtools2.uitls.CmdUtils;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;
import com.bluetooth.modbus.snrtools2.view.MainView;
import com.bluetooth.modbus.snrtools2.view.NoFocuseTextview;
import com.bluetooth.modbus.snrtools2.view.VarItemView;
import com.tencent.bugly.crashreport.CrashReport;

public class SNRMainActivity extends BaseActivity implements View.OnClickListener {

    // private Handler mHandler;
//    private Thread mThread;
    private NoFocuseTextview mTvAlarm;
    private LinearLayout mViewMore, llContent;
    private Button btnMore;
    private boolean isSetting = false;
    /**
     * 是否已经有命令发送
     */
    private boolean hasSend = false;
    private PopupWindow mPop;
    private AbHorizontalProgressBar mAbProgressBar;
    // 最大100
    private int max = 100;
    private int progress = 0;
    private TextView numberText, maxText;
    private AlertDialog mAlertDialog = null;
    private List<Main> mainList;
    private Main currentMain;
    private MainView mainView;
    private long totalSyncCount;
    private int click = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snr_main_activity);
        DBManager.getInstance().clearSession();
        mAbHttpUtil = AbHttpUtil.getInstance(this);
        mainList = DBManager.getInstance().getMainList();
        totalSyncCount = mainList.size() + AppStaticVar.mProductInfo.pdVarCount;
        initUI();
        setTitleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(click==0){
                    click = 10;
                    Intent intent = new Intent(mContext,DBDataActivity.class);
                    startActivity(intent);
                }
                click--;
            }
        });
        setTitleContent(AppStaticVar.mCurrentName);
        setRightButtonContent(getResources().getString(R.string.string_settings), R.id.btnRight1);
        hideRightView(R.id.view2);
        hideRightView(R.id.btnRight1);
        showRightView(R.id.rlMenu);
        initHandler();
    }

    @Override
    public void rightButtonOnClick(int id) {
        switch (id) {
            case R.id.btnRight1:
                isSetting = true;
                showProgressDialog(getResources().getString(R.string.string_progressmsg1));
                break;
            case R.id.rlMenu:
                showMenu(findViewById(id));
                break;
        }
    }

    private void dealVar(String result) {
        try {
            String str = result.substring(12,result.length()-4);
            String varHexNo = result.substring(4,8);
            for(int i=0;i<mViewMore.getChildCount();i++){
                if(mViewMore.getChildAt(i) instanceof  VarItemView){
                    VarItemView varItemView = (VarItemView) mViewMore.getChildAt(i);
                    Var var = (Var) varItemView.getTag();
                    if(var.getHexNo().equals(varHexNo)){
                        System.out.println("varHexNo=========="+varHexNo);
                        System.out.println("result=========="+result);
                        System.out.println("var=========="+var);
                        String value = AppUtil.getValueByType(var.getType(), var.getUnit(), var.getCount(), str, true);
                        varItemView.setValue(value);
                        break;
                    }
                }
            }
//            Var var = DBManager.getInstance().getVar(varHexNo);
//            if(var != null) {
//                System.out.println("varHexNo=========="+varHexNo);
//                System.out.println("result=========="+result);
//                System.out.println("var=========="+var);
//                String value = AppUtil.getValueByType(var.getType(), var.getUnit(), var.getCount(), str, true);
//                ((VarItemView)mViewMore.getChildAt(AppStaticVar.currentVarIndex-mainList.size())).setValue(value);
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startReadParam() {
                    System.out.println("====主页面开始恢复==pause状态" + AppStaticVar.isSNRMainPause);
                    if (!AppStaticVar.isSNRMainPause) {
                            if (AppStaticVar.currentVarIndex >= totalSyncCount) {
                                AppStaticVar.currentVarIndex = 0;
                            }
                        CrashReport.postCatchedException(new Throwable("====================currentVarIndex=="+AppStaticVar.currentVarIndex+"===mainList.size()="+mainList.size()+"===totalSyncCount=="+totalSyncCount));
                        System.out.println("====================currentVarIndex=="+AppStaticVar.currentVarIndex+"===mainList.size()="+mainList.size()+"===totalSyncCount=="+totalSyncCount);
                            if(AppStaticVar.currentVarIndex>=mainList.size()) {
                                String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentVarIndex-mainList.size()), 4, '0');
                                String cmd = "0x01 0x43 " + noHexStr + "0x00 0x00";
                                Var var = DBManager.getInstance().getVar(noHexStr);
                                CmdUtils.sendCmd(cmd,
                                        ("0".equals(var.getType())||"1".equals(var.getType())||"2".equals(var.getType())
                                                ||"3".equals(var.getType())||"4".equals(var.getType()))?20:24
                                        ,new CmdListener() {
                                    @Override
                                    public void start() {
                                        hasSend = true;
                                    }

                                    @Override
                                    public void result(String result) {
                                        System.out.println("====================更多变量=====接收到通过的数据"+result);
                                        CrashReport.postCatchedException(new Throwable("====================更多变量=====接收到通过的数据"+result));
                                        hasSend = false;
                                        dealVar(result);
                                        AppStaticVar.currentVarIndex++;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            startReadParam();
                                        }
                                    }

                                    @Override
                                    public void failure(String msg) {
                                        hasSend = false;
                                        AppStaticVar.currentVarIndex++;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            startReadParam();
                                        }
                                    }

                                    @Override
                                    public void timeOut(String msg) {
                                        hasSend = false;
                                        AppStaticVar.currentVarIndex++;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            showToast(getResources().getString(R.string.string_error_msg3));
                                            startReadParam();
                                        }
                                    }

                                    @Override
                                    public void connectFailure(String msg) {
                                        hasSend = false;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            showConnectDevice();
                                        }
                                    }

                                    @Override
                                    public void finish() {

                                    }
                                });
                            }else {

                                currentMain = mainList.get(AppStaticVar.currentVarIndex);
                                String cmd = "";
                                int backLength = 0;
                                if ("0".equals(currentMain.getType())) {
                                    cmd = "0x01 0x43 " + currentMain.getHexNo() + "0x00 0x00";
                                    Var var = DBManager.getInstance().getVar(currentMain.getHexNo());
                                    if(var == null){
                                        AppStaticVar.currentVarIndex++;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            startReadParam();
                                        }
                                        return;
                                    }
                                    backLength = ("0".equals(var.getType())||"1".equals(var.getType())||"2".equals(var.getType())
                                            ||"3".equals(var.getType())||"4".equals(var.getType()))?20:24;
                                } else if ("1".equals(currentMain.getType())) {
                                    cmd = "0x01 0x44 " + currentMain.getHexNo() + "0x00 0x00";
                                    Param var = DBManager.getInstance().getParam(currentMain.getHexNo());
                                    if(var == null){
                                        AppStaticVar.currentVarIndex++;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            startReadParam();
                                        }
                                        return;
                                    }
                                    backLength = ("0".equals(var.getType())||"1".equals(var.getType())||"2".equals(var.getType())
                                            ||"3".equals(var.getType())||"4".equals(var.getType()))?20:24;
                                } else {
                                    AppStaticVar.currentVarIndex++;
                                    if (!AppStaticVar.isSNRMainPause) {
                                        startReadParam();
                                    }
                                    return;
                                }

                                CmdUtils.sendCmd(cmd,
                                        backLength
                                        ,new CmdListener() {
                                    @Override
                                    public void start() {
                                        hasSend = true;
                                    }

                                    @Override
                                    public void result(String result) {
                                        hasSend = false;
                                        if ("0".equals(currentMain.getType())) {
                                            CrashReport.postCatchedException(new Throwable("==============主屏===变量========接收到通过的数据"+result));
                                            System.out.println("==============主屏===变量========接收到通过的数据"+result);
                                            try {
                                                String str = result.substring(12, result.length() - 4);
                                                String varHexNo = result.substring(4, 8);
                                                Var var = DBManager.getInstance().getVar(varHexNo);
                                                if (var != null) {
                                                    String value = AppUtil.getValueByType(var.getType(), var.getUnit(), var.getCount(), str, false);
                                                    currentMain.setValue(value);
                                                    currentMain.setUnitStr(DBManager.getInstance().getStr(var.getUnit()));
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                CrashReport.postCatchedException(new Throwable("=================主屏变量========"+e.toString()));
                                                System.out.println("=================主屏变量========"+e.toString());
                                            }
                                        } else if ("1".equals(currentMain.getType())) {
                                            CrashReport.postCatchedException(new Throwable("===========主屏===参数===========接收到通过的数据"+result));
                                            System.out.println("===========主屏===参数===========接收到通过的数据"+result);
                                            try {
                                                Param param = DBManager.getInstance().getParam(currentMain.getHexNo());
                                                String str = result.substring(12, result.length() - 4);
                                                String value = AppUtil.getValueByType(param.getType(), param.getUnit(), param.getCount(), str, false);
                                                currentMain.setValue(value);
                                                currentMain.setUnitStr(DBManager.getInstance().getStr(param.getUnit()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                CrashReport.postCatchedException(new Throwable("====================主屏参数====="+e.toString()));
                                                System.out.println("====================主屏参数====="+e.toString());
                                            }
                                        }
                                        mainView.notifyDataSetChange();
                                        AppStaticVar.currentVarIndex++;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            startReadParam();
                                        }
                                    }

                                    @Override
                                    public void failure(String msg) {
                                        hasSend = false;
                                        AppStaticVar.currentVarIndex++;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            startReadParam();
                                        }
                                    }

                                    @Override
                                    public void timeOut(String msg) {
                                        hasSend = false;
                                        AppStaticVar.currentVarIndex++;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            showToast(getResources().getString(R.string.string_error_msg3));
                                            startReadParam();
                                        }
                                    }

                                    @Override
                                    public void connectFailure(String msg) {
                                        hasSend = false;
                                        if (!AppStaticVar.isSNRMainPause) {
                                            showConnectDevice();
                                        }
                                    }

                                    @Override
                                    public void finish() {

                                    }
                                });
                            }
                    }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMore:
                if (mViewMore.getVisibility() == View.VISIBLE) {
                    mViewMore.setVisibility(View.GONE);
                    ((Button) v).setText(getResources().getString(R.string.string_more));
                } else {
                    mViewMore.setVisibility(View.VISIBLE);
                    ((Button) v).setText(getResources().getString(R.string.string_shouqi));
                }
                break;

            case R.id.textView1:// 新功能
                hideMenu();
                showDialogOne(getResources().getString(R.string.string_menu_msg1), null);
                break;
            case R.id.textView2:// 关于
                hideMenu();
                showDialogOne(getResources().getString(R.string.string_menu_msg2), null);
                break;
            case R.id.textView3:// 版本更新
                hideMenu();
                downloadXml();
                break;
            case R.id.textView4:// 退出
                hideMenu();
                exitApp();
                break;
            case R.id.textView5:// 清除缓存
                hideMenu();
                AbFileUtil.deleteFile(new File(AbFileUtil.getFileDownloadDir(mContext)));
                AbFileUtil.deleteFile(new File(Constans.Directory.DOWNLOAD));
                break;

        }
    }

    private void showMenu(View v) {
        if (mPop == null) {
            View contentView = View.inflate(this, R.layout.main_menu, null);
            mPop = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mPop.setBackgroundDrawable(new BitmapDrawable());
            mPop.setOutsideTouchable(true);
            mPop.setFocusable(true);
        }
        mPop.showAsDropDown(v, R.dimen.menu_x, 20);
    }

    private void hideMenu() {
        if (mPop != null && mPop.isShowing()) {
            mPop.dismiss();
        }
    }

    private void downloadXml() {
        String url = "http://www.sinier.com.cn/download/beta2/version.xml";
        mAbHttpUtil.get(url, new AbFileHttpResponseListener(url) {
            // 获取数据成功会调用这里
            @Override
            public void onSuccess(int statusCode, File file) {
                int version = 0;
                String url = "";
                String md5 = "";
                XmlPullParser xpp = Xml.newPullParser();
                try {
                    xpp.setInput(new FileInputStream(file), "utf-8");

                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                if ("version".equals(xpp.getName())) {
                                    try {
                                        version = Integer.parseInt(xpp.nextText());
                                    } catch (NumberFormatException e1) {
                                        e1.printStackTrace();
                                        showToast(getResources().getString(R.string.string_error_msg1));
                                    }
                                }
                                if ("url".equals(xpp.getName())) {
                                    url = xpp.nextText();
                                }
                                if ("MD5".equals(xpp.getName())) {
                                    md5 = xpp.nextText();
                                }
                                break;
                            default:
                                break;
                        }
                        eventType = xpp.next();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PackageManager manager;
                PackageInfo info = null;
                manager = getPackageManager();
                try {
                    info = manager.getPackageInfo(getPackageName(), 0);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (version != info.versionCode) {
                    String fileName = url.substring(url.lastIndexOf("/") + 1);
                    File apk = new File(Constans.Directory.DOWNLOAD + fileName);
                    if (md5.equals(AppUtil.getFileMD5(apk))) {
                        // Intent intent = new Intent(Intent.ACTION_VIEW);
                        // intent.setDataAndType(Uri.fromFile(apk),
                        // "application/vnd.android.package-archive");
                        // startActivity(intent);
                        AbAppUtil.installApk(mContext, apk);
                        return;
                    }
                    try {
                        if (!apk.getParentFile().exists()) {
                            apk.getParentFile().mkdirs();
                        }
                        apk.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mAbHttpUtil.get(url, new AbFileHttpResponseListener(apk) {
                        public void onSuccess(int statusCode, File file) {
                            // Intent intent = new Intent(Intent.ACTION_VIEW);
                            // intent.setDataAndType(Uri.fromFile(file),
                            // "application/vnd.android.package-archive");
                            // startActivity(intent);
                            AbAppUtil.installApk(mContext, file);
                        }

                        ;

                        // 开始执行前
                        @Override
                        public void onStart() {
                            // 打开进度框
                            View v = LayoutInflater.from(mContext).inflate(R.layout.progress_bar_horizontal, null, false);
                            mAbProgressBar = (AbHorizontalProgressBar) v.findViewById(R.id.horizontalProgressBar);
                            numberText = (TextView) v.findViewById(R.id.numberText);
                            maxText = (TextView) v.findViewById(R.id.maxText);

                            maxText.setText(progress + "/" + String.valueOf(max) + "%");
                            mAbProgressBar.setMax(max);
                            mAbProgressBar.setProgress(progress);

                            mAlertDialog = showDialog(getResources().getString(R.string.string_progressmsg2), v);
                        }

                        // 失败，调用
                        @Override
                        public void onFailure(int statusCode, String content, Throwable error) {
                            showToast(error.getMessage());
                        }

                        // 下载进度
                        @Override
                        public void onProgress(long bytesWritten, long totalSize) {
                            if (totalSize / max == 0) {
                                onFinish();
                                showToast(getResources().getString(R.string.string_error_msg2));
                                return;
                            }
                            maxText.setText(bytesWritten / (totalSize / max) + "/" + max + "%");
                            mAbProgressBar.setProgress((int) (bytesWritten / (totalSize / max)));
                        }

                        // 完成后调用，失败，成功
                        public void onFinish() {
                            // 下载完成取消进度框
                            if (mAlertDialog != null) {
                                mAlertDialog.cancel();
                                mAlertDialog = null;
                            }

                        }

                        ;
                    });
                } else {
                    showToast(getResources().getString(R.string.string_tips_msg1));
                }

            }

            // 开始执行前
            @Override
            public void onStart() {
                // 打开进度框
                View v = LayoutInflater.from(mContext).inflate(R.layout.progress_bar_horizontal, null, false);
                mAbProgressBar = (AbHorizontalProgressBar) v.findViewById(R.id.horizontalProgressBar);
                numberText = (TextView) v.findViewById(R.id.numberText);
                maxText = (TextView) v.findViewById(R.id.maxText);

                maxText.setText(progress + "/" + String.valueOf(max) + "%");
                mAbProgressBar.setMax(max);
                mAbProgressBar.setProgress(progress);

                mAlertDialog = showDialog(getResources().getString(R.string.string_progressmsg2), v);
            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, String content, Throwable error) {
                showToast(error.getMessage());
            }

            // 下载进度
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                if (totalSize / max == 0) {
                    onFinish();
                    showToast(getResources().getString(R.string.string_error_msg2));
                    return;
                }
                maxText.setText(bytesWritten / (totalSize / max) + "/" + max + "%");
                mAbProgressBar.setProgress((int) (bytesWritten / (totalSize / max)));
            }

            // 完成后调用，失败，成功
            public void onFinish() {
                // 下载完成取消进度框
                if (mAlertDialog != null) {
                    mAlertDialog.cancel();
                    mAlertDialog = null;
                }
            }
        });
    }

    private void initUI() {
        mainView = (MainView) findViewById(R.id.mainview);
        btnMore = (Button) findViewById(R.id.btnMore);
        btnMore.setOnClickListener(this);
        mViewMore = (LinearLayout) findViewById(R.id.llMore);
        llContent = (LinearLayout) findViewById(R.id.llContent);
        mTvAlarm = (NoFocuseTextview) findViewById(R.id.tvAlarm);
        mTvAlarm.setVisibility(View.GONE);
        mTvAlarm.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_alpha));
        mainView.setValues(mainList);
        if(AppStaticVar.mProductInfo!= null) {
            for (int i = 0; i < AppStaticVar.mProductInfo.pdVarCount; i++) {
                Var var = DBManager.getInstance().getVar(NumberBytes.padLeft(Integer.toHexString(i),4,'0'));
                VarItemView varItemView = new VarItemView(mContext);
//                varItemView.hideLabel();
                varItemView.setLabel(var.getName());
                varItemView.setValue(getString(R.string.string_tips_msg2));
                varItemView.setTag(var);
                CrashReport.postCatchedException(new Throwable("==============var=="+var));
                if (i == AppStaticVar.mProductInfo.pdVarCount - 1) {
                    varItemView.setBottomLineStatus(false);
                }
                mViewMore.addView(varItemView);
            }
        }
    }

    private void hasAlarm(String s) {
        if (mTvAlarm.getVisibility() != View.VISIBLE) {
            mTvAlarm.setVisibility(View.VISIBLE);
        }
        if (!mTvAlarm.getText().toString().contains(s)) {
            mTvAlarm.setText(mTvAlarm.getText() + " " + s);
        }
    }

    private void hasNoAlarm(String s) {
        mTvAlarm.setText(mTvAlarm.getText().toString().replace(" " + s, ""));
        if (TextUtils.isEmpty(mTvAlarm.getText().toString().trim())) {
            mTvAlarm.setVisibility(View.GONE);
        }
    }

//    @Override
//    public void handleMessage(Activity activity, Message msg, String name) {
//        super.handleMessage(activity, msg, name);
//
//        switch (msg.what) {
//            case Constans.CONTACT_START:
//                System.out.println(name + "开始读取数据=====");
//                break;
//            case Constans.NO_DEVICE_CONNECTED:
//                System.out.println(name + "连接失败=====");
//                hasSend = false;
//                if (!AppStaticVar.isSNRMainPause) {
//                    showConnectDevice();
//                }
//                break;
//            case Constans.DEVICE_RETURN_MSG:
//                System.out.println(name + "收到数据=====" + msg.obj.toString());
//                hasSend = false;
//                if (!AppStaticVar.isSNRMainPause) {
//                    startReadParam();
//                }
//                break;
//            case Constans.CONNECT_IS_CLOSED:
//                System.out.println(name + "连接关闭=====");
//                hasSend = false;
//                showConnectDevice();
//            case Constans.ERROR_START:
//                System.out.println(name + "接收数据错误=====");
//                hasSend = false;
//                if (!AppStaticVar.isSNRMainPause) {
//                    startReadParam();
//                }
//                break;
//            case Constans.TIME_OUT:
//                System.out.println(name + "连接超时=====");
//                hasSend = false;
////                if (mThread != null && !mThread.isInterrupted()) {
////                    mThread.interrupt();
////                }
//                if (!AppStaticVar.isSNRMainPause) {
//                    showToast(getResources().getString(R.string.string_error_msg3));
//                    startReadParam();
//                }
//                break;
//        }
//
//    }

    private void initHandler() {
        mInnerHandler = new InnerHandler(this, "主页面");
    }

    @Override
    public void reconnectSuccss() {
        startReadParam();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppStaticVar.isSNRMainPause = false;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startReadParam();
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppStaticVar.isSNRMainPause = true;
    }
}
