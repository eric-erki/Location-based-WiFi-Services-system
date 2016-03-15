package com.lows;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;



/**
 * This class implements the LoWS Background Scanner Service, this IntentService is executed periodically 
 * by the AlarmManager which is set within the LowsActivity class.
 *
 * TODO: 	-find reason why the background scanner crashes sometimes
 * 			-move display and search string storage to sqlite
 * 			-Try to find a way to prevent parsing the Scan Results twice, currently we parse them within the 
 * 			 LowsActivity and within this class.
 * 			-Same as in the LowsActivity, the extended (flexible) LoWS parsing is not complete,
 * 			 currently fragmentation, encryption, signature validation and extended type extraction
 * 			 is not supported, when a way has found to only parse the data once for the BackgroundScanner
 * 			 and the Main LowsActivity, then this should be done.
 * 
 * @author Sven Zehl
 *
 * 
 */
public class LowsBackgroundAlarmScanner extends IntentService {

	private static final String TAG = "com.lows.LowsBackgroundAlarmScanner";
	//search strings
	private String[] searchNCompareData;
	private int searchNCompareDataLength;
	//display strings if a match occured
	private String[] alarmMessagesData;
	private int alarmMessagesDataLength;
	//Handler to access the Wifi API
	WifiManager mainWifiObj;
	//Broadcast Receiver for new Wifi Scan Results
	WifiBackgroundScanReceiver wifiRecieverBackground;
	private boolean scanFinished = false;
	//AccessPoint List
	private List<AccessPoint> aps;
	private AccessPoint tempAp;
	//LoWS List
	private List<LoWS> lows;
	
	@Override
	protected void onHandleIntent(Intent arg0) {
		Log.i(TAG, "Background Scanner Intent Service started");
		//Get the searchStrings and the DisplayStrings
		searchNCompareData=arg0.getStringArrayExtra("searchNCompareData");
		alarmMessagesData=arg0.getStringArrayExtra("alarmMessagesData");
		searchNCompareDataLength = searchNCompareData.length;
		alarmMessagesDataLength = alarmMessagesData.length;
		if(searchNCompareDataLength!=alarmMessagesDataLength)
		{
			Log.i(TAG, "Intent Service Error != ");
		}
		else if(searchNCompareDataLength==0)
		{
			Log.i(TAG, "Intent Service Error ==0");
		}
		else
		{
			//what else?
		}
		//Start the Background Scanner 
		scanNsearch();

	}

	public LowsBackgroundAlarmScanner() {
	 	   super("LowsBackgroundAlarmScanner");
	}
	
	/**
	 * This function starts the IEEE 802.11 scanning process via the standard Android Wifi API
	 */
	void scanNsearch()
	{
		//Get the Wifi System Service
		mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//New Broadcast Receiver
		wifiRecieverBackground = new WifiBackgroundScanReceiver();
		registerReceiver(wifiRecieverBackground, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		//Start the IEEE 802.11 scan via the Wifi System Service APO
		mainWifiObj.startScan(); 
		/*Only exit this function when the scan was completed, if this is not done, the BackgroundScanner 
		 * is terminated before the scan was complete!
		 */
		waitForScanFinished();
		
	}

	/**
	 * WifiScanReceiver, the BroadcastReceiver class for the IEEE 802.11 scan
	 * 
	 * @author Sven Zehl
	 *
	 */
	class WifiBackgroundScanReceiver extends BroadcastReceiver {

		public void onReceive(Context c, Intent intent) 
		{	
		    //start nlscanner binary to get all the ScanResults from the driver
			Log.i(TAG, "Background Scanner Received an WIFI Alert!");
			//start the nlscanner binary
			startNLscanner();
			//notify that the scan was completed
			scanFinished();
		}

	}
	
	@Override
	public void onDestroy()
	{
		unregisterReceiver(wifiRecieverBackground);
		super.onDestroy();
	}
	
	/**
	 * This function waits till the IEEE 802.11 scan was finished,
	 * this is needed to keep the BackgroundScanner alive
	 */
    public void waitForScanFinished(){
        synchronized(this){
            while(!scanFinished){
                try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
    }
    
    /**
     * This function is called when the full IEEE 802.11 scan was completed (incl. nlscanner binary call)
     * the function simply sets the variable scanFinished to true and therefore ends the waitForScanFinsihed()
     * function.
     */
    public void scanFinished(){
        synchronized(this){
             this.scanFinished = true;
             notifyAll();
        }
    }
    
    /**
     * Start the nlscanner binary and parse the data
     */
    void startNLscanner()
    {
    	//initialize the AccessPoint List
		aps = new ArrayList<AccessPoint>();
		//Set the RootTools Command
		Command command = new Command(0, "/data/data/com.lows/files/nlscanner"){
			@Override
			//Start ieParser() if the nlscanner succeeded
			public void commandCompleted(int id, int exitCode) {
				Log.i(TAG, "nlscanner sucessfully executed!");
				ieParser();
			}

			@Override
			//Parse the output of the nlscanner binary and fill the AccessPoint array
			public void commandOutput(int id, String line) {
				//String modifyText = outputText.getText().toString();
				String delimiter = new String();
				int pos;
				//Recognition of the first and all new access point entries
				if(line.indexOf("*")==0||line.indexOf("#")==0)
				{
					if(!(line.indexOf("#")==0))
					{
						aps.add(tempAp);
					}
						
					tempAp = new AccessPoint();
				}
				else if(line.indexOf(delimiter = "BSS ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setBssid(line.substring( pos + delimiter.length() ));
				}
				else if(line.indexOf(delimiter = "freq: ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setFreq(  Integer.parseInt(line.substring(pos + delimiter.length())) );
				}
				else if(line.indexOf(delimiter = "interval: ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setBeaconInterval(  Integer.parseInt(line.substring(pos + delimiter.length())) );	
				}	
				else if(line.indexOf(delimiter = "(dBm): ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setSignal(  Double.parseDouble(line.substring(pos + delimiter.length())) );	
				}
				else if(line.indexOf(delimiter = "(ms ago): ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setLastSeen(  Integer.parseInt(line.substring(pos + delimiter.length())) );	
				}
				else if(line.indexOf(delimiter = "SSID: ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.setSsid(line.substring( pos + delimiter.length() ));
				}
				else if(line.indexOf(delimiter = "IE data (hex): ")!=-1)
				{
					pos = line.indexOf(delimiter);
				    tempAp.addIE(line.substring( pos + delimiter.length() ));		    
				}	
			}

			@Override
			public void commandTerminated(int id, String reason) {
				Log.i(TAG, "nlscanner terminated!, reason: " + reason);
			}
			
		};
		
		//Execute nlscanner
        try {
			RootTools.getShell(false).add(command);
		} catch (IOException e) {
			Log.i(TAG, "nlscanner IO exception!");
		} catch (TimeoutException e) {
			Log.i(TAG, "nlscanner Timeout Exception");
			
		} catch (RootDeniedException e) {
			Log.i(TAG, "nlscanner root denied Exception!");
		}
    
    }
    
    /**
     * Parse the Information Elements stored in the AccessPoint array
     */
    void ieParser()
	{
		if(aps==null)
		{
			Log.i(TAG, "aps array == null, ieParser() cant do anything....");;
		}
		else
		{
			lows = new ArrayList<LoWS>();
			int numberAps = aps.size();
			int i,j;
			int numberIEs;
			AccessPoint tempReadAp;
			for(i=0; i<numberAps; i++)
			{
				tempReadAp = aps.get(i);
				numberIEs = tempReadAp.getIESize();
				for(j=0; j<numberIEs; j++)
				{
					String tempIE = tempReadAp.getIE(j);
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
			Log.i(TAG, "ieParser() Finished");
			//start the LoWS Parser
			lowsParser();
			//start to compare the found LoWS with the search strings
			compareResultWithSearchStrings();
		}
	}
    
    /**
     * This function parses the LoWS and extracts the LoWS raw data
     * Beside, this function starts the Broadcast Intent to notify other applications
     * that new LoWS messages have been found. Further the service data is sent within the Intent
     */
    public void lowsParser()
	{
		if(lows==null)
		{
			//debugText = debugText + "\n-" + "No LoWS found in your current area, lowsParser terminated.";
			return;
		}
		else
		{
			int numberLows = lows.size();
			int i;
			LoWS tempReadLows;
			for(i=0; i<numberLows; i++)
			{
				tempReadLows = lows.get(i);
				String tempLowsData = tempReadLows.getLowsData();
				
				//Send Broadcast to enable other applications to use the lows data
				Intent broadcastIntent = new Intent();
				broadcastIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				broadcastIntent.setAction("com.lows.newlows");
				broadcastIntent.putExtra("lows", tempLowsData);
				sendBroadcast(broadcastIntent);

				String tempLowsFormatType = tempLowsData.subSequence(0, 2).toString();
				int tempLowsFormatTypeInt=Integer.parseInt(tempLowsFormatType, 16);
				
				if(tempLowsFormatTypeInt<128)
				{
					//We have a lows in reduced Format
					parseReducedLows(i, tempLowsFormatTypeInt); 
					//In the reduced format, the format type is also the real type
				}
				else if(tempLowsFormatTypeInt >127)
				{
					//We have a lows in extended (flexible) Format
					parseExtendedLows(i);
				}
				
			}
		}
	
	}
	
    /**
     * Parse service data of the reduced LoWS message
     * @param i, index within the LoWS List
     * @param type, LoWS type, e.g. BEPS
     */
	public void parseReducedLows(int i, int type)
	{
		LoWS tempReducedLows = lows.get(i);
		tempReducedLows.setFormatType(1);
		tempReducedLows.setType(type);
		tempReducedLows.setLowsServiceData(tempReducedLows.getLowsData().subSequence(2, 6).toString());
		lows.set(i, tempReducedLows);
		
	}
	
	/**
	 * Parse service data of extended (flexible) LoWS message
	 * @param i, index within the LoWS List
	 */
	public void parseExtendedLows(int i)
	{
		LoWS tempExtendedLows = lows.get(i);
		tempExtendedLows.setFormatType(2);
		lows.set(i, tempExtendedLows);
		parseExtendedOptionByte(i); 
		//underlying function will delete lows from array if it is a fragment and if it is not fully reassembled yet
		//Now we know that current LoWS is not a fragment or it is already fully reassembled
		parseExtendedType(i);
		parseExtendedSecurityByte(i);
		encryptExtendedData(i);
		verifyExtendedSignature(i);
		parseExtendedData(i);
		
		//debugText = debugText + "\n-" + "parseExtendedLows: " +tempExtendedLows.getLowsData();
	}
	
	public void parseExtendedData(int i) {
		LoWS tempExtendedLows = lows.get(i);
		int extendedTypeDataLength = tempExtendedLows.getLowsData().length();
		int startOfServiceData = tempExtendedLows.getBeginOfServiceData();
		String extendedLowsServiceData = tempExtendedLows.getLowsData().subSequence(startOfServiceData, extendedTypeDataLength).toString();
		tempExtendedLows.setLowsServiceData(extendedLowsServiceData);
		lows.set(i, tempExtendedLows);
	}

	public void verifyExtendedSignature(int i) {
		// TODO Auto-generated method stub
		
	}

	public void encryptExtendedData(int i) {
		// TODO Auto-generated method stub
		
	}

	public void parseExtendedSecurityByte(int i)
	{
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
	
	public void parseExtendedType(int i)
	{
		LoWS tempExtendedLows = lows.get(i);
		String tempLowsData = tempExtendedLows.getLowsData();
		if(tempExtendedLows.isSeqNumberIsPresent())
		{
			//Length of Seq# must be determined, because Type follows after the Seq#
		}
		else
		{
			//We have no Seq# the type starts with the second byte of the LoWS data.
			String tempLowsType = tempLowsData.subSequence(2, 4).toString();
			int tempLowsTypeInt=Integer.parseInt(tempLowsType, 16);
			if(tempLowsTypeInt<128)
			{
				//Next Byte does not belong to the type, tempLowsTypeInt is the type of the LoWS
				tempExtendedLows.setType(tempLowsTypeInt);
				tempExtendedLows.setEndOfType(4); //Till the next byte isn't also a type field, the end of type is at byte position 2
				//Now that we know the Type, we can set the DisplayString which will be displayed in the Scan Result fragment.
				//tempExtendedLows.setLowsDisplayString(getDisplayStringFromType(2,tempLowsTypeInt));
				if(!tempExtendedLows.isEncryptionIsPresent() && !tempExtendedLows.isSignatureIsPresent())
				{
					tempExtendedLows.setBeginOfServiceData(4);
				}
				lows.set(i, tempExtendedLows);
			}
			else
			{
				//Next Byte belongs also to the Type
				//Parsing of the lows must be continued till the byte is smaller than 128.
			}
		}
	}
	
	
	
	public void parseExtendedOptionByte(int i)
	{
		//LoWS tempExtendedLows = lows.get(i);
		//parse the options
		//parse SequenceNumber if Fragmented here by calling the Fragmented Function!!
		//If fragmented, LoWS must be deleted from lows array and stored somewhere else till LoWS is completed
		//(if all fragments were received) afterwards the complete LoWS should be stored in lows array.
		//If fragmented and not fully reassembeld, function should exit processing here!
	}
	
    
    
    
    
    /**
     * This function is called after all LoWS messages have been parsed.
     * The function now compares all the stored LoWS service data with the search strings that have been
     * submitted before. If a match occured a notification is started via the function sendNotification()
     */
    void compareResultWithSearchStrings()
    {
    	int i=0;
    	int j=0;
    	LoWS searchLow;
    	for(i=0; i< lows.size(); i++)
    	{
    		searchLow=lows.get(i);
    		for(j=0;j<searchNCompareDataLength; j++)
    		{
    			if(searchNCompareData[j].equals(searchLow.getLowsData()))
    			{
    				Log.i(TAG, "MATCH FOUND! " + searchNCompareData[j] + "==" + searchLow.getLowsData());
    				Log.i(TAG, "String to display: " + alarmMessagesData[j]);
    				sendNotification(alarmMessagesData[j], searchNCompareData[j], searchLow);
    			}
    		}
    	}
    }
    
    
    /**
     * This function is called from the compareResultWithSearchStrings() function if a match with the
     * search strings occured.
     * 
     * @param displayMessage, The message the should be displayed within the notification
     * @param matchString, the string that caused the match
     * @param matchLows, the LoWS that corresponds to the match
     */
    void sendNotification(String displayMessage, String matchString, LoWS matchLows)
	{
		int mID=2;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
	    .setSmallIcon(R.drawable.ic_launcher)
	    .setContentTitle("LoW-S Alarm")
	    .setContentText(displayMessage);
		
		//Prepare the Intent that starts the AlarmClickActivity when the user taps on the notification
		Intent resultIntent = new Intent(this, AlarmClickActivity.class);
		resultIntent.putExtra("matchString", matchString);
		resultIntent.putExtra("displayMessage", displayMessage);
		resultIntent.putExtra("formatType", matchLows.getFormatType());
		resultIntent.putExtra("BSSID", matchLows.getBssid());
		resultIntent.putExtra("SSID", matchLows.getSsid());
		resultIntent.putExtra("signalStrength", matchLows.getSignalStrength());
		resultIntent.putExtra("frequency", matchLows.getFrequency());
		resultIntent.putExtra("type", matchLows.getType());
		resultIntent.putExtra("serviceData", matchLows.getLowsServiceData());
		resultIntent.putExtra("alarmMessagesData", alarmMessagesData);
		resultIntent.putExtra("searchNCompareData", searchNCompareData);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		//Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(AlarmClickActivity.class);
		//Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationCompat.InboxStyle inboxStyle =
	        new NotificationCompat.InboxStyle();
		// Sets a title for the Inbox in expanded layout
		inboxStyle.setBigContentTitle("LoW-S Alarm");
	    inboxStyle.addLine(displayMessage);
		mBuilder.setStyle(inboxStyle);
		//mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
		Notification note = mBuilder.build();
	    note.defaults |= Notification.DEFAULT_VIBRATE;
	    note.defaults |= Notification.DEFAULT_LIGHTS;
	    note.defaults |= Notification.DEFAULT_SOUND;
	    note.defaults |= Notification.PRIORITY_MAX;
	    note.flags |= Notification.FLAG_AUTO_CANCEL;
	    //mId would allow to update the notification later on, we do not need it at the moment
	    mNotificationManager.notify(mID, note);
	}
}