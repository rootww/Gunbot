package com.firearms.gunbot;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener{
	private static final String LOG_TAG = "MainActivity";
	
	private int m_defaultSubcategoryIndex = 0;
	private int m_currentSubcategoryId = 0;
	private GunbotDatabase m_database;
	private List<GunbotCategory> m_categories;
	private List<GunbotProduct> m_products;
	boolean m_isUpdating;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initListView();
		
		m_database = new GunbotDatabase(getApplicationContext());
		m_categories = m_database.getCategoryInformation();
		m_defaultSubcategoryIndex = Integer.parseInt(m_database.getAppInfo("selected_subcategory", "0"));
		m_products = m_database.getProducts(1, m_categories.get(0).getSubcategory(m_defaultSubcategoryIndex).getId());
		
		initSpinner();
	}
	
	@Override
	protected void onDestroy (){
		super.onDestroy();
		
		m_database.close();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		installBackgroundServiceIfNecessary();
		
		filterAndSortProducts();
	}
	
	private List<GunbotProduct> getInStockProducts(){
		List<GunbotProduct> filteredProducts = new Vector<GunbotProduct>();
		
		for (GunbotProduct product : m_products){
			if (product.isInStock())
				filteredProducts.add(product);
		}
		
		return filteredProducts;
	}
	
	private void filterAndSortProducts(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		final String sortMethod = preferences.getString("preference_projectlist_sortmode", getResources().getString(R.string.preference_projectlist_sortmode_default));
		final String[] sortOptions = getResources().getStringArray(R.array.preference_projectlist_sortmode_choices);
		List<GunbotProduct> filteredProducts = null;
		
		if (preferences.getBoolean("preference_projectlist_outofstock", false))
			filteredProducts = m_products;
		else
			filteredProducts = getInStockProducts();
		
		Collections.sort(filteredProducts, new Comparator<GunbotProduct>() {
	        @Override
	        public int compare(GunbotProduct p1, GunbotProduct p2) {
	        	if (sortMethod.equals(sortOptions[0]))
	        		return p1.getDescription().compareTo(p2.getDescription());
	        	else if (sortMethod.equals(sortOptions[1]))
	        		return Integer.valueOf(p1.getPricePerRound()).compareTo(Integer.valueOf(p2.getPricePerRound()));
	        	else
	        		return Integer.valueOf(p1.getTotalPrice()).compareTo(Integer.valueOf(p2.getTotalPrice()));
	        }});
		
		String defaultSortDirection = getResources().getString(R.string.preference_projectlist_sortmode_direction_default);
		String sortDirection = preferences.getString("preference_projectlist_sortmode_direction", defaultSortDirection);
		
		if (!sortDirection.equals(defaultSortDirection))
			Collections.reverse(filteredProducts);
		
		ListView listView = (ListView) findViewById(R.id.ammo_list);
		listView.setAdapter(new GunbotProductWatchAdapter(this, R.layout.product_list_item, filteredProducts));
	}
	
	private void installBackgroundServiceIfNecessary(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (!preferences.getBoolean("preference_notification", true))
			return;
		
		int interval = Integer.parseInt(preferences.getString("preference_refresh_interval", "300000"));
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		Intent intent = new Intent(MainActivity.this, GunbotBackgroundUpdater.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pending = PendingIntent.getService(this, 0, intent, 0);
		
		alarmManager.cancel(pending);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+ 5000, interval, pending);
		Log.i(LOG_TAG,"Background service installed");
	}
	
	private void initSpinner(){
		Spinner ammoSpinner = (Spinner) findViewById(R.id.ammo_selection);
		GunbotCategory category = m_categories.get(0);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, category.getSubcategoryNames());
		ammoSpinner.setAdapter(spinnerArrayAdapter);
		ammoSpinner.setSelection(m_defaultSubcategoryIndex);
		ammoSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		//this is called when the category spinner changes
		m_currentSubcategoryId = m_categories.get(0).getSubcategory(pos).getId();
		m_database.setAppInfo("selected_subcategory", String.valueOf(pos));
		
		doProductRefresh();
	}
	
	private void doProductRefresh(){
		if (!m_isUpdating){
			new ProductRefreshTask(this).execute(new Integer[]{1, m_currentSubcategoryId});
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_watches:
	    	showWatchView();
	    	return true;
	    case R.id.menu_settings:
	    	showSettings();
	    	return true;
	    case R.id.menu_refresh:
	    	doProductRefresh();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	private void activateProductLink(String productUrl){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(productUrl));
		startActivity(browserIntent);
	}
	
	private ListView initListView(){
		ListView listView = (ListView) findViewById(R.id.ammo_list);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			  @Override
			  public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
				  activateProductLink(getProductLinkUrl(position));
			};
		});
		
		return listView;
	}
	
	private String getProductLinkUrl(int position){
		return "http://www.gunbot.net".concat(m_products.get(position).getUrl());
	}
	
	private void showWatchView(){
		Intent intent = new Intent(getApplicationContext(), GunbotWatchViewActivity.class);
		startActivity(intent);
	}
	
	private void showSettings(){
		Intent intent = new Intent(getApplicationContext(), GunbotSettingsActivity.class);
		startActivity(intent);
	}

	
	private class ProductRefreshTask extends AsyncTask<Integer, Void, Void> implements GunbotProductFetcher.Listener{
		private boolean m_fetchSuccess = false;
		private String m_errorMessage;
		private Activity m_activityContext;
		
		public ProductRefreshTask(Activity activityContext){
			m_activityContext = activityContext;
		}
		
		@Override
		protected void onPreExecute (){
			
		}
		
		@Override
		protected Void doInBackground(Integer... params) {
			new GunbotProductFetcher(getApplicationContext(), m_database, params[0], params[1], this).run();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (m_fetchSuccess)
				filterAndSortProducts();
			else
				Toast.makeText(getApplicationContext(), m_errorMessage, Toast.LENGTH_SHORT).show();
			
	     }

		
		@Override
		public void onProductListFetched(List<GunbotProduct> products) {
			m_products = products;
			m_fetchSuccess = true;
		}

		@Override
		public void onProductListFetchFailure(String message) {
			m_errorMessage = message;
			m_products = null;
			m_fetchSuccess = false;
		}
	}
	
	private class GunbotProductWatchAdapter extends ArrayAdapter<GunbotProduct>{
		private Context m_context;
		private int m_layoutResourceId;
		private List<GunbotProduct> m_products;
		
		GunbotProductWatchAdapter(Context context, int layoutResourceId, List<GunbotProduct> products){
			super(context, layoutResourceId, products);
			m_context = context;
			m_layoutResourceId = layoutResourceId;
			m_products = products;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			View row = convertView;
			
			if (row == null){
				LayoutInflater inflater = ((Activity)m_context).getLayoutInflater();
				row = inflater.inflate(m_layoutResourceId, parent, false);
			}
			
			setRow(row, position);
			return row;
		}
		
		private void setRow(View row, int position){
			GunbotProduct product = m_products.get(position);
			
			TextView nameText = (TextView)row.findViewById(R.id.watch_row_name);
			TextView detailText = (TextView)row.findViewById(R.id.watch_row_detail);
			
			nameText.setText(product.getDescription());
			detailText.setText(buildDetailString(product));
		}
		
		private String buildDetailString(GunbotProduct product){
			StringBuilder str = new StringBuilder();
			
			if (product.isInStock())
				str.append("[IN STOCK]");
			else
				str.append("[OUT OF STOCK]");
			
			str.append("   ".concat(GunbotUtils.centsToDollarStr(product.getPricePerRound()).concat("/rd")));
			str.append("   ".concat(GunbotUtils.centsToDollarStr(product.getTotalPrice())));
			
			return str.toString();
		}
	}
}
