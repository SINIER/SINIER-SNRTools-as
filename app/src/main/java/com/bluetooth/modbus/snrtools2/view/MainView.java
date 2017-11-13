package com.bluetooth.modbus.snrtools2.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.db.Main;
import com.bluetooth.modbus.snrtools2.uitls.AppUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cchen on 2017/8/25.
 */

public class MainView extends View {

    private Paint mPaint;
    private static int COLUMN_COUNT = 128;
    private static float TEXT_SIZE_SMALL = 128;
    private static float TEXT_SIZE_NORMAL = 128;
    private static float TEXT_SIZE_LARGE = 128;
    private List<Main> values = new ArrayList<>();
    private Bitmap bitmap1, bitmap2, bitmap3, bitmap4, bitmap5, bitmap6, bitmap7, bitmap8, bitmap9, bitmap10, bitmap11, bitmap12;

    public MainView(Context context) {
        super(context);
        init();
    }

    public MainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MainView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        bitmap1 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_sub_blue_up_40872)).getBitmap();
        bitmap2 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_sub_blue_down_40866)).getBitmap();
        bitmap3 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_sub_blue_prev_40868)).getBitmap();
        bitmap4 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_sub_blue_next_40867)).getBitmap();
        bitmap5 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_warning_blue_40884)).getBitmap();
        bitmap6 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_battery_full_40685)).getBitmap();
        bitmap7 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_battery_half_40686)).getBitmap();
        bitmap8 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_battery_empty_40684)).getBitmap();
        bitmap9 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_battery_error_40684)).getBitmap();
        bitmap10 = ((BitmapDrawable) getResources().getDrawable(R.drawable.control)).getBitmap();
        bitmap11 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_bluetooth_blue_40697)).getBitmap();
        bitmap12 = ((BitmapDrawable) getResources().getDrawable(R.drawable.if_bluetooth_blue_40697)).getBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TEXT_SIZE_SMALL = getWidth() / (8 * 2);
        TEXT_SIZE_NORMAL = TEXT_SIZE_SMALL * 2;
        TEXT_SIZE_LARGE = TEXT_SIZE_SMALL * 3;
        for (int i = 0; i < values.size(); i++) {
            Main main = values.get(i);
            if ("0".equals(main.getType()) || "1".equals(main.getType()) || "3".equals(main.getType())) {
                if ("0".equals(main.getFontSize())) {
                    mPaint.setTextSize(TEXT_SIZE_NORMAL);
                } else if ("1".equals(main.getFontSize())) {
                    mPaint.setTextSize(TEXT_SIZE_SMALL);
                } else if ("2".equals(main.getFontSize())) {
                    mPaint.setTextSize(TEXT_SIZE_LARGE);
                }
                if ("0".equals(main.getGravity())) {
                    mPaint.setTextAlign(Paint.Align.LEFT);
                } else if ("1".equals(main.getGravity())) {
                    mPaint.setTextAlign(Paint.Align.RIGHT);
                } else if ("2".equals(main.getGravity())) {
                    mPaint.setTextAlign(Paint.Align.CENTER);
                }
                Paint.FontMetrics fm = mPaint.getFontMetrics();
                double dy = Math.ceil(fm.descent - fm.ascent);
                Path path = new Path();
                path.moveTo(AppUtil.parseToInt(main.getY(),0) * getWidth() / 128, AppUtil.parseToInt(main.getX(),0) * getWidth() / (2 * 8));
                path.lineTo((AppUtil.parseToInt(main.getY(),0) + AppUtil.parseToInt(main.getWidth(),0)) * getWidth() / 128, AppUtil.parseToInt(main.getX(),0) * getWidth() / (2 * 8));

                canvas.drawTextOnPath(main.getValue(), path, 0, (float) dy * 2 / 3, mPaint);
            } else if ("2".equals(main.getType())) {
                Rect dest = new Rect(AppUtil.parseToInt(main.getY(),0) * getWidth() / 128,
                        AppUtil.parseToInt(main.getX(),0) * getWidth() / (2 * 8),
                        (AppUtil.parseToInt(main.getY(),0) + AppUtil.parseToInt(main.getWidth(),0)) * getWidth() / 128,
                        (AppUtil.parseToInt(main.getX(),0)+1) * getWidth() / (2 * 8));
                if ("1".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap1.getWidth(), bitmap1.getHeight());
                    canvas.drawBitmap(bitmap1, src, dest, mPaint);
                } else if ("2".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight());
                    canvas.drawBitmap(bitmap2, src, dest, mPaint);
                } else if ("3".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap3.getWidth(), bitmap3.getHeight());
                    canvas.drawBitmap(bitmap3, src, dest, mPaint);
                } else if ("4".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap4.getWidth(), bitmap4.getHeight());
                    canvas.drawBitmap(bitmap4, src, dest, mPaint);
                } else if ("5".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap5.getWidth(), bitmap5.getHeight());
                    canvas.drawBitmap(bitmap5, src, dest, mPaint);
                } else if ("6".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap6.getWidth(), bitmap6.getHeight());
                    canvas.drawBitmap(bitmap6, src, dest, mPaint);
                } else if ("7".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap7.getWidth(), bitmap7.getHeight());
                    canvas.drawBitmap(bitmap7, src, dest, mPaint);
                } else if ("8".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap8.getWidth(), bitmap8.getHeight());
                    canvas.drawBitmap(bitmap8, src, dest, mPaint);
                } else if ("9".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap9.getWidth(), bitmap9.getHeight());
                    canvas.drawBitmap(bitmap9, src, dest, mPaint);
                } else if ("10".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap10.getWidth(), bitmap10.getHeight());
                    canvas.drawBitmap(bitmap10, src, dest, mPaint);
                } else if ("11".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap11.getWidth(), bitmap11.getHeight());
                    canvas.drawBitmap(bitmap11, src, dest, mPaint);
                } else if ("12".equals(main.getHexNo())) {
                    Rect src = new Rect(0, 0, bitmap12.getWidth(), bitmap12.getHeight());
                    canvas.drawBitmap(bitmap12, src, dest, mPaint);
                }
            } else if ("4".equals(main.getType())) {

            }
        }
        super.onDraw(canvas);
    }

    public void setValues(List<Main> values) {
        this.values.clear();
        if (values != null) {
            this.values.addAll(values);
        }
        invalidate();
    }

    public void notifyDataSetChange() {
        invalidate();
    }
}
