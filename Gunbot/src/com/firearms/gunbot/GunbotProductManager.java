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
	private ListView m_productList = null;
	private Context m_context = null;
	private boolean m_isUpdating = false;
	private Vector<GunbotProduct> m_currentItems = null;
	private GunbotDataFetcherTask m_currentTask = null;
	
	
	public GunbotProductManager(Context context, ListView listview){
		m_productList = listview;
		m_context = context;
	}
	
	public boolean isUpdating(){
		return m_isUpdating;
	}
	
	private String getAmmoURL(int option){
		String ammoURL = "http://www.gunbot.net/index2.php?cal=";
		
		switch (option){
		case 0:
			ammoURL = ammoURL.concat("223-556");
			break;
		case 1:
			ammoURL = ammoURL.concat("22lr");
			break;
		};
		
		return ammoURL;
	}
	
	public String getProductLinkUrl(int position){
		return "http://www.gunbot.net".concat(m_currentItems.get(position).getUrl());
	}
	
	private void doRefresh(int pos){
		m_isUpdating = true;
		Log.i(LOG_TAG, "Refreshing product list");
		m_currentTask = new GunbotDataFetcherTask();
		m_currentTask.execute(pos);
	}
	
	public void refreshProducts(int pos, boolean force){
		if (!m_isUpdating){
			doRefresh(pos);
		}
		else if (force && m_currentTask!= null){
			m_currentTask.cancel(true);
			doRefresh(pos);
		}
		
	}
	
	private class GunbotDataFetcherTask extends AsyncTask<Integer, Void, Void>{
		private static final String DEBUG_TAG = "GunbotDataFetcher";
		protected String[] items;
		
		protected Void doInBackground(Integer... options){
			try{
				Document doc = Jsoup.connect(getAmmoURL(options[0])).get();
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
