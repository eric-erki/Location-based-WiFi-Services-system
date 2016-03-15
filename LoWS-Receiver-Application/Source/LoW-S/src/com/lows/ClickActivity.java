package com.lows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.lows.contentprovider.MyCodeBookContentProvider;
import com.lows.database.CodeBookTable;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import android.widget.Switch;
import android.widget.TextView;
/**
 * This Activity is started when a user taps on a single item of the ListView displayed 
 * via the ResultListFragment, this activity lists details about the specifc LoWS
 * and allows the setting of search strings. If a dichotomous code LoWS is used, this Activity
 * tries to decode the ldc, if the ldc is not available the Codebook Updater is started.
 * 
 *TODO: -make the variable CB_CHECK_INTERVAL adjustable via the settings menu and combine the
 *		 variable with the CB_CHECK_INTERVAL variable of the AlarmClickActivity
 *		-Currently only reduced LoWS together with the dichotomous code are supported,
 *		 this should be extended to also support dichotomous codes within a extended (flexible) LoWS message.
 *		-Display the ldc after the codebook was updated immediately, currently the string
 * 		 "Currently no location specific data available (no codebook entry found)" is displayed if no codebook
 * 		 was available but this string is not automatically replaced after the codebook was updated, it is 
 * 		 only replaced after the ClickActivity is reloaded manually by the user by returning and tapping again
 * 		 on the same LoWS entry in the ListView.
 * 
 * @author Sven Zehl
 *
 *
 */
public class ClickActivity extends Activity {
	int position;
	ProgressDialog dialog;
	boolean alarmSwitchState = false;
	EditText searchEditText;
	Button saveButton;
	private static final String TAG = "com.lows.ClickActivity";
	private static final int CB_CHECK_INTERVAL = 10; //check for new codebook if entry is older than CB_CHECK_INTERVAL 
													//You should also check the constant of AlarmClickActivity!

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.click_activity);
		final TextView typeTextView = (TextView) findViewById(R.id.type);
		final TextView dataTextView = (TextView) findViewById(R.id.data);
		final TextView locationTextView = (TextView) findViewById(R.id.locdata);
		final TextView cbDescTextView = (TextView) findViewById(R.id.codebook_desc);
		final TextView rssiTextView = (TextView) findViewById(R.id.rssi);
		saveButton = (Button) findViewById(R.id.save_button);

		searchEditText = (EditText) findViewById(R.id.searchText);
		final TextView searchTextView = (TextView) findViewById(R.id.alarmIf);
		Switch alarmSwitch = (Switch) findViewById(R.id.alarm_switch);

		Bundle bundle = getIntent().getExtras();
		position = bundle.getInt("position");
		String bundleType = bundle.getString("type");
		String bundleData = bundle.getString("data");
		String bundleSearchText = bundle.getString("searchText");
		String serviceData = bundle.getString("serviceData");
		int serviceType = bundle.getInt("serviceType");
		int formatType = bundle.getInt("formatType");
		String macData = bundle.getString("mac");
		double rssiData = bundle.getDouble("rssi");
		// Fields for database lookup
		String hardcodedValue = "0x00";
		String codebookValue = "0x00";
		String typeValue = "0x" + Integer.toHexString(serviceType);
		//TODO: Here also the case when a dichotomous code is embedded within a extended (flexible) type should
		// 		be supported somehow.
		if (formatType == 1) {
			// Codebook format (reduced format)
			hardcodedValue = "0x" + serviceData.substring(0, 2);
			codebookValue = "0x" + serviceData.substring(2, 4);
			// Database Stuff
			// Get correct row
			Cursor cursor = getContentResolver().query(
					MyCodeBookContentProvider.CONTENT_URI,
					null,
					"mac LIKE '" + macData + "' AND servicetype LIKE '"
							+ typeValue + "' AND hardcodedvalue LIKE '"
							+ hardcodedValue + "' AND codebookvalue LIKE '"
							+ codebookValue + "'", null, null);

			String dataValue = "Currently no location specific data available (no codebook entry found)";
			//Check if an entry was found
			if (cursor != null) {
				//If an entry was found for the ldc
				if(cursor.getCount()>0)
		    	{
		    		cursor.moveToFirst();
		    		dataValue = cursor.getString(cursor.getColumnIndexOrThrow(CodeBookTable.COLUMN_DATA));
		    		String entryDate = cursor.getString(cursor.getColumnIndexOrThrow(CodeBookTable.COLUMN_LASTCHANGED));
		    		
		    		/*Check if the codebook updater should be started because the stored codebook
		    		 * is older than CB_CHECK_INTERVAL.
		    		 */
		    		Calendar c = Calendar.getInstance();
		    		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		    		try {
		    			//Get current date and time
						Date currentDateDate = sdf.parse(sdf.format(c.getTime()));
						//Set Calendar to time of the entry
						c.setTime(sdf.parse(entryDate));
						//Increment the Calendar date (Date of the entry) to CB_CHECK_INTERVAL
						c.add(Calendar.DATE, CB_CHECK_INTERVAL); 
						//Get the time and date of the entry plus CB_CHECK_INTERVAL from calendar and format it into Date
						Date entryDateDate = sdf.parse(sdf.format(c.getTime()));
						//Compare if entryDateDate now is older than current date, if yes start codebook updater
						//Toast.makeText(getApplicationContext(), "LoWSActivity: entryDatDate:"+entryDateDate.toString()+" currentDatDate:"+currentDateDate.toString(), Toast.LENGTH_SHORT).show();
						if(entryDateDate.before(currentDateDate)){
							Log.w(TAG, "entryDatDate:"+entryDateDate.toString()+" currentDatDate:"+currentDateDate.toString());
				    		Intent cbusIntent = new Intent(this, CodeBookUpdaterService.class);
							cbusIntent.putExtra(CodeBookUpdaterService.MAC_IN_MSG, macData);
							startService(cbusIntent);
			        	}	
					} catch (ParseException e) {
						Log.e(TAG, "Error while processing date comparison "+e.toString());
					}
		    	}
				//No entry was found for the ldc
				else {
					//Start Codebook Updater
					Intent cbusIntent = new Intent(this, CodeBookUpdaterService.class);
					//Send the codebook updater the mac
					cbusIntent.putExtra(CodeBookUpdaterService.MAC_IN_MSG, macData);
					startService(cbusIntent);
				}
			} 
			//close the cursor
			cursor.close();
			locationTextView.setText(dataValue);
			//End database operations

		} else {
			cbDescTextView.setVisibility(View.INVISIBLE);
			locationTextView.setVisibility(View.INVISIBLE);
		}
		boolean showAlarmSwitch = bundle.getBoolean("showAlarmSwitch");
		boolean initialAlarmSwitchState = bundle
				.getBoolean("AlarmInitialState");
		boolean showAlarmSearchField = bundle
				.getBoolean("showAlarmSearchField");
		typeTextView.setText(bundleType);
		dataTextView.setText(bundleData);
		searchTextView.setText(bundleSearchText);
		rssiTextView.setText("Distance (RSSI): "+rssiData+" dBm");

		// Database Stuff should be deleted, only for testing purposes....

		// Add Rows
		/*
		ContentValues values = new ContentValues();
		values.put(CodeBookTable.COLUMN_MAC, "a0:cf:5b:9f:93:c1");
		values.put(CodeBookTable.COLUMN_SERVICE_TYPE, "0x21");
		values.put(CodeBookTable.COLUMN_HARDCODED_VALUE, "0x52");
		values.put(CodeBookTable.COLUMN_CODEBOOK_VALUE, "0x77");
		values.put(CodeBookTable.COLUMN_DATA,
				"Use the white door on the end of the floor to escape");
		*/
		// Uri savedUri =
		// getContentResolver().insert(MyCodeBookContentProvider.CONTENT_URI,
		// values);

		// String[] projection = { CodeBookTable.COLUMN_ID,
		// CodeBookTable.COLUMN_MAC, CodeBookTable.COLUMN_DATA };
		// CursorLoader cursorLoader = new CursorLoader(this,
		// MyCodeBookContentProvider.CONTENT_URI, projection, null, null, null);
		// Cursor cursor = cursorLoader.loadInBackground();

		if (!showAlarmSwitch) {
			alarmSwitch.setVisibility(View.INVISIBLE);
			searchTextView.setVisibility(View.INVISIBLE);
			searchEditText.setVisibility(View.INVISIBLE);
			saveButton.setVisibility(View.INVISIBLE);
		}
		if (!showAlarmSearchField) {
			searchTextView.setVisibility(View.INVISIBLE);
			searchEditText.setVisibility(View.INVISIBLE);
		}
		if (showAlarmSwitch) {
			if (initialAlarmSwitchState) {
				alarmSwitch.toggle();
				alarmSwitchState = true;

			}

		}

		alarmSwitch
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							alarmSwitchState = true;
						} else {
							alarmSwitchState = false;
						}

					}

				});

		saveButton.setOnClickListener(new View.OnClickListener() {
			// Setting the action to perform when the start button is pressed.
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("alarmSet", alarmSwitchState);
				intent.putExtra("position", position);
				String searchTextReturn = searchEditText.getEditableText()
						.toString();
				intent.putExtra("searchTextReturn", searchTextReturn);
				setResult(-1, intent);
				onBackPressed();

			}

		});

	}

	@Override
	public void onBackPressed() {
		finish();
	}

}
