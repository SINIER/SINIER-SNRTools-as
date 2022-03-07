package com.bluetooth.modbus.snrtools2;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ab.http.AbFileHttpResponseListener;
import com.ab.http.AbHttpUtil;
import com.ab.util.AbAppUtil;
import com.ab.util.AbFileUtil;
import com.ab.view.progress.AbHorizontalProgressBar;
import com.bluetooth.modbus.snrtools2.bean.RunStatus;
import com.bluetooth.modbus.snrtools2.db.DBManager;
import com.bluetooth.modbus.snrtools2.db.Main;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.db.Var;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools2.uitls.AppUtil;
import com.bluetooth.modbus.snrtools2.uitls.CmdUtils;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;
import com.bluetooth.modbus.snrtools2.view.ErrorItemView;
import com.bluetooth.modbus.snrtools2.view.MainView;
import com.bluetooth.modbus.snrtools2.view.NoFocuseTextview;
import com.bluetooth.modbus.snrtools2.view.VarItemView;
import com.tencent.bugly.crashreport.CrashReport;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class SNRMainActivity extends BaseActivity implements View.OnClickListener
{
    // private Handler mHandler;
//    private Thread mThread;
    private NoFocuseTextview mTvAlarm;
    private LinearLayout mViewMore, llWarnContent,llWarn;
    private RelativeLayout rlWarn;
    private Button btnMore;
    private TextView tvWarn,tvWarnCount;
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
    private RunStatus mRunStatus = new RunStatus();
    private boolean isFirst = true;
    /**
     * 运行状态
     */
    private static final int STATUS_COUNT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snr_main_activity);
        DBManager.getInstance().clearSession();
        mAbHttpUtil = AbHttpUtil.getInstance(this);
        mainList = DBManager.getInstance().getMainList();
        totalSyncCount = mainList.size() + AppStaticVar.mProductInfo.pdVarCount + STATUS_COUNT;
        AppStaticVar.currentVarIndex = 0;
        initUI();
        setTitleClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (click == 0) {
                    click = 10;
                    Intent intent = new Intent(mContext, DBDataActivity.class);
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
    public void rightButtonOnClick(int id)
    {
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if(isFirst)
        {
            isFirst = false;
            LayoutParams layoutParams = mainView.getLayoutParams();
            layoutParams.height = mainView.getWidth()/5*3;
            mainView.setLayoutParams(layoutParams);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void dealVar(String result)
    {
        try {
            String str = result.substring(12, result.length() - 4);
            String varHexNo = result.substring(4, 8);
            for (int i = 0; i < mViewMore.getChildCount(); i++)
            {
                if (mViewMore.getChildAt(i) instanceof VarItemView)
                {
                    VarItemView varItemView = (VarItemView) mViewMore.getChildAt(i);
                    Var var = (Var) varItemView.getTag();
                    if (var.getHexNo().equals(varHexNo))
                    {
                        System.out.println("varHexNo==========" + varHexNo);
                        System.out.println("result==========" + result);
                        System.out.println("var==========" + var);
                        String value = AppUtil.getValueByType(var.getType(), var.getUnit(), var.getCount(), str, true);
                        varItemView.setValue(value);
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void dealError(String result)
    {
        try
        {
            String str = result.substring(result.length() - 6, result.length() - 4)+result.substring(result.length() - 8, result.length() - 6);
            String errorHexNo = result.substring(4, 8);
            String value = DBManager.getInstance().getStr(str);
            if(TextUtils.isEmpty(value))
            {
                return;
            }
            boolean has = false;
            for (int i = 0; i < llWarnContent.getChildCount(); i++)
            {
                if (llWarnContent.getChildAt(i) instanceof ErrorItemView)
                {
                    ErrorItemView errorItemView = (ErrorItemView) llWarnContent.getChildAt(i);
                    Var var = (Var) errorItemView.getTag();
                    if (var.getHexNo().toLowerCase().equals(errorHexNo))
                    {
                        System.out.println("errorHexNo==========" + errorHexNo);
                        System.out.println("result==========" + result);
                        System.out.println("var==========" + var);
                        errorItemView.setValue(value);
                        has = true;
                        break;
                    }
                }
            }
            if (!has)
            {
                Var var = new Var();
                var.setHexNo(errorHexNo);
                ErrorItemView errorItemView = new ErrorItemView(mContext);
                errorItemView.setValue(DBManager.getInstance().getStr(str));
                errorItemView.setValueColor(Color.WHITE);
                errorItemView.setBackGroundColor(Color.RED);
                errorItemView.setTag(var);
                if (AppStaticVar.currentVarIndex == mRunStatus.errorCount - 1)
                {
                    errorItemView.setBottomLineStatus(false);
                }
                llWarnContent.addView(errorItemView);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void startReadParam()
    {
        System.out.println("====主页面开始恢复==pause状态" + AppStaticVar.isSNRMainPause);
        if (!AppStaticVar.isSNRMainPause)
        {
            // 读故障信息
            if (llWarn.getVisibility() == View.VISIBLE)
            {
                if (AppStaticVar.currentVarIndex >= mRunStatus.errorCount)
                {
                    AppStaticVar.currentVarIndex = 0;
                }
                System.out.println("====================currentVarIndex==" + AppStaticVar.currentVarIndex + "===errorCount=" + mRunStatus.errorCount);
                String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentVarIndex), 4, '0');
                String cmd = "0x01 0x52 " + noHexStr + "0x00 0x00";
                CmdUtils.sendCmd(cmd, 20, new CmdListener()
                        {
                            @Override
                            public void start()
                            {
                                hasSend = true;
                            }

                            @Override
                            public void result(String result)
                            {
                                System.out.println("====================故障信息=====接收到通过的数据" + result);
                                hasSend = false;
                                dealError(result);
                                AppStaticVar.currentVarIndex++;
                                if (!AppStaticVar.isSNRMainPause)
                                {
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
                            public void finish()
                            {

                            }
                        });
            } else {
                llWarnContent.removeAllViews();
                // 读一个变量
                if (AppStaticVar.currentVarIndex >= totalSyncCount)
                {
                    AppStaticVar.currentVarIndex = 0;
                }
                System.out.println("====================currentVarIndex==" + AppStaticVar.currentVarIndex + "===mainList.size()=" + mainList.size() + "===pdCount" + AppStaticVar.mProductInfo.pdVarCount + "===totalSyncCount==" + totalSyncCount);
                if (AppStaticVar.currentVarIndex >= mainList.size() + STATUS_COUNT)
                {
                    String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentVarIndex - mainList.size() - STATUS_COUNT), 4, '0');
                    String cmd = "0x01 0x43 " + noHexStr + "0x00 0x00";
                    Var var = DBManager.getInstance().getVar(noHexStr);
                    int backLength = ("0".equals(var.getType()) || "1".equals(var.getType()) || "2".equals(var.getType())
                            || "3".equals(var.getType()) || "4".equals(var.getType())) ? 20 : 24;
                    if("9".equals(var.getType())){
                        backLength = 2 * (AppUtil.parseToInt(var.getCount(), 0) + 8);//48;
                    }
                    CmdUtils.sendCmd(cmd,backLength, new CmdListener() {
                                @Override
                                public void start() {
                                    hasSend = true;
                                }

                                @Override
                                public void result(String result)
                                {
                                    hasSend = false;
                                    dealVar(result);
                                    AppStaticVar.currentVarIndex++;
                                    if (!AppStaticVar.isSNRMainPause)
                                    {
                                        startReadParam();
                                    }
                                }

                                @Override
                                public void failure(String msg)
                                {
                                    hasSend = false;
                                    AppStaticVar.currentVarIndex++;
                                    if (!AppStaticVar.isSNRMainPause)
                                    {
                                        startReadParam();
                                    }
                                }

                                @Override
                                public void timeOut(String msg)
                                {
                                    hasSend = false;
                                    AppStaticVar.currentVarIndex++;
                                    if (!AppStaticVar.isSNRMainPause) {
                                        showToast(getResources().getString(R.string.string_error_msg3));
                                        startReadParam();
                                    }
                                }

                                @Override
                                public void connectFailure(String msg)
                                {
                                    hasSend = false;
                                    if (!AppStaticVar.isSNRMainPause)
                                    {
                                        showConnectDevice();
                                    }
                                }

                                @Override
                                public void finish()
                                {

                                }
                            });
                } else if (AppStaticVar.currentVarIndex >= STATUS_COUNT && AppStaticVar.currentVarIndex < mainList.size() + STATUS_COUNT)
                {
                    currentMain = mainList.get(AppStaticVar.currentVarIndex - STATUS_COUNT);
                    String cmd = "";
                    int backLength = 0;
                    if ("0".equals(currentMain.getType()))
                    {
                        cmd = "0x01 0x43 " + currentMain.getHexNo() + "0x00 0x00";
                        Var var = DBManager.getInstance().getVar(currentMain.getHexNo());
                        if (var == null)
                        {
                            AppStaticVar.currentVarIndex++;
                            if (!AppStaticVar.isSNRMainPause)
                            {
                                startReadParam();
                            }
                            return;
                        }
                        backLength = ("0".equals(var.getType()) || "1".equals(var.getType()) || "2".equals(var.getType())
                                || "3".equals(var.getType()) || "4".equals(var.getType())) ? 20 : 24;
                        if("9".equals(var.getType())){
                            backLength = 2 * (AppUtil.parseToInt(var.getCount(), 0) + 8);//48;
                        }
                    } else if ("1".equals(currentMain.getType()))
                    {
                        cmd = "0x01 0x44 " + currentMain.getHexNo() + "0x00 0x00";
                        Param var = DBManager.getInstance().getParam(currentMain.getHexNo());
                        if (var == null)
                        {
                            AppStaticVar.currentVarIndex++;
                            if (!AppStaticVar.isSNRMainPause)
                            {
                                startReadParam();
                            }
                            return;
                        }
                        backLength = ("0".equals(var.getType()) || "1".equals(var.getType()) || "2".equals(var.getType())
                                || "3".equals(var.getType()) || "4".equals(var.getType())) ? 20 : 24;
                        if("9".equals(var.getType())){
                            backLength = 2 * (AppUtil.parseToInt(var.getCount(), 0) + 8);//48;
                        }
                    } else {
                        AppStaticVar.currentVarIndex++;
                        if (!AppStaticVar.isSNRMainPause)
                        {
                            startReadParam();
                        }
                        return;
                    }

                    CmdUtils.sendCmd(cmd, backLength, new CmdListener() {
                                @Override
                                public void start() {
                                    hasSend = true;
                                }

                                @Override
                                public void result(String result) {
                                    hasSend = false;
                                    if ("0".equals(currentMain.getType())) {
                                        System.out.println("==============主屏===变量========接收到通过的数据" + result);
                                        try {
                                            String str = result.substring(12, result.length() - 4);
                                            String varHexNo = result.substring(4, 8);
                                            Var var = DBManager.getInstance().getVar(varHexNo);
                                            if (var != null) {
                                                var.setCount(currentMain.getCount());
                                                String value = AppUtil.getValueByType(var.getType(), var.getUnit(), var.getCount(), str, false);
                                                currentMain.setValue(value);
                                                if (!"0".equals(var.getType())) {//选项型的不存在单位
                                                    currentMain.setUnitStr(DBManager.getInstance().getStr(var.getUnit()));
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            CrashReport.postCatchedException(new Throwable("=================主屏变量========" + e.toString()));
                                            System.out.println("=================主屏变量========" + e.toString());
                                        }
                                    } else if ("1".equals(currentMain.getType())) {
                                        System.out.println("===========主屏===参数===========接收到通过的数据" + result);
                                        try {
                                            Param param = DBManager.getInstance().getParam(currentMain.getHexNo());
                                            param.setCount(currentMain.getCount());
                                            String str = result.substring(12, result.length() - 4);
                                            String value = AppUtil.getValueByType(param.getType(), param.getUnit(), param.getCount(), str, false);
                                            currentMain.setValue(value);
                                            if (!"0".equals(param.getType())) {//选项型的不存在单位
                                                currentMain.setUnitStr(DBManager.getInstance().getStr(param.getUnit()));
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            CrashReport.postCatchedException(new Throwable("====================主屏参数=====" + e.toString()));
                                            System.out.println("====================主屏参数=====" + e.toString());
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
                } else if (AppStaticVar.currentVarIndex < STATUS_COUNT) {
                    // 读主控运行状态字
                    String cmd = "0x01 0x41 0x00 0x00 0x00 0x00";
                    CmdUtils.sendCmd(cmd, 20, new CmdListener()
                            {
                                @Override
                                public void start() {
                                    hasSend = true;
                                }

                                @Override
                                public void result(String result)
                                {
                                    System.out.println("==================告警===" + result);
                                    hasSend = false;
//                                                01c100000002 0584 f3f8
                                    String temp = NumberBytes.padLeft(Long.toBinaryString(Long.parseLong(result.substring(14, 16) + result.substring(12, 14), 16)), 16, '0');
                                    String errorFlag = Integer.parseInt(temp.substring(0, 1), 2) + "";
                                    String workModel = Integer.parseInt("0" + temp.substring(1, 4), 2) + "";
                                    int errorCount = Integer.parseInt(temp.substring(4, 8), 2);
                                    int logCount = Integer.parseInt(temp.substring(8, 16), 2);
                                    System.out.println("=======errFlag" + errorFlag + "==work" + workModel + "==errCount" + errorCount + "==logCount" + logCount);
                                    mRunStatus.isError = "1".equals(errorFlag);
                                    mRunStatus.workModel = workModel;
                                    mRunStatus.errorCount = errorCount;
                                    mRunStatus.logCount = logCount;
                                    tvWarnCount.setText(getString(R.string.warn_info));
                                    if(errorCount>0)
                                    {
                                        tvWarnCount.setText(errorCount+"");
                                    }
                                    else
                                    {
                                        tvWarnCount.setText("");
                                    }
                                    AppStaticVar.currentVarIndex++;
                                    if (!AppStaticVar.isSNRMainPause)
                                    {
                                        startReadParam();
                                    }
                                }

                                @Override
                                public void failure(String msg)
                                {
                                    hasSend = false;
                                    AppStaticVar.currentVarIndex++;
                                    if (!AppStaticVar.isSNRMainPause)
                                    {
                                        startReadParam();
                                    }
                                }

                                @Override
                                public void timeOut(String msg)
                                {
                                    hasSend = false;
                                    AppStaticVar.currentVarIndex++;
                                    if (!AppStaticVar.isSNRMainPause)
                                    {
                                        showToast(getResources().getString(R.string.string_error_msg3));
                                        startReadParam();
                                    }
                                }

                                @Override
                                public void connectFailure(String msg)
                                {
                                    hasSend = false;
                                    if (!AppStaticVar.isSNRMainPause)
                                    {
                                        showConnectDevice();
                                    }
                                }

                                @Override
                                public void finish()
                                {

                                }
                            });
                }
            }
        }
    }

    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.btnMore:
                if (mViewMore.getVisibility() == View.VISIBLE)
                {
                    mViewMore.setVisibility(View.GONE);
                    findViewById(R.id.llPdInfo).setVisibility(View.VISIBLE);
                    btnMore.setText(getResources().getString(R.string.string_more));
                }
                else
                {
                    mViewMore.setVisibility(View.VISIBLE);
                    llWarn.setVisibility(View.GONE);
                    findViewById(R.id.llPdInfo).setVisibility(View.GONE);
                    btnMore.setText(getResources().getString(R.string.string_shouqi));
                }
                break;
            case R.id.rlWarn:
                if(mRunStatus.errorCount == 0)
                {
                    return;
                }
                if (llWarn.getVisibility() == View.VISIBLE)
                {
                    llWarn.setVisibility(View.GONE);
                    mViewMore.setVisibility(View.GONE);
                    findViewById(R.id.llPdInfo).setVisibility(View.VISIBLE);
                    btnMore.setText(getResources().getString(R.string.string_more));
                }
                else
                {
                    llWarn.setVisibility(View.VISIBLE);
                    mViewMore.setVisibility(View.GONE);
                    findViewById(R.id.llPdInfo).setVisibility(View.GONE);
                    btnMore.setText(getResources().getString(R.string.string_more));
                }
                break;
            case R.id.textView1:// 新功能
                hideMenu();
                showDialogOne(getResources().getString(R.string.string_menu_msg1), null);
                break;
            case R.id.textView2:// 关于
                hideMenu();
                showDialogOne(String.format(getResources().getString(R.string.string_menu_msg2),AbAppUtil.getPackageInfo(this).versionName), null);
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
            case R.id.textView6:// 日志
                hideMenu();
                Intent log = new Intent(mContext,LogActivity.class);
                log.putExtra("totalSyncCount",mRunStatus.logCount);
                startActivity(log);
                break;

        }
    }

    private void showMenu(View v)
    {
        if (mPop == null)
        {
            View contentView = View.inflate(this, R.layout.main_menu2, null);
            mPop = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mPop.setBackgroundDrawable(new BitmapDrawable());
            mPop.setOutsideTouchable(true);
            mPop.setFocusable(true);
        }
        mPop.showAsDropDown(v, R.dimen.menu_x, 20);
    }

    private void hideMenu()
    {
        if (mPop != null && mPop.isShowing())
        {
            mPop.dismiss();
        }
    }

    private void downloadXml()
    {
        String url = "http://www.sinier.com.cn/download/SNRToolsV2/version.xml";
        mAbHttpUtil.get(url, new AbFileHttpResponseListener(url)
        {
            // 获取数据成功会调用这里
            @Override
            public void onSuccess(int statusCode, File file)
            {
                int version = 0;
                String url = "";
                String md5 = "";
                XmlPullParser xpp = Xml.newPullParser();
                try
                {
                    xpp.setInput(new FileInputStream(file), "utf-8");
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT)
                    {
                        switch (eventType)
                        {
                            case XmlPullParser.START_TAG:
                                if ("version".equals(xpp.getName()))
                                {
                                    try
                                    {
                                        version = Integer.parseInt(xpp.nextText());
                                    }
                                    catch (NumberFormatException e1)
                                    {
                                        e1.printStackTrace();
                                        showToast(getResources().getString(R.string.string_error_msg1));
                                    }
                                }
                                if ("url".equals(xpp.getName()))
                                {
                                    url = xpp.nextText();
                                }
                                if ("MD5".equals(xpp.getName()))
                                {
                                    md5 = xpp.nextText();
                                }
                                break;
                            default:
                                break;
                        }
                        eventType = xpp.next();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                PackageManager manager;
                PackageInfo info = null;
                manager = getPackageManager();
                try
                {
                    info = manager.getPackageInfo(getPackageName(), 0);
                }
                catch (NameNotFoundException e)
                {
                    e.printStackTrace();
                }
                if (version != info.versionCode)
                {
                    String fileName = url.substring(url.lastIndexOf("/") + 1);
                    File apk = new File(Constans.Directory.DOWNLOAD + fileName);
                    if (md5.equals(AppUtil.getFileMD5(apk)))
                    {
                        // Intent intent = new Intent(Intent.ACTION_VIEW);
                        // intent.setDataAndType(Uri.fromFile(apk),
                        // "application/vnd.android.package-archive");
                        // startActivity(intent);
                        AbAppUtil.installApk(mContext, apk);
                        return;
                    }
                    try
                    {
                        if (!apk.getParentFile().exists())
                        {
                            apk.getParentFile().mkdirs();
                        }
                        apk.createNewFile();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    mAbHttpUtil.get(url, new AbFileHttpResponseListener(apk)
                    {
                        public void onSuccess(int statusCode, File file)
                        {
                            // Intent intent = new Intent(Intent.ACTION_VIEW);
                            // intent.setDataAndType(Uri.fromFile(file),
                            // "application/vnd.android.package-archive");
                            // startActivity(intent);
                            AbAppUtil.installApk(mContext, file);
                        }

                        ;

                        // 开始执行前
                        @Override
                        public void onStart()
                        {
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
                        public void onFailure(int statusCode, String content, Throwable error)
                        {
                            showToast(error.getMessage());
                        }

                        // 下载进度
                        @Override
                        public void onProgress(long bytesWritten, long totalSize)
                        {
                            if (totalSize / max == 0)
                            {
                                onFinish();
                                showToast(getResources().getString(R.string.string_error_msg2));
                                return;
                            }
                            maxText.setText(bytesWritten / (totalSize / max) + "/" + max + "%");
                            mAbProgressBar.setProgress((int) (bytesWritten / (totalSize / max)));
                        }

                        // 完成后调用，失败，成功
                        public void onFinish()
                        {
                            // 下载完成取消进度框
                            if (mAlertDialog != null)
                            {
                                mAlertDialog.cancel();
                                mAlertDialog = null;
                            }
                        }
                      ;
                    });
                }
                else
                    {
                    showToast(getResources().getString(R.string.string_tips_msg1));
                }

            }

            // 开始执行前
            @Override
            public void onStart()
            {
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
            public void onFailure(int statusCode, String content, Throwable error)
            {
                showToast(error.getMessage());
            }

            // 下载进度
            @Override
            public void onProgress(long bytesWritten, long totalSize)
            {
                if (totalSize / max == 0)
                {
                    onFinish();
                    showToast(getResources().getString(R.string.string_error_msg2));
                    return;
                }
                maxText.setText(bytesWritten / (totalSize / max) + "/" + max + "%");
                mAbProgressBar.setProgress((int) (bytesWritten / (totalSize / max)));
            }

            // 完成后调用，失败，成功
            public void onFinish()
            {
                // 下载完成取消进度框
                if (mAlertDialog != null)
                {
                    mAlertDialog.cancel();
                    mAlertDialog = null;
                }
            }
        });
    }

    private void initUI() {
        ((TextView) findViewById(R.id.tvModel)).setText(getString(R.string.pd_model) + AppStaticVar.mProductInfo.pdModel);
        ((TextView) findViewById(R.id.tvVersion)).setText(getString(R.string.pd_version) + AppStaticVar.mProductInfo.pdVersion);
        ((TextView) findViewById(R.id.tvTime)).setText(getString(R.string.pd_build_time) + AppStaticVar.mProductInfo.pdBuildTime);
        ((TextView) findViewById(R.id.tvNo)).setText(getString(R.string.pd_sn) + "20" + AppStaticVar.mProductInfo.pdSN);
        mainView = (MainView) findViewById(R.id.mainview);
        btnMore = (Button) findViewById(R.id.btnMore);
        btnMore.setOnClickListener(this);
        rlWarn = (RelativeLayout) findViewById(R.id.rlWarn);
        rlWarn.setOnClickListener(this);
        tvWarnCount = (TextView) findViewById(R.id.tvWarnCount);
        tvWarn = (TextView) findViewById(R.id.tvWarn);
        mViewMore = (LinearLayout) findViewById(R.id.llMore);
        llWarn = (LinearLayout) findViewById(R.id.llWarn);
        llWarnContent = (LinearLayout) findViewById(R.id.llWarnContent);
        mTvAlarm = (NoFocuseTextview) findViewById(R.id.tvAlarm);
        mTvAlarm.setVisibility(View.GONE);
        mTvAlarm.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_alpha));
        mainView.setValues(mainList);
        if (AppStaticVar.mProductInfo != null)
        {
            for (int i = 0; i < AppStaticVar.mProductInfo.pdVarCount; i++)
            {
                Var var = DBManager.getInstance().getVar(NumberBytes.padLeft(Integer.toHexString(i), 4, '0'));
                VarItemView varItemView = new VarItemView(mContext);
                if(TextUtils.isEmpty(var.getName().trim()))
                {
                    varItemView.setVisibility(View.GONE);
                }
//                varItemView.hideLabel();
                varItemView.setLabel(var.getName());
                varItemView.setValue(getString(R.string.string_tips_msg2));
                varItemView.setTag(var);
                if (i == AppStaticVar.mProductInfo.pdVarCount - 1)
                {
                    varItemView.setBottomLineStatus(false);
                }
                mViewMore.addView(varItemView);
            }
        }
    }

    private void hasAlarm(String s) {
        if (mTvAlarm.getVisibility() != View.VISIBLE)
        {
            mTvAlarm.setVisibility(View.VISIBLE);
        }
        if (!mTvAlarm.getText().toString().contains(s))
        {
            mTvAlarm.setText(mTvAlarm.getText() + " " + s);
        }
    }

    private void hasNoAlarm(String s)
    {
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
    protected void onPause()
    {
        super.onPause();
        AppStaticVar.isSNRMainPause = true;
    }

    @Override
    protected void onDestroy() {
        if(AppStaticVar.mGatt != null){
            AppStaticVar.mGatt.disconnect();
            AppStaticVar.mGatt.close();
            AppStaticVar.mGatt = null;
        }
        if(AppStaticVar.mSocket != null){
            try {
                AppStaticVar.mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppStaticVar.mSocket = null;
        }
        super.onDestroy();
    }
}
