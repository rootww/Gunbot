package com.firearms.gunbot;

public class GunbotProduct {
	public String m_description;
	public String m_url;
	
	public GunbotProduct(String description, String url){
		m_description = description;
		m_url = url;
	}
	
	public String getDescription(){
		return m_description;
	}
	
	public String getUrl(){
		return m_url;
	}

}
