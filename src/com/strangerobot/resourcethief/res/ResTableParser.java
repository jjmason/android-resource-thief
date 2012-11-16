package com.strangerobot.resourcethief.res;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.util.Log;

import com.google.common.base.Preconditions;
import com.mindprod.ledatastream.LEDataInputStream;
import com.strangerobot.resourcethief.util.CountingInputStream;
import com.strangerobot.resourcethief.util.CountingInputStream.OnInputProgressListener;

/**
 * <p>
 * Parses resource (.arsc) table files. This is a very limited parser -- in the
 * interest of simplicity and performance we only gather information that we
 * absolutely can't get from the {@link Resource} object provided at runtime.
 * Specifically, we need 1) a list of resource ids, and 2) a list of resource
 * configurations for each id.
 * </p>
 * 
 * <p>
 * The details of the resource table format are available in
 * android_frameworks_base/include/androidfw/ResourceTypes.h.
 * </p>
 */
public class ResTableParser {
	// our exception
	public static class ResTableParserException extends IOException {
		private static final long serialVersionUID = 1L;

		public ResTableParserException() {
			super();
		}

		public ResTableParserException(String detailMessage) {
			super(detailMessage);
		}

		public ResTableParserException(String detailMessage, Throwable cause) {
			super(detailMessage, cause);
		}

		public ResTableParserException(Throwable cause) {
			super(cause);
		}
	}
	
	// handles parsing events
	public interface Handler {
		void start();
		void complete();
		void resource(int id, int configOffset);
		void progress(int progress, int total);
	}

	public static abstract class DefaultHandler implements Handler {
		@Override
		public void start() {}
		@Override
		public void complete() {}
		@Override
		public void resource(int id, int configOffset) {}
		@Override
		public void progress(int progress, int total) {}
	}
	
	// chunk types
	private static final short EOF_TYPE = -1, STRING_POOL_TYPE = 0x0001,
			TABLE_TYPE = 0x0002, PACKAGE_TYPE = 0x0200, TYPE_TYPE = 0x0202,
			TYPE_SPEC_TYPE = 0x0201;

	private static final boolean DBG = false;// BuildConfig.DEBUG;

	private InputStream mRawInput;
	private CountingInputStream mCounting;
	private DataInput mIn;

	private int mChunkType;
	private int mChunkSize;
	private int mChunkStart;

	private int mTypeId;
	private int mPackageId;
	private int mConfigOffset;

	private String mApkPath;

	private Handler mHandler;
	
	private int mInputUpdateInterval = 1024;
	
	private final OnInputProgressListener mOnInputProgressListener = new OnInputProgressListener() {
		
		@Override
		public void onInputProgress(int bytesRead, int bytesTotal) {
			mHandler.progress(bytesRead, bytesTotal);
		}
	};
	
	public ResTableParser(Handler handler){
		setHandler(handler);
	}
	
	public void setHandler(Handler handler){
		mHandler = Preconditions.checkNotNull(handler);
	}
	
	public void setInputUpdateInterval(int inputUpdateInterval){
		Preconditions.checkArgument(inputUpdateInterval > 0);
		mInputUpdateInterval = inputUpdateInterval;
	}
	
	public final void parse(String apkPath) throws ResTableParserException {
		reset();
		mApkPath = apkPath;
		try {
			parse();
		} catch (IOException e) {
			throw new ResTableParserException(e);
		} finally {
			cleanup();
		}
	}
	 
	private void parse() throws IOException, ResTableParserException {
		ZipFile zip = new ZipFile(mApkPath);
		ZipEntry entry = zip.getEntry("resources.arsc");
		if (entry == null) {
			throw new ResTableParserException(
					"missing resources.arsc! are you sure this is an apk file? "
							+ mApkPath);
		}
		mRawInput = new BufferedInputStream(zip.getInputStream(entry));
		mCounting = new CountingInputStream(mRawInput, 
				entry.getSize(), 
				mOnInputProgressListener, 
				mInputUpdateInterval);
		mIn = new LEDataInputStream(mCounting);
		mHandler.start();
		parseTable();
		mHandler.complete();
	}

	private void cleanup() {
		if (mRawInput != null) {
			try {
				mRawInput.close();
			} catch (IOException e) {
				Log.w(TAG, "error closing input", e);
			}
		}
		mRawInput = null;
		mIn = null;
		mCounting = null;
	}

	private void reset() {
		if (mRawInput != null) {
			try {
				mRawInput.close();
			} catch (IOException e) {
				Log.w(TAG, "error closing input stream", e);
			}
		}
		mRawInput = null;
		mIn = null;
		mCounting = null;
		mApkPath = null;
		mChunkType = mChunkSize = mChunkStart = mTypeId = mPackageId = mConfigOffset = 0;
	}

	private void parseTable() throws IOException {
		parseHeaderRequireType(TABLE_TYPE);
		skipInt(); // package count
		skipChunkRequireType(STRING_POOL_TYPE); // main strings table

		while(parseHeader() == PACKAGE_TYPE){
			parsePackage();
		}
	}

	private void parsePackage() throws IOException {
		mPackageId = (byte) mIn.readInt();
		mIn.skipBytes(272); // fixed size portion of the package header
		
		// skip two string chunks, types and keys
		skipChunkRequireType(STRING_POOL_TYPE);
		skipChunkRequireType(STRING_POOL_TYPE);

		while(parseHeader() == TYPE_TYPE){
			parseType();
		}
	}

	private void skipChunkRequireType(int type) throws IOException {
		parseHeaderRequireType(type);
		endChunk();
	}
	
	private void endChunk() throws IOException {
		int current = mCounting.getCount();
		int end = mChunkStart + mChunkSize;
		if (end < current) {
			throw new IOException(
					"tried to end chunk, but already past the end!");
		}
		mIn.skipBytes(end - current);
	}

	private void parseType() throws IOException {

		mTypeId = mIn.readByte();
		mIn.skipBytes(3);
		int count = mIn.readInt();
		mIn.skipBytes(4 * count);
		// XXX not sure if we can trust entry count?
		while (parseHeader() == TYPE_SPEC_TYPE) {
			skipInt();
			count = mIn.readInt();
			skipInt();
			mConfigOffset = mCounting.getCount();
			endChunk();
			for(int entryId=0; entryId < count; entryId ++){
				int id = ( ( mPackageId & 0xFF ) << 24 ) | 
						 ( ( mTypeId & 0xFF ) << 16 ) | 
						 ( ( entryId & 0xFFFF ) );
				mHandler.resource(id, mConfigOffset);
			}
		}
	}

	private int parseHeader() throws IOException {
		mChunkStart = mCounting.getCount();
		try {
			mChunkType = mIn.readShort();
		} catch (EOFException e) {
			mChunkType = EOF_TYPE;
			mChunkSize = 0;
			return mChunkType;
		}
		// next 2 bytes are always 8 (the header size)
		mIn.skipBytes(2);
		mChunkSize = mIn.readInt();
		return mChunkType;
	}

	private void dbgChunk(String what) {
		dbg("%s type=0x%04X size=0x%08X start=0x%08X end=0x%08X", what,
				mChunkType, mChunkSize, mChunkStart, mChunkStart + mChunkSize);
	}

	private void skipInt() throws IOException {
		mIn.skipBytes(4);
	}

	private void parseHeaderRequireType(int type) throws IOException {
		if (type != parseHeader())
			throw new IOException(String.format(
					"invalid header type 0x%04X (expected 0x%04X)", mChunkType,
					type));
	}

	private static final String TAG = "ResTableParser";

	private void warn(String message, Object... args) {
		Log.w(TAG, String.format(message, args));
	}

	private void dbg(String format, Object... args) {
		if (!DBG)
			return;
		String s = String.format("RTP@0x%08X: %s", mCounting.getCount(),
				String.format(format, args));
		Log.d(TAG, s);
	}
}
