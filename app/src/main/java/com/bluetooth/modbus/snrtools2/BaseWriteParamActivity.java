package com.bluetooth.modbus.snrtools2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.bluetooth.modbus.snrtools2.bean.Parameter;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.listener.CmdListener;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools2.uitls.CmdUtils;
import com.bluetooth.modbus.snrtools2.uitls.ModbusUtils;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;

public abstract class BaseWriteParamActivity extends BaseActivity {

//	private Thread mThread;
	public int RECONNECT_TIME = 3, RECONNECT_TIME1 = 3;
	private Param mParameter;
	private boolean hasSend;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public abstract void onSuccess();

	public void writeParameter(Param param) {
		this.mParameter = param;
		if (this.mParameter != null) {
			showProgressDialog("");
			startWriteParam();
		}
	}

	private void startWriteParam() {
		if(hasSend){
			return;
		}
		hasSend = true;
//		mThread = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
				if (RECONNECT_TIME > 0) {

					//0x01 0x46 参数编号（2bytes）0x00 len 参数数据（2-4bytes）CRCH CRCL
					String noHexStr = NumberBytes.padLeft(mParameter.getHexNo(), 4, '0');
					String len = "0x"+NumberBytes.padLeft(Integer.toHexString(mParameter.getValue().length()/2), 2, '0');
					String cmd = "0x01 0x46 " + noHexStr + "0x00 "+len+mParameter.getValue();
					CmdUtils.sendCmd(cmd,16, new CmdListener() {
						@Override
						public void start() {

						}

						@Override
						public void result(String result) {
							hasSend = false;
							hideProgressDialog();
							System.out.println("写入参数收到的数据====="
									+ result);
							if (result.length() != 16) {
								showToast(getResources().getString(R.string.string_error_msg11));
								return;
							}
							onSuccess();
						}

						@Override
						public void failure(String msg) {
							hasSend = false;
							startWriteParam();
						}

						@Override
						public void timeOut(String msg) {
							hasSend = false;
//							if (mThread != null && !mThread.isInterrupted()) {
//								mThread.interrupt();
//							}
							startWriteParam();
						}

						@Override
						public void connectFailure(String msg) {
							hasSend = false;
							hideProgressDialog();
							showConnectDevice();
						}

						@Override
						public void finish() {

						}
					});



//					ModbusUtils.writeParameter(mContext.getClass()
//							.getSimpleName(), mHandler, mParameter);
					System.out.println("===RECONNECT_TIME===" + RECONNECT_TIME);
					RECONNECT_TIME--;
				}
//			}
//		});
//		mThread.start();
	}
}
