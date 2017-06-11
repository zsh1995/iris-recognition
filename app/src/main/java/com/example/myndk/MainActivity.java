package com.example.myndk;

import java.io.File;  
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.InputStream;
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;  
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;  
import android.hardware.Camera;  
import android.hardware.Camera.PictureCallback;  
import android.os.Bundle;  
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;  
import android.view.MotionEvent;  
import android.view.Surface;  
import android.view.SurfaceHolder;  
import android.view.SurfaceHolder.Callback;  
import android.view.SurfaceView;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;  

public class MainActivity extends Activity {
	
	private TextView matres; 
	private String res;      //返回结果字符串
	private Button  Quit = null; 
	private String abs; //采集到的图片的存取路径

	static{
		System.loadLibrary("ialg");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);	
	    setContentView(R.layout.activity_main);  
	    
	    //图片保存在SD卡的FeaLib目录下
	    abs=Environment.getExternalStorageDirectory().toString()+"/FeaLib/";
	    //如果FeaLib不存在，则创建
	    isFolderExists(abs);
	    
	    //获取控件
        matres = (TextView)findViewById(R.id.eusername); //显示结果的文本框
        Quit = (Button)findViewById(R.id.quit);  //退出按钮
        Quit.setOnClickListener(quit); //监听退出按钮

	}
	
	
	 /*******************************************
	   *  //注册、认证操作
	   */	
	public void btnOnclick(View v) {  
		switch (v.getId()) {  
			case R.id.conf:   //点击认证按钮
				
				//跳转到CameraActivity,采集图像数据界面
				Intent in1 = new Intent();
	            in1.setClass(MainActivity.this, CameraActivity.class);
	            //传值到CameraActivity,传入操作标识，关键值为"operate",值为"register"，即注册操作
	            in1.putExtra( "operate", "configure"); 
	            //调转到CameraActivity，并等待CameraActivity返回结果
	            startActivityForResult(in1, 0 );


	    	    break; 
	    	    
	    	 case R.id.register:  //点击注册按钮
	    
	    		//跳转到注册界面
	            Intent in2 = new Intent();
	    		in2.setClass(MainActivity.this, RegisterActivity.class);
	    		startActivity(in2);
	    	    break; 
	    	    
	            }  	        
	    } 
	
	 /*******************************************
	   *  退出操作
	   */
	 private Button.OnClickListener  quit = new Button.OnClickListener()
	 {
	   	 public void onClick(View v)
	   	 {

	   		onDestroy();
	   		
	   	 }    
	  };

	  
	  /*******************************************
	   * 等待其他Activity的返回结果 
	   */
	 @Override  
	    protected void onActivityResult( int requestCode, int resultCode, Intent data )  
	    {  
	        switch ( resultCode ) {  
	            case RESULT_OK :  
	               
	            	//获取CameraActivity返回的结果
	                res =  data.getExtras().getString( "result" );
	                //将结果显示到文本框中
	                matres.setText(res); 
	                
	                break;  
	            default :  
	                break;  
	        }  
	          
	    }  

    /*******************************************
	  *检查目录是否存在操作，如果不存在则建立
	  * @param strFolder
	  * @return
	  */
	 boolean isFolderExists(String strFolder)
     {
         File file = new File(strFolder);
         
         if (!file.exists())
         {
             if (file.mkdir())  //创建目录
             {
                 return true;
             }
             else
                 return false;
         }
         return true;
     }
	 
	 

	 /*******************************************
	  * 退出操作函数
	  */
	 @Override

	protected void onDestroy() {

	    super.onDestroy();

	    System.exit(0);

	   }
	 
}
