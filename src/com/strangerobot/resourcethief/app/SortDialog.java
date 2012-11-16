package com.strangerobot.resourcethief.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.strangerobot.resourcethief.R;

public class SortDialog extends AlertDialog {
	public interface OnSortSelectedListener {
		void onSortSelected(int field, int order);
	}
	
	private int mField;
	private int mOrder;
	private String[] mFields;
	private Button[] mButtons;
	private OnSortSelectedListener mListener;
	
	public SortDialog(Context context, String[] fields, int field, int order, OnSortSelectedListener listener) {
		super(context);
		mField = field;
		mFields = fields;
		mOrder = order;
		mListener = listener;
		setButton(BUTTON_POSITIVE, "OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(mListener != null){
					mListener.onSortSelected(mField, mOrder);
				}
				dismiss();
			}
		});
		setButton(BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		setView(createView());
		updateButtons();
	}

	public int getField(){
		return mField;
	}
	
	public int getOrder(){
		return mOrder;
	}
	
	public void setField(int field){
		mField = field;
		if(mButtons != null){
			updateButtons();
		}
	}
	
	public void setOrder(int order){
		mOrder = order;
		if(mButtons != null){
			updateButtons();
		}
	}
	
	public View createView() {
		LinearLayout ll = new LinearLayout(getContext());
		ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setDividerDrawable(getContext().getResources().getDrawable(
				R.drawable.divider_horizontal_bright));
		mButtons = new Button[mFields.length];
		for (int i = 0; i < mFields.length; i++) {
			ll.addView(mButtons[i] = createButton(i), getButtonLayoutParams());
		}
		return ll;
	}

	private Button createButton(int index) {
		LayoutInflater inflater =(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Button btn = (Button) inflater.inflate(R.layout.sort_dialog_btn, null);
		btn.setText(mFields[index]);
		btn.setOnClickListener(new ButtonListener(index));
		return btn;
	}

	private LayoutParams getButtonLayoutParams() {
		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		return p;
	}
	
	private void updateButtons(){
		for(int i=0;i<mFields.length;i++){
			Button btn  = mButtons[i];
			int dr = i == mField ? mOrder > 0 ? R.drawable.ic_sort_down : R.drawable.ic_sort_up : 0;
			btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, dr, 0);
			btn.setCompoundDrawablePadding(dr == 0 ? 0 : -30);
		}
	}
	
	private class ButtonListener implements View.OnClickListener {
		private final int mIndex;
		public ButtonListener(int index){
			mIndex = index;
		}
		public void onClick(View v) {
			if(mIndex == mField){
				mOrder = -mOrder;
			}else{
				mField = mIndex;
			}
			updateButtons();
		}
	}
}
