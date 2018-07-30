package edu.fcpc.polaroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
	//https://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "PolaroidFrameApp";
    private static final String TABLE_CONTACTS = "users";
	private static final String KEY_USERNUMBER = "usernumber";
	private static final String KEY_LASTNAME = "lastname";
	private static final String KEY_FIRSTNAME = "firstname";
	private static final String KEY_REGISTERMONTH = "registermonth";
	private static final String KEY_REGISTERDAY = "registerday";
	private static final String KEY_REGISTERYEAR = "registeryear";
	private static final String KEY_USERNAME = "username";
	private static final String KEY_PASSWORD = "password";
	
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "(" +
                KEY_USERNUMBER + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				KEY_LASTNAME + " TEXT," + 
                KEY_FIRSTNAME + " TEXT," +
				KEY_REGISTERMONTH + " INTEGER," +
				KEY_REGISTERDAY + " INTEGER," +
				KEY_REGISTERYEAR + " INTEGER," +
				KEY_USERNAME + " TEXT UNIQUE," +
                KEY_PASSWORD + " TEXT" +
				")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

	public void addUser(User user) {
		SQLiteDatabase db = this.getWritableDatabase();
	 
		ContentValues values = new ContentValues();
		values.put(KEY_LASTNAME, user.getLastname()); 
		values.put(KEY_FIRSTNAME, user.getFirstname());
		values.put(KEY_USERNAME, user.getUsername()); 
		values.put(KEY_PASSWORD, user.getPassword());
		values.put(KEY_REGISTERYEAR, user.getRegisteryear());
		values.put(KEY_REGISTERMONTH, user.getRegistermonth());
		values.put(KEY_REGISTERDAY, user.getRegisterday());

		db.insert(TABLE_CONTACTS, null, values);
		db.close(); 
	}

	public List<User> getAllUsers() {
		List<User> userList = new ArrayList<User>();
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
	 
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
	 
		if (cursor.moveToFirst()) {
			do {
				User user = new User();
				user.setUsernumber(Integer.parseInt(cursor.getString(0)));
				user.setLastname(cursor.getString(1));
				user.setFirstname(cursor.getString(2));
				user.setRegistermonth(Integer.parseInt(cursor.getString(3)));
				user.setRegisterday(Integer.parseInt(cursor.getString(4)));
				user.setRegisteryear(Integer.parseInt(cursor.getString(5)));
				user.setUsername(cursor.getString(6));
				user.setPassword(cursor.getString(7));
				userList.add(user);
			} while (cursor.moveToNext());
		}
	 
		return userList;
	}
}
	