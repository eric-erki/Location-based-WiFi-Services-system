package com.lows;

import com.lows.LowsActivity.PrefsFragment;
import com.lows.contentprovider.MyCodeBookContentProvider;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * This class is used to manage the Settings (Preferences) Menu
 * @author Sven Zehl
 *
 */
public class LowsPreferenceActivity extends Activity {

	private static final String TAG = "com.lows.LowsPreferenceActivity";
 @Override
 protected void onCreate(Bundle savedInstanceState) {
  // TODO Auto-generated method stub
  super.onCreate(savedInstanceState);
  
  getFragmentManager().beginTransaction()
  .replace(android.R.id.content, new PrefsFragment()).commit();
  
 }
 
 protected void onPause() {
	 SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	 boolean clearDB = mySharedPreferences.getBoolean("clear_db", false);
	 if(clearDB==true)
	 {
		 Log.i(TAG, "Reset the Codebook DATABASE!!!");
		 SharedPreferences.Editor editor = mySharedPreferences.edit();
		 editor.putBoolean("clear_db", false);
		 editor.commit();
		 getContentResolver().delete(MyCodeBookContentProvider.CONTENT_URI, null, null);
		 Toast.makeText(this, "All codebook entries deleted!", Toast.LENGTH_SHORT).show();
	 }
	 else
	 {
		 Log.i(TAG, "Do nothing with the Database...");
	 }
	 super.onPause();
  }

}