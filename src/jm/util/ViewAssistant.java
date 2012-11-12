package jm.util; 
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.*; 

public class ViewAssistant {
	private final View v;
	private final Context c;
	
	public ViewAssistant(Context context, View view){
		v = view;
		c = context;
	}
	
	public ViewAssistant show(int...ids){
		return setVisibility(View.VISIBLE, ids);
	}
	
	public ViewAssistant hide(int...ids){
		return setVisibility(View.GONE, ids);
	}
	
	public View find(int id){
		return v.findViewById(id);
	}
	
	public ViewAssistant setText(int textViewId, CharSequence text){
		View v = find(textViewId);
		if(v != null && v instanceof TextView){
			((TextView)v).setText(text);
		}
		return this;
	}
	
	public ViewAssistant setText(int textViewId, int textResId){
		View v = find(textViewId);
		if(v != null && v instanceof TextView){
			((TextView)v).setText(textResId);
		}
		return this;
	}
	
	public ViewAssistant setTextOrHide(int textViewId, CharSequence text, int...otherIds){
		if(text == null)
			return hide(textViewId).hide(otherIds);
		return show(textViewId).show(otherIds).setText(textViewId, text);
	}
	
	public ViewAssistant setVisibility(int visibility, int...ids){
		for(int id : ids){
			View c = find(id);
			if(c != null){
				c.setVisibility(visibility);
			}
		}
		return this;
	}
	
	public ViewAssistant replace(int viewId, View withView, int index, LayoutParams params){
		View v = find(viewId);
		if(v == null)
			return this;
		ViewParent parent = v.getParent();
		if(parent == null || !(parent instanceof ViewGroup)){
			Log.wtf("ViewAssistant", "no parent to replace child in??");
			return this;
		} 
		ViewGroup group = (ViewGroup) parent;
		group.removeView(v);
		return add(group, withView, index, params);
	}
	
	public ViewAssistant replace(int viewId, View withView, int index){
		return replace(viewId, withView, index, null);
	}
	
	public ViewAssistant replace(int viewId, View withView, LayoutParams params){
		return replace(viewId, withView, -1,params);
	}
	
	public ViewAssistant replace(int viewId, View withView){
		return replace(viewId, withView,-1,null);
	}
	
	public ViewAssistant add(ViewGroup parent, View child, int index, LayoutParams params){
		if(index > 0){
			if(params != null){
				parent.addView(child,index,params);
			}else{
				parent.addView(child, index);
			}
		}else{
			if(params != null){
				parent.addView(child, params);
			}else{
				parent.addView(child);
			}
		}
		return this;
	}
	

	public ViewAssistant add(ViewGroup parent, View withView, int index){
		return add(parent, withView, index, null);
	}
	
	public ViewAssistant add(ViewGroup parent, View withView, LayoutParams params){
		return add(parent, withView, -1,params);
	}
	
	public ViewAssistant add(ViewGroup parent, View withView){
		return add(parent, withView,-1,null);
	}
	
	public ViewAssistant add(int parentId, View child, int index, LayoutParams params){
		View pv = find(parentId);
		if(pv != null && pv instanceof ViewGroup){
			return add((ViewGroup)pv, child, index, params);
		}
		return this;
	}
	
	public ViewAssistant add(int parent, View withView, int index){
		return add(parent, withView, index, null);
	}
	
	public ViewAssistant add(int parent, View withView, LayoutParams params){
		return add(parent, withView, -1,params);
	}
	
	public ViewAssistant add(int parent, View withView){
		return add(parent, withView,-1,null);
	}
	
	public ViewAssistant remove(View...views){
		for(View v : views){
			if(v == null)
				continue;
			ViewParent parent = v.getParent();
			if(parent != null && parent instanceof ViewGroup){
				((ViewGroup)parent).removeView(v);
			}
		}
		return this;
	}
	
	public ViewAssistant remove(int...viewIds){
		View[] views = new View[viewIds.length];
		for(int i=0;i<viewIds.length;i++){
			views[i] = find(viewIds[i]);
		}
		return remove(views);
	}
	
	public ViewAssistant removeChildren(ViewGroup parent){
		if(parent == null)
			return this;
		parent.removeAllViews();
		return this;
	}
	
	public ViewAssistant removeChildren(int parent){
		View v = find(parent);
		if(v != null && v instanceof ViewGroup){
			return removeChildren((ViewGroup) v);
		}
		return this;
	}
	
	public ViewAssistant replaceChildren(ViewGroup parent, View child, LayoutParams params) {
		return removeChildren(parent).add(parent, child,  params);
	}
	
	public ViewAssistant replaceChildren(int parent, View child, LayoutParams params){
		View v = find(parent);
		if(v != null && v instanceof ViewGroup)
			replaceChildren((ViewGroup)v, child, params);
		return this;
	}
	
	public ViewAssistant replaceChildren(ViewGroup parent, View child){
		return replaceChildren(parent, child, null);
	}
	
	public ViewAssistant replaceChildren(int parent, View child){
		return replaceChildren(parent, child, null);
	}
	
	public ViewAssistant setImageDrawable(int viewId, Drawable drawable){
		View v = find(viewId);
		if(v instanceof ImageView){
			setImageDrawable((ImageView)v, drawable);
		}
		return this;
	}
	
	public ViewAssistant setImageDrawable(ImageView view, Drawable drawable){
		view.setImageDrawable(drawable);
		return this;
	}
	
	public ViewAssistant setImageDrawable(int viewId, int drawableResId){
		return setImageDrawable(viewId, c.getResources().getDrawable(drawableResId));
	}
	
	public ViewAssistant setImageDrawable(ImageView view, int drawableResId){
		view.setImageDrawable(c.getResources().getDrawable(drawableResId));
		return this;
	}
}
