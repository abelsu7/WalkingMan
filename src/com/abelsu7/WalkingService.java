package com.abelsu7;

import java.util.Calendar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class WalkingService extends Service {

	WalkingView wv;
	SensorManager mySensorManager;
    //SensorManagerSimulator mySensormanager;
    WalkingListener wl;
	int steps = 0;
	boolean isActivityOn = false;
	boolean isServiceOn = false;
	NotificationManager nm;
	long timeInterval = 24*60*60*1000;//Handler延迟发送消息的时延
	final static int CMD_STOP = 0;
	final static int CMD_UPDATE = 1;
	CommandReceiver receiver;//声明BroadcastReceiver
	
	Handler myHandler = new Handler(){//定时上传数据
		public void handlerMessage(Message msg){
			uploadData();
			super.handleMessage(msg);
		}
	};
	
	//重写onCreate方法
	@Override
	public void onCreate(){
		super.onCreate();
		//创建监听器类
		wl = new WalkingListener(this);
		//初始化传感器
		mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		//mySensorManager = SenserManagerSimulator.getSystemService(this,SENSOR_SERVICE);
        //mySensorManager.connectSimulator();
		
		//注册监听器
		mySensorManager.registerListener(wl, 
				SensorManager.SENSOR_ACCELEROMETER,SensorManager.SENSOR_DELAY_UI);
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		Calendar c = Calendar.getInstance();
		long militime = c.getTimeInMillis();
		//将Calendar设置为第二天0时
		c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH)+1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		long nextDay = c.getTimeInMillis();
		timeInterval = nextDay - militime;
	}
	
	//重写onStart方法
	@Override
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
		isServiceOn = true;
		showNotification();//添加Notification
		receiver = new CommandReceiver();
		IntentFilter filter1 = new IntentFilter();
		filter1.addAction("com.abelsu7.WalkingService");
		registerReceiver(receiver,filter1);
		//设定Message并延迟到本日结束发送
		if(isServiceOn){
			Message msg = myHandler.obtainMessage();
			myHandler.sendMessageDelayed(msg, timeInterval);
		}
	}
	
	//重写onDestroy方法
	@Override
	public void onDestroy(){
		mySensorManager.unregisterListener(wl);
		wl = null;
		mySensorManager = null;
		nm.cancel(0);
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	//重写onBind方法
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
    
	//方法：显示Notification
    private void showNotification(){
    	Intent intent = new Intent(this,WalkingActivity.class);
    	PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
//    	Notification myNotification = new Notification(R.drawable.back_cell, "This is the test", System.currentTimeMillis());
    	Notification myNotification = new Notification.Builder(this)
				.setTicker(getResources().getString(R.string.title))
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getResources().getString(R.string.content_title))
				.setContentText(getResources().getString(R.string.content_text))
				.setContentIntent(pi)
				.setAutoCancel(true)
				.build();
		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(0,myNotification);
    }
    
    //方法：向数据库中插入今日走过的步数
    public void uploadData(){
    	MySQLiteHelper mh = new MySQLiteHelper
    			(this, WalkingActivity.DB_NAME, null, 1);
    	SQLiteDatabase db = mh.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(MySQLiteHelper.STEP, this.steps);
    	db.insert(MySQLiteHelper.TABLE_NAME, MySQLiteHelper.ID, values);
    	Cursor c = db.query
    			(MySQLiteHelper.TABLE_NAME, null, null, null, null, null, null);
    	c.close();
    	db.close();       //关闭数据库
    	mh.close();
    	if(isServiceOn){//设置24小时后再发同样的消息
    		Message msg = myHandler.obtainMessage();
    		myHandler.sendMessageDelayed(msg, 24*60*60*1000); 		
    	}
    }
   
    //开发继承自BroadcastReceiver的子类接收广播消息
    class CommandReceiver extends BroadcastReceiver{
    	@Override
    	public void onReceive(Context context, Intent intent){
    		int cmd = intent.getIntExtra("cmd", -1);
    		switch(cmd){
    		case WalkingService.CMD_STOP://停止服务
    			stopSelf();
    			break;
    		case WalkingService.CMD_UPDATE://传数据
    			isActivityOn = true;
    			Intent i = new Intent();
    			i.setAction("com.abelsu7.WalkingActivity");
    			i.putExtra("step", steps);
    			sendBroadcast(i);
    			break;
    		}
    	}
    }
}
