package com.abelsu7;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class WalkingView extends View {
	
	ArrayList<String> stepsInWeek = null;//保存历史数据
	int stepsToday = 0;                 //记录今天走的步数
	int gapY = 8;                       //屏幕最上面留出的空隙
	int distY = 10;                     //每一条的间距
	int cellHeight = 64;                //每一条的高度
	float STEP_MAX = 1000.0f;           //每天最大的步数
	int maxStepWidth = 280;             //最大步数在屏幕中的宽度
	Bitmap [] sprite;                   //运动小人的图片数组
	Bitmap [] digit;                    //数字图片数组
	Bitmap back_cell;                   //颜色渐变条
	boolean isMoving = false;
	int frameIndex;                     //记录运动小人的帧索引
	MySQLiteHelper mh;                  //操作数据库的辅助类
	SQLiteDatabase db;                  //数据库操作对象
    //构造函数
	public WalkingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		sprite = new Bitmap[12];
		digit = new Bitmap[10];
		//初始化图片
		Resources res = getResources();
		sprite[0] = BitmapFactory.decodeResource(res,R.drawable.act_0);
		sprite[1] = BitmapFactory.decodeResource(res,R.drawable.act_1);
		sprite[2] = BitmapFactory.decodeResource(res,R.drawable.act_2);
		sprite[3] = BitmapFactory.decodeResource(res,R.drawable.act_3);
		sprite[4] = BitmapFactory.decodeResource(res,R.drawable.act_4);
		sprite[5] = BitmapFactory.decodeResource(res,R.drawable.act_5);
		sprite[6] = BitmapFactory.decodeResource(res,R.drawable.act_6);
		sprite[7] = BitmapFactory.decodeResource(res,R.drawable.act_7);
		sprite[8] = BitmapFactory.decodeResource(res,R.drawable.act_8);
		sprite[9] = BitmapFactory.decodeResource(res,R.drawable.act_9);
		sprite[10] = BitmapFactory.decodeResource(res,R.drawable.act_10);
		sprite[11] = BitmapFactory.decodeResource(res,R.drawable.act_11);

        digit[0] = BitmapFactory.decodeResource(res, R.drawable.digit_0); 
		digit[1] = BitmapFactory.decodeResource(res, R.drawable.digit_1); 
		digit[2] = BitmapFactory.decodeResource(res, R.drawable.digit_2); 
		digit[3] = BitmapFactory.decodeResource(res, R.drawable.digit_3); 
		digit[4] = BitmapFactory.decodeResource(res, R.drawable.digit_4);
		digit[5] = BitmapFactory.decodeResource(res, R.drawable.digit_5);
		digit[6] = BitmapFactory.decodeResource(res, R.drawable.digit_6);
		digit[7] = BitmapFactory.decodeResource(res, R.drawable.digit_7);
		digit[8] = BitmapFactory.decodeResource(res, R.drawable.digit_8);
		digit[9] = BitmapFactory.decodeResource(res, R.drawable.digit_9);
		
        back_cell = BitmapFactory.decodeResource(res, R.drawable.back_cell);
		//获取数据库中7天内的数据
		mh = new MySQLiteHelper(context, WalkingActivity.DB_NAME, null, 1);
		stepsInWeek = getSQLData("7");
	}

	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		//drawPrevious(canvas);//画以前走的步数
		drawToday(canvas);//画今天走过的步数		
	}
    //画今天走过的步数
	private void drawToday(Canvas canvas){

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getDisplay().getMetrics(displayMetrics);
		int widthOfPhone = displayMetrics.widthPixels;

		Paint paint = new Paint();
		paint.setColor(Color.RED);
		float strokewidth = paint.getStrokeWidth();
		Style s = paint.getStyle();
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(10.0f);
		//canvas.drawLine(0, 500, 768, 500, paint);
		paint.setStyle(s);
		paint.setStrokeWidth(strokewidth);//恢复画笔
		//把当前步数换算 为在屏幕上绘制的条宽度
		int width = (int)(stepsToday/STEP_MAX*280);
//		canvas.drawBitmap(back_cell, 0, 720, paint);
		canvas.drawRect(0, 720*widthOfPhone/720, 720*widthOfPhone/720, (720+cellHeight)*widthOfPhone/720, paint);
		paint.setColor(Color.rgb(255,204,0));
		canvas.drawRect(width*widthOfPhone/720, 720*widthOfPhone/720, 720*widthOfPhone/720, (720+cellHeight)*widthOfPhone/720, paint);
		//画出遮罩层
		if(isMoving){
			//如果在运动，就切换帧序列
			canvas.drawBitmap(sprite[(++frameIndex)%11], width*widthOfPhone/720, 680*widthOfPhone/720, paint);
			isMoving = false;
		}
		else{
			//如果没在走步，就绘制静止的那张图片
			canvas.drawBitmap(sprite[3], width*widthOfPhone/720, 680*widthOfPhone/720, paint);
		}
		drawDigit(canvas,width*widthOfPhone/720);//绘制数字
	}
	//画之前走过的步数
	private void drawPrevious(Canvas canvas){
		Paint paint = new Paint();
		for(int i=0;i<stepsInWeek.size();i++){
			String os = stepsInWeek.get(i);
			int s = Integer.valueOf(os).intValue();
			int width = (int)(s/STEP_MAX * maxStepWidth);//求出指定的步数在统计条中占的宽度
			int tempY = (cellHeight+distY)*i;
			canvas.drawBitmap(back_cell, 0, (cellHeight+distY)*i, paint);//画出渐变条
			paint.setColor(Color.WHITE);
			canvas.drawRect(width, tempY, 720, tempY+cellHeight, paint);
			paint.setTextAlign(Align.LEFT);
			paint.setColor(Color.CYAN);
			paint.setAntiAlias(true);
			canvas.drawText("走了"+stepsInWeek.get(i)+"步", width, tempY+cellHeight/2, paint);
		}
	}
	//将数字通过数字图片绘制到屏幕上
	public void drawDigit(Canvas canvas, int width){

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getDisplay().getMetrics(displayMetrics);
		int widthOfPhone = displayMetrics.widthPixels;

		String sStep = "" + stepsToday;
		int l =sStep.length();
		for(int i=0;i<l;i++){
			int index = sStep.charAt(i) - '0';
			Log.d("density","X="+widthOfPhone);
			canvas.drawBitmap(digit[index], (60+180*i)*widthOfPhone/720, 300*widthOfPhone/720, null);//绘制数字图片
		}	
	}
	//从数据库中获取历史数据
	public ArrayList<String> getSQLData(String limit){
		//获得SQLiteDatabase对象
		db = mh.getReadableDatabase();
		String [] cols = {MySQLiteHelper.ID,MySQLiteHelper.STEP};
		Cursor c = db.query(MySQLiteHelper.TABLE_NAME, cols, null, null, null, null, MySQLiteHelper.ID+" DESC", limit);
		ArrayList<String> al = new ArrayList<String>();
		for(c.moveToFirst();!(c.isAfterLast());c.moveToNext()){
			al.add(c.getString(1));
		}
		c.close();
		db.close();
		return al;
		}
}
