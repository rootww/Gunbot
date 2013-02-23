package com.firearms.gunbot;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class GunbotProductManager {
	private static final String LOG_TAG = "GunbotProductManager";
	private static final String GUNBOT_URL = "http://www.gunbot.net";
	private static final String IN_STOCK_TEXT = "[in stock]";
	
	private ListView m_productList = null;
	private Context m_context = null;
	private boolean m_isUpdating = false;
	private List<GunbotProduct> m_currentItems = null;
	private GunbotDataFetcherTask m_currentTask = null;
	private List<GunbotCategory> m_categories = null;
	
	private GunbotProductAnalyzer m_analyzer;
	
	
	public GunbotProductManager(Context context, ListView listview, List<GunbotCategory> categories, GunbotProductAnalyzer analyzer){
		m_productList = listview;
		m_context = context;
		m_categories = categories;
		m_analyzer = analyzer;
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
				Elements products = doc.select("tr");
				
				m_currentItems = new Vector<GunbotProduct>();
				items = new String[products.size()];
				
				for (int i = 0; i < products.size(); i++){
					GunbotProduct p = extractProductFromRow(products.get(i));
					m_currentItems.add(p);
					
					items[i] = p.getDescription();
				}
				
				products = null;
				doc = null;
				
			}
			catch (IOException e){
				Log.e(DEBUG_TAG, "Error fetching Gunbot Data");
				Toast.makeText(m_context, "Unable to fetch data from Gunbot", Toast.LENGTH_SHORT).show();
			}
			return null;
		}
		
		private GunbotProduct extractProductFromRow(Element row){
			Elements children = row.children();
			Element product = children.get(0).children().get(0);
			
			return new GunbotProduct(product.text(), product.attr("href"), GunbotUtils.priceToCents(children.get(1).text()),GunbotUtils.priceToCents(children.get(2).text()) , itemInStock(children.get(3).text()));
		}
		
		private boolean itemInStock(String stockText){
			return IN_STOCK_TEXT.equals(stockText);
		}
		
		protected void onPostExecute(Void result){
			m_isUpdating = false;
			
			if (items != null){
				ArrayAdapter<String> adapter= new ArrayAdapter<String>(m_context,R.layout.list_item,items);
				m_productList.setAdapter(adapter);
				m_analyzer.analyzeProducts(m_currentItems);
			}
		    
		}
	}
}
