package com.firearms.gunbot;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	
	private Map<String, GunbotProduct> m_previousProducts;
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
	
	public void clearProducts(){
		m_previousProducts = null;
	}
	
	public void analyzeProducts(List<GunbotProduct> products){
		if (m_database == null)
			m_database = new GunbotDatabase(m_context);
			
		m_watches = m_database.getProductWatches();
		
		if (m_previousProducts != null && m_shouldNotify){
			for (GunbotProduct product : products){
				for (GunbotProductWatch watch : m_watches){
					if (watch.satisfies(product)){
						notifyUserOfProduct(product);
						break;
					}
				}
			}
		}
		
		mapProducts(products);
		
		if (m_shouldCloseDatabase)
			m_database.close();
	}
	
	private void mapProducts(List<GunbotProduct> products){
		m_previousProducts = new TreeMap<String, GunbotProduct>();
		
		for (GunbotProduct product : products)
			m_previousProducts.put(product.getDescription(), product);
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
		mNotificationManager.notify(55662187, mBuilder.build());
	}
	
	public void setShouldNotify(boolean shouldNotify){
		m_shouldNotify = shouldNotify;
	}
	
	public boolean getShouldNotify(){
		return m_shouldNotify;
	}
}
