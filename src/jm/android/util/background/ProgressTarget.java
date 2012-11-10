package jm.android.util.background;

import android.app.ProgressDialog;
import android.widget.ProgressBar;

/**
 * Displays progress somehow.  Methods are the same as on {@link ProgressDialog}.
 */
public interface ProgressTarget {
	void setProgress(int progress);
	void setMax(int max);
	void setIndeterminate(boolean indeterminate);
	void incrementProgressBy(int diff);
	void setSecondaryProgress(int secondaryProgress);
	void incrementSecondaryProgressBy(int diff);
	void setMessage(CharSequence message); 
	
	public static final class Factory {
		public static ProgressTarget createTarget(final ProgressDialog dialog){
			return new ProgressTarget(){
				@Override
				public void setProgress(int progress) {
					dialog.setProgress(progress);
				}
				@Override
				public void setMax(int max) {
					dialog.setMax(max);
				}
				@Override
				public void setIndeterminate(boolean indeterminate) {
					dialog.setIndeterminate(indeterminate);
				} 
				@Override
				public void incrementProgressBy(int diff) {
					dialog.incrementProgressBy(diff);
				} 
				@Override
				public void setSecondaryProgress(int secondaryProgress) {
					dialog.setSecondaryProgress(secondaryProgress);
				} 
				@Override
				public void incrementSecondaryProgressBy(int diff) {
					dialog.incrementSecondaryProgressBy(diff);
				} 
				@Override
				public void setMessage(CharSequence message) {
					// TODO this has to happen on the ui thread.
				} 
			};
		}
		public static ProgressTarget createTarget(final ProgressBar p){
			return new ProgressTarget() {
				@Override
				public void setSecondaryProgress(int secondaryProgress) {
					p.setSecondaryProgress(secondaryProgress);
				}
				@Override
				public void setProgress(int progress) {
					p.setProgress(progress);
				}
				@Override
				public void setMessage(CharSequence message) {}
				@Override
				public void setMax(int max) {
					p.setMax(max);
				}
				@Override
				public void setIndeterminate(boolean indeterminate) {
					p.setIndeterminate(indeterminate);
				}
				@Override
				public void incrementSecondaryProgressBy(int diff) {
					p.incrementSecondaryProgressBy(diff);
				}
				@Override
				public void incrementProgressBy(int diff) {
					p.incrementProgressBy(diff);
				}
			};
		}
		public static ProgressTarget createDummyTarget(){
			return new ProgressTarget() {
				public void setSecondaryProgress(int secondaryProgress) {}
				public void setProgress(int progress) {}
				public void setMessage(CharSequence message) {}
				public void setMax(int max) {}
				public void setIndeterminate(boolean indeterminate) {}
				public void incrementSecondaryProgressBy(int diff) {}
				public void incrementProgressBy(int diff) {}
			};
		}
	}
}
