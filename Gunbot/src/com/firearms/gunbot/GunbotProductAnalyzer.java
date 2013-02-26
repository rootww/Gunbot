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
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GunbotProductAnalyzer {
	private static final String LOG_TAG = "GunbotProductAnalyzer";
	
	private List<GunbotProductWatch> m_watches;
	
	private Context m_context;
	
	private GunbotDatabase m_database;
	private boolean m_shouldCloseDatabase;
	
	private boolean m_shouldNotify;
	private boolean m_shouldVibrate;
	private boolean m_shouldBeep;
	
	public GunbotProductAnalyzer(Context context){
		m_context = context;
		m_shouldCloseDatabase = true;
		setNotificationSettings();
	}
	
	public GunbotProductAnalyzer(Context context, GunbotDatabase database){
		m_context = context;
		m_database = database;
		m_shouldCloseDatabase = false;
		setNotificationSettings();
	}
	
	public GunbotProductAnalyzer(Context context, GunbotDatabase database, int categoryId, int subcategoryId, List<GunbotProduct> products){
		this(context, database);
		analyzeProducts(categoryId, subcategoryId, products);
	}
	
	private void setNotificationSettings(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_context);
		m_shouldNotify = prefs.getBoolean("preference_notification", true);
		m_shouldVibrate = prefs.getBoolean("preference_notification_vibrate", true);
		m_shouldBeep = prefs.getBoolean("preference_notification_sound", true);
	}
	
	public void analyzeProducts(int categoryId, int subcategoryId, List<GunbotProduct> products){
		if (m_database == null)
			m_database = new GunbotDatabase(m_context);
		
		if (m_shouldNotify){
			m_watches = m_database.getProductWatches(categoryId, subcategoryId);
			Map<String, GunbotProduct> previous = m_database.getProductMap(categoryId, subcategoryId);
		
			for (GunbotProduct product : products){
				if (!previous.containsKey(product.getDescription()))
					checkProductWatches(product);
			}
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
			if (watch.matches(product)){
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
		
		if (m_shouldBeep)
		        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		
		if (m_shouldVibrate)
		        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
		
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gunbot.net".concat(product.getUrl())));
		PendingIntent resultPendingIntent = PendingIntent.getActivity(m_context, 0, browserIntent, 0);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) m_context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(new Random().nextInt(), mBuilder.build());
	}
}
