package com.lows.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class implements the Codebook Database helper which is
 * needed for the codebook access.
 * 
 * @author Sven Zehl
 *
 */
public class CodeBookDatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "codebooktable.db";
  private static final int DATABASE_VERSION = 1;

  public CodeBookDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  // Method is called during creation of the database
  @Override
  public void onCreate(SQLiteDatabase database) {
    CodeBookTable.onCreate(database);
  }

  // Method is called during an upgrade of the database,
  // e.g. if you increase the database version
  @Override
  public void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    CodeBookTable.onUpgrade(database, oldVersion, newVersion);
  }
}
 