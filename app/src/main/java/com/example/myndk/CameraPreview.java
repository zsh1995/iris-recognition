package com.example.myndk;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final boolean DEBUG = true;
    private static final String TAG="WebCam";
    protected Context context;
    private SurfaceHolder holder;
    Thread mainLoop = null;
    private Bitmap bmp=null;


    private boolean cameraExists=false;
    private boolean shouldStop=false;

    private MyCallback myCallback;
    // /dev/videox (x=cameraId+cameraBase) is used.
    // In some omap devices, system uses /dev/video[0-3],
    // so users must use /dev/video[4-].
    // In such a case, try cameraId=0 and cameraBase=4
    private int cameraId=0;
    private int cameraBase=7;

    // This definition also exists in ImageProc.h.
    // Webcam must support the resolution 640x480 with YUYV format.
    static final int IMG_WIDTH=1280;
    static final int IMG_HEIGHT=720;

    // The following variables are used to draw camera images.
    private int winWidth=0;
    private int winHeight=0;
    private Rect rect;
    private int dw, dh;
    private float rate;

    // JNI functions
    public native int prepareCamera(int videoid);
    public native int prepareCameraWithBase(int videoid, int camerabase);
    public native void processCamera();
    public native void stopCamera();
    public native void pixeltobmp(Bitmap bitmap);

    static {
        System.loadLibrary("ImageProc");
    }

    public CameraPreview(Context context) {
        super(context);
        this.context = context;
        if(DEBUG) Log.d(TAG,"CameraPreview constructed");
        setFocusable(true);
        setZOrderOnTop(true);

        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSLUCENT);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if(DEBUG) Log.d(TAG,"CameraPreview constructed");
        setFocusable(true);
        setZOrderOnTop(true);
        holder = getHolder();
        holder.addCallback(this);

        holder.setFormat(PixelFormat.TRANSLUCENT);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    //拍照事件
    public void takePicture(){
        Log.d("CameraActivity","TAKING PICTURE!:STEP 1");
        Bitmap myPixels = getPicture();
        Log.d("CameraActivity","TAKING PICTURE!:STEP 2");
        myCallback.onPictureTaken(myPixels);
        Log.d("CameraActivity","TAKING PICTURE!:STEP 3");
    }
    //注册回调接口
    public void setMyCallback(MyCallback myCallback){
        this.myCallback = myCallback;
    }

    public Bitmap getPicture(){
       return bmp;
    }

    @Override
    public void run() {
        while (true && cameraExists) {
            //obtaining display area to draw a large image
            if(winWidth==0){
                winWidth=this.getWidth();
                winHeight=this.getHeight();

                if(winWidth*9/16<=winHeight){
                    dw = 0;
                    dh = (winHeight-winWidth*9/16)/2;
                    rate = ((float)winWidth)/IMG_WIDTH;
                    rect = new Rect(dw,dh,dw+winWidth-1,dh+winWidth*9/16-1);
                }else{
                    dw = (winWidth-winHeight*16/9)/2;
                    dh = 0;
                    rate = ((float)winHeight)/IMG_HEIGHT;
                    rect = new Rect(dw,dh,dw+winHeight*16/9 -1,dh+winHeight-1);
                }
            }

            // obtaining a camera image (pixel data are stored in an array in JNI).
            processCamera();
            // camera image to bmp
            pixeltobmp(bmp);

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            if (canvas != null)
            {
                // draw camera bmp on canvas
                canvas.drawBitmap(bmp,null,rect,paint);

                getHolder().unlockCanvasAndPost(canvas);
            }

            if(shouldStop){
                shouldStop = false;
                break;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(DEBUG) Log.d(TAG, "surfaceCreated");
        if(bmp==null){
            bmp = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
        }
        // /dev/videox (x=cameraId + cameraBase) is used
        int ret = prepareCameraWithBase(cameraId, cameraBase);

        if(ret!=-1) cameraExists = true;

        mainLoop = new Thread(this);
        mainLoop.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(DEBUG) Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(DEBUG) Log.d(TAG, "surfaceDestroyed");
        if(cameraExists){
            shouldStop = true;
            while(shouldStop){
                try{
                    Thread.sleep(100); // wait for thread stopping
                }catch(Exception e){}
            }
        }
        stopCamera();
    }
}

