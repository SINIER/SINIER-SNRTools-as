package com.bluetooth.modbus.snrtools2.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.bean.Parameter;
import com.bluetooth.modbus.snrtools2.db.Cmd;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.db.ParamGroup;

public class ParameterAdapter extends BaseAdapter {

    private Context mContext;
    private List<Object> mList;

    public ParameterAdapter(Context context, List<Object> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.parameter_item, null);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvValue = (TextView) convertView.findViewById(R.id.tvValue);
            holder.tvGroupName = (TextView) convertView.findViewById(R.id.tvGroupName);
            holder.ll = convertView.findViewById(R.id.ll);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mList.get(position) instanceof Param) {
            Param param = (Param) mList.get(position);
            holder.tvName.setText(param.getName()+"=="+param.getType());
            holder.tvValue.setText(param.getValueDisplay());
            holder.tvGroupName.setVisibility(View.GONE);
            holder.ll.setVisibility(View.VISIBLE);
            System.out.println("==================="+param.getHexNo()+param.getName()+"=="+param.getValue()+"=="+param.getValueDisplay());
        } else if (mList.get(position) instanceof ParamGroup) {
            ParamGroup paramGroup = (ParamGroup) mList.get(position);
            holder.tvGroupName.setVisibility(View.VISIBLE);
            holder.ll.setVisibility(View.GONE);
            holder.tvGroupName.setText(paramGroup.getName());
        } else if (mList.get(position) instanceof Cmd) {
            Cmd cmd = (Cmd) mList.get(position);
            holder.tvName.setText(cmd.getCmdName());
            holder.tvValue.setText("");
            holder.tvGroupName.setVisibility(View.GONE);
            holder.ll.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvValue;
        TextView tvGroupName;
        View ll;
    }
}
