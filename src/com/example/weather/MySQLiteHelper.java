package com.example.weather;
 
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 
public class MySQLiteHelper extends SQLiteOpenHelper {
 
  public static final String TABLE_COMMENTS = "comments";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_COMMENT = "comment";
  
  public static final String TABLE_WEATHER = "weather";
  public static final String COLUMN_TEMPF = "tempf";
  public static final String COLUMN_TEMPC = "tempc";
  public static final String COLUMN_POP = "pop";
  public static final String COLUMN_WINDSPD = "windspd";
  public static final String COLUMN_WINDDIR = "winddir";
  public static final String COLUMN_CONDITION = "condition";
  public static final String COLUMN_CURRENTHOUR = "currenthour";
  
  private static final String DATABASE_NAME = "weather.db";
  private static final int DATABASE_VERSION = 1;
 
  // Database creation sql statement
//  private static final String DATABASE_CREATE = "create table "
//      + TABLE_COMMENTS + "(" + COLUMN_ID
//      + " integer primary key autoincrement, " + COLUMN_COMMENT
//      + " text not null);";
  
  private static final String DATABASE_CREATE = "create table " 
  	  + TABLE_WEATHER + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		  + COLUMN_TEMPF + " TEXT NOT NULL, "
		  + COLUMN_TEMPC + " TEXT NOT NULL, "
		  + COLUMN_POP + " TEXT NOT NULL, "
		  + COLUMN_WINDSPD + " TEXT NOT NULL, "
		  + COLUMN_WINDDIR + " TEXT NOT NULL, "
		  + COLUMN_CONDITION + " TEXT NOT NULL, "
		  + COLUMN_CURRENTHOUR + " TEXT NOT NULL);";
 
  public MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
 
  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }
 
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEATHER);
    onCreate(db);
  }
 
} 