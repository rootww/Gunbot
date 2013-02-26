package com.firearms.gunbot;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.util.Log;

public class GunbotProductFetcher implements Runnable{
	public static final String DEBUG_TAG = "GunbotProductFetcher";
	private static final String IN_STOCK_TEXT = "[in stock]";
	private static Pattern sellerPattern = Pattern.compile("\\[(.+)\\]");
	
	private int m_category;
	private int m_subcategory;
	
	private Vector<Listener> m_listeners = new Vector<Listener>();
	private Vector<GunbotProduct> m_products = new Vector<GunbotProduct>();
	private GunbotDatabase m_database;
	private Context m_context;
	
	public GunbotProductFetcher(Context context, int category, int subcategory){
		m_context = context;
		m_category = category;
		m_subcategory = subcategory;
	}
	
	public GunbotProductFetcher(Context context, int category, int subcategory, Listener listener){
		this(context, category, subcategory);
		addListener(listener);
	}
	
	public void addListener(Listener listener){
		m_listeners.add(listener);
	}
	
	public void run(){
		m_database = new GunbotDatabase(m_context);
		
		try{
			Document doc = Jsoup.connect(getRequestUrl()).get();
			Elements products = doc.select("tr");
			
			for (int i = 0; i < products.size(); i++)
				m_products.add ( extractProductFromRow(products.get(i)));
			
			products = null;
			doc = null;
			
		}
		catch (IOException e){
			Log.e(DEBUG_TAG, "Error fetching Gunbot Data:".concat(e.getMessage()));
			notifyFetchError(e.getMessage());
		}
		
		m_database.close();
		notifyProductsFetched();
	}
	
	private String getSellerName(String sellerText){
		
		Matcher matcher = sellerPattern.matcher(sellerText);
		matcher.find();
		
		if (matcher.groupCount() != 1)
			return "";
		else
			return matcher.group(1);
	}
	
	private GunbotProduct extractProductFromRow(Element row){
		Elements children = row.children();
		Element product = children.get(0).children().get(0);
		
		return new GunbotProduct(	m_category, 
									m_subcategory,
									product.text(), 
									product.attr("href"), 
									GunbotUtils.priceToCents(children.get(1).text()),
									GunbotUtils.priceToCents(children.get(2).text()) , 
									itemInStock(children.get(3).text()),
									getSellerName(children.get(4).text()));
	}
	
	private boolean itemInStock(String stockText){
		return IN_STOCK_TEXT.equals(stockText);
	}
	
	private void notifyProductsFetched(){
		for (Listener listener : m_listeners)
			listener.onProductListFetched(m_products);
	}
	
	private void notifyFetchError(String message){
		for (Listener listener : m_listeners)
			listener.onProductListFetchFailure(message);
	}
	
	private String getRequestUrl(){
		return "http://www.gunbot.net/index2.php?cal=".concat(m_database.getProductUrl(m_category, m_subcategory));
	}
	
	public static interface Listener{
		abstract void onProductListFetched(List<GunbotProduct> products);
		abstract void onProductListFetchFailure(String message);
	}
}
