package jm.rt.view;

import jm.rt.res.Resource;
import jm.util.ObjectUtil;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ResourceIconView extends View {
	private Resource mResource;
	private int mMaxHeight 			= -1;
	private int mMaxWidth 			= -1;
	private int mIntrinsicWidth 	= -1;
	private int mIntrinsicHeight 	= -1;
	
	public ResourceIconView(Context context) {
		super(context); 
	}

	public ResourceIconView(Context context, AttributeSet attrs) {
		super(context, attrs); 
	}

	public ResourceIconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle); 
	}

	public void setResource(Resource resource){
		if(ObjectUtil.notEqual(resource, mResource)){
			mResource = resource;
			requestUpdate();
		}
	}
	
	public Resource getResource(){
		return mResource;
	}
	
	protected void updateIntrinsicSize(){
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		updateIntrinsicSize();
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int measuredWidth = getMeasuredSize(mIntrinsicWidth, mMaxWidth, width, widthMode);
		int measuredHeight = getMeasuredSize(mIntrinsicHeight, mMaxHeight, height, heightMode);
		
		setMeasuredDimension(measuredWidth, measuredHeight);
	}
	
	private int getMeasuredSize(int intrinsicSize, 
			int maxSize, 
			int requestedSize,
			int requestedMode){
		 
		switch(requestedMode){
		case MeasureSpec.EXACTLY:
			// do as we're told
			return requestedSize;
		case MeasureSpec.AT_MOST:
			return Math.min(requestedSize, Math.min(intrinsicSize, maxSize));
		case MeasureSpec.UNSPECIFIED:
			return Math.min(intrinsicSize, maxSize);
		default:
			throw new IllegalArgumentException("bad measure spec mode %d" + requestedMode);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	
	}
	
	private void requestUpdate(){
		requestLayout();
		invalidate();
	}
}
