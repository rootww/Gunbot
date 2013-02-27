package com.firearms.gunbot;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
		initSpinner();
	}
	
	@Override
	protected void onDestroy (){
		super.onDestroy();
		
		m_database.close();
	}
	
	private void initSpinner(){
		Spinner ammoSpinner = (Spinner) findViewById(R.id.ammo_selection);
		GunbotCategory category = m_categories.get(m_currentSubcategoryId);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, category.getSubcategoryNames());
		ammoSpinner.setAdapter(spinnerArrayAdapter);
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
			if (m_fetchSuccess){
				ListView listView = (ListView) findViewById(R.id.ammo_list);
				listView.setAdapter(new GunbotProductWatchAdapter(m_activityContext, R.layout.product_list_item, m_products));
			}
			else{
				Toast.makeText(getApplicationContext(), m_errorMessage, Toast.LENGTH_SHORT).show();
			}
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
