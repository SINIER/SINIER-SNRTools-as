package com.bluetooth.modbus.snrtools2.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bluetooth.modbus.snrtools2.R;

/**
 * Created by cchen on 2017/9/25.
 */

public class IPEdittext extends LinearLayout {

    private View root;
    private EditText mFirstIP;
    private EditText mSecondIP;
    private EditText mThirdIP;
    private EditText mFourthIP;

    public IPEdittext(Context context) {
        super(context);
        init();
    }

    public IPEdittext(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IPEdittext(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        root = LayoutInflater.from(getContext()).inflate(
                R.layout.ip_edittext, this);
        mFirstIP = (EditText) findViewById(R.id.ip_first);
        mSecondIP = (EditText) findViewById(R.id.ip_second);
        mThirdIP = (EditText) findViewById(R.id.ip_third);
        mFourthIP = (EditText) findViewById(R.id.ip_fourth);
        OperatingEditText(getContext());
    }

    /**
     * 获得EditText中的内容,当每个Edittext的字符达到三位时,自动跳转到下一个EditText,当用户点击.时,
     * 下一个EditText获得焦点
     */
    private void OperatingEditText(final Context context) {
        mFirstIP.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
/**
 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
 * 用户点击啊.时,下一个EditText获得焦点
 */
                if (s != null && s.length() > 0) {
                    if (s.length() > 2 || s.toString().trim().contains(".")) {
                        if (s.toString().trim().contains(".")) {
                            mFirstIP.setText(s.toString().substring(0, s.length() - 1));
                        }else
                        if (Integer.parseInt(s.toString()) > 255) {
                            Toast.makeText(context, getResources().getString(R.string.ip_error),
                                    Toast.LENGTH_LONG).show();
                            mFirstIP.setText(s.toString().substring(0, s.length() - 1));
                            mFirstIP.setSelection(mFirstIP.getText().length());
                            return;
                        }

                        mSecondIP.setFocusable(true);
                        mSecondIP.requestFocus();
                    }
                }
            }
        });

        mSecondIP.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                /**
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击啊.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0) {
                    if (s.length() > 2 || s.toString().trim().contains(".")) {
                        if (s.toString().trim().contains(".")) {
                            mSecondIP.setText(s.toString().substring(0, s.length() - 1));
                        }else

                        if (Integer.parseInt(s.toString()) > 255) {
                            Toast.makeText(context, getResources().getString(R.string.ip_error),
                                    Toast.LENGTH_LONG).show();
                            mSecondIP.setText(s.toString().substring(0, s.length() - 1));
                            mSecondIP.setSelection(mSecondIP.getText().length());
                            return;
                        }

                        mThirdIP.setFocusable(true);
                        mThirdIP.requestFocus();
                    }
                }

                /**
                 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
                 */
                if (s!=null && s.length() == 0) {
                    mFirstIP.setFocusable(true);
                    mFirstIP.requestFocus();
                    mFirstIP.setSelection(mFirstIP.getText().length());
                }
            }
        });

        mThirdIP.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
/**
 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
 * 用户点击啊.时,下一个EditText获得焦点
 */
                if (s != null && s.length() > 0) {
                    if (s.length() > 2 || s.toString().trim().contains(".")) {
                        if (s.toString().trim().contains(".")) {
                            mThirdIP.setText(s.toString().substring(0, s.length() - 1));
                        }else

                        if (Integer.parseInt(s.toString()) > 255) {
                            Toast.makeText(context, getResources().getString(R.string.ip_error),
                                    Toast.LENGTH_LONG).show();
                            mThirdIP.setText(s.toString().substring(0, s.length() - 1));
                            mThirdIP.setSelection(mThirdIP.getText().length());
                            return;
                        }

                        mFourthIP.setFocusable(true);
                        mFourthIP.requestFocus();
                    }
                }

                /**
                 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
                 */
                if (s!=null && s.length() == 0) {
                    mSecondIP.setFocusable(true);
                    mSecondIP.requestFocus();
                    mSecondIP.setSelection(mSecondIP.getText().length());
                }
            }
        });

        mFourthIP.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                /**
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击啊.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0) {

                    if (s.toString().trim().contains(".")) {
                        mFourthIP.setText(s.toString().substring(0, s.length() - 1));
                        mFourthIP.setSelection(mFourthIP.getText().length());
                        return;
                    }
                    if (Integer.parseInt(s.toString()) > 255) {
                        Toast.makeText(context, getResources().getString(R.string.ip_error), Toast.LENGTH_LONG)
                                .show();
                        mFourthIP.setText(s.toString().substring(0, s.length() - 1));
                        mFourthIP.setSelection(mFourthIP.getText().length());
                        return;
                    }

                }

                /**
                 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
                 */
                if (s != null && s.length() == 0) {
                    mThirdIP.setFocusable(true);
                    mThirdIP.requestFocus();
                    mThirdIP.setSelection(mThirdIP.getText().length());
                }
            }
        });
    }

    public String getText() {
        if (TextUtils.isEmpty(mFirstIP.getText()) || TextUtils.isEmpty(mSecondIP.getText())
                || TextUtils.isEmpty(mThirdIP.getText()) || TextUtils.isEmpty(mFirstIP.getText())) {
            Toast.makeText(getContext(), getResources().getString(R.string.ip_error), Toast.LENGTH_LONG).show();
        }
        return mFirstIP.getText() + "." + mSecondIP.getText() + "." + mThirdIP.getText() + "." + mFourthIP.getText();
    }
}
