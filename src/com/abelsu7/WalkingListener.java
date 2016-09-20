package com.abelsu7;

import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

public class WalkingListener implements SensorListener {

	WalkingService father;                //WalkingService引用
	float[] preCoordinate;                
	double currentTime = 0, lastTime = 0; //记录时间
	float WALKING_THRESHOLD = 30;
	public WalkingListener(WalkingService father){
		this.father = father;
	}
	
	@Override
	public void onAccuracyChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	//传感器发生变化后调用该方法
	public void onSensorChanged(int sensor, float[] values) {
		// TODO Auto-generated method stub
		if( sensor == SensorManager.SENSOR_ACCELEROMETER){
			analyseData(values);//调用方法分析数据
		}

	}

	//方法：分析参数进行计算
	public void analyseData(float[] values){
		//获取当前时间
		currentTime = System.currentTimeMillis();
		//每隔200ms取加速度力和前一个进行比较
		if(currentTime - lastTime > 200){
			if(preCoordinate == null){//还未存过数据
				preCoordinate = new float[3];
				for(int i=0;i<3;i++){
					preCoordinate[i] = values[i];
				}
			}
			else{//记录了原始坐标的话，就进行比较
				int angle = calculateAngle(values,preCoordinate);
				if(angle >= WALKING_THRESHOLD){
					father.steps++;//步数增加
					updateData();//更新步数
				}
				for(int i=0;i<3;i++){
					preCoordinate[i] = values[i];
				}
			}
			lastTime = currentTime;//重新计时
		}
	}
	//方法：计算加速度矢量角度的方法
	public int calculateAngle(float[] newPoints, float[] oldPoints){
		int angle = 0;
		float vectorProduct = 0;              //向量积
		float newMold = 0;                    //新向量的模
		float oldMold = 0;                    //旧向量的模
		for(int i=0;i<3;i++){
			vectorProduct += newPoints[i] * oldPoints[i];
			newMold += newPoints[i] * newPoints[i];
			oldMold += oldPoints[i] * oldPoints[i];
		}
		newMold = (float)Math.sqrt(newMold);
		oldMold = (float)Math.sqrt(oldMold);
		//计算夹角的余弦
		float cosineAngle = (float)(vectorProduct / (newMold * oldMold));
		//通过余弦值求角度
		float fangle = (float)Math.toDegrees(Math.acos(cosineAngle));
		angle = (int)fangle;
		//返回向量的夹角
		return angle;
	}
	//方法：向Activity更新步数
	public void updateData(){
		Intent intent = new Intent();         //创建Intent对象
		intent.setAction("com.abelsu7.WalkingActivity");
		intent.putExtra("step", father.steps);//添加步数
		father.sendBroadcast(intent);         //发出广播
	}
	
}
