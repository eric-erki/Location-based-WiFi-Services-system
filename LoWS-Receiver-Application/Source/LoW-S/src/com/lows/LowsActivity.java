package com.lows;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
//import android.support.v13.app.FragmentPagerAdapter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.app.AlertDialog;

/**
* Main LoWS Receiver Application Activity 
*
* TODO:
* 		- rename extended LoWS to flexible LoWS everywhere in the code
* 		- Make variable backgroundScannerInterval adjustable from the Settings Menu
* 		- Implement fragmentation management, signature detection and encryption detection 
* 		  in function parseExtendedOptionByte()
* 		- Implement Type handling with type number longer than one byte for extended (flexible) LoWS
* 		  types in function parseExtendedType()
* 		- Implement Security byte handling, Signature validation and service data encryption detection in function
* 		  parseExtendedSecurityByte(), this includes the detection of encryption type and signature type
* 		- Implement signature validation functionality and service data decryption functionality.
* 		- Change the search and display strings management from sharedPreferences to a sqlite database!
* 		- Enable searching and getting notified if PSA LoWS types are in current vicinity. The BEPS implemenation
* 		  may serve as an example implementation.
* 		- Enable searching and getting notified for ST LoWS types.
*
* @author Sven Zehl
*
*
* @version 1.0
* 
*/
public class LowsActivity extends Activity {
	
	//Interval for the Background Alarm Scanner
	//TODO Make this variable adjustable from the Settings Menu
	private static int backgroundScannerInterval = 15;
	//SectionsPager Adapter object
	SectionsPagerAdapter mSectionsPagerAdapter;
	//TAG for debugging with logcat
	private static final String TAG = "com.lows.LowsActivity";
	ViewPager mViewPager;
	//Name to store the persistent data
	public static final String PREFS_NAME = "lowsPersistentData";
	//List that holds all found APs
	private List<AccessPoint> aps;
	//List that holds all found LoWS messages
	private List<LoWS> lows;
	//List that holds all reduced type LoWS messages
	private List<LoWSReducedType> lowsRedType;
	//List that holds all extended LoWS messages
	private List<LoWSExtendedType> lowsExtType;
	/*Search Strings that the Background Alarm Scanner uses 
	* for the comparison with the LoWS message data found durin scan*/
	private String[] searchNCompareData;
	/*Strings that should be displayed in the notification if a match with the searchNCompareData 
	 * Strings occured within the Background Alarm Scanner */
	private String[] alarmMessagesData;
	private AccessPoint tempAp;
	//Access to the Android Wifi Service
	WifiManager mainWifiObj;
	//Broadcast Receiver Object
	WifiScanReceiver wifiReciever;
	//debug Text
	private String debugText;
	//not needed anymore?
	private int gotRoot=0;
	int numberLows=0;
	//Background Scanner Alarm Service Intents
	Intent BackgroundScannerIntent;
	PendingIntent BackgroundScannerPendingIntent;
	//Alarm Manger for starting the Background Alarm Scanner
	AlarmManager alarm;
	//True if scanner was already started
	boolean backgroundScannerStartedState=false;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lows);

		//Create the adapter that will return a fragment for each of the
		//primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		//Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		//get Wifi System Service
		mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//Register Broadcast Receiver to IntentFilter
        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        //Set RootTools in debug mode (could be turned off if not needed anymore)
        RootTools.debugMode = true; 
        //Set the BackgroundScannerIntent to the correct class
        BackgroundScannerIntent = new Intent(this, LowsBackgroundAlarmScanner.class);
        //Check if we have root access 
        /*deprecated, we do not do this anymore,
        *if someone has a rooted phone, he is asked to give root access
        *therefore anybody thinks we would need root, but we don't*/
        /*
        if (RootTools.isAccessGiven()) {
            // your app has been granted root access
        	setTitle("Location-based Wifi Services");
        	gotRoot=1;
        	debugText = debugText + "\n-Device is rooted";
        }
        else
        {
        	setTitle("Location-based Wifi Services");
        	gotRoot=0;
        	debugText = debugText + "\n-Device is not rooted";
        }
        */
        //We just set the title and set gotRoot to zero, We don't need root anymore!
        setTitle("Location-based Wifi Services");
    	gotRoot=0;
    	debugText = debugText + "\n-Root check disabled";
        //This lines above should be commented out if root check should be done...
    	
        //Install Binary to get the ScanResults from Driver using Netlink NL80211 communication
        //Source Code of binary can be found in project/jni folder, to build run ndk-build command
        //Afterwards binary had to be copied into res/raw folder
        if (RootTools.installBinary(LowsActivity.this, R.raw.nlscanner, "nlscanner") == false) {
        	debugText = debugText + "\n-Extraction of nlscanner binary failed.";
			new AlertDialog.Builder(LowsActivity.this)
					.setTitle("Extract failed")
					.setMessage("LoW-S was not able to install nlscanner binary!")
					.setNeutralButton("ok", null).show();
		}
        else
        {
        	debugText = debugText + "\n-Extraction of nlscanner binary succeded.";
        }
        //Set execution permissions to make the nlscanner binary executable
        setExecPerm();
        //Load all currently supported LoWS Types
        addAllTypes();      
        
        //Load the data stored in the Application persistent memory
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Find out if the Background Alarm Scanner was already started (is already running)
        backgroundScannerStartedState = settings.getBoolean("backgroundScannerStartedState", false);
        //Load current Search Strings
        String searchNCompareDataStoreString = settings.getString("searchNCompareDataStoreString", "0");
        if(!searchNCompareDataStoreString.equals("0"))
        {
        	searchNCompareData =searchNCompareDataStoreString.split(",");
        }
        //Load current Display Strings
        String alarmMessagesDataStoreString = settings.getString("alarmMessagesDataStoreString", "0");
        if(!alarmMessagesDataStoreString.equals("0"))
        {
        	alarmMessagesData =alarmMessagesDataStoreString.split(",");
        }       
        //If the Background Scanner was not already started, we need to set all standard search strings (currently only BEPS)
        if(!backgroundScannerStartedState)
        {
        	/*When the app is started the first time after a reboot, the backgroundScanner is not running
        	* Till we want automatically receive BEPS Events, the search strings must be set in advance before the user 
        	* chooses about it.
        	* Since only BEPS needs to be enabled in advance, this is not done automatically for new LoWS types.*/
        	
        	//Generate new BEPS type object
        	BEPSLoWSReducedType bepsType = new BEPSLoWSReducedType();
        	//Get all search strings
        	searchNCompareData = bepsType.getBackgroundScannerSearchStrings();
        	//Get all Alarm Strings
        	alarmMessagesData = bepsType.getBackgroundScannerDisplayStrings();
        }
        startChangeBackgroundScanService();
	}
	
	
	protected void onDestroy()
	{
		//Load the persistent data entry
		SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		//get the setting of the background scanner
		boolean runBackgroundScanner = mySharedPreferences.getBoolean("background_scanner", true);
		//If the background scanner should not run without the main application, cancel the alarm
		if(runBackgroundScanner==false)
		{
			alarm.cancel(BackgroundScannerPendingIntent);
		}
		super.onDestroy();
	}
	
	protected void onPause() {
    	super.onPause();
     }

     protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(
        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
     }
     

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lows, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent prefIntent = new Intent();
			prefIntent.setClass(LowsActivity.this, LowsPreferenceActivity.class);
	        startActivityForResult(prefIntent, 0); 
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Public getter for BackgroundScanner State 
	 * @return true, if the Background scanner is running
	 */
	public boolean getBackgroundScannerStartedState()
	{
		return this.backgroundScannerStartedState;
	}
	
	/**
	 * Getter for the current debug text displayed in the debug fragment
	 * @return the current debug text as string
	 */
	public String getDebugText()
	{
		return this.debugText;
	}
	
	/**
	 * Setter for the debug text, enables to set the debug text
	 * @param debugText
	 */
	void setDebugText(String debugText)
	{
		this.debugText=debugText;
	}
	
	/**
	 * Append new debug text
	 * @param debugTextAppend, text to append to current debug text output
	 */
	void appendDebugText(String debugTextAppend)
	{
		this.debugText= this.debugText + debugTextAppend;
	}
	
	/**
	 * Getter for all currently found Access Points
	 * @return List AccessPoint
	 */
	List<AccessPoint> getApArray()
	{
		return this.aps;
	}
	
	/**
	 * Getter for all currently found LoWS
	 * @return List LoWS
	 */
	List<LoWS> getLowsArray()
	{
		return this.lows;
	}
	
	/**
	 * Getter for all currently found reduced lows
	 * @return List LoWSReducedType
	 */
	List<LoWSReducedType> getLoWSReducedTypeArray()
	{
		return this.lowsRedType;
	}

	/**
	 * Getter for all currently found extended (flexible) LoWS
	 * @return List LoWSExtendedType
	 */
	List<LoWSExtendedType> getlowsExtendedTypeArray()
	{
		return this.lowsExtType;
	}
	
	/**
	 * Getter for the current Search Strings which will be transmitted to the Background Alarm Scanner
	 * @return String[] currentSearchStrings
	 */
	String[] getSearchNCompareDataArray()
	{
		return this.searchNCompareData;
	}
	
	/**
	 * Getter for the Display Strings (Strings that will be displayed by the Notification the Background
	 * Alarm Scanner will start if a match occurs.
	 * @return String[] AlarmMessage Strings
	 */
	String[] getAlarmMessagesDataArray()
	{
		return this.alarmMessagesData;
	}
	
	/**
	 * Send new Strings (Search Strings and Display Strings) to the Background Alarm Scanner
	 * @param searchNCompareData, the search strings
	 * @param alarmMessagesData, the corresponding strings that should be displayed if a match occurred.
	 */
	void setSearchNCompareDataArray(String[] searchNCompareData, String[] alarmMessagesData)
	{
		this.searchNCompareData = searchNCompareData;
		this.alarmMessagesData = alarmMessagesData;
		startChangeBackgroundScanService();
	}
	
	/**
	 * Function for setting the permissions of the binary to be executable for everyone
	 * 
	 * @param void
	 * @return void
	 * 
	 */
	void setExecPerm()
    {	
		//Generate new command for RootTools, chmod to 0777 command
		Command command = new Command(1, "chmod 0777 /data/data/com.lows/files/nlscanner"){
			
			//If the command succeeded add the success message to the debug text
			public void commandCompleted(int id, int exitCode) {	
				debugText = debugText + "\n-Setting execution permissions of nlscanner binary to 0777 succeeded.";
			}

			//Send the return text of the command to the debug text
			public void commandOutput(int id, String line) {
				debugText = debugText + "\n-" + line;
			}

			//If the command was terminated for whatever reason, report this via the debug text
			public void commandTerminated(int id, String reason) {
				debugText = debugText + "-Setting of the execution permissions of nlscanner binary, terminated, reason: " + reason;
			}
			
		};
		
		//Execute the command
        try {
			RootTools.getShell(false).add(command);
		} catch (IOException e) {
			new AlertDialog.Builder(LowsActivity.this)
			.setTitle("IOException failed")
			.setMessage("IOException")
			.setNeutralButton("okay", null).show();
		} catch (TimeoutException e) {
			new AlertDialog.Builder(LowsActivity.this)
			.setTitle("TimeoutException failed")
			.setMessage("TimeoutException")
			.setNeutralButton("okay", null).show();
		} catch (RootDeniedException e) {
			new AlertDialog.Builder(LowsActivity.this)
			.setTitle("RootDeniedException failed")
			.setMessage("RootDeniedException")
			.setNeutralButton("okay", null).show();
		}
    
    }
	
	
	/**
	 * WifiScanReceiver class, the BroadcastReceiver class that handles what should happen if new Wifi Scan
	 * Results are available, holds the method onReceive, which is called when new scan results were found.
	 * OnReceive then calls the nlscanner binary to get all IEEE 802.11 IE(s)
	 * 
	 * @author Sven Zehl
	 *
	 */
	class WifiScanReceiver extends BroadcastReceiver {
	    
		/**
		 * This method is called when new wifi scan results are available, we simply start the nlscanner
		 * to get all scan results together with all IEEE 802.11 IEs from the driver.
		 */
	    public void onReceive(Context c, Intent intent) 
	    {	
	    	//start nlscanner binary to get all the ScanResults from the driver
	    	startNLscanner();
	    }

	 }
	
	/**
	 * This function executes the nlscanner binary and parses all the output (ScanResults) into
	 * our AccessPoint ArrayList. It afterwards starts the ieParser() function.
	 */
	void startNLscanner()
    {
		//debugText = debugText + "\n-" + "startNLscanner() called.";
		aps = new ArrayList<AccessPoint>();
		//Set new RootTools command that executes the nlscanner binary
		Command command = new Command(0, "/data/data/com.lows/files/nlscanner"){

			//If the nlscanner binary was successfully executed, start the ieParser() function
			public void commandCompleted(int id, int exitCode) {
				ieParser();
			}

			/*The output of the nlscanner binary includes the data of the ScanResults,
			* therefore, this function had to parse the raw output of the nlscanner
			* and put the data into our AccessPoint List.
			*/
			public void commandOutput(int id, String line) {
				//String modifyText = outputText.getText().toString();
				String delimiter = new String();
				int pos;
				
				/*Detect when a new AccessPoint Entry starts (*), when this was detected, add the old AccessPoint
				* entry to the List and allocate a new AccessPoint entry (Only if this is not the first 
				* AccessPoint entry !(#)). 
				*/
				if(line.indexOf("*")==0||line.indexOf("#")==0)
				{
					if(!(line.indexOf("#")==0))
					{
						aps.add(tempAp);
					}
						
					tempAp = new AccessPoint();
				}
				//Save BSS MAC
				else if(line.indexOf(delimiter = "BSS ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setBssid(line.substring( pos + delimiter.length() ));
				}
				//Save freq
				else if(line.indexOf(delimiter = "freq: ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setFreq(  Integer.parseInt(line.substring(pos + delimiter.length())) );
				}
				//Save beacon interval
				else if(line.indexOf(delimiter = "interval: ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setBeaconInterval(  Integer.parseInt(line.substring(pos + delimiter.length())) );	
				}	
				//Save signal in dbm
				else if(line.indexOf(delimiter = "(dBm): ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setSignal(  Double.parseDouble(line.substring(pos + delimiter.length())) );	
				}
				//Save how old the entry is
				else if(line.indexOf(delimiter = "(ms ago): ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setLastSeen(  Integer.parseInt(line.substring(pos + delimiter.length())) );	
				}
				//Save SSID
				else if(line.indexOf(delimiter = "SSID: ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setSsid(line.substring( pos + delimiter.length() ));
				}
				//Save IE data!
				else if(line.indexOf(delimiter = "IE data (hex): ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.addIE(line.substring( pos + delimiter.length() ));		    
				}	
			}

			//If the command was terminated tell this via the debug text + error reason
			public void commandTerminated(int id, String reason) {
				debugText = debugText + "\n-" + "nlscanner terminated, *Scanning Error*" + reason + "*";
			}
			
		};
		
		//Execute the nlscanner binary!
        try {
			RootTools.getShell(false).add(command);
		} catch (IOException e) {
			new AlertDialog.Builder(LowsActivity.this)
			.setTitle("IOException failed")
			.setMessage("IOException when executing nlscanner binary")
			.setNeutralButton("okaay", null).show();
		} catch (TimeoutException e) {
			new AlertDialog.Builder(LowsActivity.this)
			.setTitle("TimeoutException failed")
			.setMessage("TimeoutException when executing nlscanner binary")
			.setNeutralButton("okaay", null).show();
		} catch (RootDeniedException e) {
			new AlertDialog.Builder(LowsActivity.this)
			.setTitle("RootDeniedException failed")
			.setMessage("RootDeniedException when executing nlscanner binary")
			.setNeutralButton("okay", null).show();
		}
    
    }
	/**
	 * The ieParser() function, Information Element Parser
	 * This function is called when the nlscanner binary was successfully executed and the ScanResults are 
	 * successfully parsed into the AccessPoint List. This function parses the AccessPoint List and stores all
	 * found LoWS messages into the LoWS List.
	 */
	public void ieParser()
	{
		if(aps==null)
		{
			debugText = debugText + "\n-" + "ieParser Error, aps==null";
		}
		else
		{
			//Start a new LoWS List, overwrite the old one
			lows = new ArrayList<LoWS>();
			int numberAps = aps.size();
			int i,j;
			int numberIEs;
			AccessPoint tempReadAp;
			//Parse all AccessPoint entries
			for(i=0; i<numberAps; i++)
			{
				tempReadAp = aps.get(i);
				//Get number of IE(s) included in the AccessPoint entry
				numberIEs = tempReadAp.getIESize();
				//Parse all IE(s) of this AccessPoint entry
				for(j=0; j<numberIEs; j++)
				{
					String tempIE = tempReadAp.getIE(j);
					//debugText = debugText + "\n-" + "ieParser: ie: " +tempIE;
					//Look for Cisco CCX Hostname Embedding
					if(tempIE.charAt(0)=='8' && tempIE.charAt(1) == '5')
					{
						//We have a Cisco CCX IE
						//First extract hostname out of IE
						tempIE = tempIE.subSequence(36, 81).toString();
						//Remove spaces
						tempIE = tempIE.replaceAll(" ", "");
						//Now convert to ascii
						StringBuilder output = new StringBuilder();
						for(int p = 0; p < tempIE.length(); p+=2)
						{
							String str = tempIE.substring(p, p+2);
							output.append((char)Integer.parseInt(str, 16));
						}
						//Now look if there is something embedded
						if(output.charAt(10)=='^' && output.charAt(14)=='^')//We have a LoW-S encoding
						{
							//Yes we have a lows embedding, now put it into our lows array list, together with the ap data
							//First extract data out of hostname
							tempIE = tempIE.subSequence((tempIE.length()-8), (tempIE.length()-2)).toString();
							LoWS tempLows = new LoWS(tempReadAp, tempIE, 3); //Lows Cisco Embedding is always 3 Byte
							lows.add(tempLows);
							//debugText = debugText + "\n-" + "added IE Cisco-Embedding: " +tempIE;
						}
						
					}
					//Now look for a vendor specific IE with our aa aa aa OUI
					if(tempIE.charAt(0)=='d' && tempIE.charAt(1) == 'd' && tempIE.charAt(6) == 'a' 
							&& tempIE.charAt(7) == 'a' && tempIE.charAt(9) == 'a' && tempIE.charAt(10) == 'a' 
							&& tempIE.charAt(12) == 'a' && tempIE.charAt(13) == 'a') //Vendor specific element + OUI aaaaaa
					{
						//Yes we have a lows embedding inside a Vendor Specific IE
						//Get the length of the IE
						String tempIELength = tempIE.subSequence(3, 5).toString();
						int tempIELengthInt=Integer.parseInt(tempIELength, 16); //Length is Data + OUI + type
						//Remove all the whitespaces
						tempIE = tempIE.replaceAll(" ", "");
						//Extract the data block
						
						tempIE = tempIE.subSequence(10, (14+((tempIELengthInt-5)*2))).toString();
						
						LoWS tempLows = new LoWS(tempReadAp, tempIE, (tempIELengthInt-3)); //Data_Length = IELength-OUI
						
						lows.add(tempLows);
					
						//debugText = debugText + "\n-" + "added IE Vendor Specific: " +tempIE;
						
					}
					
				}
			}
			//Start the lowsParser() function, when the output processing was completed.
			lowsParser();
		}
	}

	/**
	 * Delete double LoWS entries from LoWS List,
	 * E.g. Cisco APs often broadcast multiple BSSIDs and include the LoWS message
	 * in all beacons from all this BSSs, to not display the same LoWS in the ScanResults 
	 * for the user, the double LoWS have to be removed.
	 */
	public void removeDoubleLows()
	{
		if(lows==null)
		{
			debugText = debugText + "\n-" + "No LoWS found in your current area, removeDoubleLows() terminated.";
			return;
		}
		else
		{
			LoWS tempReadLows;
			LoWS tempCompareLows;
			int toRemove=0;
			for(int l=0; l<lows.size(); l++)
			{
				tempReadLows = lows.get(l);
				String tempLowsData = tempReadLows.getLowsServiceData();
				for(int k=0; k<lows.size(); k++)
				{
					if(k!=l)
					{
						tempCompareLows = lows.get(k);
						String tempCompareData = tempCompareLows.getLowsServiceData();
						if(tempLowsData.equals(tempCompareData))
						{
							Log.i(TAG, "Double LoWS found: "+tempLowsData+"=="+tempCompareData);
							toRemove=k;
							break;
						}
					}
				}
				if(toRemove>0)
				{
					break;
				}
		
				
			}
			if(toRemove>0)
			{
				lows.remove(toRemove);
				removeDoubleLows();
			}
			
		}
	}
	
	/**
	 * The lowsParser() function, this function is called when the IEparser function was successfully executed
	 * and therefore all AccessPoint entries are already parsed into the LoWS List. The lowsParser() now parses
	 * the LoWS List and detects reduced and extended (flexible) LoWS and starts the corresponding parser function
	 * for each LoWS type.
	 */
	public void lowsParser()
	{
		if(lows==null)
		{
			debugText = debugText + "\n-" + "No LoWS found in your current area, lowsParser terminated.";
			return;
		}
		else
		{
			int i;
			LoWS tempReadLows;
			for(i=0; i<lows.size(); i++)
			{
				tempReadLows = lows.get(i);
				String tempLowsData = tempReadLows.getLowsData();
				
				String tempLowsFormatType = tempLowsData.subSequence(0, 2).toString();
				int tempLowsFormatTypeInt=Integer.parseInt(tempLowsFormatType, 16);
				
				//If the first bit of the first byte is not set
				if(tempLowsFormatTypeInt<128)
				{
					//We have a lows in reduced Format, start the reduced LoWS parser function
					parseReducedLows(i, tempLowsFormatTypeInt); //In the reduced format, the format type is also the real type
				}
				//If the first bit of the first byte is set
				else if(tempLowsFormatTypeInt >127)
				{
					//We have a lows in extended (flexible) Format, start the extended (flexible) parser function
					parseExtendedLows(i);
				}
				
			}
			//Remove double LoWS in the LoWS List
			removeDoubleLows();
			//Display results to the user, by updating the result listview
			updateResultListFragmentFromActivity();
		}
	
	}
	
	/**
	 * Parse a reduced LoWS found in the LoWS List
	 * @param i, the position within the LoWS List where the LoWS entry can be found
	 * @param type, the LoWS service type, e.g. BEPS
	 */
	public void parseReducedLows(int i, int type)
	{
		//Get the LoWS from the LoWS List
		LoWS tempReducedLows = lows.get(i);
		//Parse the format type, 0 if not set, 1 if reduced format, 2 if extended format(flexible)
		tempReducedLows.setFormatType(1);
		//Parse the LoWS service type, e.g. BEPS, PSA...
		tempReducedLows.setType(type);
		/*Set the String that should be displayed in the Scan Results, therefore 
		 * call the getDisplayStringFromType() function which will do the mapping according to the LoWS service type
		 * This function will return e.g. "Beacon Emergency Propagation System (BEPS)" if the LoWS service type is 
		 * BEPS.
		 */
		tempReducedLows.setLowsDisplayString(getDisplayStringFromType(1,type));
		//Parse the LoWS service data (extract)
		tempReducedLows.setLowsServiceData(tempReducedLows.getLowsData().subSequence(2, 6).toString());
		//Save the parsed results in the LoWS List (overwrite the original entry)
		lows.set(i, tempReducedLows);		
		//debugText = debugText + "\n-" + "parseReducedLows: " +tempReducedLows.getLowsData();
		
	}
	
	/**
	 * Parse a extended (flexible) LoWS message found in the LoWS List
	 * @param i, position of the LoWS in the LoWS List
	 */
	public void parseExtendedLows(int i)
	{
		//get the LoWS which should be parsed from the LoWS List
		LoWS tempExtendedLows = lows.get(i);
		//Parse the format type, 0 if not set, 1 if reduced format, 2 if extended format(flexible)
		tempExtendedLows.setFormatType(2);
		//Save the parsed results in the LoWS List (overwrite the original entry)
		lows.set(i, tempExtendedLows);
		/*Parse the first byte of the extended (flexible) LoWS. This includes recognition of fragmentation,
		 * signature and encryption. Moreover, fragmentation should be fully managed by the parseExtendeOptionByte()
		 * function.
		 */
		parseExtendedOptionByte(i); 
		//underlying function (parseExtendedOptionByte()) will delete lows from array if it is a fragment and if it is not fully reassembled yet
		
		//Now we know that current LoWS is not a fragment or it is already fully reassembled
		
		//Get the LoWS service type, e.g. BEPS or ST
		parseExtendedType(i);
		//Parse security options
		parseExtendedSecurityByte(i);
		//Decrypt the service data if it was encrypted
		decryptExtendedData(i);
		//Verify the embedded signature
		verifyExtendedSignature(i);
		//Parse the LoWS message payload
		parseExtendedData(i);
		
		//debugText = debugText + "\n-" + "parseExtendedLows: " +tempExtendedLows.getLowsData();
	}
	
	/**
	 * Parse the payload of the extended (flexible) LoWS message
	 * @param i, position of the LoWS message to parse within the LoWS List
	 */
	public void parseExtendedData(int i) {
		//Get the LoWS from LoWS List
		LoWS tempExtendedLows = lows.get(i);
		//Get the length of the data
		int extendedTypeDataLength = tempExtendedLows.getLowsData().length();
		//Get the start position
		int startOfServiceData = tempExtendedLows.getBeginOfServiceData();
		//Extract service data 
		String extendedLowsServiceData = tempExtendedLows.getLowsData().subSequence(startOfServiceData, extendedTypeDataLength).toString();
		//Store data
		tempExtendedLows.setLowsServiceData(extendedLowsServiceData);
		//Save modifications
		lows.set(i, tempExtendedLows);
	}
	
	/**
	 * Verify the embedded signature
	 * TODO:	-Implement this functionality
	 * @param i, position of the corresponding LoWS within the LoWS list
	 */
	public void verifyExtendedSignature(int i) {
		// Implement this functionality here!
	}
	
	/**
	 * Decrypt the LoWS service data
	 * TODO: 	-Implement this functionality
	 * @param i, position of the corresponding LoWS within the LoWS List
	 */
	public void decryptExtendedData(int i) {
		//Implement this functionality here!
	}
	
	/**
	 * Parse the security options of the extended (flexible) LoWS message
	 * TODO: -Implement detection if a signature was embedded
	 * 		 -Implement detection if the service data is encrypted
	 * 		 -Implement detection of the security settings, e.g. which kind of encryption or which kind of
	 * 		  signature.
	 * 
	 * @param i, position of the LoWS within the LoWS list
	 */
	public void parseExtendedSecurityByte(int i)
	{
		//Get the LoWS from list
		LoWS tempExtendedLows = lows.get(i);
		//If security is present, the parseExtendedOptionByte() func. should recognized this
		if(tempExtendedLows.isEncryptionIsPresent())
		{
			//process Encryption stuff
		}
		if(tempExtendedLows.isSignatureIsPresent())
		{
			//process Signature stuff
		}
	}
	
	/**
	 * Function for parsing the extended (flexible) LoWS service types,
	 * e.g. BEPS or ST
	 * @param i, position of the LoWS to parse within the LoWS List
	 * 
	 * TODO: -Implement LoWS service type parsing if a sequence number is present
	 * 		 -Implement LoWS service type parsing if the LoWS service type is longer
	 * 		  than one byte.
	 * 
	 */
	public void parseExtendedType(int i)
	{
		//Get the LoWS Entry
		LoWS tempExtendedLows = lows.get(i);
		//Get the LoWS data
		String tempLowsData = tempExtendedLows.getLowsData();
		if(tempExtendedLows.isSeqNumberIsPresent())
		{
			//Length of Seq# must be determined, because Type follows after the Seq#
			//Implement this functionality here
		}
		else
		{
			//We have no Seq# the type starts with the second byte of the LoWS data.
			//Get the second byte (3. and 4. hex character) of the LoWS message entry
			String tempLowsType = tempLowsData.subSequence(2, 4).toString();
			//parse hex string to Integer
			int tempLowsTypeInt=Integer.parseInt(tempLowsType, 16);
			//If first bit of the byte is not set, the type number consists only of one byte
			if(tempLowsTypeInt<128)
			{
				//Next Byte does not belong to the type, tempLowsTypeInt is the type of the LoWS
				tempExtendedLows.setType(tempLowsTypeInt);
				tempExtendedLows.setEndOfType(4); 
				//Till the next byte isn't also a type field, the end of type is at byte position 2
				//Now that we know the Type, we can set the DisplayString which will be displayed in 
				//the Scan Result fragment.
				tempExtendedLows.setLowsDisplayString(getDisplayStringFromType(2,tempLowsTypeInt));
				if(!tempExtendedLows.isEncryptionIsPresent() && !tempExtendedLows.isSignatureIsPresent())
				{
					tempExtendedLows.setBeginOfServiceData(4);
				}
				//Save the modifications
				lows.set(i, tempExtendedLows);
			}
			//The first bit of the byte is set, the next byte also belongs to the service type 
			else
			{
				//Next Byte belongs also to the Type
				//Parsing of the lows must be continued till the byte is smaller than 128.
			}
		}
	}
	
	
	/**
	 * This function should handle the fragmentation,
	 * but currently we not support fragmentation
	 * TODO: - Implement the Fragmentation functionality here!
	 * 		 - Implement recognition of Signature Embedding and Encryption
	 * @param i, position of the LoWS within the LoWS List
	 */
	public void parseExtendedOptionByte(int i)
	{
		//LoWS tempExtendedLows = lows.get(i);
		//parse the options
		//parse SequenceNumber if Fragmented here by calling the Fragmented Function!!
		//If fragmented, LoWS must be deleted from lows array and stored somewhere else till LoWS is completed
		//(if all fragments were received) afterwards the complete LoWS should be stored in lows array.
		//If fragmented and not fully reassembled, function should exit processing here!
	}
	
	
	
	/**
	 * LoWS Type (e.g BEPS) to Display String Mapping Function
	 * @param formatType, reduced or extended (flexible) LoWS type
	 * @param type, LoWS service type, e.g. BEPS
	 * @return String that should be displayed for the corresponding LoWS service
	 */
	String getDisplayStringFromType(int formatType, int type)
	{
		String returnValue="failure in getDisplayStringFromType() func@LoWSActivity.java";
		if(formatType==1)
		{	
			int lowsRedTypeSize = lowsRedType.size();
			int j=0;
			LoWSReducedType tempRedType;
			for(j=0; j<lowsRedTypeSize; j++)
			{
				tempRedType = lowsRedType.get(j);
				if(tempRedType.getTypeNumber()==type)
				{
					returnValue = "" + tempRedType.getDisplayString();
					j=lowsRedTypeSize;
				}
				else
				{
					returnValue= "Unsupported Type "+tempRedType.getTypeNumber();
				}
			}		
		}
		else if(formatType==2)
		{
			int lowsExtTypeSize = lowsRedType.size();
			int j=0;
			LoWSExtendedType tempExtType;
			for(j=0; j<lowsExtTypeSize; j++)
			{
				tempExtType = lowsExtType.get(j);
				if(tempExtType.getTypeNumber()==type)
				{
					returnValue = "" + tempExtType.getDisplayString();
					j=lowsExtTypeSize;
				}
				else
				{
					returnValue = "Unsupported Type "+tempExtType.getTypeNumber();
				}
			}	
		}
		else
		{
			debugText = debugText + "\n-" + "Error getting DisplayString for formatType which is not 1 or 2";
			returnValue = "FormatType not supported";
		}
		return returnValue;
	}
	
	
	
	

	/**
	 * Update ListView in first fragment (Scan Result ListView)
	 */
	private void updateResultListFragmentFromActivity()
	{
		String fragmentTag = makeFragmentName(R.id.pager, 0);
		if(fragmentTag==null)
		{
			return;
		}
		ResultListFragment fragment = (ResultListFragment) getFragmentManager().findFragmentByTag(fragmentTag);
		if(fragment ==null)
		{
			return;
		}
		else
		{
			//Call function to update ListView
			fragment.updateListView();
		}
	}
	
	private static String makeFragmentName(int viewId, int index)
	{
	     return "android:switcher:" + viewId + ":" + index;
	     //where viewId is id of ViewPager
	}
	
	
	/**
	 * Start the Background Scan Service, if already running, cancel the AlarmManager 
	 * (allows the periodic execution), transmit all search strings and display strings
	 * and start the periodic execution via the AlarmManager.
	 * 
	 */
	public void startChangeBackgroundScanService()
	{
		backgroundScannerStartedState=true;
		debugText = debugText + "\n-startChangeBackgroundScanService() triggered...";
		//Toast.makeText(getApplicationContext(), "LoWSActivity: startChangeBackgroundScanService()", Toast.LENGTH_SHORT).show();
	    if(BackgroundScannerPendingIntent!=null)
	    {
	    	alarm.cancel(BackgroundScannerPendingIntent);
	    }
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
	

	
	/*************************************************************************/
	//Supported Types//
	/**
	 * Function for loading all currently supported LoWS types
	 * 
	 * @param void
	 * @return void
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
	
	
	
	
	
	public static class PrefsFragment extends PreferenceFragment {
		 
        @Override
        public void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);

                    // Load the preferences from an XML resource
                    addPreferencesFromResource(R.xml.lowspreference);
        }
}
	
	
	
}