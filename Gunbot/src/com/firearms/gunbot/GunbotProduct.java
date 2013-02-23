package com.firearms.gunbot;

public class GunbotProduct {
	private String m_description;
	private String m_url;
	private int m_pricePerRound;
	private int m_totalPrice;
	private boolean m_isInStock;
	
	
	public GunbotProduct(String description, String url, int pricePerRound, int totalPrice, boolean inStock){
		m_description = description;
		m_url = url;
		m_pricePerRound = pricePerRound;
		m_totalPrice = totalPrice;
		m_isInStock = inStock;
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

}
