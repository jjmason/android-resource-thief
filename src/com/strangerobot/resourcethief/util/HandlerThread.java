package com.strangerobot.resourcethief.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public abstract class HandlerThread extends Thread {
	private static final int PRIVATE_MESSAGE_MASK = 0x0F;
	private static final int PUBLIC_MESSAGE_SHIFT = 4;

	private static final int QUIT = 1;

	private final CountDownLatch mInitializedLatch = new CountDownLatch(1);

	private Handler mHandler;

	private class HandlerThreadHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			int privateWhat = msg.what & PRIVATE_MESSAGE_MASK;
			try {
				if (privateWhat != 0) {
					switch (privateWhat) {
					case QUIT:
						Log.i("HandlerThread", "QUITING");
						Looper.myLooper().quit();
						break;
					}
				} else {
					msg.what = (msg.what >> PUBLIC_MESSAGE_SHIFT);
					HandlerThread.this.handleMessage(msg);
				}
			} finally {
				msg.recycle();
			}
		}
	}

	protected abstract void handleMessage(Message msg);

	protected void initialize() {

	}

	public void sendQuit(){
		mHandler.sendMessage(Message.obtain(mHandler, QUIT));
	}
	
	public void sendMessage(int what, Object obj) {
		what = 0xFFFFFFF0 & (what << 4);
		Message m = Message.obtain(mHandler, what, obj);
		mHandler.sendMessage(m);
	}

	public final void run() {
		try {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			Looper.prepare();
			mHandler = new HandlerThreadHandler();
			initialize();
		} finally {
			mInitializedLatch.countDown();
		}
		Looper.loop();
	}

	public final synchronized void start() {
		super.start();
		try {
			mInitializedLatch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
