package com.firearms.gunbot;

import java.util.List;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class GunbotNewWatchActivity extends Activity {
	private static final String Edittext = null;
	private long m_watchId;
	private GunbotDatabase m_database;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gunbot_new_watch);
		m_watchId = getIntent().getLongExtra(GunbotUtils.EXTRA_ID, 0);

		
		GunbotDatabase database = new GunbotDatabase(getApplicationContext());
		List<GunbotCategory> categories = database.getCategoryInformation();
		
		Spinner categorySpinner = (Spinner) findViewById(R.id.watch_category);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories.get(0).getSubcategoryNames());
		categorySpinner.setAdapter(spinnerArrayAdapter);
		
		if (m_watchId != 0)
			loadDataForExistingWatch();
		else
			addNewWatch();

		initActionBar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_gunbot_new_watch, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:
			saveWatch();
			finish();
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("NewApi")
	private void initActionBar(){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
			getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void addNewWatch(){
		LinearLayout filterContainer = (LinearLayout)findViewById(R.id.text_filter_container);
		
		
		LinearLayout filterLayout = new LinearLayout(this);
		filterLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			Spinner filterType = new Spinner(this);
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.filter_types));
			filterType.setAdapter(spinnerArrayAdapter);
			filterLayout.addView(filterType);
			
			TextView filterLabel = new TextView(this);
			filterLabel.setText(R.string.label_filter_text);
			filterLayout.addView(filterLabel);
			
			EditText filterText = new EditText(this);
			filterText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
			filterLayout.addView(filterText);
			
			Button removeButton = new Button(this);
			removeButton.setText(R.string.label_filter_remove);
			removeButton.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	            	 LinearLayout filterContainer = (LinearLayout)findViewById(R.id.text_filter_container);
	                 filterContainer.removeView((View)v.getParent());
	             }
	         });
			filterLayout.addView(removeButton);

			filterContainer.addView(filterLayout);
			
	}
	
	public void onAddFilterClick(View view){
		addNewWatch();
	}
	
	private void loadDataForExistingWatch(){
		
	}
	
	private void saveWatch(){
		EditText nameText = (EditText) findViewById(R.id.watch_name);
		Spinner categorySpinner = (Spinner) findViewById(R.id.watch_category);
		CheckBox inStockCheckbox = (CheckBox) findViewById(R.id.watch_in_stock);
		EditText maxPerRoundText = (EditText) findViewById(R.id.watch_max_price_per_round);
		EditText maxPriceText = (EditText) findViewById(R.id.watch_max_price);
		
		GunbotProductWatch productWatch = new GunbotProductWatch(m_watchId, nameText.getText().toString());
		productWatch.setCategory(categorySpinner.getSelectedItemPosition());
		productWatch.setMustBeInStock(inStockCheckbox.isChecked());
		productWatch.setMaxPricePerRound(GunbotUtils.priceToCents(maxPerRoundText.getText().toString()));
		productWatch.setMaxPrice(GunbotUtils.priceToCents(maxPriceText.getText().toString()));
		
		LinearLayout filterContainer = (LinearLayout)findViewById(R.id.text_filter_container);
		
		for (int i = 0; i < filterContainer.getChildCount(); i++){
			LinearLayout filterRow = (LinearLayout) filterContainer.getChildAt(i);
			Spinner filterType = (Spinner) filterRow.getChildAt(0);
			EditText filterText = (EditText) filterRow.getChildAt(2);
			
			productWatch.addTextFilter(filterType.getSelectedItemPosition(), filterText.getText().toString());
		}
		
		if (m_database == null)
			m_database = new GunbotDatabase(getApplicationContext());
		
		m_database.saveProductWatch(productWatch);
	}

}
