package com.example.houhuihua.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by houhuihua on 2018/1/25.
 */

public class CheckSdcardPermission {
    Activity mContext;
    public static final int REQUEST_WRITE = 1;

    public CheckSdcardPermission(Activity activity) {
        mContext = activity;
        checkPermission();
    }

    private void restart(){
        Dialog alertDialog = new AlertDialog.Builder(mContext).
                setTitle("Restart").
                setMessage("Restart.....").
                setIcon(R.mipmap.ic_launcher).
                setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mContext.finish();
                    }
                }).
                create();
        alertDialog.show();
    }


    private void showAlert(){
        Dialog alertDialog = new AlertDialog.Builder(mContext).
                setTitle("Permission").
                setMessage("You should enable to write sdcard!").
                setIcon(R.mipmap.ic_launcher).
                setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
                    }
                }).
                setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mContext.finish();
                    }
                }).
                create();
        alertDialog.show();
    }


    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showAlert();
                } else {
                    ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
                }
            }
        }
    }

}
