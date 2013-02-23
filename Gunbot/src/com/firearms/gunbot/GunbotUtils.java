package com.firearms.gunbot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GunbotUtils {
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
}
