package com.example.myndk;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Android手指拍照
 *
 * @author wwj
 * @date 2013/4/29
 */
public class CameraActivity extends Activity {
    private View layout;
    private Camera camera;
    private Camera.Parameters parameters = null;
    private String op;
    private SQLiteDatabase db;
    private String dataBased = "/data/data/com.example.myndk/Feadb";

    private SurfaceView surfaceView = null;


    Bundle bundle = null; // 声明一个Bundle对象，用来存储数据

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 显示界面
        setContentView(R.layout.camera);
        Intent in1 = getIntent();

        surfaceView = (SurfaceView) this
                .findViewById(R.id.CamerasurfaceView);
        surfaceView.getHolder()
                .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceCallback());

        surfaceView.getHolder().setFixedSize(640, 480); //设置Surface分辨率
        surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
        Log.d("CameraActivity","on Create");
    }

    /**
     * 按钮被点击触发的事件
     *
     * @param v
     */
    public void btnOnclick(View v) {
        if (true) {
            switch (v.getId()) {
                case R.id.ensure:

                    //拍照的功能还没有完善

                    Log.d("CameraActivity","TAKING PICTURE!:STEP 0");
                    camera.takePicture(null, null, new MyPictureCallback());

                    break;
                case R.id.choose:

                    //点击选择图片操作
                    Intent in2 = new Intent();
                    in2.setClass(CameraActivity.this, FileActivity.class);
                    //获取前一个Activity传过来的值
                    op = getIntent().getExtras().getString("operate");
                    //将上个Activity传来的值传入FileActivity
                    in2.putExtra("operate", op);
                    startActivityForResult(in2, 0);

                    break;
            }
        }
    }

    private void dealPicture(char[] bmpdata, String picname) {
        String nm = null, op = null;
        char[] ttCcodes = new char[1500];
        int[] info = new int[1000];
        int[] bestIndexs = new int[2];

        //获取点击的图片名字
        //打开文件，返回图片纯数据


        //虹膜图像处理
        Test tt = new Test();
        long sysDate = System.currentTimeMillis();
        int res1 = tt.JniPreProcess(bmpdata, 640, 480, info);
        int res2 = tt.JniFeaEx(bmpdata, 640, 480, 1, info, bestIndexs, ttCcodes);

        Intent in2 = new Intent();
        //获取前一个Activity传递过来的值
        op = getIntent().getExtras().getString("operate");
        //如果为注册，则将所选择的文件的名字，传递给上一个Activity
        if (op.equals("register")) {
            in2.putExtra("result", picname);
            Toast.makeText(this, "图片添加成功，返回", Toast.LENGTH_LONG).show();
            Intent in1 = new Intent();
            in1.putExtra("result", picname);
            setResult(RESULT_OK, in1);
        }
        //如果为认证，则进行匹配处理，将匹配结果返回
        else if (op.equals("configure")) {
            //遍历数据库进行匹配，返回结果nm
            nm = query(ttCcodes);
            //测试执行时间
            long sysDate1 = System.currentTimeMillis();
            long time = sysDate1 - sysDate;
            //  Log.i("Mytag","time"+time);
            in2.putExtra("result", nm + "   " + time + " ms");
            setResult(RESULT_OK, in2);
            Log.d("CameraActivity",nm + "   " + time + " ms");
            Toast.makeText(this, "图片查找成功，返回", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "操作错误，请重新输入", Toast.LENGTH_LONG).show();
        }
        finish();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                //取出FileActivity传回来的值
                String res = data.getExtras().getString("result");
                //将该值传回给上一个Activity
                Intent in1 = new Intent();
                in1.putExtra("result", res);
                setResult(RESULT_OK, in1);
                finish();
                break;
            default:
                break;
        }

    }

    /**
     * 图片被点击触发的时间
     *
     * @param
     */


    private final class MyPictureCallback implements PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = null;
            if(null == data) return;
            try {
                bundle = new Bundle();
                bundle.putByteArray("bytes", data); //将图片字节数据保存在bundle当中，实现数据交换
                Log.d("CameraActivity","bytes:"+data.length);
                int length = data.length;
                bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Log.d("CameraActivity",""+width+","+height);
                camera.stopPreview();
                String path = saveToSDCard(bitmap); // 保存图片到sd卡中
                Toast.makeText(getApplicationContext(), "成功",
                        Toast.LENGTH_SHORT).show();
                ByteBuffer buf = ByteBuffer.allocate(bitmap.getByteCount());


                bitmap.copyPixelsToBuffer(buf);

                char[] charArray = getChars(buf.array());
                dealPicture(charArray, path);
                camera.startPreview(); // 拍完照后，重新开始预览

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private final class MyPictureCallbackxx implements MyCallback {

        @Override
        public void onPictureTaken(Bitmap bmp) {
            Bitmap bitmap = bmp;
            try {
                 int height = bitmap.getHeight();
                int width = bitmap.getWidth();

                ByteBuffer buf = ByteBuffer.allocate(bitmap.getByteCount());

                bitmap.copyPixelsToBuffer(buf);

                char[] charArray = getChars(buf.array());

                String path = saveToSDCard(bitmap); // 保存图片到sd卡中
                Toast.makeText(getApplicationContext(), "成功",
                        Toast.LENGTH_SHORT).show();
                dealPicture(charArray, path);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }

        return inSampleSize;
    }


    /**
     * 将拍下来的照片存放在SD卡中
     *
     * @param
     * @throws IOException
     */
    public static String saveToSDCard(Bitmap data) throws IOException {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
        SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                "yyyy_MM_dd_HH_mm_ss");
        String currentTime = timeStampFormat.format(new Date());
        String filename = currentTime +".bmp";
        File fileFolder = new File(Environment.getExternalStorageDirectory()
                + "/FeaLib/");
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
            fileFolder.mkdir();
        }

        File bmpFile = new File(fileFolder, filename);

        new FormateConvert().convertToBmp(data,bmpFile);

        String path = Environment.getExternalStorageDirectory() + "/FeaLib/" + filename;

        return filename;

    }


    /**
     * 点击手机屏幕是，显示两个按钮
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //     layout.setVisibility(ViewGroup.VISIBLE); // 设置视图可见
                break;
        }
        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA: // 按下拍照按钮
                if (event.getRepeatCount() == 0) {
                    // 拍照
                    //注：调用takePicture()方法进行拍照是传入了一个PictureCallback对象——当程序获取了拍照所得的图片数据之后
                    //，PictureCallback对象将会被回调，该对象可以负责对相片进行保存或传入网络
                    camera.takePicture(null, null, new MyPictureCallback());
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    private final class SurfaceCallback implements Callback {

        // 拍照状态变化时调用该方法
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            parameters = camera.getParameters(); // 获取各项参数
            parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
            parameters.setPreviewSize(width, height); // 设置预览大小
            parameters.setPreviewFrameRate(25);  //设置每秒显示4帧
            parameters.setPictureSize(width, height); // 设置保存的图片尺寸
            parameters.setJpegQuality(80); // 设置照片质量
            camera.setParameters(parameters);
        }

        // 开始拍照时调用该方法
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open(); // 打开摄像头
                camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
                camera.setDisplayOrientation(getPreviewDegree(CameraActivity.this));
                camera.startPreview(); // 开始预览
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 停止拍照时调用该方法
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.release(); // 释放照相机
                camera = null;
            }
        }
    }




    private String query(char[] codes) {

        String sql = "select * from meminfo";
        db = SQLiteDatabase.openDatabase(dataBased, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);

        Cursor cur = db.rawQuery(sql, null);
        char[] strChar = new char[1500];
        int[] bst = new int[2];
        int res = 999;
        //back存储匹配的结果，默认为nomatch
        String back = "nomatch";
        Test tmp = new Test();

        //遍历数据库，若发现有匹配的条目则跳出循环，返回当前用户的姓名
        labe:
        while (cur.moveToNext()) {
            for (int i = 0; i < cur.getCount(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                cur.moveToPosition(i);
                String a1 = cur.getString(1);

                String a2 = cur.getString(2);

                String a3 = cur.getString(3);

                String a4 = cur.getString(4);

                String a5 = cur.getString(5);
                strChar = a5.toCharArray();
                res = tmp.JniMatch(codes, strChar, 1, bst);

                if (res == 0) {
                    back = a1;
                    break labe;
                }
            }
        }

        db.close();
        return back;

    }
}