package com.example.weather;
 
import java.util.ArrayList;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
 
public class CommentsDataSource {
 
  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
 // private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
 //     MySQLiteHelper.COLUMN_COMMENT };
 
  private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
  	  MySQLiteHelper.COLUMN_TEMPF,
		  MySQLiteHelper.COLUMN_TEMPC,
		  MySQLiteHelper.COLUMN_POP,
		  MySQLiteHelper.COLUMN_WINDSPD,
		  MySQLiteHelper.COLUMN_WINDDIR,
		  MySQLiteHelper.COLUMN_CONDITION,
		  MySQLiteHelper.COLUMN_CURRENTHOUR
  };
  
  public CommentsDataSource(Context context) {
    dbHelper = new MySQLiteHelper(context);
  }
 
  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }
 
  public void close() {
    dbHelper.close();
  }
  
  public boolean addWeather(String[] data) {
	  try {
		  ContentValues values = new ContentValues();
		  values.put(MySQLiteHelper.COLUMN_TEMPF, data[0]);
		  values.put(MySQLiteHelper.COLUMN_TEMPC, data[1]);
		  values.put(MySQLiteHelper.COLUMN_POP, data[2]);
		  values.put(MySQLiteHelper.COLUMN_WINDSPD, data[3]);
		  values.put(MySQLiteHelper.COLUMN_WINDDIR, data[4]);
		  values.put(MySQLiteHelper.COLUMN_CONDITION, data[5]);
		  values.put(MySQLiteHelper.COLUMN_CURRENTHOUR, data[6]);
		  
		  long insertId = database.insert(MySQLiteHelper.TABLE_WEATHER, null, values);
		  Cursor cursor = database.query(MySQLiteHelper.TABLE_WEATHER,
			        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
			        null, null, null);
			    //cursor.moveToFirst();
			    //Comment newComment = cursorToComment(cursor);
			    cursor.close();
		  
		  return true;
	  } catch (Exception e) {
		  e.printStackTrace();
		  return false;
	  }
  }
 
//  public Comment createComment(String comment) {
//    ContentValues values = new ContentValues();
//    values.put(MySQLiteHelper.COLUMN_COMMENT, comment);
//    long insertId = database.insert(MySQLiteHelper.TABLE_COMMENTS, null,
//        values);
//    Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
//        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
//        null, null, null);
//    cursor.moveToFirst();
//    Comment newComment = cursorToComment(cursor);
//    cursor.close();
//    return newComment;
//  }
 
  public void deleteComment(Comment comment) {
    long id = comment.getId();
    System.out.println("Weather info deleted with id: " + id);
    database.delete(MySQLiteHelper.TABLE_WEATHER, MySQLiteHelper.COLUMN_ID
        + " = " + id, null);
  }
 
//  public List<Comment> getAllComments() {
//    List<Comment> comments = new ArrayList<Comment>();
//
//    Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
//        allColumns, null, null, null, null, null);
//
//    cursor.moveToFirst();
//    while (!cursor.isAfterLast()) {
//      Comment comment = cursorToComment(cursor);
//      comments.add(comment);
//      cursor.moveToNext();
//    }
//    // Make sure to close the cursor
//    cursor.close();
//    return comments;
//  }
  
  
  public List<String[]> getAllWeather() {
	  List<String[]> weather = new ArrayList<String[]>();
	  Cursor cursor = database.query(MySQLiteHelper.TABLE_WEATHER,
			  allColumns, null, null, null, null, null);
	  
	  cursor.moveToFirst();
	  while(!cursor.isAfterLast()) {
		  String[] temp = cursorToWeather(cursor);
		  weather.add(temp);
		  cursor.moveToNext();
		  
	  }
	  
	  cursor.close();
	  return weather;
  }
 
  
  private String[] cursorToWeather(Cursor cursor) {
	  String[] temp = new String[8];
	  temp[0] = cursor.getString(0);
	  temp[1] = cursor.getString(1);
	  temp[2] = cursor.getString(2);
	  temp[3] = cursor.getString(3);
	  temp[4] = cursor.getString(4);
	  temp[5] = cursor.getString(5);
	  temp[6] = cursor.getString(6);
	  temp[7] = cursor.getString(7);
	  return temp;
 
 
  }
//  private Comment cursorToComment(Cursor cursor) {
//    Comment comment = new Comment();
//    comment.setId(cursor.getLong(0));
//    comment.setComment(cursor.getString(1));
//    return comment;
//  }
} 