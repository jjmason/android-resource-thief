package jm.util;

import java.util.HashSet;

import jm.util.Tuple.Tuple2;
import android.os.Handler;

public class Future<Result> {
	public interface ErrorCallback {
		void error(Exception e);
	}

	public interface ResultCallback<Result> {
		void result(Result result);
	}

	private final Handler mHandler;
	private Result mResult;
	private boolean mReady;
	private Exception mError;

	private final HashSet<Tuple2<ErrorCallback, ResultCallback<Result>>> mRequests = new HashSet<Tuple.Tuple2<ErrorCallback, ResultCallback<Result>>>();

	public Future(Handler handler) {
		mHandler = Args.notNull(handler);
	}

	public Future() {
		this(new Handler());
	}
 
	public boolean ready(){
		return mReady;
	}

	public boolean succeeded(){
		return ready() && mError == null;
	}
	
	public boolean failed(){
		return ready() && mError != null;
	}
	
	public Result confidentlyGet(){
		try{
			return get();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public Result get() throws Exception {
		if(failed())
			throw mError;
		return mResult;
	}
	
	public void request(ResultCallback<Result> result, ErrorCallback error){
		synchronized(this){
			if(!ready()){
				mRequests.add(new Tuple2<ErrorCallback, ResultCallback<Result>>(error,result));
				return;
			}
		}
		fulfill(result, error);
	}
	
	public void request(ResultCallback<Result> result){
		request(result, null);
	}
	
	public void request(ErrorCallback error){
		request(null, error);
	}
	
	public synchronized void succeed(Result result) {
		if(ready())
			throw new IllegalStateException("result or error already set");
		mResult = result;
		mReady = true;
		mHandler.post(mProcessRequests);
	}
	
	public synchronized void fail(Exception ex){
		if(ready())
			throw new IllegalStateException("result or error already set");
		mError = ex;
		mReady = true;
		mHandler.post(mProcessRequests);
	}

	private void fulfill(ResultCallback<Result> rc, ErrorCallback ec){
		if(mError != null && ec != null){
			ec.error(mError);
		}else if(rc != null){
			rc.result(mResult);
		}
	}
	
	private final Runnable mProcessRequests = new Runnable() {
		public void run() {
			try {
				for (Tuple2<ErrorCallback, ResultCallback<Result>> cbs : mRequests) {
					fulfill(cbs.second, cbs.first);
				}
			} finally {
				mRequests.clear();
			}
		}
	};
}
