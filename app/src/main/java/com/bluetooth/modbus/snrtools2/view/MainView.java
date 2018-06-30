package com.bluetooth.modbus.snrtools2.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
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
    private static float TEXT_SIZE_SMALL = 0;
    private static float TEXT_SIZE_NORMAL = 0;
    private static float TEXT_SIZE_LARGE = 0;
    private List<Main> values = new ArrayList<>();
    private Bitmap arrow_l, arrow_r, arrow_t, arrow_b,
            battery_empty, battery_error, battery_full, battery_half,
            wait, warning, controler, bluetooth,s_bluetooth,
            s_gprs0, s_gprs1, s_gprs2, s_gprs3, s_gprs4,
            s_chatou,s_battery_error,s_battery0,s_battery1,
            s_battery2,s_battery3,s_battery4,s_battery5,s_battery6,
            s_battery7,s_battery8,s_battery9,s_battery10;

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
        mPaint.setColor(Color.parseColor("#333333"));
        arrow_l = ((BitmapDrawable) getResources().getDrawable(R.drawable.arrow_l)).getBitmap();
        arrow_r = ((BitmapDrawable) getResources().getDrawable(R.drawable.arrow_r)).getBitmap();
        arrow_b = ((BitmapDrawable) getResources().getDrawable(R.drawable.arrow_b)).getBitmap();
        arrow_t = ((BitmapDrawable) getResources().getDrawable(R.drawable.arrow_t)).getBitmap();
        battery_empty = ((BitmapDrawable) getResources().getDrawable(R.drawable.battery_empty)).getBitmap();
        battery_error = ((BitmapDrawable) getResources().getDrawable(R.drawable.battery_error)).getBitmap();
        battery_full = ((BitmapDrawable) getResources().getDrawable(R.drawable.battery_full)).getBitmap();
        battery_half = ((BitmapDrawable) getResources().getDrawable(R.drawable.battery_half)).getBitmap();
        controler = ((BitmapDrawable) getResources().getDrawable(R.drawable.controler)).getBitmap();
        wait = ((BitmapDrawable) getResources().getDrawable(R.drawable.wait)).getBitmap();
        bluetooth = ((BitmapDrawable) getResources().getDrawable(R.drawable.bluetooth)).getBitmap();
        warning = ((BitmapDrawable) getResources().getDrawable(R.drawable.warning)).getBitmap();
        s_bluetooth = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_bluetooth)).getBitmap();
        s_gprs0 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_gprs0)).getBitmap();
        s_gprs1 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_gprs1)).getBitmap();
        s_gprs2 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_gprs2)).getBitmap();
        s_gprs3 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_gprs3)).getBitmap();
        s_gprs4 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_gprs4)).getBitmap();
        s_chatou = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_chatou)).getBitmap();
        s_battery_error = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery_error)).getBitmap();
        s_battery0 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery0)).getBitmap();
        s_battery1 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery1)).getBitmap();
        s_battery2 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery2)).getBitmap();
        s_battery3 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery3)).getBitmap();
        s_battery4 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery4)).getBitmap();
        s_battery5 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery5)).getBitmap();
        s_battery6 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery6)).getBitmap();
        s_battery7 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery7)).getBitmap();
        s_battery8 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery8)).getBitmap();
        s_battery9 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery9)).getBitmap();
        s_battery10 = ((BitmapDrawable) getResources().getDrawable(R.drawable.s_battery10)).getBitmap();
    }

    private void clearCanvas(Canvas canvas) {
        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    public float getTextWidth(String text, float textSize) {
//        TextPaint paint = new TextPaint();
//        paint.setTextSize(textSize);
        mPaint.setTextSize(textSize);
        return mPaint.measureText(text);
    }

    private void calcTextSize(){
        if(TEXT_SIZE_SMALL==0) {
            String small = "ABCDEFGHIJKLMNOPQRSTU";
            String normal = "可以容纳八个汉字";
            String large = "999999999";
            float index = getWidth()/2;
            while (TEXT_SIZE_SMALL==0){
                if(getTextWidth(small,index)<getWidth()){
                    TEXT_SIZE_SMALL = index;
                    break;
                }
                index--;
            }
            index = getWidth()/2;
            while (TEXT_SIZE_NORMAL==0){
                if(getTextWidth(normal,index)<getWidth()){
                    TEXT_SIZE_NORMAL = index;
                    break;
                }
                index--;
            }
            index = getWidth()/2;
            while (TEXT_SIZE_LARGE==0){
                if(getTextWidth(large,index)<getWidth()){
                    TEXT_SIZE_LARGE = index;
                    break;
                }
                index--;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.parseColor("#00FFA9"));
        calcTextSize();
//        float scale = 1f;
//        TEXT_SIZE_SMALL = scale*getWidth() / 21f;
//        TEXT_SIZE_NORMAL = scale*getWidth() / 16f;
//        TEXT_SIZE_LARGE = scale*getWidth() / 9f;
        for (int i = 0; i < values.size(); i++) {
            Main main = values.get(i);
            if ("0".equals(main.getType()) || "1".equals(main.getType()) || "3".equals(main.getType())) {
                float valueSize = 0, unitSize = 0;
                if ("0".equals(main.getFontSize())) {
                    valueSize = TEXT_SIZE_NORMAL;
                    unitSize = TEXT_SIZE_SMALL;
                } else if ("1".equals(main.getFontSize())) {
                    valueSize = TEXT_SIZE_SMALL;
                    unitSize = TEXT_SIZE_SMALL;
                } else if ("2".equals(main.getFontSize())) {
                    valueSize = TEXT_SIZE_LARGE;
                    unitSize = TEXT_SIZE_NORMAL;
                }
                float valueWidth = getTextWidth(main.getValue(), valueSize);
                float unitWidth = getTextWidth(main.getUnitStr(), unitSize);
                mPaint.setTextSize(valueSize);
                mPaint.setTextAlign(Paint.Align.LEFT);
                Paint.FontMetrics fm = mPaint.getFontMetrics();
                double dy = Math.ceil(fm.descent - fm.ascent);
//                float valueWidth = mPaint.measureText(main.getValue());
//                float unitWidth = mPaint.measureText(main.getUnitStr());
                int startX = AppUtil.parseToInt(main.getY(), 0) * getWidth() / 128;
                int startY = AppUtil.parseToInt(main.getX(), 0) * getHeight() / 8;
//                int startY = AppUtil.parseToInt(main.getX(), 0) * getWidth() / (2 * 8);
                int viewWidth = AppUtil.parseToInt(main.getWidth(), 0) * getWidth() / 128;//占用宽度
                Path path = new Path();
                if ("2".equals(main.getGravity())) {
                    mPaint.setTextAlign(Paint.Align.CENTER);
                    path.moveTo(startX + (viewWidth - valueWidth - unitWidth) / 2, startY);
                    path.lineTo(startX + (viewWidth - valueWidth - unitWidth) / 2 + valueWidth, startY);
                } else if ("1".equals(main.getGravity())) {
                    mPaint.setTextAlign(Paint.Align.RIGHT);
                    path.moveTo(startX + viewWidth - unitWidth - valueWidth, startY);
                    path.lineTo(startX + viewWidth - unitWidth, startY);
                } else {
                    path.moveTo(startX, startY);
                    path.lineTo(startX + valueWidth, startY);
                }
                canvas.drawTextOnPath(main.getValue(), path, 0, (float) dy * 2 / 3, mPaint);
                if (!TextUtils.isEmpty(main.getUnitStr())) {
                    path = new Path();
                    mPaint.setTextSize(unitSize);
                    if ("2".equals(main.getGravity())) {
                        mPaint.setTextAlign(Paint.Align.LEFT);
                        path.moveTo(startX + (viewWidth - valueWidth - unitWidth) / 2 + valueWidth, startY);
                    } else if ("1".equals(main.getGravity())) {
                        mPaint.setTextAlign(Paint.Align.RIGHT);
                        path.moveTo(startX + viewWidth - unitWidth, startY);
                    } else {
                        path.moveTo(startX + valueWidth, startY);
                    }
                    path.lineTo(startX + viewWidth, startY);
                    canvas.drawTextOnPath(main.getUnitStr(), path, 0, (float) dy * 2 / 3, mPaint);
                }

            } else if ("2".equals(main.getType())) {
                if ("0".equals(main.getFontSize())) {
                    if ("1".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, arrow_t.getWidth(), arrow_t.getHeight());
                        canvas.drawBitmap(arrow_t, src, getDest(arrow_t,main,2), mPaint);
                    } else if ("2".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, arrow_b.getWidth(), arrow_b.getHeight());
                        canvas.drawBitmap(arrow_b, src, getDest(arrow_b,main,2), mPaint);
                    } else if ("3".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, arrow_l.getWidth(), arrow_l.getHeight());
                        canvas.drawBitmap(arrow_l, src, getDest(arrow_l,main,2), mPaint);
                    } else if ("4".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, arrow_r.getWidth(), arrow_r.getHeight());
                        canvas.drawBitmap(arrow_r, src, getDest(arrow_r,main,2), mPaint);
                    } else if ("5".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, warning.getWidth(), warning.getHeight());
                        canvas.drawBitmap(warning, src, getDest(warning,main,2), mPaint);
                    } else if ("6".equals(main.getNo())) {

                    } else if ("7".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, battery_full.getWidth(), battery_full.getHeight());
                        canvas.drawBitmap(battery_full, src, getDest(battery_full,main,2), mPaint);
                    } else if ("8".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, battery_half.getWidth(), battery_half.getHeight());
                        canvas.drawBitmap(battery_half, src, getDest(battery_half,main,2), mPaint);
                    } else if ("9".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, battery_empty.getWidth(), battery_empty.getHeight());
                        canvas.drawBitmap(battery_empty, src, getDest(battery_empty,main,2), mPaint);
                    } else if ("10".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, battery_error.getWidth(), battery_error.getHeight());
                        canvas.drawBitmap(battery_error, src, getDest(battery_error,main,2), mPaint);
                    } else if ("11".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, controler.getWidth(), controler.getHeight());
                        canvas.drawBitmap(controler, src, getDest(controler,main,2), mPaint);
                    } else if ("12".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, bluetooth.getWidth(), bluetooth.getHeight());
                        canvas.drawBitmap(bluetooth, src, getDest(bluetooth,main,2), mPaint);
                    } else if ("13".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, wait.getWidth(), wait.getHeight());
                        canvas.drawBitmap(wait, src, getDest(wait,main,2), mPaint);
                    }
                } else if ("1".equals(main.getFontSize())) {
                    if ("1".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_gprs0.getWidth(), s_gprs0.getHeight());
                        canvas.drawBitmap(s_gprs0, src, getDest(s_gprs0,main,1), mPaint);
                    } else if ("2".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_gprs1.getWidth(), s_gprs1.getHeight());
                        canvas.drawBitmap(s_gprs1, src, getDest(s_gprs1,main,1), mPaint);
                    } else if ("3".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_gprs2.getWidth(), s_gprs2.getHeight());
                        canvas.drawBitmap(s_gprs2, src, getDest(s_gprs2,main,1), mPaint);
                    } else if ("4".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_gprs3.getWidth(), s_gprs3.getHeight());
                        canvas.drawBitmap(s_gprs3, src, getDest(s_gprs3,main,1), mPaint);
                    } else if ("5".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_gprs4.getWidth(), s_gprs4.getHeight());
                        canvas.drawBitmap(s_gprs4, src, getDest(s_gprs4,main,1), mPaint);
                    } else if ("6".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_bluetooth.getWidth(), s_bluetooth.getHeight());
                        canvas.drawBitmap(s_bluetooth, src, getDest(s_bluetooth,main,1), mPaint);
                    } else if ("7".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery_error.getWidth(), s_battery_error.getHeight());
                        canvas.drawBitmap(s_battery_error, src, getDest(s_battery_error,main,1), mPaint);
                    } else if ("8".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery0.getWidth(), s_battery0.getHeight());
                        canvas.drawBitmap(s_battery0, src, getDest(s_battery0,main,1), mPaint);
                    } else if ("9".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery1.getWidth(), s_battery1.getHeight());
                        canvas.drawBitmap(s_battery1, src, getDest(s_battery1,main,1), mPaint);
                    } else if ("10".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery2.getWidth(), s_battery2.getHeight());
                        canvas.drawBitmap(s_battery2, src, getDest(s_battery2,main,1), mPaint);
                    } else if ("11".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery3.getWidth(), s_battery3.getHeight());
                        canvas.drawBitmap(s_battery3, src, getDest(s_battery3,main,1), mPaint);
                    } else if ("12".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery4.getWidth(), s_battery4.getHeight());
                        canvas.drawBitmap(s_battery4, src, getDest(s_battery4,main,1), mPaint);
                    } else if ("13".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery5.getWidth(), s_battery5.getHeight());
                        canvas.drawBitmap(s_battery5, src, getDest(s_battery5,main,1), mPaint);
                    } else if ("14".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery6.getWidth(), s_battery6.getHeight());
                        canvas.drawBitmap(s_battery6, src, getDest(s_battery6,main,1), mPaint);
                    } else if ("15".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery7.getWidth(), s_battery7.getHeight());
                        canvas.drawBitmap(s_battery7, src, getDest(s_battery7,main,1), mPaint);
                    } else if ("16".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery8.getWidth(), s_battery8.getHeight());
                        canvas.drawBitmap(s_battery8, src, getDest(s_battery8,main,1), mPaint);
                    } else if ("17".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery9.getWidth(), s_battery9.getHeight());
                        canvas.drawBitmap(s_battery9, src, getDest(s_battery9,main,1), mPaint);
                    } else if ("18".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_battery10.getWidth(), s_battery10.getHeight());
                        canvas.drawBitmap(s_battery10, src, getDest(s_battery10,main,1), mPaint);
                    } else if ("19".equals(main.getNo())) {
                        Rect src = new Rect(0, 0, s_chatou.getWidth(), s_chatou.getHeight());
                        canvas.drawBitmap(s_chatou, src, getDest(s_chatou,main,1), mPaint);
                    }
                }
            } else if ("4".equals(main.getType())) {

            }
        }
        super.onDraw(canvas);
    }

    private Rect getDest(Bitmap bitmap, Main main, int scale) {
        int left = AppUtil.parseToInt(main.getY(), 0) * getWidth() / 128;
        int top = AppUtil.parseToInt(main.getX(), 0) * getHeight() / 8;
//        int top = AppUtil.parseToInt(main.getX(), 0) * getWidth() / (2 * 8);
        int right = AppUtil.parseToInt(main.getY(), 0) * getWidth() / 128 + (int)(bitmap.getWidth()*((scale* getWidth() / (2f * 8f))/bitmap.getHeight()));
        int bottom = (AppUtil.parseToInt(main.getX(), 0) + scale) * getHeight() / 8;
//        int bottom = (AppUtil.parseToInt(main.getX(), 0) + scale) * getWidth() / (2 * 8);
        Rect dest = new Rect(left, top, right,bottom);
        return dest;
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
