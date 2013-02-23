package com.firearms.gunbot;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import android.util.Log;

public class GunbotProductAnalyzer {
	private static final String LOG_TAG = "GunbotProductAnalyzer";
	
	private Map<String, GunbotProduct> m_previousProducts;
	private List<GunbotProductWatch> m_watches;
	
	private boolean m_shouldNotify = true;
	
	public GunbotProductAnalyzer(){
	}
	
	public void clearProducts(){
		m_previousProducts = null;
	}
	
	public void analyzeProducts(List<GunbotProduct> products){
		if (m_previousProducts != null && m_shouldNotify){
			for (GunbotProduct product : products){
				for (GunbotProductWatch watch : m_watches){
					if (watch.satisfies(product))
						notifyUserOfProduct(product);
				}
			}
		}
		
		mapProducts(products);
	}
	
	private void mapProducts(List<GunbotProduct> products){
		m_previousProducts = new TreeMap<String, GunbotProduct>();
		
		for (GunbotProduct product : products)
			m_previousProducts.put(product.getDescription(), product);
	}
	
	private void notifyUserOfProduct(GunbotProduct product){
		Log.i(LOG_TAG, "Notify user of product: ".concat(product.getDescription()));
	}
	
	public void setShouldNotify(boolean shouldNotify){
		m_shouldNotify = shouldNotify;
	}
	
	public boolean getShouldNotify(){
		return m_shouldNotify;
	}
}
