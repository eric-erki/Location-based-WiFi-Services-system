package com.lows;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.lows.contentprovider.MyCodeBookContentProvider;
import com.lows.database.CodeBookTable;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

/**
 * This Activity is started by the BackgroundScanner after a notification has been generated and the user
 * has tapped on this notification
 * 
 * TODO: -As described in the ClickActivity class, the CB_CHECK_INTERVAL should be made adjustable via the 
 * 		  settings of the application. Moreover, the intervals from the ClickActivity and this activity should
 * 		  be somehow connected.
 * 		 -backgroundScannerInterval should be adjustable via the Settings Menu of the application. Moreover, 
 * 		  the LowsActivity class uses the same variable, both should be connected somehow.
 * 
 * @author Sven Zehl
 *
 *
 */
public class AlarmClickActivity extends Activity{
	public static final String PREFS_NAME = "lowsPersistentData";
	private static final String TAG = "com.lows.AlarmClickActivity";
	private static final int CB_CHECK_INTERVAL = 10; //check for new codebook if entry is older than CB_CHECK_INTERVAL
													//You should also check this constant in the ClickActivity!
	
	//Interval for the Background Alarm Scanner
	//TODO Make this variable adjustable from the Settings Menu
	private static int backgroundScannerInterval = 15;
	
	private String[] searchNCompareData;
	private String[] alarmMessagesData;
	String displayString;
	String matchString;
	int formatType;
	String bssid;
	String ssid;
	String serviceData;
	double signalStrength;
	int frequency;
	int type;
	private List<LoWSReducedType> lowsRedType;
	private List<LoWSExtendedType> lowsExtType;
	String typeText;
	String standardText;
	String enhancedText;
	String iconName;
	String outputString;
	Button clearButton;
	
	Intent BackgroundScannerIntent;
	PendingIntent BackgroundScannerPendingIntent;
	AlarmManager alarm;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.alarm_click_activity);
	    addAllTypes();
	    final ImageView iconView = (ImageView) findViewById(R.id.icon);
	    final TextView typeTextView = (TextView) findViewById(R.id.typeText);
	    clearButton = (Button) findViewById(R.id.clear_button);
	    final TextView displayTextView = (TextView) findViewById(R.id.displayText);
	    final TextView outputTextView = (TextView) findViewById(R.id.output_text);
	    Bundle bundle = getIntent().getExtras();
	    displayString = bundle.getString("displayMessage");
	    matchString = bundle.getString("matchString");
	    formatType = bundle.getInt("formatType");
	    bssid = bundle.getString("BSSID");
	    ssid = bundle.getString("SSID");
	    signalStrength = bundle.getDouble("signalStrength");
	    frequency = bundle.getInt("frequency");
	    type = bundle.getInt("type");
	    serviceData = bundle.getString("serviceData");
	    searchNCompareData= bundle.getStringArray("searchNCompareData");
		alarmMessagesData= bundle.getStringArray("alarmMessagesData");
		if(formatType==1 && type==33) //BEPS is the only notification which have a not clearable alarm
	    {
	    	clearButton.setVisibility(View.INVISIBLE);
	    }
	    else
	    {
	    	clearButton.setVisibility(View.VISIBLE);
	    }
	    Log.i(TAG, "matchString: "+matchString +" FormatType: " + formatType + " type: " +type);
	    Log.i(TAG, "displayString: "+displayString +" BSSID: " + bssid + " SSID: " +ssid + " signalStrength: " + signalStrength + " Frequency: " + frequency);
	    if(formatType==1)
	    {
		    for(int p=0; p<lowsRedType.size(); p++)
		  	{
			  LoWSReducedType tempRedType=lowsRedType.get(p);
			  if(type==tempRedType.getTypeNumber())
			  {
				  iconName = tempRedType.getIconName();
				  typeText = tempRedType.getDisplayString();
				  outputString = tempRedType.getAlarmClickStandardText(serviceData);
				  outputString = outputString + "\n"; //+ tempRedType.getEnhancedClickText(serviceData);
				  
				  
				  String hardcodedValue = "0x"+serviceData.substring(0,2);
				  String codebookValue = "0x"+serviceData.substring(2,4);
				  String typeValue = "0x"+Integer.toHexString(type);
				  String macData = bssid;
			    	//Database Stuff
			    	//Get correct row
				  Cursor cursor = getContentResolver().query(MyCodeBookContentProvider.CONTENT_URI, null, 
				    		"mac LIKE '"+macData+"' AND servicetype LIKE '"+ typeValue+
				    		"' AND hardcodedvalue LIKE '"+hardcodedValue+
				    		"' AND codebookvalue LIKE '"+codebookValue+"'",
				    		null,null);
				    
				    String dataValue="Currently no location specific data available (no codebook entry found)";
				    if (cursor != null)
				    {
				    	if(cursor.getCount()>0)
				    	{
				    		cursor.moveToFirst();
				    		dataValue = cursor.getString(cursor.getColumnIndexOrThrow(CodeBookTable.COLUMN_DATA));
				    		String entryDate = cursor.getString(cursor.getColumnIndexOrThrow(CodeBookTable.COLUMN_LASTCHANGED));
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
				    	else
				    	{
				    		Intent cbusIntent = new Intent(this, CodeBookUpdaterService.class);
							cbusIntent.putExtra(CodeBookUpdaterService.MAC_IN_MSG, macData);
							startService(cbusIntent);
				    	}
				    }    // always close the cursor
				    cursor.close();
				    outputString = outputString + dataValue;
				    
				    //End database  
				  
			  }
		  	}
	    
		}
	    else if(formatType==2)
		{
			for(int p=0; p<lowsRedType.size(); p++)
		  	{
			  LoWSExtendedType tempExtType=lowsExtType.get(p);
			  if(type==tempExtType.getTypeNumber())
			  {
				  iconName = tempExtType.getIconName();
				  typeText = tempExtType.getDisplayString();
				  outputString = tempExtType.getAlarmClickStandardText(serviceData);
				  if(tempExtType.getAlarmClickStandardText(serviceData)==null)
				  {
					  outputTextView.setVisibility(View.INVISIBLE);
				  }
				  if(tempExtType.getEnhancedClickText(serviceData)!=null)
				  {
					  outputString = outputString + "\n\n" + tempExtType.getEnhancedClickText(serviceData);
				  }
			  }
		  	}
		}
	       
	    
	    iconView.setImageResource(getImageId(getApplicationContext(), iconName));
	    typeTextView.setText(typeText);
	    displayTextView.setText(displayString);
	    if(outputString != null)
	    {
	    	outputTextView.setText(outputString);
	    }
	    else
	    {
	    	outputTextView.setVisibility(View.INVISIBLE);
	    }
	    
	   
	    
	    
	    
	    
	    clearButton.setOnClickListener(new View.OnClickListener() {
			// Setting the action to perform when the start button is pressed.
			@Override
			/**
		     * Function that is called when the user taps on the switch for clearing the alarm,
		     * as the alarm should be turned off, this function searches for the search string that
		     * caused the alarm and deletes it from the String array and the display string array.
		     * This function afterwards stores the changes in the persistent memory using the SharedPreferences
		     * Moreover, the startChangeBackgroundScanService() function is used to transmit the changes to the 
		     * Background Scan Service.
		     */
			public void onClick(View v) {
				//Clear Search String from Arrays
				//submit changes to background scanner
				//save changes in arrays in persistent app data
				if(alarmMessagesData==null || searchNCompareData==null || searchNCompareData.length==0 || alarmMessagesData.length==0)
				{
					Log.i(TAG, "Search array is already empty");
					return;
				}
				else
				{
					int i=0;
					int q;
					int entryToRemove=-1;
					Log.i(TAG, "length>0 && !=null");
					for(i=0; i<alarmMessagesData.length; i++)
					{
						if(searchNCompareData[i].equals(matchString))
						{
							entryToRemove=i;
						}
					}
					if(entryToRemove==-1)
					{
						Log.i(TAG, "Search String to remove not found");
					}
					String[] alarmMessagesDataNew = new String[alarmMessagesData.length-1]; 
					String[] searchNCompareDataNew = new String[alarmMessagesData.length-1]; 
					q=0;
					for(i=0; i<(alarmMessagesData.length -1); i++)
					{
						if(i==entryToRemove)
						{
							//do nothing
						}
						else
						{
							alarmMessagesDataNew[i]=alarmMessagesData[i];
							searchNCompareDataNew[i]=searchNCompareData[i];
							q++;
						}
					}
					alarmMessagesData=alarmMessagesDataNew;
					searchNCompareData=searchNCompareDataNew;
				}
				//Search String is removed, now submit changes to background process
				startChangeBackgroundScanService();
				
				//Now save the new arrays in persistent data
				 SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
			     SharedPreferences.Editor editor = settings.edit();
			       
			       if(searchNCompareData==null || searchNCompareData.length==0 || alarmMessagesData.length==0 || alarmMessagesData==null)
			       {
			    	   editor.putString("searchNCompareDataStoreString", "0");
			    	   editor.putString("alarmMessagesDataStoreString", "0");
			       }
			       else
			       {
				       StringBuilder searchNCompareDataStoreString = new StringBuilder();
				       for (int i = 0; i < searchNCompareData.length; i++) {
				    	   searchNCompareDataStoreString.append(searchNCompareData[i]).append(",");
				       }
				       editor.putString("searchNCompareDataStoreString", searchNCompareDataStoreString.toString());
				       
				       StringBuilder alarmMessagesDataStoreString = new StringBuilder();
				       for (int i = 0; i < alarmMessagesData.length; i++) {
				    	   alarmMessagesDataStoreString.append(alarmMessagesData[i]).append(",");
				       }
				       editor.putString("alarmMessagesDataStoreString", alarmMessagesDataStoreString.toString());
			       }
			       
			       
			       // Commit the edits!
			       editor.commit();
			       onBackPressed();
			       Toast.makeText(getApplicationContext(), "Alarm cleared!", Toast.LENGTH_SHORT).show();
			}
			
		});
	  }
	

	/*************************************************************************/
	//Supported Types//
	/**
	 * Function for loading all supported LoWS types
	 * into LoWS lowsRedType and lowsExtType Lists
	 */
	private void addAllTypes()
	{
		//Reduced Types
		lowsRedType = new ArrayList<LoWSReducedType>();
		BEPSLoWSReducedType bepsType = new BEPSLoWSReducedType();
		PSALoWSReducedType psaType = new PSALoWSReducedType();
		lowsRedType.add(bepsType);
		lowsRedType.add(psaType);
		
		
		//Extended Types
		lowsExtType = new ArrayList<LoWSExtendedType>();
		WTNLoWSExtendedType wtsType = new WTNLoWSExtendedType();
		STLoWSExtendedType stType = new STLoWSExtendedType();
		lowsExtType.add(wtsType);
		lowsExtType.add(stType);
	}
	
	/**
	 * Function that returns the correct ImageRessource as String
	 * @param context, the Context e.g. use getApplicationContext()
	 * @param imageName, the name of the image
	 * @return String with the ImageRessource
	 */
	 public static int getImageId(Context context, String imageName) {
		    return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
		}
	 
	 /**
	  * Same function as it was used in the LowsActivity class but without restart option
	  * 
	  * Start the Background Scan Service
	  * transmit all search strings and display strings
	  * and start the periodic execution via the AlarmManager.
	  */
	 public void startChangeBackgroundScanService()
		{	    
		    if((searchNCompareData == null || alarmMessagesData == null))
		    {
		    	searchNCompareData = new String[0];
		    	alarmMessagesData = new String[0];
		    }
		    BackgroundScannerIntent = new Intent(this, LowsBackgroundAlarmScanner.class);
			alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		    Calendar cal = Calendar.getInstance();
	        BackgroundScannerIntent.removeExtra("searchNCompareData");
	        BackgroundScannerIntent.removeExtra("alarmMessagesData");
		    BackgroundScannerIntent.putExtra("searchNCompareData", searchNCompareData);
		    BackgroundScannerIntent.putExtra("alarmMessagesData", alarmMessagesData);
		    
		    BackgroundScannerPendingIntent = PendingIntent.getService(this, 0, BackgroundScannerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		    // Start every 15 seconds
		    alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), backgroundScannerInterval*1000, BackgroundScannerPendingIntent); 
		}
}
