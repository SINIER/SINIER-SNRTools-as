package com.bluetooth.modbus.snrtools2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools2.db.DBManager;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.manager.ActivityManager;
import com.bluetooth.modbus.snrtools2.uitls.AppUtil;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;
import com.bluetooth.modbus.snrtools2.view.IPEdittext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class InputParamActivity extends BaseWriteParamActivity
{
	private TextView mTvTitle,tvRange;
	private EditText mEtParam;
	private IPEdittext mEtIp;
	private EditText mEtYear,mEtMonth,mEtDay,mEtHour,mEtMin,mEtSec;
	private LinearLayout llDate;
	private Param p;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_param_activity);
		p = (Param) getIntent().getSerializableExtra("param");
		initUI();
	}

	private void initUI()
	{
		mTvTitle = (TextView) findViewById(R.id.tvTitle);
		tvRange = (TextView) findViewById(R.id.tvRange);
		mEtParam = (EditText) findViewById(R.id.editText1);
		llDate = (LinearLayout) findViewById(R.id.llDate);
		mEtYear = (EditText) findViewById(R.id.etYear);
		mEtMonth = (EditText) findViewById(R.id.etMonth);
		mEtDay = (EditText) findViewById(R.id.etDay);
		mEtHour = (EditText) findViewById(R.id.etHour);
		mEtMin = (EditText) findViewById(R.id.etMin);
		mEtSec = (EditText) findViewById(R.id.etSec);
		mEtIp = (IPEdittext) findViewById(R.id.ip);
		mTvTitle.setText(getIntent().getStringExtra("title"));
		mEtParam.setHint(getIntent().getStringExtra("value"));
		tvRange.setHint("(" + p.getMin() + "~" + p.getMax() + ")"+ DBManager.getInstance().getStr(p.getUnit()));
		mEtIp.setVisibility(View.GONE);
		llDate.setVisibility(View.GONE);
		mEtParam.setVisibility(View.VISIBLE);
		if ("1".equals(p.getType()+"")||			// dt_short 16位有符号短整数
			 "2".equals(p.getType()+"")||		// dt_word 16位无符号短整数
			 "5".equals(p.getType()+"")||		// dt_int 32位有符号整数
			 "6".equals(p.getType()+""))		// dt_dword 32位无符号整数;
		{
		 	 mEtParam.setKeyListener(new NumberKeyListener()
			{
				 @Override
				 protected char[] getAcceptedChars()
				 {
					 return new char[] { '1', '2', '3', '4', '5', '6', '7', '8','9', '0','-' };
				 }
				 @Override
				 public int getInputType()
				 {
					 // TODO Auto-generated method stub
					 return InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_CLASS_NUMBER;
				 }
			 });
		 }
		else if("9".equals(p.getType()+""))
		{
			//dt_string
			mEtParam.setInputType(EditorInfo.TYPE_CLASS_TEXT);
			tvRange.setVisibility(View.GONE);
		}
		else if("8".equals(p.getType()+""))
		{
			// dt_ipadd IP地址
			mEtParam.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
			mEtParam.setKeyListener(new NumberKeyListener()
			{
				@Override
				protected char[] getAcceptedChars()
				{
					return new char[] { '1', '2', '3', '4', '5', '6', '7', '8','9', '0','.' };
				}
				@Override
				public int getInputType() {
					return InputType.TYPE_CLASS_NUMBER;
				}
			});
			mEtIp.setVisibility(View.VISIBLE);
			mEtParam.setVisibility(View.GONE);
			tvRange.setVisibility(View.GONE);
		}
		else if("13".equals(p.getType()+""))
		{
			// 日期
			llDate.setVisibility(View.VISIBLE);
			mEtIp.setVisibility(View.GONE);
			mEtParam.setVisibility(View.GONE);
			tvRange.setVisibility(View.GONE);
			try {
				Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(p.getValueDisplay());
				mEtYear.setHint(String.valueOf(date.getYear()));
				mEtYear.setText(String.valueOf(date.getYear()));
				mEtMonth.setHint(String.valueOf(date.getMonth()+1));
				mEtMonth.setText(String.valueOf(date.getMonth()+1));
				mEtDay.setHint(String.valueOf(date.getDay()));
				mEtDay.setText(String.valueOf(date.getDay()));
				mEtHour.setHint(String.valueOf(date.getHours()));
				mEtHour.setText(String.valueOf(date.getHours()));
				mEtMin.setHint(String.valueOf(date.getMinutes()));
				mEtMin.setText(String.valueOf(date.getMinutes()));
				mEtSec.setHint(String.valueOf(date.getSeconds()));
				mEtSec.setText(String.valueOf(date.getSeconds()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.button2:
				if (p != null)
				{
					if("8".equals(p.getType()))
					{
						// dt_ipadd IP地址
						String str = mEtIp.getText();
						String[] ips = str.split("\\.");
						if(ips.length!=4)
						{
							showToast(getResources().getString(R.string.ip_error));
							return;
						}
						for(int i=0;i<ips.length;i++)
						{
							if(AppUtil.parseToInt(ips[i],-1)<0||AppUtil.parseToInt(ips[i],-1)>255)
							{
								showToast(getResources().getString(R.string.ip_error));
								return;
							}
						}
						p.setValue(AppUtil.getWriteValueByType(p.getType(), p.getCount(), str));
						p.setValueDisplay(str);
					}
					else if("13".equals(p.getType()))
					{
						// 日期
						if(TextUtils.isEmpty(mEtYear.getText()) || TextUtils.isEmpty(mEtMonth.getText())
								|| TextUtils.isEmpty(mEtDay.getText()) || TextUtils.isEmpty(mEtHour.getText())
								|| TextUtils.isEmpty(mEtMin.getText()) || TextUtils.isEmpty(mEtSec.getText())){
							showToast("格式不对");
							return;
						}
						int year = Integer.parseInt(mEtYear.getText().toString());
						int month = Integer.parseInt(mEtMonth.getText().toString());
						int day = Integer.parseInt(mEtDay.getText().toString());
						int hour = Integer.parseInt(mEtHour.getText().toString());
						int min = Integer.parseInt(mEtMin.getText().toString());
						int sec = Integer.parseInt(mEtSec.getText().toString());
						if(month > 12 || month < 1){
							showToast("格式不对");
							return;
						}
						int monthDays = 28;
						if(year % 4 == 0 && year %100 != 0){
							monthDays = 29;
						}
						if(day > monthDays || day < 1){
							showToast("格式不对");
							return;
						}
						if(hour > 23 || hour < 0){
							showToast("格式不对");
							return;
						}
						if(min > 59 || min < 0){
							showToast("格式不对");
							return;
						}
						if(sec > 59 || sec < 0){
							showToast("格式不对");
							return;
						}
						Calendar calendar = Calendar.getInstance();
						calendar.set(year,month-1,day,hour,min,sec);
						p.setValue(AppUtil.getWriteValueByType(p.getType(), p.getCount(), calendar.getTimeInMillis()+""));
						p.setValueDisplay(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).format(calendar.getTime()));
					}else if("9".equals(p.getType())){
						String str = mEtParam.getText().toString().trim();
						p.setValue(AppUtil.getWriteValueByType(p.getType(), p.getCount(), str));
						p.setValueDisplay(str);
					}
					else
					{
						if (TextUtils.isEmpty(mEtParam.getText().toString().trim()))
						{
							showToast(getResources().getString(R.string.string_tips_msg8));
							return;
						}
						double valueIn = 0;
						valueIn = Double.parseDouble(mEtParam.getText().toString().trim());
						if (valueIn > AppUtil.parseToDouble(p.getMax(), 0))
						{
							showToast(getResources().getString(R.string.string_tips_msg9) + AppUtil.parseToDouble(p.getMax(), 0) + "!");
							return;
						}
						if (valueIn < AppUtil.parseToDouble(p.getMin(), 0))
						{
							showToast(getResources().getString(R.string.string_tips_msg10) + AppUtil.parseToDouble(p.getMin(), 0) + "!");
							return;
						}
						p.setValue(AppUtil.getWriteValueByType(p.getType(), p.getCount(), valueIn + ""));
						p.setValueDisplay(NumberBytes.subZeroAndDot(valueIn+"")+DBManager.getInstance().getStr(p.getUnit()));
					}
					writeParameter(p);
				}
				break;
		}
	}

	@Override
	public void reconnectSuccss()
	{
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN && isOutOfBounds(this, event))
		{
			ActivityManager.getInstances().finishActivity(this);
			return true;
		}
		return super.onTouchEvent(event);
	}

	private boolean isOutOfBounds(Activity context, MotionEvent event)
	{
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
		final View decorView = context.getWindow().getDecorView();
		return (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop))
				|| (y > (decorView.getHeight() + slop));
	}

	@Override
	public void onSuccess()
	{
		Intent intent = new Intent();
		intent.putExtra("position", getIntent().getIntExtra("position", -1));
		intent.putExtra("value", p.getValueDisplay());
		intent.putExtra("valueIn", p.getValue());
		setResult(RESULT_OK, intent);
		ActivityManager.getInstances().finishActivity(InputParamActivity.this);
	}
}
