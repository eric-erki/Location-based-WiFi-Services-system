package com.lows.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * This class implements the CodeBookTable, which is used
 * to store all the ldc parts.
 * 
 * @author Sven Zehl
 *
 */
public class CodeBookTable {

  // Database table
  public static final String TABLE_CB = "codebook";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_MAC = "mac";
  public static final String COLUMN_SERVICE_TYPE = "servicetype";
  public static final String COLUMN_HARDCODED_VALUE = "hardcodedvalue";
  public static final String COLUMN_CODEBOOK_VALUE = "codebookvalue";
  public static final String COLUMN_DATA = "data";
  public static final String COLUMN_VERSION = "version";
  public static final String COLUMN_LASTCHANGED = "lastchanged";

  // Database creation SQL statement
  private static final String DATABASE_CREATE = "create table " 
      + TABLE_CB
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_MAC + " text not null, " 
      + COLUMN_SERVICE_TYPE + " text not null," 
      + COLUMN_HARDCODED_VALUE + " text not null," 
      + COLUMN_CODEBOOK_VALUE + " text not null," 
      + COLUMN_DATA + " text not null, " 
      + COLUMN_LASTCHANGED + " text not null, "
      + COLUMN_VERSION
      + " integer" 
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    Log.w(CodeBookTable.class.getName(), "Upgrading database from version "
        + oldVersion + " to " + newVersion
        + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_CB);
    onCreate(database);
  }
} 