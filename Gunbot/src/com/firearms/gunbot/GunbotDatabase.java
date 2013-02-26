package com.firearms.gunbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GunbotDatabase extends SQLiteOpenHelper{
	private static final String LOG_TAG = "GunbotDatabase";
	
	private SQLiteDatabase m_database = null;
	private Context m_context = null;

	public GunbotDatabase(Context context) {
		super(context, "gunbot.db", null, 1);
		
		m_context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		m_database = db;
		
		Log.i(LOG_TAG, "Creating GunbotDatabase");
		LoadSchema("schema.sql");

	}
	
	public GunbotProductWatch saveProductWatch(GunbotProductWatch productWatch){
		open();
		ContentValues contentValues = getContentValues(productWatch);
		
		if (productWatch.getId() == 0){
			long id = m_database.insert("product_watches", null, contentValues);
			productWatch.setId(id);
			return productWatch;
		} else{
			m_database.update("product_watches", contentValues, "id = ?", new String[]{String.valueOf(productWatch.getId())});
			return productWatch;
		}
	}
	
	public void clearProducts(int categoryId, int subcategoryId){
		open();
		
		m_database.delete("products","categoryId = ? AND subcategoryId = ?;", new String[]{String.valueOf(categoryId),String.valueOf(subcategoryId)});
	}
	
	private long insertProduct(GunbotProduct product){
		ContentValues contentValues = getContentValues(product);
		return m_database.insert("products", null, contentValues);
	}
	
	public void insertProducts(List<GunbotProduct> products){
		open();
		
		for (GunbotProduct product : products)
			insertProduct(product);
	}
	
	public GunbotProductWatch getProductWatchById(long id){
		open();
		Cursor result = m_database.rawQuery("SELECT * FROM product_watches where id = ?;", new String[]{String.valueOf(id)});
		GunbotProductWatch watch = null;
		
		if (result.getCount() > 0){
			result.moveToNext();
			watch = getProductWatchFromCursor(result);
		}

		result.close();
		return watch;
	}
	
	public String getProductUrl(int productType, int categoryId){
		open();
		Cursor result = m_database.rawQuery("SELECT url FROM product_categories where parent = ? and id = ?;", new String[]{String.valueOf(productType), String.valueOf(categoryId)});
		
		String url = "";
		
		if (result.moveToNext())
			url = result.getString(0);
		
		result.close();
		return url;
	}
	
	public void deleteProductWatchById(long id){
		open();
		m_database.delete("product_watches","id = ?;", new String[]{String.valueOf(id)});
	}
	
	public List<GunbotProductWatch> getProductWatches(){
		open();
		Vector <GunbotProductWatch> watches = new Vector<GunbotProductWatch>();
		
		Cursor result = m_database.rawQuery("SELECT * from product_watches;", null);
		
		while (result.moveToNext())
			watches.add(getProductWatchFromCursor(result));
		
		result.close();
		return watches;
	}
	
	public Map<String, GunbotProduct> getProductMap(int category, int subcategory){
		TreeMap<String, GunbotProduct> products = new TreeMap<String, GunbotProduct>();
		
		open();
		Cursor result = m_database.rawQuery("SELECT * FROM products where categoryId = ? AND subcategoryId = ?;", new String[]{String.valueOf(category),String.valueOf(subcategory)});
		
		while (result.moveToNext()){
			GunbotProduct product = getProductFromCursor(result);
			products.put(product.getDescription(), product);
		}
		return products;
	}
	
	public List<GunbotProduct> getProducts(int category, int subcategory){
		Vector<GunbotProduct> products = new Vector<GunbotProduct>();
		
		open();
		Cursor result = m_database.rawQuery("SELECT * FROM products where categoryId = ? AND subcategoryId = ?;", new String[]{String.valueOf(category),String.valueOf(subcategory)});
		
		while (result.moveToNext())
			products.add(getProductFromCursor(result));
		
		return products;
	}
	
	private static GunbotProduct getProductFromCursor(Cursor cursor){
		return new GunbotProduct(	cursor.getInt(0),
									cursor.getInt(1),
									cursor.getString(2),
									cursor.getString(3),
									cursor.getInt(4),
									cursor.getInt(5),
									cursor.getInt(6) == 1,
									cursor.getString(7));
	}
	
	private static GunbotProductWatch getProductWatchFromCursor(Cursor cursor){
		long id = cursor.getLong(0);
		String name = cursor.getString(1);
		
		GunbotProductWatch productWatch = new GunbotProductWatch(id, name);
		productWatch.setCategory(cursor.getInt(2));
		productWatch.setMaxPrice(cursor.getInt(3));
		productWatch.setMaxPricePerRound(cursor.getInt(4));
		productWatch.setMustBeInStock(cursor.getInt(5) == 1);
		
		try {
			String json = cursor.getString(6);
			JSONArray filters = new JSONArray(json);
			
			for (int i = 0; i < filters.length(); i++){
				JSONObject filter = filters.getJSONObject(i);
				productWatch.addTextFilter(filter.getInt("filterType"), filter.getString("filterText"));
			}
		} catch (JSONException e) {
			Log.i(LOG_TAG, "JSON parse error: ".concat(e.getMessage()));
		}
		
		return productWatch;
	}
	
	private static ContentValues getContentValues(GunbotProduct product){
		ContentValues values = new ContentValues();
		
		values.put("categoryId", product.getCategory());
		values.put("subcategoryId", product.getSubcategory());
		values.put("description", product.getDescription());
		values.put("url", product.getUrl());
		values.put("pricePerRound", product.getPricePerRound());
		values.put("totalPrice", product.getTotalPrice());
		values.put("inStock", product.isInStock() ? 1 : 0);
		values.put("seller", product.getSeller());
		
		return values;
	}
	
	private static ContentValues getContentValues(GunbotProductWatch product){
		ContentValues values = new ContentValues();
			
		values.put("name", product.getName());
		values.put("category", product.getCategory());
		values.put("maxPrice", product.getMaxPrice());
		values.put("maxParicePerRound", product.getMaxPricePerRound());
		values.put("mustBeInStock", product.getMustBeInStock() ? 1 : 0);
		
		JSONArray filters = new JSONArray();
	
		for (int i = 0; i < product.getTextFilterCount(); i++){
			GunbotProductWatch.TextFilter f = product.getTextFilter(i);
			JSONObject filter = new JSONObject();
			try {
				filter.put("filterType", f.getFilterType());
				filter.put("filterText", f.getFilterText());
			} catch (JSONException e) {
				Log.i(LOG_TAG, "JSON encode error: ".concat(e.getMessage()));
			}
			
			filters.put(filter);
		}
		
		String json = filters.toString();
		values.put("filters", json);
		
		return values;
	}
	
	public List<GunbotCategory> getCategoryInformation(){
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
