package com.bluetooth.modbus.snrtools2;

import android.os.Bundle;
import android.widget.EditText;

import com.bluetooth.modbus.snrtools2.db.Cmd;
import com.bluetooth.modbus.snrtools2.db.DBManager;
import com.bluetooth.modbus.snrtools2.db.Main;
import com.bluetooth.modbus.snrtools2.db.OfflineString;
import com.bluetooth.modbus.snrtools2.db.Param;
import com.bluetooth.modbus.snrtools2.db.ParamGroup;
import com.bluetooth.modbus.snrtools2.db.Value;
import com.bluetooth.modbus.snrtools2.db.Var;

import java.util.List;

/**
 * 创建人 cchen
 * 创建日期 2017/11/21
 */

public class DBDataActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.db_layout);
        EditText tv = (EditText) findViewById(R.id.tv);
        StringBuilder sb = new StringBuilder("");
        sb.append("\n主界面变量\n");
        List<Main> mains = DBManager.getInstance().getMainList();
        for(int i=0;i<mains.size();i++){
            Main main = mains.get(i);
            sb.append("  hexno=");
            sb.append(main.getHexNo());
            sb.append("  count=");
            sb.append(main.getCount());
            sb.append("  fontsize=");
            sb.append(main.getFontSize());
            sb.append("  bt=");
            sb.append(main.getBtAddress());
            sb.append("  gravity=");
            sb.append(main.getGravity());
            sb.append("  height=");
            sb.append(main.getHeight());
            sb.append("  width=");
            sb.append(main.getWidth());
            sb.append("  x=");
            sb.append(main.getX());
            sb.append("  y=");
            sb.append(main.getY());
            sb.append("  type=");
            sb.append(main.getType());
            sb.append("  value=");
            sb.append(main.getValue());
            sb.append("\n");
        }
        sb.append("\n参数\n");
        List<Param> params = DBManager.getInstance().getAllParams();
        for(int i=0;i<params.size();i++){
            Param param = params.get(i);
            sb.append("  hexno=");
            sb.append(param.getHexNo());
            sb.append("  count=");
            sb.append(param.getCount());
            sb.append("  linkvariable=");
            sb.append(param.getLinkVariable());
            sb.append("  bt=");
            sb.append(param.getBtAddress());
            sb.append("  max=");
            sb.append(param.getMax());
            sb.append("  min=");
            sb.append(param.getMin());
            sb.append("  name=");
            sb.append(param.getName());
            sb.append("  grouphexno=");
            sb.append(param.getParamGroupHexNo());
            sb.append("  type=");
            sb.append(param.getType());
            sb.append("  unit=");
            sb.append(param.getUnit());
            sb.append("  value=");
            sb.append(param.getValue());
            sb.append("  valueDisplay=");
            sb.append(param.getValueDisplay());
            sb.append("\n");
        }
        sb.append("\n参数组\n");
        List<ParamGroup> groups = DBManager.getInstance().getAllParamGroups();
        for(int i=0;i<groups.size();i++){
            ParamGroup paramGroup = groups.get(i);
            sb.append("  hexno=");
            sb.append(paramGroup.getHexNo());
            sb.append("  level=");
            sb.append(paramGroup.getLevel());
            sb.append("  name=");
            sb.append(paramGroup.getName());
            sb.append("  bt=");
            sb.append(paramGroup.getBtAddress());
            sb.append("\n");
        }
        sb.append("\n命令\n");
        List<Cmd> cmds = DBManager.getInstance().getAllCmds();
        for(int i=0;i<cmds.size();i++){
            Cmd cmd = cmds.get(i);
            sb.append("  hexno=");
            sb.append(cmd.getHexNo());
            sb.append("  pwd=");
            sb.append(cmd.getCmdPwd());
            sb.append("  name=");
            sb.append(cmd.getCmdName());
            sb.append("  bt=");
            sb.append(cmd.getBtAddress());
            sb.append("  ext=");
            sb.append(cmd.getExt());
            sb.append("\n");
        }
        sb.append("\nvalue\n");
        List<Value> values = DBManager.getInstance().getAllValue();
        for(int i=0;i<values.size();i++){
            Value value = values.get(i);
            sb.append("  bt=");
            sb.append(value.getBtAddress());
            sb.append("  key=");
            sb.append(value.getKey());
            sb.append("  value=");
            sb.append(value.getValue());
            sb.append("\n");
        }
        sb.append("\n变量\n");
        List<Var> vars = DBManager.getInstance().getAllVar();
        for(int i=0;i<vars.size();i++){
            Var var = vars.get(i);
            sb.append("  hexno=");
            sb.append(var.getHexNo());
            sb.append("  count=");
            sb.append(var.getCount());
            sb.append("  name=");
            sb.append(var.getName());
            sb.append("  bt=");
            sb.append(var.getBtAddress());
            sb.append("  type=");
            sb.append(var.getType());
            sb.append("  unit=");
            sb.append(var.getUnit());
            sb.append("  nameHexNo=");
            sb.append(var.getNameHexNo());
            sb.append("\n");
        }
        sb.append("\n字符串\n");
        List<OfflineString> strs = DBManager.getInstance().getAllStrs();
        for(int i=0;i<strs.size();i++){
            OfflineString string = strs.get(i);
            sb.append("  hexno=");
            sb.append(string.getHexNo());
            sb.append("  en=");
            sb.append(string.getStringEn());
            sb.append("  zh=");
            sb.append(string.getStringZh());
            sb.append("  bt=");
            sb.append(string.getBtAddress());
            sb.append("\n");
        }
        tv.setText(sb);
    }

    @Override
    public void reconnectSuccss() {

    }
}
