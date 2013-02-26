package com.firearms.gunbot;

import java.util.Vector;

public class GunbotProductWatch {
	private String m_name;
	private long m_id;
	private int m_subcategory = 0;
	private int m_category = 0;
	
	private Vector<TextFilter> m_textFilters = new Vector<TextFilter>();
	private int m_maxPrice = 0;
	private int m_maxPricePerRound = 0;
	private boolean m_mustBeInStock = false;
	
	public GunbotProductWatch(long id, String name, int category, int subcategory){
		m_id = id;
		m_name = name;
		
		m_category = category;
		m_subcategory = subcategory;
	}
	
	public boolean matches(GunbotProduct product){
		if (m_category != product.getCategory())
			return false;
		
		if (m_subcategory != product.getSubcategory())
			return false;
		
		if (m_mustBeInStock && !product.isInStock())
			return false;
		
		if (m_maxPrice != 0 && product.getTotalPrice() > m_maxPrice)
			return false;
		
		if (m_maxPricePerRound != 0 && product.getPricePerRound() > m_maxPricePerRound)
			return false;
		
		for (TextFilter filter : m_textFilters){
			if (!filter.satisfies(product.getDescription()))
				return false;
		}
		
		return true;
	}
	
	public TextFilter addTextFilter(int filterType, String filterText){
		m_textFilters.add(new TextFilter(filterType, filterText));
		return m_textFilters.lastElement();
	}
	
	public int getTextFilterCount(){
		return m_textFilters.size();
	}
	
	public TextFilter getTextFilter(int index){
		if (index < 0 || index >= m_textFilters.size())
			return null;
		
		return m_textFilters.get(index);
	}
	
	public boolean removeTextFilter(int index){
		if (index >= 0 && index < m_textFilters.size()){
			m_textFilters.remove(index);
			return true;
		}
		
		return false;
	}
	
	public String getName(){
		return m_name;
	}
	
	public void setName(String name){
		m_name = name;
	}
	
	public int getMaxPrice(){
		return m_maxPrice;
	}
	
	public void setMaxPrice(int maxPrice){
		m_maxPrice = maxPrice;
	}
	
	public int getMaxPricePerRound(){
		return m_maxPricePerRound;
	}
	
	public void setMaxPricePerRound(int maxPricePerRound){
		m_maxPricePerRound = maxPricePerRound;
	}
	
	public boolean getMustBeInStock(){
		return m_mustBeInStock;
	}
	
	public void setMustBeInStock(boolean mustBeInStock){
		m_mustBeInStock = mustBeInStock;
	}
	
	public long getId(){
		return m_id;
	}
	
	public void setId(long id){
		m_id = id;
	}
	
	public int getSubcategory(){
		return m_subcategory;
	}
	
	public void setSubcategory(int subcategory){
		m_subcategory = subcategory;
	}
	
	public int getCategory(){
		return m_category;
	}
	
	public static class TextFilter{
		public static final int CONTAINS = 1;
		public static final int DOES_NOT_CONTAIN = 0;
		
		private int m_filterType;
		private String m_filterText;
		
		public TextFilter(int filterType, String filterText){
			m_filterType = filterType;
			m_filterText = filterText.toLowerCase();
		}
		
		public boolean satisfies(String text){
			if (m_filterType == CONTAINS)
				return text.toLowerCase().contains(m_filterText);
			else
				return !text.toLowerCase().contains(m_filterText);
			
		}
		
		public int getFilterType(){
			return m_filterType;
		}
		
		public String getFilterText(){
			return m_filterText;
		}
	}
}
