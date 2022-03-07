package com.bluetooth.modbus.snrtools2.uitls;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.bluetooth.modbus.snrtools2.R;

public class PermissionUtil {
    public final static int REQUEST_PERMISSION_SETTING = 0x11;

    public static void judgeCallPermission(final Activity act, final String phone)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phone));
        act.startActivity(intent);
    }


    public static boolean judgeAudioPermission(final Activity act)
    {
        if (ContextCompat.checkSelfPermission(act,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {

            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(act, act.getString(R.string.record_permission),Toast.LENGTH_SHORT).show();
                }
            });
            ActivityCompat.requestPermissions(act, new String[]
                    {
                            Manifest.permission.RECORD_AUDIO
                    }, 0x22);
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean judgeCameraPermission(final Activity act)
    {
        if (ContextCompat.checkSelfPermission(act,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {

            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(act, act.getString(R.string.record_permission),Toast.LENGTH_SHORT).show();
                }
            });
            ActivityCompat.requestPermissions(act, new String[]
                    {
                            Manifest.permission.CAMERA
                    }, 0x33);
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean judgeStoragePermission(final Activity act)
    {
        if (ContextCompat.checkSelfPermission(act,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    CustomerToast.infoCustomerToast(act, act.getString(R.string.ZKGJ028652));
                }
            });
            ActivityCompat.requestPermissions(act, new String[]
                    {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 0x44);
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean judgeStorageCameraPermission(final Activity act)
    {
        if (ContextCompat.checkSelfPermission(act,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(act,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    CustomerToast.infoCustomerToast(act, act.getString(R.string.ZKGJ028653));
                }
            });
            ActivityCompat.requestPermissions(act, new String[]
                    {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                    }, 0x55);
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean judgeLocPermission(final Activity act)
    {
        if (ContextCompat.checkSelfPermission(act,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {Toast.makeText(act, act.getString(R.string.loc_permission),Toast.LENGTH_SHORT).show();
                }
            });
            ActivityCompat.requestPermissions(act, new String[]
                    {
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 0x66);
            return false;
        }
        else
        {
            return true;
        }
    }

    // 以下代码可以跳转到应用详情，可以通过应用详情跳转到权限界面
    public static void getAppDetailSettingIntent(Activity act)
    {
        Intent localIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        localIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        localIntent.setData(Uri.fromParts("package", act.getPackageName(), null));
        act.startActivityForResult(localIntent, REQUEST_PERMISSION_SETTING);
    }



}
