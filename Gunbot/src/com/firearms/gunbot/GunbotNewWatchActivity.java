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
	private long m_watchId;

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
			loadDataForExistingWatch(database);
		else
			addNewTextFilter();

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
	
	private void addNewTextFilter(int filterType, String filterText){
		//TODO: inflate me
		LinearLayout filterContainer = (LinearLayout)findViewById(R.id.text_filter_container);
		
		LinearLayout filterLayout = new LinearLayout(this);
		filterLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			Spinner filterTypeSpinner = new Spinner(this);
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.filter_types));
			filterTypeSpinner.setAdapter(spinnerArrayAdapter);
			filterTypeSpinner.setSelection(filterType);
			filterLayout.addView(filterTypeSpinner);
			
			TextView filterLabel = new TextView(this);
			filterLabel.setText(R.string.label_filter_text);
			filterLayout.addView(filterLabel);
			
			EditText filterTextSpinner = new EditText(this);
			filterTextSpinner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
			filterTextSpinner.setText(filterText);
			filterLayout.addView(filterTextSpinner);
			
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
	
	private void addNewTextFilter(){
		addNewTextFilter(0, "");
	}
	
	public void onAddFilterClick(View view){
		addNewTextFilter();
	}
	
	private void loadDataForExistingWatch(GunbotDatabase database){
		ProductWatchWidgets widgets = getWatchWidgets();
		GunbotProductWatch watch = database.getProductWatchById(m_watchId);
		
		widgets.nameText.setText(watch.getName());
		widgets.categorySpinner.setSelection(watch.getCategory());
		widgets.inStockCheckbox.setChecked(watch.getMustBeInStock());
		widgets.maxPerRoundText.setText(GunbotUtils.centsToDollarStr(watch.getMaxPricePerRound()));
		widgets.maxPriceText.setText(GunbotUtils.centsToDollarStr(watch.getMaxPrice()));
		
		for (int i = 0; i < watch.getTextFilterCount(); i++){
			GunbotProductWatch.TextFilter filter = watch.getTextFilter(i);
			addNewTextFilter(filter.getFilterType(), filter.getFilterText());
		}
	}
	
	private void saveWatch(){
		ProductWatchWidgets widgets = getWatchWidgets();
		
		GunbotProductWatch productWatch = new GunbotProductWatch(m_watchId, widgets.nameText.getText().toString());
		productWatch.setCategory(widgets.categorySpinner.getSelectedItemPosition());
		productWatch.setMustBeInStock(widgets.inStockCheckbox.isChecked());
		productWatch.setMaxPricePerRound(GunbotUtils.priceToCents(widgets.maxPerRoundText.getText().toString()));
		productWatch.setMaxPrice(GunbotUtils.priceToCents(widgets.maxPriceText.getText().toString()));
		
		for (int i = 0; i < widgets.filterContainer.getChildCount(); i++){
			LinearLayout filterRow = (LinearLayout) widgets.filterContainer.getChildAt(i);
			Spinner filterType = (Spinner) filterRow.getChildAt(0);
			EditText filterText = (EditText) filterRow.getChildAt(2);
			
			String txt = filterText.getText().toString();
			
			if (txt.length() > 0)
				productWatch.addTextFilter(filterType.getSelectedItemPosition(), txt);
		}

		new GunbotDatabase(getApplicationContext()).saveProductWatch(productWatch);
	}
	
	private ProductWatchWidgets getWatchWidgets(){
		ProductWatchWidgets widgets = new ProductWatchWidgets();
		
		widgets.nameText = (EditText) findViewById(R.id.watch_name);
		widgets.categorySpinner = (Spinner) findViewById(R.id.watch_category);
		widgets.inStockCheckbox = (CheckBox) findViewById(R.id.watch_in_stock);
		widgets.maxPerRoundText = (EditText) findViewById(R.id.watch_max_price_per_round);
		widgets.maxPriceText = (EditText) findViewById(R.id.watch_max_price);
		widgets.filterContainer = (LinearLayout)findViewById(R.id.text_filter_container);
		
		return widgets;
	}

	private static class ProductWatchWidgets{
		public EditText nameText;
		public Spinner categorySpinner;
		public CheckBox inStockCheckbox;
		public EditText maxPerRoundText;
		public EditText maxPriceText;
		public LinearLayout filterContainer;
	}
}
