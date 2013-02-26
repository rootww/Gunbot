package com.firearms.gunbot;

public class GunbotProduct {
	private int m_category;
	private int m_subcategory;
	
	private String m_description;
	private String m_url;
	private int m_pricePerRound;
	private int m_totalPrice;
	private boolean m_isInStock;
	private String m_seller;
	
	
	public GunbotProduct(int category, int subcategory, String description, String url, int pricePerRound, int totalPrice, boolean inStock, String seller){
		m_category = category;
		m_subcategory = subcategory;
		m_description = description;
		m_url = url;
		m_pricePerRound = pricePerRound;
		m_totalPrice = totalPrice;
		m_isInStock = inStock;
		m_seller = seller;
	}
	
	public int getCategory(){
		return m_category;
	}
	
	public int getSubcategory(){
		return m_subcategory;
	}
	
	public String getDescription(){
		return m_description;
	}
	
	public String getUrl(){
		return m_url;
	}
	
	public int getPricePerRound(){
		return m_pricePerRound;
	}
	
	public int getTotalPrice(){
		return m_totalPrice;
	}
	
	public boolean isInStock(){
		return m_isInStock;
	}
	
	public String getSeller(){
		return m_seller;
	}

}
