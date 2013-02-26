package com.firearms.gunbot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GunbotUtils {
	public static final String EXTRA_ID = "com.firearms.gunbot.EXTRA_ID";
	
	static Pattern pricePattern = Pattern.compile("\\D*(\\d*)\\.(\\d*)\\D*");
	
	public static int priceToCents(String price){
		Matcher matcher = pricePattern.matcher(price);
		matcher.find();
		
		if (price.length() == 0 || matcher.groupCount() != 2)
			return 0;
		
		int dollars = Integer.parseInt(matcher.group(1));
		int cents = Integer.parseInt(matcher.group(2));
		
		return (dollars * 100) + cents;
	}
	
	public static String centsToDollarStr(int cents){
		int d = cents / 100;
		int c = cents % 100;
		
		StringBuilder str = new StringBuilder("$");
		str.append(d);
		str.append('.');
		if (c < 10)
			str.append('0');
		str.append(c);
		
		return str.toString();
	}
	
	public static class Pair<T1,T2>{
		public T1 first;
		public T2 second;
		
		public Pair() {}
		public Pair(T1 a , T2 b){
			first = a;
			second = b;
		}
	}
}
