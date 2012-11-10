package jm.util;

import android.os.AsyncTask;

public abstract class Task<Result> {
	public abstract Result run() throws Exception;
	public Future<Result> execute(){
		final Future<Result> future = new Future<Result>();
		final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try{
					future.succeed(run());
				}catch(Exception e){
					future.fail(e);
				}
				return null;
			}
		};
		task.execute();
		return future;
	}
	
}
