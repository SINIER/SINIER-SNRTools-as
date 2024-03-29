package com.bluetooth.modbus.snrtools2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.bluetooth.modbus.snrtools2.adapter.LogAdapter;
import com.bluetooth.modbus.snrtools2.bean.LogInfo;
import com.bluetooth.modbus.snrtools2.db.DBManager;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.uitls.CmdUtils;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;

import java.util.ArrayList;

public class LogActivity extends BaseActivity
{

	private String NO_DEVICE_CAN_CONNECT;
	private ListView mListView;
	private ArrayList<LogInfo> list;
	private LogAdapter mAdapter;
	private long totalSyncCount,currentSyncCount;
	private boolean isRead = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		NO_DEVICE_CAN_CONNECT = getResources().getString(R.string.string_tips_msg3);
		setContentView(R.layout.log_layout);
		setTitleContent(getResources().getString(R.string.log));
		hideRightView(R.id.view2);
		hideRightView(R.id.btnRight1);
		init();
		totalSyncCount = getIntent().getIntExtra("totalSyncCount",0);
		totalSyncCount = 4;
		startReadLog();
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

	@Override
	public void reconnectSuccss() {

	}

	private void init()
	{
		list = new ArrayList<LogInfo>();
		mAdapter = new LogAdapter(this, list);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setFastScrollEnabled(true);
	}

	private void startReadLog(){
		if(totalSyncCount == 0){
			showToast(getString(R.string.no_log));
			return;
		}
		if (currentSyncCount >= totalSyncCount) {
			currentSyncCount = 0;
		}
		String noHexStr = NumberBytes.padLeft(Long.toHexString(currentSyncCount), 4, '0');
		String cmd = "0x01 0x51 " + noHexStr + "0x00 0x00";
		CmdUtils.sendCmd(cmd, 40, new CmdListener() {
					@Override
					public void start() {
						
					}

					@Override
					public void result(String result) {
						System.out.println("====================日志=====接收到通过的数据" + result);
						dealLog(result);
						currentSyncCount++;
						if (isRead) {
							startReadLog();
						}
						//0151 0001 000c598c6321 29 00 04 03 e1 2e 0000 6138
						//0151 0002 000c5a8c6321 29 00 04 03 e2 2e 0000 2679
						//0151 0003 000c5b8c6321 29 00 04 03 e3 2e 0000 1b46
						//0151 0000 000c588c6321 29 00 04 03 e0 2e 0000 5c07
					}

					@Override
					public void failure(String msg) {
						currentSyncCount++;
						if (isRead) {
							startReadLog();
						}
					}

					@Override
					public void timeOut(String msg) {
						currentSyncCount++;
						if (isRead) {
							showToast(getResources().getString(R.string.string_error_msg3));
							startReadLog();
						}
					}

					@Override
					public void connectFailure(String msg) {
						if (isRead) {
							showConnectDevice();
						}
					}

					@Override
					public void finish() {

					}
				});
	}

	private void dealLog(String result) {
		try {
			System.out.println("============"+result);
			//0x01 0x51日志编号（2bytes）0x00 0x08年（10bytes）月（1byte）日（1byte）时（1byte）分（1byte）秒（1byte）事件码（1byte）字符串(2bytes) CRCH CRCL
			String str = result.substring(result.length() - 6, result.length() - 4)+result.substring(result.length() - 8, result.length() - 6);
//			String time
			String hexNo = result.substring(4, 8);
			String value = DBManager.getInstance().getStr(str);
			if(TextUtils.isEmpty(value)) {
				return;
			}
			LogInfo logInfo = new LogInfo();
			logInfo.hexNo = hexNo;

			if(!list.contains(logInfo)) {
				list.add(logInfo);
				mAdapter.notifyDataSetChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		isRead = false;
		super.onDestroy();
	}
}
