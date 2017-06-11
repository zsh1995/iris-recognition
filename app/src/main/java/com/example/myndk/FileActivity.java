package com.example.myndk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class FileActivity extends Activity {
	private ListView listView;
	private List<String> list;
	private TextView user;
	private View layout;
	private Camera camera;
	private Camera.Parameters parameters = null;
	private  SQLiteDatabase db;
	private String abs;

	//	private static final String CREATETABLE = "create table if not exists meminfo (tid varchar(2),name varchar(20),sex varchar(2),age varchar(4),phone varchar(20),codes varchar(1500))";
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// 显示界面
		Intent in2 = getIntent();

		//获取文件列表
		FileViewer fileview;
		abs=Environment.getExternalStorageDirectory().toString()+"/FeaLib/";


		//getListFiles获取文件列表，abs为路径名，bmp为图片格式，boolean为是否访问下级目录
		list = FileViewer.getListFiles(abs,"bmp",false);
		listView = new ListView(this);
		setContentView(listView);
		ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<String>
				(this,android.R.layout.simple_list_item_1,list);
		listView.setAdapter(myArrayAdapter);
		listView.setOnItemClickListener(new OnItemClickListener(){

			//Item点击事件
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

				Toast.makeText(FileActivity.this, "正在处理图片，请稍后", Toast.LENGTH_LONG).show();
				String picname = null,op = null,nm = null;
				char[] ttCcodes = new char[1500];
				char[] bmpdata;
				int[] info = new int[1000];
				int[] bestIndexs = new int[2];

				//获取点击的图片名字
				picname = list.get(arg2);
				//打开文件，返回图片纯数据
				bmpdata = readFile(picname);


				//虹膜图像处理
				Test tt = new Test();
				long sysDate=System.currentTimeMillis();
				int res1 = tt.JniPreProcess(bmpdata,640,480,info);
				int res2 = tt.JniFeaEx(bmpdata,640,480,1,info,bestIndexs,ttCcodes);





				Intent in2 = new Intent();
				//获取前一个Activity传递过来的值
				op = getIntent().getExtras().getString("operate" );
				//如果为注册，则将所选择的文件的名字，传递给上一个Activity
				if(op.equals("register"))
				{
					Log.d("FileActivity","register");
					in2.putExtra( "result", picname);
					Toast.makeText(FileActivity.this, "图片添加成功，返回", Toast.LENGTH_LONG).show();
				}
				//如果为认证，则进行匹配处理，将匹配结果返回
				else if(op.equals("configure"))
				{
					Log.d("FileActivity","configure");
					//遍历数据库进行匹配，返回结果nm
					nm = query(ttCcodes);
					//测试执行时间
					long sysDate1=System.currentTimeMillis();
					long time = sysDate1-sysDate;
					//  Log.i("Mytag","time"+time);
					in2.putExtra( "result", nm+"   "+time+" ms");
					Toast.makeText(FileActivity.this, "图片添加成功，返回", Toast.LENGTH_LONG).show();
				}else
				{
					Toast.makeText(FileActivity.this, "操作错误，请重新输入", Toast.LENGTH_LONG).show();
				}
				setResult( RESULT_OK, in2 );
				finish();

			}

		});

	}
	//读取图片函数
	public char[] readFile(String path) {
		try {
			/**
			 * Environment.getExternalStorageState()获取路径是否成功
			 * 回来要添加错误处理提醒
			 */

			FileInputStream is = new FileInputStream(abs+path);

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

			char bmpdatas[] = new char[heigth*width];

			//只能处理现在这种图片的格式，以后要添加读取图片格式的代码，并依据格式进行跳过
			int skipnum = 0;
			if (width * 3 / 4 != 0) {
				skipnum = 4 - width * 3 % 4;
			}
			// 读取位图中的数据，位图中数据时从54位开始的，在读取数据前要丢掉前面的数据
			is.skip(28+1024);
			byte []dataBitmap = new byte[heigth*width];

			is.read(dataBitmap);

			for(int cnt = 0 ; cnt < dataBitmap.length;cnt++){
				bmpdatas[cnt] = (char)dataBitmap[cnt];
			}

			return bmpdatas;

		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;

	}


	static {
		System.loadLibrary("ialg");

	}

	private String query(char[] codes) {

		String sql ="select * from meminfo";
		db=SQLiteDatabase.openDatabase("/data/data/com.example.myndk/Feadb", null, SQLiteDatabase.OPEN_READWRITE|SQLiteDatabase.CREATE_IF_NECESSARY);

		Cursor cur = db.rawQuery(sql,null);
		char[] strChar = new char[1500];
		int[] bst = new int[2];
		int res = 999;
		//back存储匹配的结果，默认为nomatch
		String back = "nomatch";
		Test tmp = new Test();
		int cnt = cur.getCount();
		Log.d("CameraActivity","sql"+"!length");
		//遍历数据库，若发现有匹配的条目则跳出循环，返回当前用户的姓名
		labe: while (cur.moveToNext())
		{
			Log.d("FileActivity","sql"+":length="+cur.getCount());
			for(int i=0;i<cur.getCount();i++)
			{
				HashMap<String,Object> map = new HashMap<String,Object>();
				cur.moveToPosition(i);
				String a1 = cur.getString(1);
				String a2 = cur.getString(2);

				String a3 = cur.getString(3);

				Log.d("FileActivity","sql:"+a1+","+a2+","+a3);

				String a4 = cur.getString(4);

				String a5 = cur.getString(5);

				strChar = a5.toCharArray();

				res = tmp.JniMatch(codes,strChar,1,bst);
				Log.d("FileActivity","sql:res"+res);


				if(res == 0)
				{
					back = a1;
					break labe;
				}
			}
		}

		db.close();
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
