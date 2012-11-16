package com.strangerobot.resourcethief.app.detail;

import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.strangerobot.resourcethief.R;
import com.strangerobot.resourcethief.app.SmartListAdapter;
import com.strangerobot.resourcethief.res.ResSpec;
import com.strangerobot.resourcethief.res.ResTable;
import com.strangerobot.resourcethief.res.ResType;
import com.strangerobot.resourcethief.util.ViewAssistant;

public class DrawablesAndColors extends ListFragmentBase {

	private static final String TAG = "DrawablesAndColors";

	private ResourceListParent mResourceListParent;
	private MyListAdapter mListAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "attach");
		if (!(activity instanceof ResourceListParent)) {
			throw new IllegalStateException(
					"parent activity must implement ResourceListParent");
		}
		mResourceListParent = (ResourceListParent) activity;
		Futures.addCallback(mResourceListParent.getResTable(),
				new FutureCallback<ResTable>() {
					@Override
					public void onSuccess(ResTable result) {
						setResTable(result);
					}

					@Override
					public void onFailure(Throwable t) {/* unused */
					}
				});

		mListAdapter = new MyListAdapter();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(TAG, "detach");
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (mListAdapter == null) {
			Log.w(TAG, "mListAdapter was null in onViewCreated!");
			mListAdapter = new MyListAdapter();
		}
		setListAdapter(mListAdapter);
	}

	private void setResTable(ResTable table) {
		// we're probably already on a background thread, but
		// we'll start a new one anyway just to be sure.
		new ItemBuilderTask().execute(table);
	}

	private ResourceListParent getResourceListParent() {
		return mResourceListParent;
	}

	public static final Page PAGE = new Page() {

		@Override
		public CharSequence getTitle(Context context) {
			return context.getText(R.string.drawables_and_colors);
		}

		@Override
		public ListFragmentBase createFragment(Context context,
				FragmentManager fm) {
			return new DrawablesAndColors();
		}
	};

	private static class ItemBuilderProgress {
		public static final int TYPE_ITEMS = 0;
		public static final int TYPE_LOADED = 1;
		public static final int TYPE_ERROR = 2;
		public int type;
		public List<Item> items;
		public Item item;
		public Drawable drawable;
		public Throwable error;

		public ItemBuilderProgress(List<Item> items) {
			this.items = items;
			type = TYPE_ITEMS;
		}

		public ItemBuilderProgress(Item item, Drawable drawable) {
			this.item = item;
			this.drawable = drawable;
			type = TYPE_LOADED;
		}

		public ItemBuilderProgress(Item item, Throwable error) {
			this.item = item;
			this.error = error;
			type = TYPE_ERROR;
		}
	}

	private class ItemBuilderTask extends
			AsyncTask<ResTable, ItemBuilderProgress, Void> {

		@Override
		protected Void doInBackground(ResTable... tables) {
			ResTable table = tables[0];

			List<Item> items = Lists.newArrayList();
			for (ResType type : table.getTypes()) {
				if (!"color".equals(type.getName())
						&& !"drawable".equals(type.getName())) {
					continue;
				}
				for (ResSpec spec : type.getSpecs()) {
					Item item = new Item();
					item.spec = spec;
					item.state = ItemState.LOADING;
					items.add(item);
				}
			}

			publishProgress(new ItemBuilderProgress(items));

			Resources res = table.getResources();

			for (Item item : items) {
				ResSpec spec = item.spec;
				Drawable drawable = null;
				Throwable error = null;
				try {
					drawable = loadDrawable(res, spec);
				} catch (Throwable e) {
					error = e;
				}
				if (drawable != null) {
					publishProgress(new ItemBuilderProgress(item, drawable));
				} else if (error != null) {
					publishProgress(new ItemBuilderProgress(item, error));
				}
			}

			return null;
		}

		private Drawable loadDrawable(Resources res, ResSpec spec) {
			if ("color".equals(spec.getType().getName())) {
				return new ColorDrawable(res.getColor(spec.getId()));
			}
			return res.getDrawable(spec.getId());
		}

		@Override
		protected void onProgressUpdate(ItemBuilderProgress... values) {
			for (ItemBuilderProgress p : values) {
				switch (p.type) {
				case ItemBuilderProgress.TYPE_ITEMS:
					mListAdapter.setItems(p.items);
					break;
				case ItemBuilderProgress.TYPE_LOADED:
					p.item.drawable = p.drawable;
					p.item.state = ItemState.LOADED;
					mListAdapter.notifyDataSetChanged();
					break;
				case ItemBuilderProgress.TYPE_ERROR:
					p.item.state = ItemState.ERROR;
					mListAdapter.notifyDataSetChanged();
					break;
				}
			}
		}

	}

	private static enum ItemState {
		LOADING, LOADED, ERROR
	}

	private static class Span {
		public final int start;
		public final int end;

		public Span(int start, int end) {
			this.start = start;
			this.end = end;
		}
 
	}

	private static class Constraint {
		public String query;
		public boolean colors;
		public boolean drawables;
	}

	private static enum Order {
		NameAsc, NameDesc, TypeAsc, TypeDesc;
		public final Ordering<Item> ordering() {
			Ordering<Item> o;
			if (this == NameAsc || this == NameDesc) {
				o = Ordering.from(new Comparator<Item>() {
					public int compare(Item lhs, Item rhs) {
						return lhs.spec.getName().compareToIgnoreCase(
								rhs.spec.getName());
					}
				});
			} else {
				o = Ordering.from(new Comparator<Item>() {
					public int compare(Item lhs, Item rhs) {
						int c = lhs.spec.getType().getName()
								.compareTo(rhs.spec.getType().getName());
						if (c == 0) {
							return lhs.drawable
									.getClass()
									.getSimpleName()
									.compareTo(
											rhs.drawable.getClass()
													.getSimpleName());
						}
						return c;
					}
				});
			}
			if (this == NameDesc || this == TypeDesc) {
				return o.reverse();
			} else {
				return o;
			}
		}
	}

	private class CheckListener implements OnCheckedChangeListener {
		private final ResSpec mSpec;

		public CheckListener(ResSpec spec) {
			mSpec = spec;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (getResourceListParent() != null) {
				getResourceListParent().setSpecSelected(mSpec, isChecked);
			}
		}

	}

	private class MyListAdapter extends
			SmartListAdapter<Item, Constraint, Order> {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.generic_list_item, null);
			}
			ViewAssistant v = new ViewAssistant(getActivity(), convertView);
			Item item = getItem(position);
			CharSequence text = item.spec.getName();
			if(item.matchedText != null){
				Spannable sp = new SpannableString(text);
				sp.setSpan(new ForegroundColorSpan(
						getResources().getColor(R.color.matched_text_forground)), 
						item.matchedText.start, 
						item.matchedText.end, 0);
				sp.setSpan(new BackgroundColorSpan(
						getResources().getColor(R.color.matched_text_background)), 
						item.matchedText.start, 
						item.matchedText.end, 0);
				text = sp;
			}
			
			v.setText(R.id.text1, text);
			
			Drawable drawable;
			ItemState state;
			synchronized (item) {
				state = item.state;
				drawable = item.drawable;
			}
			if (state == ItemState.LOADING) {
				v.show(R.id.progress)
						.hide(R.id.image)
						.setText(R.id.text2, getText(R.string.loading_drawable));
			} else {
				if (state == ItemState.ERROR) {
					drawable = getActivity().getResources().getDrawable(
							R.drawable.error_loading_drawable);
					v.setText(R.id.text2, R.string.error_loading_drawable);
				} else {
					v.setText(R.id.text2, drawable.getClass().getSimpleName());
				}
				v.show(R.id.image).hide(R.id.progress)
						.setImageDrawable(R.id.image, drawable);
			}

			CheckBox checkBox = v.findCheckBox(R.id.checkBox);
			// don't fire a previous listener
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(getResourceListParent().isSpecSelected(
					item.spec));
			// if you don't do this list items aren't clickable
			checkBox.setFocusable(false);
			checkBox.setOnCheckedChangeListener(new CheckListener(item.spec));
			return convertView;
		}

		@Override
		protected List<Item> performSort(List<Item> unsorted, Order order) {
			return order.ordering().sortedCopy(unsorted);
		}

		@Override
		protected List<Item> performFilter(List<Item> unfiltered,
				Constraint constraint) {
			List<Item> result = Lists.newArrayList();
			for (Item item : unfiltered) {
				if (constraint.query != null
						&& !matchQuery(item, constraint.query))
					continue;
				if (!constraint.colors
						&& item.spec.getType().getName().equals("color"))
					continue;
				if (!constraint.drawables
						&& item.spec.getType().getName().equals("drawable"))
					continue;
				result.add(item);
			}
			return result;
		}
	}

	private static boolean matchQuery(Item item, String query) {
		int offset = item.spec.getName().toLowerCase()
				.indexOf(query.toLowerCase());
		if (offset >= 0) {

		}
		return true; // TODO matchQuery1
	}
}
