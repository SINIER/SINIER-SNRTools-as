package com.bluetooth.modbus.snrtools2.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bluetooth.modbus.snrtools2.R;
import com.bluetooth.modbus.snrtools2.bean.ChaoBiao;

import java.util.List;

public class ChaoBiaoAdapter extends BaseAdapter {
    private Context context;
    private List<ChaoBiao> list;

    public ChaoBiaoAdapter(Context context, List<ChaoBiao> list2) {
        this.context = context;
        list = list2;
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
        ChaoBiao item = list.get(position);
        if (convertView == null) {
            convertView = View.inflate(context,R.layout.chaobiao_item, null);
            viewHolder = new ViewHolder(
                    (TextView) convertView.findViewById(R.id.tvBianHao),
                    (TextView) convertView.findViewById(R.id.tvShunShiLiuLiang),
                    (TextView) convertView.findViewById(R.id.tvLeiJiLiuLiang),
                    (TextView) convertView.findViewById(R.id.tvShuJuBao));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvBianHao.setText(item.bianhao);
        viewHolder.tvShunShiLiuLiang.setText(item.shunshiliuliang);
        viewHolder.tvLeiJiLiuLiang.setText(item.leijiliuliang);
        viewHolder.tvShuJuBao.setText(String.valueOf(item.shujubao));

        return convertView;
    }

    class ViewHolder {
        TextView tvBianHao;
        TextView tvShunShiLiuLiang;
        TextView tvLeiJiLiuLiang;
        TextView tvShuJuBao;

        ViewHolder(TextView bianhao, TextView shunshiliuliang,TextView leijiliuliang, TextView shujubao) {
            this.tvBianHao = bianhao;
            this.tvShunShiLiuLiang = shunshiliuliang;
            this.tvLeiJiLiuLiang = leijiliuliang;
            this.tvShuJuBao = shujubao;
        }
    }
}
