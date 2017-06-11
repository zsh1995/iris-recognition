package com.example.myndk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


public class RegisterActivity extends Activity{
	
	private EditText name,sex,age,phone;   //各个输入框句柄
	private String tname,tsex,tage,tphone,picName;  //各个输入框中的字符串，picName为注册时提取的虹膜图像
	private SQLiteDatabase sld; //数据库操作对象
	//创建数据库语句
	private static final String CREATETABLE = "create table if not exists meminfo (tid varchar(2),name varchar(20),sex varchar(2),age varchar(4),phone varchar(20),codes varchar(1500))"; 
	private  String abs; //虹膜图片的存储路径
	private String dataBased = "/data/data/com.example.myndk/Feadb";



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		
		Intent in2 = getIntent();
		//获取各个控件ID
		findviews(); 
		
		 
	}
	
	 /** 
     * 获取控件ID
     *
     */  
	private void findviews()
	    {	     
	        name = (EditText)findViewById(R.id.ename);  
	        sex = (EditText)findViewById(R.id.esex);  
	        age = (EditText)findViewById(R.id.eage);  
	        phone = (EditText)findViewById(R.id.ephone); 
	      	    	
	    }  
	 
	 /** 
     * 按钮被点击触发的事件 
     *  
     * @param v 
	 * @throws IOException 
     */  
    public void  Onclick(View v) throws IOException {   
            try {
				 switch (v.getId()) {  
				 	case R.id.submit:   //点击提交按钮
				 
				 char[] tempdata; //存储从readFile获取的图片纯数据
				 int[] tmpinfo = new int[1000]; //存储图片预处理后的结果
				 char[] tmpCcodes = new char[1500]; //存储图片特征提取后的特征值
				 int[] best = new int[1]; //返回质量最好的图片的下标
				
				  //创建或打开数据库
				  createOrOpenDatabase();
				   
				  //获取到界面上各个输入框中填充的字符串
				  tname=name.getText().toString();	//姓名
				  tsex=sex.getText().toString();	//性别
				  tage=age.getText().toString();	//年龄
				  tphone=phone.getText().toString(); //手机号
				   
				  				  
				  String sql ="select * from meminfo"; //查询meminfo表中的所有数据，meminfo中存储着已注册人员的信息
				  Cursor cur = sld.rawQuery(sql,null); //执行查询，返回结果游标
				  int tid = cur.getCount();            //获取表中当前条目数量
				  Integer i = new Integer(tid);       //整数转化为整形对象
				  String s = i.toString();             //整型对象转化为字符串，方便数据库存储
				
				  //新建Test对象，Test对象定义了底层SO库的调用接口函数
				  Test tmp = new Test(); 
				  //获取虹膜图像数据，返回图像数据存储到tempdata中
				  tempdata = readFile(picName);
				  //对图像进行预处理，传入图像数据，高和宽，这里分辨率固定为640*480，tmpinfo存储预处理之后的结果
				  int res1 = tmp.JniPreProcess(tempdata,640,480,tmpinfo);
						Log.d("RegisterActivity","string of:"+String.valueOf(tmpinfo).substring(0,10));

						//对处理后的图像进行特征提取，返回最好的图片的下标存到best，返回特征码存到tmpCcodes
	          	  int res2 = tmp.JniFeaEx(tempdata,640,480,1,tmpinfo,best,tmpCcodes);
	          	  //将特征码转成字符串方便存储
	              String str = String.valueOf(tmpCcodes);



				char[] strChar = str.toCharArray();

						//执行数据库插入操作
				 sld.execSQL( "insert into meminfo values(?,?,?,?,?,?)",new Object[] {s,tname,tsex,tage,tphone,str});
		         sld.close();
		         Toast.makeText(RegisterActivity.this, "注册成功，欢迎您", Toast.LENGTH_LONG).show();
						int[] bst = new int[2];


						char[] tempCode = new char[1500];
						for(int cnt = 0; cnt < 1500;cnt++){
							tempCode[cnt] = tmpCcodes[cnt];
						}


		         Log.d("RegisterActivity","test:"+tmp.JniMatch(tmpCcodes, tempCode, 1, bst));
						Log.d("RegisterActivity:","test1:"+bst[0]+","+bst[1]);
				break;  
				
				case R.id.exact:  
				    
					//点击虹膜提取按钮，跳转到CameraActivity
					Intent in1 = new Intent();
					in1.setClass(RegisterActivity.this, CameraActivity.class);
					//传递"register"操作
					in1.putExtra( "operate", "register"); 
	            	startActivityForResult(in1, 0 ); 			
				    break;  
				}
			} catch (SQLException e) {
				
				e.printStackTrace();
			}  
         
    } 
    @Override  
    protected void onActivityResult( int requestCode, int resultCode, Intent data )  
    {  
        switch ( resultCode ) {  
            case RESULT_OK :  
               
            	//如果为"resgister"操作则返回选择的图片的名字
                picName =  data.getExtras().getString( "result" );               
                     
                break;  
            default :  
                break;  
        }  
          
    }  
    
    public void createOrOpenDatabase()
    {
   	 
   	 try
   
	 {
   		 //打开数据库
		 sld=SQLiteDatabase.openDatabase("/data/data/com.example.myndk/Feadb", null, SQLiteDatabase.OPEN_READWRITE|SQLiteDatabase.CREATE_IF_NECESSARY);
		// sld.execSQL("drop table meminfo");
		 //创建meminfo表
		 sld.execSQL(CREATETABLE);
		
		
	 }
	 catch(Exception e)
	 {
		 Toast.makeText(this, "数据库错误：" +e.toString(),Toast.LENGTH_SHORT).show();;
	 }
    }
   
    
    public char[] readFile(String path) {

		try {

	       abs=Environment.getExternalStorageDirectory().toString()+"/FeaLib/";
		   FileInputStream is = new FileInputStream(abs+path);
			Log.d("RegisterActivity","paht:"+path);
		
			// 读取图片的18~21的宽度
			is.skip(18);
			byte[] b = new byte[4];
			is.read(b);
			// 读取图片的高度22~25
			byte[] b2 = new byte[4];
			is.read(b2);


			// 得到图片的高度和宽度
			int width = byte2Int(b);
			int heigth = byte2Int(b2);
			
			 
			// 使用数组保存得图片的高度和宽度
			int[][] date = new int[heigth][width];
			char bmpdatas[] = new char[heigth*width];
			
		    
			int skipnum = 0;
			if (width * 3 / 4 != 0) {
				skipnum = 4 - width * 3 % 4;
			}
			// 读取位图中的数据，位图中数据时从54位开始的，在读取数据前要丢掉前面的数据
			is.skip(28+1024);
			for (int i = 0; i < date.length; i++) {
				for (int j = 0; j < date[i].length; j++) {
					// bmp的图片在window里面世3个byte为一个像素
					int blue = is.read();

					bmpdatas[i*width+j] =(char) blue;
				
				}
				
			}
			
			return bmpdatas;
			
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;

	}
   
	
	static {
		//加载ialg库
		System.loadLibrary("ialg");
		
	}
	
      
      //查询数据库函数
	private String query(char[] codes) {

	String sql = "select * from meminfo";
		SQLiteDatabase mysld = SQLiteDatabase.openDatabase(dataBased, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);

	Cursor cur = mysld.rawQuery(sql, null);
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

		mysld.close();
		return back;

}
	
	public int byte2Int(byte[] by) {
		int t1 = by[3] & 0xff;
		int t2 = by[2] & 0xff;
		int t3 = by[1] & 0xff;
		int t4 = by[0] & 0xff;
		int num = t1 << 24 | t2 << 16 | t3 << 8 | t4;
		return num;

	}
	
	public static byte[] intToByteArray(int i) {   
        byte[] result = new byte[4];   
        //由高位到低位
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF); 
        result[3] = (byte)(i & 0xFF);
        return result;
      }

      /**
       * byte[]转int
       * @param bytes
       * @return
       */
	
      /**
       * byte[]转int
       * @param bytes
       * @return
       */
      public static int byteArrayToInt(byte[] bytes) {
             int value= 0;
             //由高位到低位
             for (int i = 0; i < 4; i++) {
                 int shift= (4 - 1 - i) * 8;
                 value +=(bytes[i] & 0x000000FF) << shift;//往高位游
             }
             return value;
       }
}
