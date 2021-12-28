package com.bluetooth.modbus.snrtools2;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ab.http.AbFileHttpResponseListener;
import com.ab.http.AbHttpUtil;
import com.ab.util.AbAppUtil;
import com.ab.util.AbFileUtil;
import com.ab.view.progress.AbHorizontalProgressBar;
import com.bluetooth.modbus.snrtools2.adapter.ParameterAdapter;
import com.bluetooth.modbus.snrtools2.bean.Selector;
import com.bluetooth.modbus.snrtools2.db.Cmd;
import com.bluetooth.modbus.snrtools2.db.DBManager;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.db.ParamGroup;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools2.uitls.AppUtil;
import com.bluetooth.modbus.snrtools2.uitls.CmdUtils;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class CheckPasswordActivity extends BaseActivity implements Observer
{
	private Handler mHandler;
//	private Thread mThread;
	private EditText editText1, editText2;
	private boolean isClear = false;
	private boolean isUpdate = false;
	private View mViewCheckPsd, mViewSetParam;

	private ListView mListview;
	private ParameterAdapter mAdapter;
	private List<Object> mDataList = new ArrayList<>();
	private final static int SELECT_PARAM = 0x100001;
	private final static int INPUT_PARAM = 0x100002;
	private boolean flag = false;
	/** 判断当前页面是否已经发送过命令请求 */
	private boolean hasSend = false;
	private PopupWindow mPop;
	private AbHorizontalProgressBar mAbProgressBar;
	// 最大100
	private int max = 100;
	private int progress = 0;
	private TextView numberText, maxText;
	private AlertDialog mAlertDialog = null;
	/** 当前初始化的参数序号*/
	private int currentInitParamCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_pass_activity);
		mAbHttpUtil = AbHttpUtil.getInstance(this);
		setTitleContent(getResources().getString(R.string.string_title3));
		hideRightView(R.id.btnRight1);
		hideRightView(R.id.view2);
		showRightView(R.id.rlMenu);
//		initHandler();
		editText1 = (EditText) findViewById(R.id.editText1);
		editText2 = (EditText) findViewById(R.id.editText2);
		mViewCheckPsd = findViewById(R.id.checkpsd);
		mViewSetParam = findViewById(R.id.setparam);
		mViewSetParam.setVisibility(View.GONE);
		initUI();
		setListeners();
	}

	@Override
	public void rightButtonOnClick(int id)
	{
		switch (id) {
		case R.id.rlMenu:
			showMenu(findViewById(id));
			break;
		}
	}

	private void syncSuccess()
	{
		hideProgressDialog();
		mAdapter.notifyDataSetChanged();
		editText1.setText("");
		mViewCheckPsd.setVisibility(View.GONE);
		mViewSetParam.setVisibility(View.VISIBLE);
		setTitleContent(getResources().getString(R.string.string_title2));
		AbAppUtil.closeSoftInput(mContext);
	}

	private void syncFailure(String msg)
	{
		showToast(msg);
		hideProgressDialog();
	}

	private void initParam()
	{
		if(currentInitParamCount<mDataList.size())
		{
			hasSend = true;
//			mThread = new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					if(currentInitParamCount==0){
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
					Object obj = mDataList.get(currentInitParamCount);
					if(!(obj instanceof Param))
					{
						currentInitParamCount++;
						initParam();
					}
					else
					{
						Param param = (Param) obj;
						String noHexStr = param.getHexNo();
						String cmd = "0x01 0x44 " + noHexStr + "0x00 0x00";
						int backLength = ("0".equals(param.getType())||"1".equals(param.getType())||"2".equals(param.getType())
								||"3".equals(param.getType())||"4".equals(param.getType()))?20:24;
						if("9".equals(param.getType())){
							backLength = 48;
						}
						CmdUtils.sendCmd(cmd,backLength, new CmdListener()
								{
							@Override
							public void start()
							{
//								showProgressDialog(getString(R.string.sync_params) + "(" + currentInitParamCount*100/mDataList.size() + "/100%)", false);
								showProgressDialog(getString(R.string.sync_params) + "(" + currentInitParamCount + "/" + mDataList.size() + ")", false);
							}

							@Override
							public void result(String result)
							{
								hasSend = false;
//							0x01 0x44 参数编号（2bytes）0x00 len 参数数据（2-4bytes）CRCH CRCL
								Param param = (Param) mDataList.get(currentInitParamCount);
								try
								{
									String str = result.substring(12, result.length() - 4);
									String value = AppUtil.getValueByType(param.getType(), param.getUnit(), param.getCount(), str, true);
									param.setValueDisplay(value);
									param.setValue(str);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
								currentInitParamCount++;
								initParam();
							}

							@Override
							public void failure(String msg)
							{
								hasSend = false;
								syncFailure(msg);
							}

							@Override
							public void timeOut(String msg)
							{
								hasSend = false;
//								if (mThread != null && !mThread.isInterrupted()) {
//									mThread.interrupt();
//								}
								syncFailure(msg);
							}

							@Override
							public void connectFailure(String msg)
							{
								hasSend = false;
								syncFailure(msg);
								showConnectDevice();
							}

							@Override
							public void finish()
							{

							}
						});
					}
//				}
//			});
//			mThread.start();
		}
		else
		{
			syncSuccess();
		}
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.button2:
//			if (AppStaticVar.mParamList == null || AppStaticVar.mParamList.size() == 0) {
//				showToast(getResources().getString(R.string.string_error_msg4));
//				return;
//			}
//			if (TextUtils.isEmpty(editText1.getText().toString().trim())) {
//				showToast(getResources().getString(R.string.string_tips_msg12));
//				return;
//			}
			checkPsw(editText1.getText().toString().trim());
//			if (AppStaticVar.PASSWORD_LEVEAL != -1) {
				mDataList.clear();
				List<Cmd> cmdList = new ArrayList<>();
				cmdList.addAll(DBManager.getInstance().getCmds(editText1.getText().toString().trim()));
				if(cmdList.size()>0)
				{
					ParamGroup paramGroup = new ParamGroup();
					paramGroup.setName(getResources().getString(R.string.label_cmd));
					mDataList.add(paramGroup);
					mDataList.addAll(cmdList);
				}
				List<ParamGroup> paramGroups = new ArrayList<>();
				paramGroups.addAll(DBManager.getInstance().getParamGroups(AppStaticVar.PASSWORD_LEVEAL+""));
				for(int i=0;i<paramGroups.size();i++)
				{
					ParamGroup paramGroup = paramGroups.get(i);
					mDataList.add(paramGroup);
					mDataList.addAll(DBManager.getInstance().getParams(paramGroup.getHexNo()));
				}
				currentInitParamCount = 0;
				initParam();




				// Intent intent = new
				// Intent(mContext,ParamSettingActivity.class);
				// startActivity(intent);
				// finish();
//			} else {
//				Toast.makeText(mContext, getResources().getString(R.string.string_error_msg5), Toast.LENGTH_SHORT).show();
//			}
			break;
//		case R.id.button3:
//			if (TextUtils.isEmpty(editText2.getText().toString().trim())) {
//				showToast(getResources().getString(R.string.string_tips_msg12));
//				return;
//			}
//			if (Constans.PasswordLevel.LEVEL_6 == Long.parseLong(editText2.getText().toString())) {
//				isClear = true;
//				editText2.setText("");
//				ModbusUtils.clearZL("总量清零", mHandler);
//			} else {
//				Toast.makeText(mContext, getResources().getString(R.string.string_error_msg5), Toast.LENGTH_SHORT).show();
//			}
//			break;

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
						};

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

	private void checkPsw(String psw)
	{
		long p = AppUtil.parseToLong(psw,-1314);
		if (AppStaticVar.mProductInfo.pdPasswordUser==p)
		{
			AppStaticVar.PASSWORD_LEVEAL = 1;
		}
		else if (AppStaticVar.mProductInfo.pdPasswordAdvance==p)
		{
			AppStaticVar.PASSWORD_LEVEAL = 2;
		}
		else if (AppStaticVar.mProductInfo.pdPasswordSensor==p)
		{
			AppStaticVar.PASSWORD_LEVEAL = 3;
		}
		else if (AppStaticVar.mProductInfo.pdPasswordFactory==p)
		{
			AppStaticVar.PASSWORD_LEVEAL = 4;
		}
		else
		{
			AppStaticVar.PASSWORD_LEVEAL = 0;
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		if (!flag)
		{
			mViewCheckPsd.setVisibility(View.VISIBLE);
			setTitleContent(getResources().getString(R.string.string_title3));
			mViewSetParam.setVisibility(View.GONE);
			hasSend = false;
		}
		super.onPause();
	}

	@Override
	public void reconnectSuccss()
	{
	}

	private void setListeners()
	{
		mAdapter = new ParameterAdapter(mContext, mDataList);
		mListview.setAdapter(mAdapter);
		mListview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Object obj = mAdapter.getItem(position);
				if(obj instanceof Cmd)
				{
					Cmd cmd = (Cmd) obj;
					CmdUtils.sendCmd("0x01 0x45 " + cmd.getHexNo() + "0x00 0x00 ",16, new CmdListener()
					{
						@Override
						public void start()
						{
							showProgressDialog(false);
							hasSend = true;
						}

						@Override
						public void result(String result)
						{
							hasSend = false;
							hideProgressDialog();
							showToast(getString(R.string.cmd_success));
						}

						@Override
						public void failure(String msg) {
							hasSend = false;
						}

						@Override
						public void timeOut(String msg)
						{
							hasSend = false;
//							if (mThread != null && !mThread.isInterrupted()) {
//								mThread.interrupt();
//							}
						}

						@Override
						public void connectFailure(String msg)
						{
							hasSend = false;
							hideProgressDialog();
							showConnectDevice();
						}

						@Override
						public void finish()
						{

						}
					});
					return;
				}
				if (!(obj instanceof Param))
				{
					return;
				}
				flag = true;
				Param param = (Param) obj;
				Intent intent = new Intent();
				if ("0".equals(param.getType()))
				{
					// 选项型参数
					List<Selector> selectors = new ArrayList<>();
					int count = AppUtil.parseToInt(param.getCount(),0);
					int startIndex = AppUtil.parseHexStrToInt(param.getUnit(),0);
					// 列表中添加选项字符串
					for(int i=0;i<count;i++)
					{
						String hexNo = NumberBytes.padLeft(Long.toHexString(startIndex+i),4,'0');
						Selector selector = new Selector();
						selector.name = DBManager.getInstance().getStr(hexNo);
						// 这里选项赋的数值，应该转换为十六进制 daichenhai
						// String temp = NumberBytes.padLeft(i+"",4,'0');
						String temp = NumberBytes.padLeft(Long.toHexString(i),4,'0');
						selector.value = temp.substring(2,4)+temp.substring(0,2);
						selectors.add(selector);
					}

					intent.setClass(mContext, SelectActivity.class);
					intent.putExtra("position", position);
					intent.putExtra("title", param.getName());
					intent.putExtra("value", param.getValueDisplay());
					intent.putExtra("valueIn", param.getValue());
					intent.putExtra("list", (Serializable) selectors);
					intent.putExtra("param", param);
					startActivityForResult(intent, SELECT_PARAM);
				}
				else
				{
					// 输入数值型参数
					intent.setClass(mContext, InputParamActivity.class);
					intent.putExtra("position", position);
					intent.putExtra("title", param.getName());
					intent.putExtra("value", param.getValueDisplay());
					intent.putExtra("valueIn", param.getValue());
					intent.putExtra("param", param);
					startActivityForResult(intent, INPUT_PARAM);
				}
			}
		});
	}

	private void initUI() {
		mListview = (ListView) findViewById(R.id.listView1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("==========onActivityResult============");
		flag = false;
		hideProgressDialog();
		if (resultCode == RESULT_OK)
		{
			if (requestCode == SELECT_PARAM)
			{
				int position = data.getIntExtra("position", -1);
				Selector selector = (Selector) data.getSerializableExtra("selector");
				Param param = (Param) mAdapter.getItem(position);
				if (position != -1)
				{
					param.setValue(selector.value);
					param.setValueDisplay(selector.name);
					mAdapter.notifyDataSetChanged();
				}
			}
			else if (requestCode == INPUT_PARAM)
			{
				int position = data.getIntExtra("position", -1);
				Param param = (Param) mAdapter.getItem(position);
				String value = data.getStringExtra("value");
				String valueIn = data.getStringExtra("valueIn");
				if (position != -1)
				{
					param.setValue(valueIn);
					param.setValueDisplay(value);
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	public void update(Observable observable, Object data)
	{
		if (data != null && "showProgress".equals(data.toString()))
		{
			System.out.println("-------");
//			showProgressDialog(getResources().getString(R.string.string_tips_msg11));
		}
		else
		{
			if (!hasSend)
			{
				System.out.println("主页面通知密码页开始发送命令");
//				initParam();
			}
		}
	}

	@Override
	protected void onDestroy()
	{
		AppStaticVar.PASSWORD_LEVEAL = -1;
		super.onDestroy();
	}
}
