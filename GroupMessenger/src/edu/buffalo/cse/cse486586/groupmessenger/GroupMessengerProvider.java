package edu.buffalo.cse.cse486586.groupmessenger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

	private myDatabase database;
	private SQLiteDatabase sqlDb;
	private static final String AUTHORITY = "edu.buffalo.cse.cse486586.groupmessenger.provider";
	private static final String BASE_PATH = myDatabase.TABLE_NAME;
	public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/" + BASE_PATH);
	public static final String TAG= "Shiyam";
	
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that I used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
    	sqlDb = database.getWritableDatabase();
		ContentValues cv= new ContentValues(values);
		//long rowId= db.insert(myHelper.TABLE_NAME, myHelper.VALUE_FIELD, values);
		long rowId= sqlDb.replace(myDatabase.TABLE_NAME, myDatabase.VALUE_FIELD, cv);
		System.out.println(rowId);
		if (rowId > 0) {
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		}
		else {
			Log.e(TAG, "Insert to db failed");
		}
		return null;
        
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
		database = new myDatabase(getContext());
		database.getWritableDatabase();
		return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
    	
    	System.out.println("inside query");
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         * 
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         * 
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
    	sqlDb= database.getReadableDatabase();
    	Cursor cursor = 
                sqlDb.query(myDatabase.TABLE_NAME, // a. table
                projection, // b. column names
                myDatabase.KEY_FIELD+"=?", // c. selections 
                new String[]{selection}, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
		return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }
}
