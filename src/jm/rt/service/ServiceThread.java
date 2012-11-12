package jm.rt.service;

import java.util.concurrent.CountDownLatch;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

abstract class ServiceThread extends Thread {
	private static final String TAG = "ServiceWorkerThread";
	
	private Handler mHandler;
	
	// prevent start from being called except from
	// startAndWait
	private boolean mCanCallStart = false;
	
	private final CountDownLatch mReadyLatch = 
			new CountDownLatch(1);
	
	public ServiceThread(String name) {
		super(name);
	}

	/**
	 * Starts this thread and blocks until it is fully initialized
	 * and ready to receive messages.
	 */
	public void startAndWait(){
		mCanCallStart = true;
		start();
        while (true) {
            try {
                mReadyLatch.await();
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
	}
	
	/**
	 * Calling this method directly will throw an exception. 
	 * Use {@link #startAndWait} instead.
	 */
	@Override
	public final synchronized void start() {
		if(!mCanCallStart){
			throw new IllegalStateException("you cannot call start() directly on " +
					"this thread -- call startAndWait instead.");
		}
		super.start();
	}
	
	@Override
	public final void run(){
		try{
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			initialize();
			Looper.prepare();
			mHandler = new MessageHandler();
		}finally{
			// don't deadlock
			mReadyLatch.countDown();
		}
		Looper.loop();
	}
	
	/**
	 * Sends a message to this threads handler.
	 */
	protected void sendMessage(Message msg){
		mHandler.sendMessage(msg);
	}
	
	/**
	 * Handle a message from another thread.
	 */
	protected abstract void handleMessage(Message msg);
	
	/**
	 * This is called when the thread starts, and before any
	 * calls to {@link #handleMessage(Message)}
	 */
	protected void initialize(){
		
	}
	
	/**
     * Create a message targeting our handler.
	 */
	protected  Message obtainMessage(int what){
		return Message.obtain(mHandler, what);
	}
	
	protected Message obtainMessage(){
		return Message.obtain(mHandler);
	}
	
	@SuppressLint("HandlerLeak")
	private class MessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			ServiceThread.this.handleMessage(msg);
		}
	}
}
