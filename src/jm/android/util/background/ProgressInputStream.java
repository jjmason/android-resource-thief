package jm.android.util.background;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends InputStream {
	private static final int UPDATE_INTERVAL = 2048;
	
	private final int mLength;
	private final ProgressTarget mProgressTarget;
	private final InputStream mInputStream;
	
	private int mLastUpdate;
	private int mBytesRead;
	
	public ProgressInputStream(InputStream inputStream, int length,
			ProgressTarget progressTarget) {
		if(inputStream instanceof BufferedInputStream){
			mInputStream = inputStream;
		}else{
			mInputStream = new BufferedInputStream(inputStream);
		} 
		mLength = length;
		mProgressTarget = progressTarget == null ? ProgressTarget.Factory
				.createDummyTarget() : progressTarget;
		mProgressTarget.setMax(mLength);
	}

	@Override
	public void close() throws IOException { 
		mInputStream.close();
	}
	
	@Override
	public int available() throws IOException {
		return mLength;
	}
	
	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int n = mInputStream.read(buffer, offset, length);
		onBytesRead(n);
		return n;
	}
	
	@Override
	public int read() throws IOException {
		int b = mInputStream.read();
		if(b != -1)
			onBytesRead(1);
		return b;
	}
	
	private void onBytesRead(int numBytesRead){
		mBytesRead += numBytesRead;
		if(mBytesRead - mLastUpdate >= UPDATE_INTERVAL){
			mLastUpdate = mBytesRead;
			mProgressTarget.setProgress(mBytesRead);
		}
	}

}
