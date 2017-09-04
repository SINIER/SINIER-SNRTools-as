package com.bluetooth.modbus.snrtools2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;

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
import android.provider.Settings;
import android.util.Log;
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
import com.ab.util.AbSharedUtil;
import com.ab.view.progress.AbHorizontalProgressBar;
import com.bluetooth.modbus.snrtools2.adapter.DeviceListAdapter;
import com.bluetooth.modbus.snrtools2.bean.ProductInfo;
import com.bluetooth.modbus.snrtools2.bean.SiriListItem;
import com.bluetooth.modbus.snrtools2.common.CRC16;
import com.bluetooth.modbus.snrtools2.db.Cmd;
import com.bluetooth.modbus.snrtools2.db.DBManager;
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

public class SelectDeviceActivity extends BaseActivity
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		NO_DEVICE_CAN_CONNECT = getResources().getString(R.string.string_tips_msg3);
		setContentView(R.layout.activity_main);
		mAbHttpUtil = AbHttpUtil.getInstance(this);
		setTitleContent(getResources().getString(R.string.string_tips_msg4));
		hideRightView(R.id.view2);
		setRightButtonContent(getResources().getString(R.string.string_search), R.id.btnRight1);
		init();
		showRightView(R.id.rlMenu);
		if (AppUtil.checkBluetooth(mContext))
		{
			searchDevice();
		}
	}

	@Override
	public void reconnectSuccss()
	{
		CmdUtils.sendCmd("0x01 0x61 0x00 0x00 0x00 0x00", new CmdListener() {
			@Override
			public void start() {
				showProgressDialog(getString(R.string.sync_config),false);
			}

			@Override
			public void result(final String result) {
				AppStaticVar.mProductInfo = ProductInfo.buildModel(result);
				if(AppStaticVar.mProductInfo != null) {
					currentSyncCount = 0;
					totalSyncCount = 0;
					String oldCRC = AbSharedUtil.getString(mContext, "key_crc");
					if (!(oldCRC+"").equals(AppStaticVar.mProductInfo.pdCfgCrc)){
						totalSyncCount=AppStaticVar.mProductInfo.pdCmdCount+AppStaticVar.mProductInfo.pdParCount+AppStaticVar.mProductInfo.pdParGroupCount+AppStaticVar.mProductInfo.pdStringCount;
					}
					totalSyncCount += AppStaticVar.mProductInfo.pdVarCount;
					syncVar();
				}else {
					syncFailure(getString(R.string.get_setting_error));
				}
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

	private void syncSuccess(){
		hideProgressDialog();
		AppStaticVar.mProductInfo.pdModel = DBManager.getInstance().getStr(AppStaticVar.mProductInfo.pdModel);
		AppStaticVar.mProductInfo.pdVersion = DBManager.getInstance().getStr(AppStaticVar.mProductInfo.pdVersion);
		AbSharedUtil.putString(mContext,"key_crc",AppStaticVar.mProductInfo.pdCfgCrc);
		Intent intent = new Intent(mContext, MainActivity.class);
		// Intent intent = new Intent(mContext, SNRMainActivity.class);
		startActivity(intent);
	}

	private void syncFailure(String msg){
		showToast(msg);
		hideProgressDialog();
	}

	private void syncVar(){
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdVarCount){
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x66 "+noHexStr+"0x00 0x00", new CmdListener() {
				@Override
				public void start() {
					currentSyncCount++;
					showProgressDialog(getString(R.string.sync_config)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result) {
					String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
					String type = Long.parseLong(result.substring(12,14),16)+"";
					String count = Long.parseLong(result.substring(14,16),16)+"";
					String unit = result.substring(18,20)+result.substring(16,18);
//					String unit = Long.parseLong(result.substring(18,20)+result.substring(16,18),16)+"";
					Var var = new Var();
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
				public void finish() {

				}
			});
		}else {
			AppStaticVar.currentSyncIndex = 0;
			String oldCRC = AbSharedUtil.getString(mContext, "key_crc");
			if (!(oldCRC+"").equals(AppStaticVar.mProductInfo.pdCfgCrc)){
				AppStaticVar.currentSyncIndex = 0;
				syncStr();
			}else {
				syncSuccess();
			}
		}
	}

	private void syncStr(){
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdStringCount){
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x6A "+noHexStr+"0x00 0x00", new CmdListener() {
				@Override
				public void start() {
					currentSyncCount++;
					showProgressDialog(getString(R.string.sync_config)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result) {
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
				public void finish() {

				}
			});
		}else {
			AppStaticVar.currentSyncIndex = 0;
			syncCmd();
		}
	}

	private void syncCmd(){
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdCmdCount){
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x69 "+noHexStr+"0x00 0x00", new CmdListener() {
				@Override
				public void start() {
					currentSyncCount++;
					showProgressDialog(getString(R.string.sync_config)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result) {
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
				public void finish() {

				}
			});
		}else {
			AppStaticVar.currentSyncIndex = 0;
			syncParamGroup();
		}
	}

	private void syncParamGroup(){
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdParGroupCount){
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x67 "+noHexStr+"0x00 0x00", new CmdListener() {
				@Override
				public void start() {
					currentSyncCount++;
					showProgressDialog(getString(R.string.sync_config)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result) {
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
		}else {
			AppStaticVar.currentSyncIndex = 0;
			syncParam();
		}
	}

	private void syncParam(){
		if(AppStaticVar.currentSyncIndex<AppStaticVar.mProductInfo.pdParCount){
			String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
			CmdUtils.sendCmd("0x01 0x68 "+noHexStr+"0x00 0x00", new CmdListener() {
				@Override
				public void start() {
					currentSyncCount++;
					showProgressDialog(getString(R.string.sync_config)+"("+currentSyncCount+"/"+totalSyncCount+")",false);
				}

				@Override
				public void result(String result) {
					System.out.println("==========================="+result);
					String noHexStr = NumberBytes.padLeft(Integer.toHexString(AppStaticVar.currentSyncIndex),4,'0');
					String paramGroupHexNo = NumberBytes.padLeft(result.substring(12,14),4,'0');
					String type = Long.parseLong(result.substring(14,16),16)+"";
					String name = DBManager.getInstance().getStr(result.substring(18,20)+result.substring(16,18));
					String linkVariable = result.substring(20,22);
					String count = Long.parseLong(result.substring(22,24))+"";
					String unit = result.substring(26,28)+result.substring(24,26);
					String max = AppUtil.getValueByType("7",unit,count,result.substring(28,36),false);
					String min = AppUtil.getValueByType("7",unit,count,result.substring(36,44),false);
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
		}else {
			syncSuccess();
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
				if (NO_DEVICE_CAN_CONNECT.equals(info))
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
				showDialogOne(getResources().getString(R.string.string_menu_msg2), null);
				break;
			case R.id.textView3:// 版本更新
				hideMenu();
				downloadXml();
				break;
			case R.id.textView4:// 退出
				hideMenu();
				onBackPressed();
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
			View contentView = View.inflate(this, R.layout.main_menu, null);
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
		String url = "http://www.sinier.com.cn/download/beta2/version.xml";
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
			mAdapter.notifyDataSetChanged();

			Set<BluetoothDevice> pairedDevices = AppStaticVar.mBtAdapter.getBondedDevices();
			if (pairedDevices.size() > 0)
			{
				for (BluetoothDevice device : pairedDevices)
				{
						list.add(new SiriListItem(device.getName() + "\n" + device.getAddress(), true));
						mAdapter.notifyDataSetChanged();
						mListView.setSelection(list.size() - 1);
				}
			}
			else
			{
				list.add(new SiriListItem(NO_DEVICE_CAN_CONNECT, true));
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
						searchDevice();
						showDialog(getResources().getString(R.string.string_tips_msg6),
								getResources().getString(R.string.string_search),
								getResources().getString(R.string.string_bluetooth_search), new MyAlertDialogListener()
								{
									@Override
									public void onClick(View view)
									{
										switch (view.getId())
										{
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
					else
					{
						if (device.getName() != null)
						{
							list.add(new SiriListItem(device.getName() + "\n" + device.getAddress(), false));
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
					list.add(new SiriListItem(getResources().getString(R.string.string_tips_msg7), false));
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(list.size() - 1);
				}
				setRightButtonContent(getResources().getString(R.string.string_search), R.id.btnRight1);
			}
		}
	};
}
