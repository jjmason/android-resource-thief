package jm.rt.fragment;

import jm.rt.R;
import jm.rt.activity.FragmentParams;
import jm.rt.activity.FragmentParamsProvider;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class ResourceListFragment extends SherlockListFragment {
	public static final String INDEX_KEY = "fragmentIndex";

	public interface ListFragmentListener {
		void onListItemClick(int index, int position);
	}

	public interface ListAdapterProvider {
		ListAdapter getListAdapter(int index);
	}

	private int mIndex;
	private ListAdapter mListAdapter;
	private ListFragmentListener mListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_list_fragment, container,
				false);
	}
 
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mIndex = getArguments().getInt(INDEX_KEY); 
		FragmentParams params = ((FragmentParamsProvider)getActivity()).getFragmentParams(mIndex);
		mListAdapter = params.getListAdapter();
		mListener = (ListFragmentListener)getActivity();
		
		((TextView)getView().findViewById(android.R.id.empty)).setText(params.getEmptyText());
		setListAdapter(mListAdapter);
	} 
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mListener.onListItemClick(mIndex, position);
	}
}
