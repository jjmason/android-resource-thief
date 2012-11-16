package com.strangerobot.resourcethief.app.detail;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.strangerobot.resourcethief.R;
import com.strangerobot.resourcethief.res.ResSpec;
import com.strangerobot.resourcethief.util.ViewAssistant;

public class ResourceListAdapter extends BaseAdapter {
	private final Context mContext;
	private List<ResourceListItem> mItems;
	private SelectionModel<ResSpec> mSelectionModel;

	public ResourceListAdapter(Context context) {
		mContext = context;
	}

	public void setSelectionModel(SelectionModel<ResSpec> selectionModel) {
		mSelectionModel = selectionModel;
		notifyDataSetChanged();
	}

	public SelectionModel<ResSpec> getSelectionModel() {
		return mSelectionModel;
	}

	public List<ResourceListItem> getItems() {
		if (mItems == null) {
			mItems = Collections.emptyList();
		}
		return mItems;
	}

	public void setItems(List<ResourceListItem> items) {
		if (items == null) {
			items = Collections.emptyList();
		}
		mItems = items;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mItems == null) {
			return 0;
		}
		return mItems.size();
	}

	@Override
	public ResourceListItem getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getSpec().getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = inflate(R.layout.generic_list_item);
		}
		ResourceListItem item = getItem(position);
		ViewAssistant v = new ViewAssistant(mContext, convertView)
			.setText(R.id.text1, item.getSpec().getName())
			.setText(R.id.text2, getText2(item))
			.setImageDrawable(R.id.image, getDrawable(item));
		
		CheckBox cb = (CheckBox) v.find(R.id.checkBox);
		cb.setOnCheckedChangeListener(null);
		cb.setFocusable(false); // if true, list items aren't clickable
		if(mSelectionModel != null){
			cb.setChecked(mSelectionModel.isItemSelected(item.getSpec()));
		}else{
			cb.setChecked(false);
		}
		
		final ResSpec fResSpec = item.getSpec();
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(mSelectionModel != null){
					mSelectionModel.setItemSelected(fResSpec, isChecked);
				}
			}
		});
		
		return convertView;
	}
	
	public Drawable getDrawable(ResourceListItem item){
		if(item.isError() && item.getDrawable() == null){
			return mContext.getResources().getDrawable(R.drawable.error_loading_drawable);
		}
		return item.getDrawable();
	}
	
	public Drawable getErrorImage(){
		return mContext.getResources().getDrawable(R.drawable.error_loading_drawable);
	}
	
	public CharSequence getText2(ResourceListItem item){
		if(item.isError()){
			return mContext.getText(R.string.error_loading_drawable);
		}
		return item.getDrawable().getClass().getSimpleName();
	}

	public View inflate(int layoutId) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(layoutId, null);
	}

}
