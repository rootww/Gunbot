package com.firearms.gunbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GunbotDatabase extends SQLiteOpenHelper{
	private static final String DEBUG_TAG = "GunbotDatabase";
	
	private SQLiteDatabase m_database = null;
	private Context m_context = null;

	public GunbotDatabase(Context context) {
		super(context, "gunbot.db", null, 1);
		
		m_context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		m_database = db;
		
		Log.i(DEBUG_TAG, "Creating GunbotDatabase");
		LoadSchema("schema.sql");

	}
	
	public Vector<GunbotCategory> getCategoryInformation(){
		open();
		
		Cursor catCursor = m_database.rawQuery("SELECT name, id FROM product_categories WHERE parent = 0;", null);
		Vector<GunbotCategory> categories = new Vector<GunbotCategory>();
		
		while (catCursor.moveToNext()){
			GunbotCategory cat= new GunbotCategory(catCursor.getString(0), catCursor.getInt(1));
			getSucategoryInformation(cat);
			categories.add(cat);
		}
		
		catCursor.close();
		return categories;
	}
	
	private void getSucategoryInformation(GunbotCategory category){
		Cursor subcatCursor = m_database.rawQuery("SELECT id, name, url FROM product_categories WHERE parent = ?;", new String[]{String.valueOf(category.getId())});
		
		while (subcatCursor.moveToNext())
			category.AddSubcategory(subcatCursor.getInt(0), subcatCursor.getString(1), subcatCursor.getString(2));
		
		subcatCursor.close();
	}
	
	private void LoadSchema(String assetPath){
		try{
			String sqlStatement;
			InputStream inputStream = m_context.getAssets().open(assetPath);
			InputStreamReader reader = new InputStreamReader(inputStream);
			BufferedReader br = new BufferedReader(reader);
			
			while ((sqlStatement = br.readLine()) != null) {
				if (sqlStatement.length() > 0)
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
			m_database = getWritableDatabase();
	}

}
