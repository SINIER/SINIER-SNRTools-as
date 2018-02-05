package com.bluetooth.modbus.snrtools2.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools2.R;


public class ErrorItemView extends LinearLayout {

    private View view,ll;
    private TextView tvValue;

    public ErrorItemView(Context context) {
        super(context);
        initUI();
    }

    public ErrorItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initUI();
    }

    public ErrorItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI();
    }

    private void initUI(){
        setOrientation(LinearLayout.VERTICAL);
        view = View.inflate(getContext(), R.layout.error_item_layout,this);
        ll = view.findViewById(R.id.ll);
        tvValue = (TextView) view.findViewById(R.id.tvValue);
    }

    public void hideValue(){
        tvValue.setVisibility(GONE);
    }

    public void setValue(String value){
        tvValue.setText(value);
    }

    public void setBackGroundColor(int color){
        ll.setBackgroundColor(color);
    }

    public void setValueColor(int color){
        tvValue.setTextColor(color);
    }

    public void setBottomLineStatus(boolean isShow){
        view.findViewById(R.id.line).setVisibility(isShow?VISIBLE:GONE);
    }
}
