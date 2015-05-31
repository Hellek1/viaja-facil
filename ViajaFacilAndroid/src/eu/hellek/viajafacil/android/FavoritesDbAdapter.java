package eu.hellek.viajafacil.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * database adapter for local handling of favorites (add, delete, list, etc.) in local SQlite DB
 */
public class FavoritesDbAdapter {
	
	public static final String KEY_AE_KEY = "key";
    public static final String KEY_ADDR = "address";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LON = "lon";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "FavoritesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_CREATE =
        "create table favorites (_id integer primary key autoincrement, "
        + "key text not null, address text not null, lat double not null, lon double not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "favorites";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS favorites");
            onCreate(db);
        }
    }

    public FavoritesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public FavoritesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    public void clearDB() {
    	mDb.delete(DATABASE_TABLE, null, null);
    }

    public long addFavorite(String key, String addr, double lat, double lon) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_AE_KEY, key);
        initialValues.put(KEY_ADDR, addr);
        initialValues.put(KEY_LAT, lat);
        initialValues.put(KEY_LON, lon);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteFavorite(String key) {
        return mDb.delete(DATABASE_TABLE, KEY_AE_KEY + "= '" + key + "'", null) > 0;
    }
    
    public boolean deleteFavorite(long rowid) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowid, null) > 0;
    }

    public Cursor fetchAllFavorites() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_AE_KEY,
                KEY_ADDR, KEY_LAT, KEY_LON}, null, null, null, null, null);
    }

    public Cursor fetchFavorite(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_AE_KEY, KEY_ADDR, KEY_LAT, KEY_LON}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean updateFavorite(long rowId, String key, String addr, double lat, double lon) {
        ContentValues args = new ContentValues();
        args.put(KEY_AE_KEY, key);
        args.put(KEY_ADDR, addr);
        args.put(KEY_LAT, lat);
        args.put(KEY_LON, lon);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
