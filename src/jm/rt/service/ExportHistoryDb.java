package jm.rt.service;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class ExportHistoryDb extends SQLiteOpenHelper {
	public static final String NAME = "resource_export_history.db";
	public static final int VERSION = 0;
	public static final String[] COLUMNS = {
		"packageName", "resourceId", "status"
	};
	
	private SQLiteDatabase mDb;
	
	public ExportHistoryDb(Context context) {
		super(context, NAME, null, VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE exports (" +
						"packageName TEXT," +
						"resourceId INTEGER," +
						"status INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE exports");
		onCreate(db);
	}

	public SQLiteDatabase getDb(){
		if(mDb == null){
			mDb = getWritableDatabase();
		}
		return mDb;
	}
	
	public Cursor query(){
		return getDb().query("exports" ,COLUMNS,null,null,null,null,null);
	} 
	
	public void delete(String packageName, int resourceId){
		getDb().delete("exports", "packageName=? AND resourceId=?", 
				new String[]{packageName, String.valueOf(resourceId)});
	}
		
	public void save(String packageName, int resourceId, int status){
		delete(packageName, resourceId);
		ContentValues values = new ContentValues();
		values.put("packageName", packageName);
		values.put("resourceId", resourceId);
		values.put("status", status);
		getDb().insert("exports", null, values);
	}
	
	public List<ExportStatus> load(){
		Cursor c = query();
		ArrayList<ExportStatus> result = new ArrayList<ExportStatus>(c.getCount());
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			String packageName = c.getString(0);
			int resourceId = c.getInt(1);
			int status = c.getInt(2);
			result.add(new ExportStatus(packageName, resourceId, status));
		}
		return result;
	}
}
