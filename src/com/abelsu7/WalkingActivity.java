package com.abelsu7;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class WalkingActivity extends Activity implements OnClickListener{

	WalkingView wv;       //WalkingView对象引用

	public static final String DB_NAME = "step.db";  //数据库名称

	MySQLiteHelper mh;      //声明数据库辅助类
	SQLiteDatabase db;      //数据库对象
	Button btnToBackstage;  //转入后台按钮
	Button btnStopService;  //停止服务按钮
	Button btnDeleteData;   //删除数据按钮

	StepUpdateReceiver receiver;

	//定义一个继承自BroadcastReceiver的内部类StepUpdateReceiver来接收传感器的信息
	public class StepUpdateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
//			Toast.makeText(context, "Receive!", Toast.LENGTH_SHORT).show();
			Bundle bundle = intent.getExtras();  //获得Bundle 
			int steps = bundle.getInt("step");   //读取步数
			wv.stepsToday = steps;
			wv.isMoving = true;
			wv.postInvalidate();               //刷新WalkingView

		}
	}

	//重写onCreate方法，在Activity被创建时调用
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);  //设置当前屏幕
		wv = (WalkingView)findViewById(R.id.walkingView);
		wv.setBackgroundColor(Color.rgb(102,153,255));
		btnToBackstage = (Button)findViewById(R.id.btnDispose);
		btnToBackstage.setOnClickListener(this);
		btnStopService = (Button)findViewById(R.id.btnStop);
		btnStopService.setOnClickListener(this);
		btnDeleteData = (Button)findViewById(R.id.btnDeleteData);
		btnDeleteData.setOnClickListener(this);

		//注册Receiver	
		receiver = new StepUpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.abelsu7.WalkingActivity");
		registerReceiver(receiver,filter);
		//启动注册了传感器监听的Service
		Intent i = new Intent(this,WalkingService.class);
		startService(i);
		mh = new MySQLiteHelper(this, DB_NAME, null, 1);
		requireData();
	}

	//重写onDestroy方法
	@Override
	protected void onDestroy(){
		unregisterReceiver(receiver);             //注销Receiver
		super.onDestroy();
	}

	//方法：向Service请求今日走过的步数
	private void requireData() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();             //创建Intent
		intent.setAction("com.abelsu7.WalkingService");
		intent.putExtra("cmd", WalkingService.CMD_UPDATE);
		sendBroadcast(intent);                    //发出消息广播
	}

    //重写OnClickListener接口的onClick方法
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view == btnStopService){
			//停止后台服务
			Toast.makeText(this, "停止计步", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setAction("com.abelsu7.WalkingService");
			intent.putExtra("cmd", WalkingService.CMD_STOP);
			sendBroadcast(intent);
		}
		else if(view == btnToBackstage){
			Toast.makeText(this, "后台运行中", Toast.LENGTH_SHORT).show();
			finish();//转到后台
		}
		else if(view == btnDeleteData){
			//删除历史数据
			Toast.makeText(this, "数据已清空", Toast.LENGTH_SHORT).show();
			SQLiteDatabase db = (SQLiteDatabase)
					openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
			db.delete(MySQLiteHelper.TABLE_NAME,null,null);
			db.close();
			wv.stepsInWeek = wv.getSQLData("7");
			wv.postInvalidate();
			}
	}
}
