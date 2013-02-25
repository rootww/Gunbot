package com.firearms.gunbot;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GunbotWatchViewActivity extends ListActivity {
	private List<GunbotProductWatch> m_productWatches;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initActionBar();
		
		ListView listView = getListView();
		listView.setLongClickable(true);
		registerForContextMenu(listView);
	}
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id){
		showNewWatchActivity(m_productWatches.get(position).getId());
	}
	
	@Override
	protected void onResume (){
		super.onResume();
		
		GunbotDatabase database = new GunbotDatabase(getApplicationContext());
		m_productWatches = database.getProductWatches();
		getListView().setAdapter(new GunbotProductWatchAdapter(this, R.layout.product_watch_view_row, m_productWatches));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_gunbot_watch_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_new_watch:
			showNewWatchActivity(0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("NewApi")
	private void initActionBar(){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
			getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void showNewWatchActivity(long id){
		Intent intent = new Intent(getApplicationContext(), GunbotNewWatchActivity.class);
		intent.putExtra(GunbotUtils.EXTRA_ID, id);
		startActivity(intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		menu.setHeaderTitle(m_productWatches.get(info.position).getName());
		menu.add(Menu.NONE, info.position, 0, R.string.menu_delete);
	}
	
	private void deleteListItem(int pos){
		GunbotProductWatch watch = m_productWatches.remove(pos);
		new GunbotDatabase(this).deleteProductWatchById(watch.getId());
		
		GunbotProductWatchAdapter adapter = (GunbotProductWatchAdapter) getListView().getAdapter();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		final int id = item.getItemId();
		
		new AlertDialog.Builder(this)
			.setTitle("Confirm Delete")
			.setMessage("Do you really want to delete this item?")
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteListItem(id);
				}
			})
			.setNegativeButton(android.R.string.no, null)
			.show();
		
		return true;
	}
	
	private static class GunbotProductWatchAdapter extends ArrayAdapter<GunbotProductWatch>{
		private Context m_context;
		private int m_layoutResourceId;
		private List<GunbotProductWatch> m_productWatches;
		
		GunbotProductWatchAdapter(Context context, int layoutResourceId, List<GunbotProductWatch> productWatches){
			super(context, layoutResourceId, productWatches);
			
			m_context = context;
			m_layoutResourceId = layoutResourceId;
			m_productWatches = productWatches;
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
			GunbotProductWatch watch = m_productWatches.get(position);
			
			TextView nameText = (TextView)row.findViewById(R.id.watch_row_name);
			TextView detailText = (TextView)row.findViewById(R.id.watch_row_detail);
			
			nameText.setText(watch.getName());
			detailText.setText(buildDetailString(watch));
		}
		
		private String buildDetailString(GunbotProductWatch watch){
			StringBuilder str = new StringBuilder();
			boolean space = false;
			
			if (watch.getMustBeInStock()){
				str.append("Must be in stock");
				space = true;
			}
			
			if (watch.getMaxPricePerRound() > 0){
				if (space)
					str.append("    ");
				
				str.append("max price/rd: ");
				str.append(GunbotUtils.centsToDollarStr(watch.getMaxPricePerRound()));
				space = true;
			}
			else
				space = false;
			
			if (watch.getMaxPrice() > 0){
				if (space)
					str.append("    ");
				
				str.append("max price: ");
				str.append(GunbotUtils.centsToDollarStr(watch.getMaxPrice()));
			}
			return str.toString();
		}
	}

}
