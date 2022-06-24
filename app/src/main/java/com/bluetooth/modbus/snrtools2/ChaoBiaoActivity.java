package com.bluetooth.modbus.snrtools2;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.widget.ListView;

import com.bluetooth.modbus.snrtools2.adapter.ChaoBiaoAdapter;
import com.bluetooth.modbus.snrtools2.bean.ChaoBiao;
import com.bluetooth.modbus.snrtools2.common.CRC16;
import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;
import com.bluetooth.modbus.snrtools2.uitls.AppUtil;
import com.bluetooth.modbus.snrtools2.uitls.NumberBytes;

import java.util.ArrayList;
import java.util.List;

public class ChaoBiaoActivity extends BaseActivity{

    private ListView mListview;
    private List<ChaoBiao> chaoBiaos = new ArrayList<>();
    private ChaoBiaoAdapter adapter = null;
    private BluetoothLeScanner mBleScanner;
    private ScanCallback scanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chaobiao_activity);
        setTitleContent(getString(R.string.chaobiao));
        hideRightView(R.id.btnRight1);
        hideRightView(R.id.view2);
        hideRightView(R.id.rlMenu);
        mListview = (ListView) findViewById(R.id.listView1);
        adapter = new ChaoBiaoAdapter(this,chaoBiaos);
        mListview.setAdapter(adapter);

        mBleScanner = AppStaticVar.mBtAdapter.getBluetoothLeScanner();
        ScanSettings scanSettings = new ScanSettings.Builder()
                //设置功耗平衡模式
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                try {
                    String str = CRC16.byteToHex(result.getScanRecord().getBytes());
                    System.out.println("======================"+str);
                    if(!str.toUpperCase().startsWith("020106") || !str.toUpperCase().contains("534E5254")){
                        return;
                    }
                    String bianHaoHex = str.substring(18,26);
                    // dt_dword 32位无符号整数
                    //0-4294967295
                    long biaoHaoL = Long.parseLong(bianHaoHex.substring(6, 8) + bianHaoHex.substring(4, 6) + bianHaoHex.substring(2, 4) + bianHaoHex.substring(0, 2), 16);
                    String bianHao = "20"+String.valueOf(biaoHaoL);

                    String leiJiLiuLiangHex = str.substring(26,34);
                    // dt_dword 32位无符号整数
                    //0-4294967295
                    long leiJiLiuLiangL = Long.parseLong(leiJiLiuLiangHex.substring(6, 8) + leiJiLiuLiangHex.substring(4, 6) + leiJiLiuLiangHex.substring(2, 4) + leiJiLiuLiangHex.substring(0, 2), 16);
                    String leiJiLiuLiang = String.valueOf(leiJiLiuLiangL);

                    String shunShiLiuLiangHex = str.substring(34,42);
                    // dt_float 32位浮点数
                    long i = Long.parseLong(shunShiLiuLiangHex.substring(6, 8) + shunShiLiuLiangHex.substring(4, 6) + shunShiLiuLiangHex.substring(2, 4) + shunShiLiuLiangHex.substring(0, 2), 16);
                    float v = Float.intBitsToFloat((int) (i <= 2147483647L ? i : (i - 4294967296L)));
                    String shunShiLiuLiang = NumberBytes.subZeroAndDot(AppUtil.dealNoCountResult(String.valueOf(v), 5));

                    ChaoBiao chaoBiao = null;
                    for(ChaoBiao cb:chaoBiaos){
                        if(bianHao.equals(cb.bianhao)){
                            chaoBiao = cb;
                            break;
                        }
                    }
                    if(chaoBiao == null){
                        chaoBiao = new ChaoBiao();
                        chaoBiaos.add(chaoBiao);
                    }
                    chaoBiao.bianhao = bianHao;
                    chaoBiao.shunshiliuliang = shunShiLiuLiang;
                    chaoBiao.leijiliuliang = leiJiLiuLiang;
                    chaoBiao.shujubao++;
                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mBleScanner.startScan(null,scanSettings,scanCallback);
    }

    @Override
    public void reconnectSuccss() {

    }

    @Override
    protected void onDestroy() {
        if(mBleScanner != null){
            mBleScanner.stopScan(scanCallback);
        }
        super.onDestroy();
    }
}
