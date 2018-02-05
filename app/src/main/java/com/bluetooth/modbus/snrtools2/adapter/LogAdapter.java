package com.bluetooth.modbus.snrtools2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.bean.LogInfo;
import com.bluetooth.modbus.snrtools2.bean.SiriListItem;

import java.util.ArrayList;

public class LogAdapter extends BaseAdapter {
    private ArrayList<LogInfo> list;
    private LayoutInflater mInflater;

    public LogAdapter(Context context, ArrayList<LogInfo> list2) {
        list = list2;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        LogInfo item = list.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder(
                    (View) convertView.findViewById(R.id.list_child),
                    (TextView) convertView.findViewById(R.id.chat_msg),
                    (TextView) convertView.findViewById(R.id.label));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

//        if (item.isSiri()) {
//            viewHolder.child.setBackgroundColor(Color.parseColor("#46E109"));
//            viewHolder.label.setVisibility(View.VISIBLE);
//        } else {
            viewHolder.child.setBackgroundColor(Color.WHITE);
            viewHolder.label.setVisibility(View.GONE);
//        }
        viewHolder.msg.setText(item.timeStr+"  "+item.code+"  "+item.content);

        return convertView;
    }

    class ViewHolder {
        View child;
        TextView msg;
        TextView label;

        ViewHolder(View child, TextView msg, TextView label) {
            this.child = child;
            this.msg = msg;
            this.label = label;
        }
    }
}
