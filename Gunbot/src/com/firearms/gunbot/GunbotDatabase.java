package com.firearms.gunbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GunbotDatabase extends SQLiteOpenHelper{
	private static final String DEBUG_TAG = "GunbotDatabase";
	
	private SQLiteDatabase m_database = null;
	private Context m_context = null;

	public GunbotDatabase(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		
		m_context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		m_database = db;
		
		Log.i(DEBUG_TAG, "Creating GunbotDatabase");
		LoadSchema("schema.sql");

	}
	
	private void LoadSchema(String assetPath){
		try{
			String sqlStatement;
			InputStream inputStream = m_context.getAssets().open(assetPath);
			InputStreamReader reader = new InputStreamReader(inputStream);
			BufferedReader br = new BufferedReader(reader);
			
			while ((sqlStatement = br.readLine()) != null) {
				m_database.execSQL(sqlStatement);
		    }
		}
		catch (IOException e){ }
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public void open(){
		if (m_database == null)
			m_database = getReadableDatabase();
	}

}
