package com.firearms.gunbot;

import java.util.Vector;

public class GunbotCategory {
	private String m_name;
	private int m_id;
	
	private Vector<GunbotSubcategory> m_subcategories = new Vector<GunbotSubcategory>();
	
	public GunbotCategory(String name, int id){
		m_name = name;
		m_id = id;
	}
	
	public String getName(){
		return m_name;
	}
	
	public int getId(){
		return m_id;
	}
	
	public GunbotSubcategory AddSubcategory(int id, String name, String url){
		m_subcategories.add(new GunbotSubcategory(id, name, url));
		return m_subcategories.lastElement();
	}
	
	public int getSubcategoryCount(){
		return m_subcategories.size();
	}
	
	public GunbotSubcategory getSubcategory(int index){
		if (index < 0 || index >= m_subcategories.size())
			return null;
		else
			return m_subcategories.get(index);
	}
	
	public GunbotSubcategory getSubcategoryById(int id){
		for (GunbotSubcategory subcategory : m_subcategories){
			if (subcategory.getId() == id)
				return subcategory;
		}
		
		return null;
	}
	
	public String[] getSubcategoryNames(){
		String[] subcategoryNames = new String[m_subcategories.size()];
		
		for (int i = 0; i < m_subcategories.size(); i++)
			subcategoryNames[i] = m_subcategories.get(i).getName();
		
		return subcategoryNames;
	}
	
	public static class GunbotSubcategory{
		private String m_name;
		private String m_url;
		private int m_id;
		
		public GunbotSubcategory(int id, String name, String url){
			m_id = id;
			m_name = name;
			m_url = url;
		}
		
		public int getId(){
			return m_id;
		}
		
		public String getName(){
			return m_name;
		}
		
		public String getUrl(){
			return m_url;
		}
	}
}
