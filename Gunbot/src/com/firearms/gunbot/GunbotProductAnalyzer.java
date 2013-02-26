package com.firearms.gunbot;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GunbotProductAnalyzer {
	private static final String LOG_TAG = "GunbotProductAnalyzer";
	
	private List<GunbotProductWatch> m_watches;
	
	private Context m_context;
	
	private GunbotDatabase m_database;
	private boolean m_shouldCloseDatabase;
	
	private boolean m_shouldNotify = true;
	
	public GunbotProductAnalyzer(Context context){
		m_context = context;
		m_shouldCloseDatabase = true;
	}
	
	public GunbotProductAnalyzer(Context context, GunbotDatabase database){
		m_context = context;
		m_database = database;
		m_shouldCloseDatabase = false;
	}
	
	public GunbotProductAnalyzer(Context context, GunbotDatabase database, int categoryId, int subcategoryId, List<GunbotProduct> products){
		this(context, database);
		analyzeProducts(categoryId, subcategoryId, products);
	}
	
	public void analyzeProducts(int categoryId, int subcategoryId, List<GunbotProduct> products){
		if (m_database == null)
			m_database = new GunbotDatabase(m_context);
		
		
		m_watches = m_database.getProductWatches(categoryId, subcategoryId);
			
		Map<String, GunbotProduct> previous = m_database.getProductMap(categoryId, subcategoryId);
		
		for (GunbotProduct product : products){
			if (!previous.containsKey(product.getDescription()))
				checkProductWatches(product);
		}
		
		updateProductCategory(categoryId, subcategoryId, products);
		
		if (m_shouldCloseDatabase)
			m_database.close();
	}
	
	private void updateProductCategory(int categoryId, int subcategoryId, List<GunbotProduct> products){
		m_database.clearProducts(categoryId, subcategoryId);
		m_database.insertProducts(products);
	}
	
	private void checkProductWatches(GunbotProduct product){
		for (GunbotProductWatch watch : m_watches){
			if (watch.satisfies(product)){
				notifyUserOfProduct(product);
				break;
			}
		}
	}
	
	private void notifyUserOfProduct(GunbotProduct product){
		Log.i(LOG_TAG, "Notify user of product: ".concat(product.getDescription()));
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(m_context)
		        .setSmallIcon(android.R.drawable.ic_dialog_alert)
		        .setContentTitle("New Product Found!")
		        .setContentText(product.getDescription());
		
		        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
		
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gunbot.net".concat(product.getUrl())));
		PendingIntent resultPendingIntent = PendingIntent.getActivity(m_context, 0, browserIntent, 0);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) m_context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(new Random().nextInt(), mBuilder.build());
	}
	
	public void setShouldNotify(boolean shouldNotify){
		m_shouldNotify = shouldNotify;
	}
	
	public boolean getShouldNotify(){
		return m_shouldNotify;
	}
}
