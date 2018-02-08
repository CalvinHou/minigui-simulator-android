package com.example.houhuihua.myapplication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    /* A native method that is implemented by the
     * 'hello-jni' native library, which is packaged
     * with this application.
     */
    public native String  stringFromJNI();
    public native String  startMiniGUIMain(int w, int h);
    public native int peekMiniGUIMessage();
    public native String  updateMiniGUIEvent(int x, int y, int flags);

    public native String setAndroidGALBuffer(Bitmap bitmap);
    public native String fillAndroidGALBuffer();

    /* This is another native method declaration that is *not*
     * implemented by 'hello-jni'. This is simply to show that
     * you can declare as many native methods in your Java code
     * as you want, their implementation is searched in the
     * currently loaded native libraries only the first time
     * you call them.
     *
     * Trying to call this function will result in a
     * java.lang.UnsatisfiedLinkError exception !
     */
    public native String  unimplementedStringFromJNI();

    /* this is used to load the 'hello-jni' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.hellojni/lib/libhello-jni.so at
     * installation time by the package manager.
     */
    static {
        //System.loadLibrary("helloworld");
        System.loadLibrary("cellphone");
    }

    //private static final int RATIO = 3;
    private static float RATIO_W = 1.45f;
    private static float RATIO_H = 1.45f;

    private CheckSdcardPermission mSdcardPerssion = null;
    private MiniGUIView mMiniGUIView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initCellphoneRes();

        setContentView(R.layout.activity_main);
        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        */

        if (checkCellphoneXml() == false) {
            mSdcardPerssion = new CheckSdcardPermission(this);

            TextView tv = (TextView) findViewById(R.id.hello_textview);
            //tv.setText(stringFromJNI());
            tv.setText("Request sdcard write permission!");
        }
        else {
            mMiniGUIView = new MiniGUIView(getApplicationContext());
            setContentView(mMiniGUIView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CheckSdcardPermission.REQUEST_WRITE
                && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            initCellphoneRes();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMiniGUIView != null) {
        }
    }

    private class MiniGUIView extends SurfaceView implements SurfaceHolder.Callback {
        SurfaceHolder mHolder = getHolder();
        private Bitmap mBitmap = null;
        private Rect mDstRect = new Rect();
        private Rect mSrcRect = new Rect();
        private boolean mMiniGUIStarting = false;
        private Object mLock;
        private boolean mGALExit = true;

        public MiniGUIView(Context context) {
            super(context);
            mHolder.addCallback(this);
            mLock = new Object();
        }

        public boolean getGALStatus() {
            return mGALExit;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int flags = 0;

            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                flags |= 1;

            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                flags = 0;
            }
            else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                flags = 0;
            }

            if (mMiniGUIStarting == true)
                updateMiniGUIEvent((int)(event.getX()/RATIO_W) , (int)(event.getY()/RATIO_H) , flags);

            Log.e("MainActivity", "JavaEvent, x = " + event.getX() + " y:" + event.getY() + " flags:" + flags);

            //return super.onTouchEvent(event);
            return true;
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            initRate();
            initFrameBuffer();
            String string = setAndroidGALBuffer(mBitmap);
            Log.e("surfaceCreateed", "string:" + string);
            new Thread(new MiniGUIThread()).start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            synchronized (mLock) {
                mMiniGUIStarting = false;
            }
        }

        private void initRate() {
            if (getWidth() < getHeight()) {
                RATIO_W = getWidth() / 320;
            }
            else {
                RATIO_W = getHeight() / 320;
            }
            if (RATIO_W >= 3) {
                RATIO_W -= 0.8f;
            }
            RATIO_H = RATIO_W;
        }

        private void initFrameBuffer() {
            if (mBitmap != null) {
                if (mBitmap.getWidth() == getWidth()/RATIO_W  && mBitmap.getHeight() == getHeight()/RATIO_H )
                    return;
                mBitmap.recycle();
            }

            mSrcRect.set(0, 0, (int)(getWidth() / RATIO_W), (int)(getHeight() /RATIO_H) );
            mDstRect.set(0, 0, getWidth(), getHeight());
            mBitmap = Bitmap.createBitmap((int)(getWidth() / RATIO_W ), (int)(getHeight()/RATIO_H) , Bitmap.Config.RGB_565);

        }

        class GALUpdateThread implements Runnable{

            @Override
            public void run() {
                while (mMiniGUIStarting) {
                    mGALExit = false;
                    synchronized (mLock) {

                        setAndroidGALBuffer(mBitmap);

                        if (mBitmap != null) {
                            try {
                                Canvas canvas = mHolder.lockCanvas(null);
                                if (mMiniGUIStarting == true && MainActivity.this.isFinishing() == false
                                        )
                                    canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);
                                mHolder.unlockCanvasAndPost(canvas);
                            }
                            catch (Exception e) {
                                System.exit(0);
                                break;
                            }
                        }
/*
                        try {
                            Thread.sleep(1);
                        } catch (Exception e) {

                        }
                        */
                    }
                }

                mGALExit = true;

                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
            }
        }

        private Thread mGALThread = null;

        class MiniGUIThread implements Runnable {
            @Override
            public void run() {
                String statusStr = startMiniGUIMain(mBitmap.getWidth(), mBitmap.getHeight());
                mMiniGUIStarting = true;

                mGALThread = new Thread(new GALUpdateThread());
                mGALThread.start();

                while (mMiniGUIStarting){
                    int ret = peekMiniGUIMessage();
                    mMiniGUIStarting = false;
                }
            }
        }
    }


    //save file to data/local/tmp/

    public static void saveDataFile(String filename, byte[] outbytes){
        try {
            FileOutputStream os = new FileOutputStream(filename);
            os.write(outbytes);
            os.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String paramString)
    {
        String str;
        try
        {
            FileInputStream localFileInputStream = new FileInputStream(new File(paramString));
            byte[] arrayOfByte = new byte[localFileInputStream.available()];
            localFileInputStream.read(arrayOfByte);
            str = new String(arrayOfByte);
            return str;
        }
        catch (Exception localException)
        {
            localException.printStackTrace();
            str = null;
        }
        return str;
    }

    private byte[] readRaw(int paramInt)
    {
        String str;
        try
        {
            InputStream localInputStream = getResources().openRawResource(paramInt);
            byte[] arrayOfByte = new byte[localInputStream.available()];
            localInputStream.read(arrayOfByte);
            localInputStream.close();
            return arrayOfByte;
        }
        catch (Exception localException)
        {
        }
        return null;
    }

    String files[][] = {
            {R.raw.cellphone + "", "cellphone.xml"},
            {R.raw.helvetica + "", "Helvetica.ttf"},
            {R.raw.mgncs + "", "mgncs.cfg"},
            {R.raw.mgncs4pad + "", "mgncs4pad.cfg"},
            {R.raw.system + "", "system.db" }
    };

    private boolean checkCellphoneXml() {
        String num = Math.random() + "";
        String name = "/sdcard/etc/" + num;
        saveDataFile(name, "test".getBytes());
        File file = new File(name);
        if (file.exists() == false) {
            return false;
        }
        return true;
    }

    private boolean initCellphoneRes() {

        createDir("/sdcard/etc/");

        for (int i = 0; i < files.length; i++) {
            try {
                int resId = Integer.parseInt(files[i][0]);
                byte [] rawBytes = readRaw(resId);
                String name = "/sdcard/etc/" + files[i][1];
                if (rawBytes != null) {
                    File file = new File(name);
                    if (file.exists() == false) {
                        saveDataFile("/sdcard/etc/" + files[i][1], rawBytes);
                    }
                }
            } catch(Exception e){
                return false;
            }
        }
        return true;
    }


    public void createDir (String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            return;
        }
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }
        if (dir.mkdirs()) {
        }
    }

}
