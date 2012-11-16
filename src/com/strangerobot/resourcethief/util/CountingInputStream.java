package com.strangerobot.resourcethief.util;

import java.io.IOException;
import java.io.InputStream;

import com.strangerobot.resourcethief.util.CountingInputStream.OnInputProgressListener;

/**
 * <p>An {@link InputStream} wrapper that counts the number 
 * of bytes read and optionally notifies an observer of
 * input progress.</p>
 */
public class CountingInputStream extends InputStream {
	public static final int DEFAULT_UPDATE_INTERVAL = 1024;
	
	public interface OnInputProgressListener {
		void onInputProgress(int bytesRead, int bytesTotal);
	}
	
	private final InputStream mIn;
	private final int mLength;
	private final int mUpdateInterval;
	private final OnInputProgressListener mListener;
	
	private int mLastUpdate;
	private int mCount;
	
	/**
	 * Create a wrapper for an {@link InputStream}, without 
	 * an observer for progress.
	 * @param in the input stream
	 */
	public CountingInputStream(InputStream in){
		this(in, -1, null, -1);
	}
	
	/**
	 * Create a wrapper for an {@link InputStream} that
	 * will notify the given {@link OnInputProgressListener}
	 * every {@link CountingInputStream#DEFAULT_UPDATE_INTERVAL} bytes.
	 * 
	 * @param in the input stream to wrap
	 * @param length the length of the stream in bytes, which is required to
	 * 				 pass to the listener.
	 * @param progressListener the listener to notify every time the specified number
	 * 						   of bytes have been read.
	 */
	public CountingInputStream(InputStream in, int length, OnInputProgressListener progressListener){
		this(in, length, progressListener, DEFAULT_UPDATE_INTERVAL);
	}

	/**
	 * Create a wrapper for an {@link InputStream} that
	 * will notify the given {@link OnInputProgressListener}
	 * every <code>updateInterval</code> bytes.
	 * 
	 * @param in the input stream to wrap
	 * @param length the length of the stream in bytes, which is required to
	 * 				 pass to the listener.
	 * @param progressListener the listener to notify every time the specified number
	 * 						   of bytes have been read.
	 * @param updateInterval the minimum number of bytes to read between progress updates. 
	 * 						 
	 */
	public CountingInputStream(InputStream in, int length, 
			OnInputProgressListener progressListener, int updateInterval){
		mIn = Args.notNull(in);
		if(progressListener != null && length < 0){
			throw new IllegalArgumentException(
					"length must be provided if progressListener is not null");
		}
		mLength = length;
		mListener = progressListener;
		mUpdateInterval = updateInterval < 0 ? DEFAULT_UPDATE_INTERVAL : updateInterval;
	}
	
	public CountingInputStream(InputStream rawInput, long size,
			OnInputProgressListener onInputProgressListener,
			int inputUpdateInterval) {
		this(rawInput, (int)size, onInputProgressListener, inputUpdateInterval);
	}

	/**
	 * The length of the stream in bytes.  Only valid if this object was created with the
	 * constructor
	 * {@link CountingInputStream#CountingInputStream(InputStream, int, OnInputProgressListener)}
	 * or 
	 * {@link CountingInputStream#CountingInputStream(InputStream, int, OnInputProgressListener, int)}.
	 * @return
	 */
	public int getLength(){
		return mLength;
	}
	
	/**
	 * Get the number of bytes read so far
	 */
	public int getCount(){
		return mCount;
	} 
	
	@Override
	public void close() throws IOException {
		mIn.close();
	}
	
	@Override
	public long skip(long byteCount) throws IOException {
		long skipped = mIn.skip(byteCount);
		afterRead((int) skipped);
		return skipped;
	}
	
	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int n = mIn.read(buffer, offset, length);
		if(n > 0){
			afterRead(n);
		}
		return n;
	} 
	
	@Override
	public int read() throws IOException {
		int c = mIn.read();
		if(c > -1){
			afterRead(1);
		}
		return c;
	}  
	private void afterRead(int bytesRead){
		mCount += bytesRead;
		if(mListener != null && mCount - mLastUpdate > mUpdateInterval){
			mListener.onInputProgress(mCount, mLength);
			mLastUpdate = mCount;
		}
	} 
}
