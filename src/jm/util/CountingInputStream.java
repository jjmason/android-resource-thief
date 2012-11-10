package jm.util;

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends InputStream {
	private final InputStream mIn;
	private int mCount;
	
	public CountingInputStream(InputStream in){
		mIn = in;
	}
	
	public int count(){
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
		afterRead(n);
		return n;
	}
	
	@Override
	public int read() throws IOException {
		int r = mIn.read();
		if(r != -1)
			afterRead(1);
		return r;
	}

	protected void afterRead(int bytesRead){
		mCount += bytesRead;
	}
}
