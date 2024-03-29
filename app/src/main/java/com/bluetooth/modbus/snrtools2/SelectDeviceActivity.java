package com.bluetooth.modbus.snrtools2;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ab.http.AbFileHttpResponseListener;
import com.ab.http.AbHttpUtil;
import com.ab.util.AbAppUtil;
import com.ab.util.AbFileUtil;
import com.ab.view.progress.AbHorizontalProgressBar;
import com.bluetooth.modbus.snrtools2.adapter.DeviceListAdapter;
import com.bluetooth.modbus.snrtools2.bean.ProductInfo;
import com.bluetooth.modbus.snrtools2.bean.SiriListItem;
import com.bluetooth.modbus.snrtools2.common.CRC16;
import com.bluetooth.modbus.snrtools2.db.Cmd;
import com.bluetooth.modbus.snrtools2.db.DBManager;
import com.bluetooth.modbus.snrtools2.db.Main;
import com.bluetooth.modbus.snrtools2.db.OfflineString;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.db.ParamGroup;
import com.bluetooth.modbus.snrtools2.db.Var;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools2.uitls.AppUtil;
import com.bluetooth.modbus.snrtools2.uitls.CmdUtils;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;
import com.bluetooth.modbus.snrtools2.view.MyAlertDialog.MyAlertDialogListener;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SelectDeviceActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks
{
	private String NO_DEVICE_CAN_CONNECT;
	private ListView mListView;
	private ArrayList<SiriListItem> list;
	private DeviceListAdapter mAdapter;
	private PopupWindow mPop;
	private AbHorizontalProgressBar mAbProgressBar;
	// 最大100
	private int max = 100;
	private int progress = 0;
	private TextView numberText, maxText;
	private AlertDialog mAlertDialog = null;
	private long totalSyncCount,currentSyncCount;
	private int click = 10;

	private final static int REQEST_PERMS = 0x888;
	private String[] permissions =
			{
					Manifest.permission.ACCESS_FINE_LOCATION
			};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		NO_DEVICE_CAN_CONNECT = getResources().getString(R.string.string_tips_msg3);
		setContentView(R.layout.activity_main);
		mAbHttpUtil = AbHttpUtil.getInstance(this);
		AppStaticVar.isBluetoothOpen = AppUtil.checkBluetooth(this,false);
		setTitleClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(click==0)
				{
					click = 10;
					Intent intent = new Intent(mContext,DBDataActivity.class);
					startActivity(intent);
				}
				click--;
			}
		});
		setTitleContent(getResources().getString(R.string.string_tips_msg4));
		hideRightView(R.id.view2);
		setRightButtonContent(getResources().getString(R.string.string_search), R.id.btnRight1);
		init();
		showRightView(R.id.rlMenu);
		if (AppUtil.checkBluetooth(mContext))
		{
			permissionJudge();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// Forward results to EasyPermissions
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	//同意授权
	@Override
	public void onPermissionsGranted(int requestCode, List<String> perms) {
		permissionEnd();
	}

	//拒绝授权
	@Override
	public void onPermissionsDenied(int requestCode, List<String> perms) {
		permissionEnd();
	}

	@AfterPermissionGranted(REQEST_PERMS)
	private void permissionJudge()
	{
		// 首先判断权限是否已被授权,防止重复申请权限时,多次回调
		if (EasyPermissions.hasPermissions(this, permissions))
		{
			permissionEnd();
		}
		else
		{
//			EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, REQEST_PERMS, permissions)
//					.setRationale(R.string.loc_permission)
//					.setTheme(R.style.PermissionAlertDialog)
//					.build());
			EasyPermissions.requestPermissions(this,getString(R.string.loc_permission), REQEST_PERMS, permissions);
		}
	}

	private void permissionEnd(){
		if(AppStaticVar.mBtAdapter != null)
		{
			list.clear();
			Set<BluetoothDevice> pairedDevices = AppStaticVar.mBtAdapter.getBondedDevices();
			if (pairedDevices.size() > 0)
			{
				for (BluetoothDevice device : pairedDevices)
				{
					list.add(new SiriListItem(device.getAddress(),device.getName() + "\n" + device.getAddress(), true));
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(list.size() - 1);
				}
			}
			else
			{
				list.add(new SiriListItem("-1",NO_DEVICE_CAN_CONNECT, true));
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(list.size() - 1);
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						searchDevice();
					}
				},1000);
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	// 同步产品配置信息
	public void reconnectSuccss()
	{
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				CmdUtils.sendCmd("0x01 0x61 0x00 0x00 0x00 0x00",104, new CmdListener()
				{
					@Override
					// 启动
					public void start()
					{
						showProgressDialog(getString(R.string.sync_config),false);
					}
					// 读成功
					@Override
					public void result(final String result)
					{
						AppStaticVar.mProductInfo = ProductInfo.buildModel(result);
						if(AppStaticVar.mProductInfo != null)
						{
							// 判断系统语言
							String value = AppUtil.getValue("sync_language","-1");
							AppUtil.saveValue("sync_language",AppStaticVar.isChinese?"zh":"en");
							boolean isNeedUpdate = !value.equals(AppStaticVar.isChinese?"zh":"en");
							currentSyncCount = 0;
							totalSyncCount = 0;
							// 判断配置块CRC是否被更新
							String oldCRC = AppUtil.getValue("key_crc","");
							if (!(oldCRC+"").equals(AppStaticVar.mProductInfo.crcModel))
							{
								totalSyncCount=AppStaticVar.mProductInfo.pdCmdCount+AppStaticVar.mProductInfo.pdParCount+AppStaticVar.mProductInfo.pdParGroupCount+AppStaticVar.mProductInfo.pdStringCount;
							}
							else if(isNeedUpdate)
							{
								totalSyncCount=AppStaticVar.mProductInfo.pdCmdCount+AppStaticVar.mProductInfo.pdParCount+AppStaticVar.mProductInfo.pdParGroupCount;
							}
							totalSyncCount += AppStaticVar.mProductInfo.pdVarCount;
							totalSyncCount += AppStaticVar.mProductInfo.pdDispMainCount;
							// CRC不符合，重新读取产品配置数据
							AppStaticVar.currentSyncIndex = 0;
							if (!(oldCRC+"").equals(AppStaticVar.mProductInfo.crcModel))
							{
								syncStr();
							}
							else if(isNeedUpdate)
							{
								syncCmd();
							}
							else
							{
								syncVar();
							}
						}
						else
						{
							syncFailure(getString(R.string.get_setting_error));
						}
					}
					// 失败
					@Override
					public void connectFailure(String msg) {
						syncFailure(msg);
					}
					// 超时
					@Override
					public void timeOut(String msg) {
						syncFailure(msg);
					}
					// 失败
					@Override
					public void failure(String msg) {
						syncFailure(msg);
					}
					// 完成
					@Override
					public void finish() { 	}
				});
			}
		},100);
	}

	private void syncSuccess()
	{
		hideProgressDialog();
		AppStaticVar.mProductInfo.pdModel = DBManager.getInstance().getStr(AppStaticVar.mProductInfo.pdModel);
		AppStaticVar.mProductInfo.pdVersion = DBManager.getInstance().getStr(AppStaticVar.mProductInfo.pdVersion);
//		AbSharedUtil.putString(mContext,"key_crc",AppStaticVar.mProductInfo.pdCfgCrc);
		AppUtil.saveValue("key_crc",AppStaticVar.mProductInfo.crcModel);
		Intent intent = new Intent(mContext, MainActivity.class);
		// Intent intent = new Intent(mContext, SNRMainActivity.class);
		startActivity(intent);
	}

	private void syncFailure(String msg)
	{
		showToast(msg);
		hideProgressDialog();
	}

	// 同步变量
	private void syncVar()
	{
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdVarCount)
		{
			if(AppStaticVar.currentSyncIndex == 0)
			{
				DBManager.getInstance().clearVar();
			}
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x66 "+noHexStr+"0x00 0x00",28, new CmdListener()
			{
				@Override
				public void start()
				{
					currentSyncCount++;
//					showProgressDialog(getString(R.string.sync_var)+"("+currentSyncCount*100/totalSyncCount+"/100%)",false);
					showProgressDialog(getString(R.string.sync_var)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result)
				{
					String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
					String nameHexNo = result.substring(14,16)+result.substring(12,14);
					String name = DBManager.getInstance().getStr(result.substring(14,16)+result.substring(12,14));
					String type = Long.parseLong(result.substring(16,18),16)+"";
					String count = Long.parseLong(result.substring(18,20),16)+"";
					String unit = result.substring(22,24)+result.substring(20,22);
					Var var = new Var();
					var.setName(name);
					var.setNameHexNo(nameHexNo);
					var.setHexNo(noHexStr);
					var.setType(type);
					var.setCount(count);
					var.setUnit(unit);
					DBManager.getInstance().saveVar(var);
					AppStaticVar.currentSyncIndex++;
					syncVar();
				}

				@Override
				public void connectFailure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void timeOut(String msg) {
					syncFailure(msg);
				}

				@Override
				public void failure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void finish() {}
			});
		}
		else
		{
			AppStaticVar.currentSyncIndex = 0;
			syncMain();
		}
	}
	// 同步主界面配置信息
	private void syncMain()
	{
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdDispMainCount)
		{
			if(AppStaticVar.currentSyncIndex==0)
			{
				DBManager.getInstance().clearMain();
			}
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x62 "+noHexStr+"0x00 0x00",32, new CmdListener()
			{
				@Override
				public void start()
				{
					currentSyncCount++;
//					showProgressDialog(getString(R.string.sync_mainview)+"("+currentSyncCount*100/totalSyncCount+"/100%)",false);
					showProgressDialog(getString(R.string.sync_mainview)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result) {
					// 解析主界面显示配置
					// 类型：0-变量，1-参数，2-图标，3-字符串，4-波形
					String type = Long.parseLong(result.substring(12, 14), 16) + "";
					// 显示格式
					String temp = NumberBytes.padLeft(Long.toBinaryString(Long.parseLong(result.substring(14, 16), 16)), 8, '0');
					// bit7:bit6 字体（2-大字体，1-小字体，0-普通字体）
					String fontSize = Integer.parseInt(temp.substring(0, 2), 2) + "";
					// bit5:bit4 对齐（2-中间，1-右对齐，0-左对齐）
					String gravity = Integer.parseInt(temp.substring(2, 4), 2) + "";
					// bit3:bit0 小数点位数（0-7位） 注（1）
					String count = Integer.parseInt(temp.substring(4, 8), 2) + "";
					// 显示行坐标：0-7行（以8像素为一个行单位）
					String x = Long.parseLong(result.substring(16, 18), 16) + "";
					// 显示列坐标：0-127列
					String y = Long.parseLong(result.substring(18, 20), 16) + "";
					// 显示区域宽度：0-128
					String width = Long.parseLong(result.substring(20, 22), 16) + "";
					// 显示区域高度(预留未使用)
					String height = Long.parseLong(result.substring(22, 24), 16) + "";
					// 数据索引号：变量/参数/字符串/图标编号
					String noHexStr = result.substring(26, 28) + result.substring(24, 26);

					// 数值
					String value = "";
					if ("0".equals(type))            // 0-变量
					{
						value = getString(R.string.string_tips_msg2);
					} else if ("1".equals(type))        // 1-参数
					{
						value = getString(R.string.string_tips_msg2);
					} else if ("2".equals(type))        // 2-图标
					{
						value = noHexStr;
					} else if ("3".equals(type))        // 3-字符串
					{
						value = DBManager.getInstance().getStr(noHexStr);
					}
					// 过滤异常数值
					if (!(("0".equals(type)) || ("1".equals(type)) || ("2".equals(type)) || ("3".equals(type)))) {
						type = "0";
						value = "";
						width = "0";
					}
					if (!(("0".equals(fontSize)) || ("1".equals(fontSize)) || ("2".equals(fontSize)))){
						fontSize = "0";
						value = "";
						width = "0";
					}
					if (!(("0".equals(gravity)) || ("1".equals(gravity)) || ("2".equals(gravity)))){
						gravity = "0";
						value = "";
						width = "0";
					}
					if((AppUtil.parseToInt(x, 0) < 0) || (AppUtil.parseToInt(x, 0) > 7)) {
						x = "0";
						value = "";
						width = "0";
					}
					if((AppUtil.parseToInt(y, 0) < 0) || (AppUtil.parseToInt(y, 0) > 127)) {
						y = "0";
						value = "";
						width = "0";
					}

					Main main = new Main();
					main.setType(type);
					main.setFontSize(fontSize);
					main.setGravity(gravity);
					main.setCount(count);
					main.setX(x);
					main.setY(y);
					main.setWidth(width);
					main.setHeight(height);
					main.setHexNo(noHexStr);
					main.setValue(value);
					if(!"0".equals(width)) {
						DBManager.getInstance().saveMain(main);
					}
					AppStaticVar.currentSyncIndex++;
					syncMain();
				}

				@Override
				public void connectFailure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void timeOut(String msg) {
					syncFailure(msg);
				}

				@Override
				public void failure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void finish() {

				}
			});
		}
		else
		{
			syncSuccess();
		}
	}

	// 同步字符串
	private void syncStr()
	{
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdStringCount)
		{
			if(AppStaticVar.currentSyncIndex == 0)
			{
//				DBManager.getInstance().clearStr();
			}
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x6A "+noHexStr+"0x00 0x00",80, new CmdListener()
			{
				@Override
				public void start()
				{
					currentSyncCount++;
//					showProgressDialog(getString(R.string.sync_string)+"("+currentSyncCount*100/totalSyncCount+"/100%)",false);
					showProgressDialog(getString(R.string.sync_string)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result)
				{
					String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
					String zh = NumberBytes.byte2Char(CRC16.HexString2Buf(result.substring(12,44)));
					String en = NumberBytes.byte2Char(CRC16.HexString2Buf(result.substring(44,76)));
					System.out.println("===================="+result);
					System.out.println("===================="+"--"+zh+"--"+en);
					OfflineString offlineString = new OfflineString();
					offlineString.setHexNo(noHexStr);
					offlineString.setStringEn(en);
					offlineString.setStringZh(zh);
					DBManager.getInstance().saveOfflineString(offlineString);
					AppStaticVar.currentSyncIndex++;
					syncStr();
				}

				@Override
				public void connectFailure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void timeOut(String msg) {
					syncFailure(msg);
				}

				@Override
				public void failure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void finish() {}
			});
		}
		else
		{
//			String newCrcModel = AppStaticVar.mProductInfo.pdCfgCrc+DBManager.getInstance().getStr(AppStaticVar.mProductInfo.pdModel);
//			DBManager.getInstance().updateOfflineStringCrcModel(AppStaticVar.mProductInfo.crcModel,newCrcModel);
//			AppStaticVar.mProductInfo.crcModel = newCrcModel;
			AppStaticVar.currentSyncIndex = 0;
			syncCmd();
		}
	}
	// 同步命令
	private void syncCmd()
	{
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdCmdCount)
		{
			if(AppStaticVar.currentSyncIndex == 0)
			{
			}
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x69 "+noHexStr+"0x00 0x00",32, new CmdListener()
			{
				@Override
				public void start()
				{
					currentSyncCount++;
//					showProgressDialog(getString(R.string.sync_cmd)+"("+currentSyncCount*100/totalSyncCount+"/100%)",false);
					showProgressDialog(getString(R.string.sync_cmd)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result)
				{
					String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
					String str0 = DBManager.getInstance().getStr(result.substring(14,16)+result.substring(12,14));
					String str1 = result.substring(16,20);
					String str2 = Long.parseLong(result.substring(26,28)+result.substring(24,26)+result.substring(22,24)+result.substring(20,22),16)+"";
					Cmd cmd = new Cmd();
					cmd.setHexNo(noHexStr);
					cmd.setCmdName(str0);
					cmd.setExt(str1);
					cmd.setCmdPwd(str2);
					DBManager.getInstance().saveCmd(cmd);
					AppStaticVar.currentSyncIndex++;
					syncCmd();
				}

				@Override
				public void connectFailure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void timeOut(String msg) {
					syncFailure(msg);
				}

				@Override
				public void failure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void finish() {}
			});
		}
		else
		{
			AppStaticVar.currentSyncIndex = 0;
			syncParamGroup();
		}
	}
	// 同步参数组
	private void syncParamGroup()
	{
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdParGroupCount)
		{
			if(AppStaticVar.currentSyncIndex == 0)
			{
//				DBManager.getInstance().clearParamGroup();
			}
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x67 "+noHexStr+"0x00 0x00",24, new CmdListener()
			{
				@Override
				public void start()
				{
					currentSyncCount++;
//					showProgressDialog(getString(R.string.sync_params)+"("+currentSyncCount*100/totalSyncCount+"/100%)",false);
					showProgressDialog(getString(R.string.sync_params)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result)
				{
					String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
					String str0 = DBManager.getInstance().getStr(result.substring(14,16)+result.substring(12,14));
					String str1 = Long.parseLong(result.substring(18,20)+result.substring(16,18),16)+"";
					ParamGroup paramGroup = new ParamGroup();
					paramGroup.setHexNo(noHexStr);
					paramGroup.setName(str0);
					paramGroup.setLevel(str1);
					DBManager.getInstance().saveParamGroup(paramGroup);
					AppStaticVar.currentSyncIndex++;
					syncParamGroup();
				}

				@Override
				public void connectFailure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void timeOut(String msg) {
					syncFailure(msg);
				}

				@Override
				public void failure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void finish() {

				}
			});
		}
		else
		{
			AppStaticVar.currentSyncIndex = 0;
			syncParam();
		}
	}
	// 同步参数
	private void syncParam()
	{
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdParCount)
		{
			if(AppStaticVar.currentSyncIndex == 0)
			{
//				DBManager.getInstance().clearParam();
			}
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x68 "+noHexStr+"0x00 0x00",48, new CmdListener()
			{
				@Override
				public void start()
				{
					currentSyncCount++;
//					showProgressDialog(getString(R.string.sync_params)+"("+currentSyncCount*100/totalSyncCount+"/100%)",false);
					showProgressDialog(getString(R.string.sync_params)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result)
				{
					System.out.println("==========================="+result);
					String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
					String paramGroupHexNo = NumberBytes.padLeft(result.substring(12,14),4,'0');
					String type = Long.parseLong(result.substring(14,16),16)+"";
					String name = DBManager.getInstance().getStr(result.substring(18,20)+result.substring(16,18));
					String linkVariable = result.substring(20,22);
					String count = Long.parseLong(result.substring(22,24),16)+"";
					String unit = result.substring(26,28)+result.substring(24,26);
					String max = NumberBytes.subZeroAndDot(AppUtil.getValueByType("7",unit,count,result.substring(28,36),false));
					String min = NumberBytes.subZeroAndDot(AppUtil.getValueByType("7",unit,count,result.substring(36,44),false));
					Param param = new Param();
					param.setHexNo(noHexStr);
					param.setParamGroupHexNo(paramGroupHexNo);
					param.setType(type);
					param.setName(name);
					param.setLinkVariable(linkVariable);
					param.setCount(count);
					param.setUnit(unit);
					param.setMax(max);
					param.setMin(min);
					DBManager.getInstance().saveParam(param);
					AppStaticVar.currentSyncIndex++;
					syncParam();
				}

				@Override
				public void connectFailure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void timeOut(String msg) {
					syncFailure(msg);
				}

				@Override
				public void failure(String msg) {
					syncFailure(msg);
				}

				@Override
				public void finish() {

				}
			});
		}
		else
		{
			AppStaticVar.currentSyncIndex = 0;
			syncVar();
		}
	}

	@Override
	public void BackOnClick(View v)
	{
		switch (v.getId())
		{
			case R.id.ivBack:
				onBackPressed();
		}
	}

	private void init()
	{
		list = new ArrayList<SiriListItem>();
		mAdapter = new DeviceListAdapter(this, list);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setFastScrollEnabled(true);
		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				SiriListItem item = list.get(arg2);
				if (item == null)
				{
					return;
				}
				String info = item.getMessage();
				if ("-1".equals(item.getAddr()+""))
				{
					return;
				}
				if (getResources().getString(R.string.string_tips_msg7).equals(info))
				{
					return;
				}
				String address = info.substring((info.length() - 17) > 0 ? info.length() - 17 : 0);
				String name = info.substring(0, (info.length() - 17) > 0 ? info.length() - 17 : 0);
				AppStaticVar.mCurrentAddress = address;
				AppStaticVar.mCurrentName = name;

				showDialog(getResources().getString(R.string.string_tips_msg5) + "\n"+ item.getMessage(),
						new MyAlertDialogListener()
						{

							@Override
							public void onClick(View view)
							{
								switch (view.getId())
								{
									case R.id.btnCancel:
										AppStaticVar.mCurrentAddress = null;
										AppStaticVar.mCurrentName = null;
										hideDialog();
										break;
									case R.id.btnOk:
										setRightButtonContent(getResources().getString(R.string.string_search),
												R.id.btnRight1);
										connectDevice(AppStaticVar.mCurrentAddress);
										break;
								}
							}
						});
			}
		});

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);
	}

	@Override
	protected void rightButtonOnClick(int id)
	{
		switch (id)
		{
			case R.id.btnRight1:
				if (AppUtil.checkBluetooth(mContext))
				{
					searchDevice();
				}
				break;
			case R.id.rlMenu:
				showMenu(findViewById(id));
				break;
		}
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
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
				onBackPressed();
				break;
			case R.id.btnChaoBiao:// 抄表
				hideMenu();
				Intent intent = new Intent(mContext,ChaoBiaoActivity.class);
				startActivity(intent);
				break;
			case R.id.textView5:// 清除缓存
				hideMenu();
				System.out.println("删除临时文件==="
						+ AbFileUtil.deleteFile(new File(AbFileUtil.getFileDownloadDir(mContext))));
				System.out.println("删除下载文件===" + AbFileUtil.deleteFile(new File(Constans.Directory.DOWNLOAD)));
				break;
		}
	}

	private void showMenu(View v)
	{
		if (mPop == null)
		{
			View contentView = View.inflate(this, R.layout.main_menu_select_device, null);
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
		File xml = new File(Constans.Directory.DOWNLOAD+"version.xml");
		try
		{
			if (!xml.getParentFile().exists())
			{
				xml.getParentFile().mkdirs();
			}
			xml.delete();
			xml.createNewFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		mAbHttpUtil.get(url, new AbFileHttpResponseListener(xml)
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
						AppUtil.chmod("777",apk.getAbsolutePath());
						AppUtil.installApk(mContext, apk);
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
							AppUtil.chmod("777",file.getAbsolutePath());
							AppUtil.installApk(mContext, file);
						};

						// 开始执行前
						@Override
						public void onStart()
						{
							// 打开进度框
							View v = LayoutInflater.from(mContext).inflate(R.layout.progress_bar_horizontal, null,
									false);
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

						};
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
			};
		});
	}

	private void searchDevice()
	{
		if(AppStaticVar.mBtAdapter == null)
		{
			return;
		}
		if (AppStaticVar.mBtAdapter.isDiscovering())
		{
			AppStaticVar.mBtAdapter.cancelDiscovery();
			setRightButtonContent(getResources().getString(R.string.string_search), R.id.btnRight1);
		}
		else
		{
			showProgressDialog(getResources().getString(R.string.string_progressmsg1), true);
			list.clear();
			Set<BluetoothDevice> pairedDevices = AppStaticVar.mBtAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					list.add(new SiriListItem(device.getAddress(),device.getName() + "\n" + device.getAddress(), true));
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(list.size() - 1);
				}
			} else {
				list.add(new SiriListItem("-1",NO_DEVICE_CAN_CONNECT, true));
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(list.size() - 1);
			}
			/* 开始搜索 */
			AppStaticVar.mBtAdapter.startDiscovery();
			setRightButtonContent(getResources().getString(R.string.string_stop), R.id.btnRight1);
		}
	}

	@Override
	public void onBackPressed()
	{
		exitApp();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy()
	{
		this.unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	boolean isDialogShow = false;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent == null)
			{
				return;
			}
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED)
				{
					if (device.getName() == null || device.getAddress() == null)
					{
//						searchDevice();
//							showDialog(getResources().getString(R.string.string_tips_msg6),
//									getResources().getString(R.string.string_search),
//									getResources().getString(R.string.string_bluetooth_search), new MyAlertDialogListener() {
//										@Override
//										public void onClick(View view) {
//											isDialogShow = false;
//											switch (view.getId()) {
//												case R.id.btnOk:
//													Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
//													startActivity(intent);
//													break;
//												case R.id.btnCancel:
//													searchDevice();
//													break;
//											}
//										}
//									});
					}
					else
					{
						if (device.getName() != null)
						{
							if(list.size()==1&&"-1".equals(list.get(0).getAddr()+"")){
								list.clear();
							}
							SiriListItem item = new SiriListItem(device.getAddress(),device.getName() + "\n" + device.getAddress(), false);
							if(!list.contains(item)) {
								list.add(item);
							}
							mAdapter.notifyDataSetChanged();
							mListView.setSelection(list.size() - 1);
						}
					}
				}
			}
			else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
			{
				if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON)
				{
					if (AppUtil.checkBluetooth(mContext))
					{
						searchDevice();
					}
				}
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				hideProgressDialog();
				setProgressBarIndeterminateVisibility(false);
				if (mListView.getCount() == 0)
				{
					list.add(new SiriListItem("-1",getResources().getString(R.string.string_tips_msg7), false));
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(list.size() - 1);
				}

				if(list.size()==1&&"-1".equals(list.get(0).getAddr()+"")){
					if(!isDialogShow) {
						isDialogShow = true;
						showDialog(getResources().getString(R.string.string_tips_msg6),
								getResources().getString(R.string.string_search),
								getResources().getString(R.string.string_bluetooth_search), new MyAlertDialogListener() {
									@Override
									public void onClick(View view) {
										isDialogShow = false;
										switch (view.getId()) {
											case R.id.btnOk:
												Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
												startActivity(intent);
												break;
											case R.id.btnCancel:
												searchDevice();
												break;
										}
									}
								});
					}
				}
				setRightButtonContent(getResources().getString(R.string.string_search), R.id.btnRight1);
			}
		}
	};
}
