

package com.lows.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import com.lows.database.CodeBookDatabaseHelper;
import com.lows.database.CodeBookTable;

/**
 * This class implements the ContentProvider for the Codebook 
 * which holds all ldc parts.
 * 
 * 
 * @author Sven Zehl
 *
 */
public class MyCodeBookContentProvider extends ContentProvider {

  // database
  private CodeBookDatabaseHelper database;

  // used for the UriMacher
  private static final int LOWS = 10;
  private static final int LOWS_ID = 20;

  private static final String AUTHORITY = "com.lows.contentprovider";

  private static final String BASE_PATH = "lows";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
      + "/" + BASE_PATH);

  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
      + "/lows";
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
      + "/lows";

  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, LOWS);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", LOWS_ID);
  }

  @Override
  public boolean onCreate() {
    database = new CodeBookDatabaseHelper(getContext());
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {

    // Uisng SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

    // check if the caller has requested a column which does not exists
    checkColumns(projection);

    // Set the table
    queryBuilder.setTables(CodeBookTable.TABLE_CB);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case LOWS:
      break;
    case LOWS_ID:
      // adding the ID to the original query
      queryBuilder.appendWhere(CodeBookTable.COLUMN_ID + "="
          + uri.getLastPathSegment());
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = database.getWritableDatabase();
    Cursor cursor = queryBuilder.query(db, projection, selection,
        selectionArgs, null, null, sortOrder);
    // make sure that potential listeners are getting notified
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }
    

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsDeleted = 0;
    long id = 0;
    switch (uriType) {
    case LOWS:
      id = sqlDB.insert(CodeBookTable.TABLE_CB, null, values);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return Uri.parse(BASE_PATH + "/" + id);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsDeleted = 0;
    switch (uriType) {
    case LOWS:
      rowsDeleted = sqlDB.delete(CodeBookTable.TABLE_CB, selection,
          selectionArgs);
      break;
    case LOWS_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsDeleted = sqlDB.delete(CodeBookTable.TABLE_CB,
            CodeBookTable.COLUMN_ID + "=" + id, 
            null);
      } else {
        rowsDeleted = sqlDB.delete(CodeBookTable.TABLE_CB,
            CodeBookTable.COLUMN_ID + "=" + id 
            + " and " + selection,
            selectionArgs);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {

    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsUpdated = 0;
    switch (uriType) {
    case LOWS:
      rowsUpdated = sqlDB.update(CodeBookTable.TABLE_CB, 
          values, 
          selection,
          selectionArgs);
      break;
    case LOWS_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsUpdated = sqlDB.update(CodeBookTable.TABLE_CB, 
            values,
            CodeBookTable.COLUMN_ID + "=" + id, 
            null);
      } else {
        rowsUpdated = sqlDB.update(CodeBookTable.TABLE_CB, 
            values,
            CodeBookTable.COLUMN_ID + "=" + id 
            + " and " 
            + selection,
            selectionArgs);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsUpdated;
  }

  private void checkColumns(String[] projection) {
    String[] available = { CodeBookTable.COLUMN_MAC,
        CodeBookTable.COLUMN_SERVICE_TYPE, CodeBookTable.COLUMN_HARDCODED_VALUE,
        CodeBookTable.COLUMN_CODEBOOK_VALUE, CodeBookTable.COLUMN_LASTCHANGED,
        CodeBookTable.COLUMN_ID, CodeBookTable.COLUMN_DATA, CodeBookTable.COLUMN_VERSION};
    if (projection != null) {
      HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
      HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
      // check if all columns which are requested are available
      if (!availableColumns.containsAll(requestedColumns)) {
        throw new IllegalArgumentException("Unknown columns in projection");
      }
    }
  }

} 
