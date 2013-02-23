package com.firearms.gunbot;

import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class MainActivity extends Activity implements OnItemSelectedListener{
	private int m_currentProductType = 0;
	private GunbotProductManager m_productManager = null;
	private List<GunbotCategory> m_categories;
	private GunbotProductAnalyzer m_analyzer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ListView listView = initListView();
		
		GunbotDatabase database = new GunbotDatabase(getApplicationContext());
		m_categories = database.getCategoryInformation();
		m_analyzer = new GunbotProductAnalyzer();
		m_productManager = new GunbotProductManager(getApplicationContext(), listView, m_categories, m_analyzer);
		
		
		initSpinner();
	}
	
	private void initSpinner(){
		Spinner ammoSpinner = (Spinner) findViewById(R.id.ammo_selection);
		GunbotCategory category = m_categories.get(m_currentProductType);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, category.getSubcategoryNames());
		ammoSpinner.setAdapter(spinnerArrayAdapter);
		ammoSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		m_currentProductType = pos;
		m_analyzer.clearProducts();
		m_productManager.refreshProducts(0,m_currentProductType, true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_watches:
	    	showWatchView();
	    	return true;
	    case R.id.menu_settings:
	    	return true;
	    case R.id.menu_refresh:
	    	m_productManager.refreshProducts(0,m_currentProductType, false);
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
				  activateProductLink(m_productManager.getProductLinkUrl(position));
			};
		});
		
		return listView;
	}
	
	private void showWatchView(){
		Intent intent = new Intent(getApplicationContext(), GunbotWatchViewActivity.class);
		startActivity(intent);
	}
}
