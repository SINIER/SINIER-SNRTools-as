package com.bluetooth.modbus.snrtools2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.method.NumberKeyListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools2.bean.Parameter;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.manager.ActivityManager;
import com.bluetooth.modbus.snrtools2.uitls.AppUtil;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;
import com.bluetooth.modbus.snrtools2.view.IPEdittext;

public class InputParamActivity extends BaseWriteParamActivity
{

	private TextView mTvTitle;
	private EditText mEtParam;
	private IPEdittext mEtIp;
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
		mEtParam = (EditText) findViewById(R.id.editText1);
		mEtIp = (IPEdittext) findViewById(R.id.ip);
		mTvTitle.setText(getIntent().getStringExtra("title"));
		mEtParam.setHint(getIntent().getStringExtra("value") + "(" + getResources().getString(R.string.string_hint1)
				+ p.getMin()+p.getUnit() + "~" + p.getMax()+p.getUnit() + ")");
		mEtIp.setVisibility(View.GONE);
		mEtParam.setVisibility(View.VISIBLE);
		 if ("1".equals(p.getType()+"")||"2".equals(p.getType()+"")||"5".equals(p.getType()+"")||"6".equals(p.getType()+"")) {
			 mEtParam.setKeyListener(new NumberKeyListener() {
				 @Override
				 protected char[] getAcceptedChars() {
					 return new char[] { '1', '2', '3', '4', '5', '6', '7', '8','9', '0','-' };
				 }
				 @Override
				 public int getInputType() {
					 // TODO Auto-generated method stub
					 return InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_CLASS_NUMBER;
				 }
			 });
		 }else if("8".equals(p.getType()+"")){
			 mEtParam.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
			 mEtParam.setKeyListener(new NumberKeyListener() {
				 @Override
				 protected char[] getAcceptedChars() {
					 return new char[] { '1', '2', '3', '4', '5', '6', '7', '8','9', '0','.' };
				 }
				 @Override
				 public int getInputType() {
					 return InputType.TYPE_CLASS_NUMBER;
				 }
			 });
			 mEtIp.setVisibility(View.VISIBLE);
			 mEtParam.setVisibility(View.GONE);
		 }
//		 else if (p.type == 2) {
//		 mEtParam.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
//		 mEtParam.setKeyListener(new DigitsKeyListener(false, true));
//		 } else if (p.type == 3) {
//		 mEtParam.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
//		 }
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.button2:
				if (p != null)
				{
					if("8".equals(p.getType())){
						String str = mEtIp.getText();
						String[] ips = str.split("\\.");
						if(ips.length!=4){
							showToast(getResources().getString(R.string.ip_error));
							return;
						}
						for(int i=0;i<ips.length;i++){
							if(AppUtil.parseToInt(ips[i],-1)<0||AppUtil.parseToInt(ips[i],-1)>255){
								showToast(getResources().getString(R.string.ip_error));
								return;
							}
						}
						p.setValue(AppUtil.getWriteValueByType(p.getType(), p.getCount(), str));
						p.setValueDisplay(str);
					}else {
						if (TextUtils.isEmpty(mEtParam.getText().toString().trim()))
						{
							showToast(getResources().getString(R.string.string_tips_msg8));
							return;
						}
						double valueIn = 0;
						valueIn = Double.parseDouble(mEtParam.getText().toString().trim());
						if (valueIn > AppUtil.parseToDouble(p.getMax(), 0)) {
							showToast(getResources().getString(R.string.string_tips_msg9) + AppUtil.parseToDouble(p.getMax(), 0) + "!");
							return;
						}
						if (valueIn < AppUtil.parseToDouble(p.getMin(), 0)) {
							showToast(getResources().getString(R.string.string_tips_msg10) + AppUtil.parseToDouble(p.getMin(), 0) + "!");
							return;
						}
						p.setValue(AppUtil.getWriteValueByType(p.getType(), p.getCount(), valueIn + ""));
						p.setValueDisplay(valueIn + "");
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
