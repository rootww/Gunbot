package com.firearms.gunbot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class GunbotBackgroundUpdater extends Service{
	private static final String LOG_TAG = "GunbotBackgroundUpdater";
	private WakeLock m_wakeLock; 
	private GunbotDatabase m_database;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void doBackgroundUpdate(Intent intent){
		try{
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE); 
		m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Gunbot"); 
		m_wakeLock.acquire();
		
		m_database = new GunbotDatabase(this);
		
		// check the global background data setting 
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); 
		NetworkInfo network = connectivityManager.getActiveNetworkInfo();
		
		if (network == null || !network.isConnected()){
			stopSelf();
			return;
		}
		
		List<GunbotUtils.Pair<Integer, Integer>> categoryUpdateas = m_database.getWatchCategorys();
		
		if (categoryUpdateas.size() == 0)
			return;
		
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(categoryUpdateas.size());
		for (GunbotUtils.Pair<Integer, Integer> category : categoryUpdateas)
			tasks.add(new GunbotProductFetcher(this, m_database, category.first, category.second));
		
		ExecutorService executor = Executors.newFixedThreadPool(3);
		executor.invokeAll(tasks);
		}
		catch (InterruptedException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
		finally{
			m_wakeLock.release();
			m_database.close();
		}
			
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.i(LOG_TAG, "onStartCommand");
		doBackgroundUpdate(intent);
		return START_NOT_STICKY;
	}
}