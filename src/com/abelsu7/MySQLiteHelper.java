package com.abelsu7;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
	
	public static final String TABLE_NAME = "step_table";
	public static final String ID = "id";
	public static final String STEP = "step";
	public static final String UPDATE_DATE = "up_date";

	public MySQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table if not exists " + TABLE_NAME + "(" //创建数据库表
				                                 + ID + " integer primary key,"
				                                 + STEP + " integer)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
}
