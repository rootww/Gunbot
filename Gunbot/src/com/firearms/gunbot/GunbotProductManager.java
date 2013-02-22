package com.firearms.gunbot;

import java.io.IOException;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GunbotProductManager {
	private static final String LOG_TAG = "GunbotProductManager";
	private static final String GUNBOT_URL = "http://www.gunbot.net";
	
	private ListView m_productList = null;
	private Context m_context = null;
	private boolean m_isUpdating = false;
	private Vector<GunbotProduct> m_currentItems = null;
	private GunbotDataFetcherTask m_currentTask = null;
	private Vector<GunbotCategory> m_categories = null;
	
	
	public GunbotProductManager(Context context, ListView listview, Vector<GunbotCategory> categories){
		m_productList = listview;
		m_context = context;
		m_categories = categories;
	}
	
	public boolean isUpdating(){
		return m_isUpdating;
	}
	
	private String getRequestUrl(int category, int subcategory){
		return "http://www.gunbot.net/index2.php?cal=".concat(m_categories.get(category).getSubcategory(subcategory).getUrl());
	}
	
	public String getProductLinkUrl(int position){
		return GUNBOT_URL.concat(m_currentItems.get(position).getUrl());
	}
	
	private void doRefresh(int category, int subcategory){
		m_isUpdating = true;
		Log.i(LOG_TAG, "Refreshing product list");
		m_currentTask = new GunbotDataFetcherTask();
		m_currentTask.execute(getRequestUrl(category, subcategory));
	}
	
	public void refreshProducts(int category, int subcategory, boolean force){
		if (!m_isUpdating){
			doRefresh(category, subcategory);
		}
		else if (force && m_currentTask!= null){
			m_currentTask.cancel(true);
			doRefresh(category, subcategory);
		}
		
	}
	
	private class GunbotDataFetcherTask extends AsyncTask<String, Void, Void>{
		private static final String DEBUG_TAG = "GunbotDataFetcher";
		protected String[] items;
		
		protected Void doInBackground(String... options){
			try{
				Document doc = Jsoup.connect(options[0]).get();
				Elements products = doc.select("td a");
				
				m_currentItems = new Vector<GunbotProduct>();
				items = new String[products.size()];
				
				for (int i = 0; i < products.size(); i++){
					Element element = products.get(i);
					
					GunbotProduct p = new GunbotProduct(element.text(), element.attr("href"));
					m_currentItems.add(p);
					
					items[i] = p.getDescription();
				}
				
				products = null;
				doc = null;
				
			}
			catch (IOException e){
				Log.e(DEBUG_TAG, "Error fetching Gunbot Data");
			}
			return null;
		}
		
		protected void onPostExecute(Void result){
			ArrayAdapter<String> adapter= new ArrayAdapter<String>(m_context,R.layout.list_item,items);
			m_productList.setAdapter(adapter);
		    m_isUpdating = false;
		}
	}
}
