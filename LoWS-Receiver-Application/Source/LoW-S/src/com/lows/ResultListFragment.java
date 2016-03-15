package com.lows;


//import java.util.Arrays;
//import java.util.HashSet;
import java.util.List;
//import java.util.Set;

 

import android.app.Fragment;
import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import android.widget.AdapterView.OnItemClickListener;
/**
 * ListView Fragment which is displayed when LoWS Main Receiver Application starts.
 * All LoWS messages currently found in the vicinity are displayed within this fragment.
 *
 * TODO: The Processing of the searchStrings should be made more flexible for adding new LoWS types.
 * 
 * @author Sven Zehl
 * 
 * @version 1.0
 *
 * 
 */
public class ResultListFragment extends Fragment {
	
	String[] typeText = new String[0];
	String[] serviceText = new String[0];
	String[] iconName = new String[0];
	String[] searchText = new String[0];
	boolean[] showAlarmSwitch = new boolean[0];
	boolean[] alarmInitialState = new boolean[0];
	boolean[] showAlarmSearchField = new boolean[0];
	boolean globalBepsEnabled=true;
	public static final String PREFS_NAME = "lowsPersistentData";
	private static final String TAG = "com.lows.ResultListFragment";
	int number=0;
	LowsAdapter dataAdapter;
	private List<LoWS> lows;
	private List<LoWSReducedType> lowsRedType;
	private List<LoWSExtendedType> lowsExtType;
	String[] searchNCompareData;
	String[] alarmMessagesData;
	
	ProgressDialog dialog;
	
	@Override
 	public void onCreate(Bundle savedInstanceState) {
	 super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
	 super.onActivityCreated(savedInstanceState);
   
	 //Restore preferences
     SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
     //BEPS globally enabled?
     globalBepsEnabled = settings.getBoolean("globalBepsEnabled", true);
	 //Generate list View from ArrayList
	 displayListView();
	 
   
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
   Bundle savedInstanceState) {
	 	View view = inflater.inflate(R.layout.result_list_fragment, container, false);
	 	return view;
 	}
 	
 	
 	 @Override
	public void onStop(){
        super.onStop();

       //Save the settings in the shared preferences (the persistent application memory)
       SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
       SharedPreferences.Editor editor = settings.edit();
       editor.putBoolean("globalBepsEnabled", globalBepsEnabled);
       editor.putBoolean("backgroundScannerStartedState", ((LowsActivity)getActivity()).getBackgroundScannerStartedState());
       
       //Save the SearchStrings and DisplayStrings Arrays by putting them into one comma separated String 
       searchNCompareData= ((LowsActivity)getActivity()).getSearchNCompareDataArray();
       alarmMessagesData = ((LowsActivity)getActivity()).getAlarmMessagesDataArray();
       
       //Save zero if no search or display strings are available
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
     }
  
 	 
 	/**
 	 *  This function is executed when the fragment is loaded the first time.
 	 *  It displays the Scanning in process message.
 	 */
 	private void displayListView() {
	 		//Show Scanning in process dialog until updateListView() function is called
	 		dialog = ProgressDialog.show(getActivity(), "LoWS scanning in progress", "Scanning for Location-based Wifi Services in your area...", true);
	 		
	 		
	 		//LoWS tempLows;
	 		//Get the LoWS List from the LoWSActivity 
	 		lows=(((LowsActivity)getActivity()).getLowsArray());
	 		
	 		//Get the supported LoWS reduced types from the LoWSActivity
	 		lowsRedType=((LowsActivity)getActivity()).getLoWSReducedTypeArray();
	 		//Get the supported LoWS extended (flexible) types from the LoWSActivity
	 		lowsExtType=((LowsActivity)getActivity()).getlowsExtendedTypeArray();
	 		
	 		//Get the search strings from the LoWSActivity
	 		searchNCompareData= ((LowsActivity)getActivity()).getSearchNCompareDataArray();
	 		//Get the display strings from the LoWSActivity
	 		alarmMessagesData = ((LowsActivity)getActivity()).getAlarmMessagesDataArray();
	 		
	 		//Create new ArrayAdapter
	 		dataAdapter = new LowsAdapter(getActivity(), lows, typeText, serviceText, iconName);
			 
	 		ListView listView = (ListView) getView().findViewById(R.id.lows_list_view);
			// Assign adapter to ListView
	 		listView.setAdapter(dataAdapter);
		
 	}
 	
 	/**
 	 * This function is called when new scan results were successfully parsed
 	 * The function is called from the updateResultListFragmentFromActivity() from the LoWSActivity class
 	 * It updates the ListView with all currently found LoWS in vicinity
 	 */
 	public void updateListView()
 	{
 		//setListShown(false);
 		//Get ListView
 		ListView listView = (ListView) getView().findViewById(R.id.lows_list_view);
 		
 		if(listView==null)
 		{
 			return;
 		}
 		
 		LoWS tempLows;
 		//Get the LoWS List from LowsActivity
 		lows=(((LowsActivity)getActivity()).getLowsArray());
 		
 		//Get the supported reduced LoWS types from LowsActivity
 		lowsRedType=((LowsActivity)getActivity()).getLoWSReducedTypeArray();
 		//Get the supported extended (flexible) LoWS types from LowsActivity
 		lowsExtType=((LowsActivity)getActivity()).getlowsExtendedTypeArray();
 		
 		int lowsSize;
 		if(lows!=null)
 		{
		  lowsSize = lows.size();
 		}
 		else
 		{
 			lowsSize = 0;
 		}
 		//Initialize all data needed to display the single entries
 		
 		//Text which is displayed because of the LoWS Type e.g. BEPS
		typeText = new String[lowsSize];
		//Text which is displayed if the LoWS message data is correctly decoded
		serviceText = new String[lowsSize];
		//Name of the Icon which should be displayed together with the LoWS
		iconName = new String[lowsSize];
		//Should the alarm switch be displayed within the detailed view of the LoWS
		showAlarmSwitch = new boolean[lowsSize];
		//Should the Search Field be displayed within the detailed view of the LoWS
		showAlarmSearchField = new boolean[lowsSize];
		//What should be displayed together with the search field
		searchText = new String[lowsSize];
		//Alarm initially turned on?
		alarmInitialState = new boolean[lowsSize];
		
		//Process all LoWS in LoWS List
		for(int i=0; i<lowsSize; i++)
		{
			tempLows = lows.get(i);
			typeText[i]= tempLows.getLowsDisplayString();
			int tempType=tempLows.getType();
			int tempFormatType =tempLows.getFormatType();
			if(tempFormatType==1){
			for(int p=0; p<lowsRedType.size(); p++)
			{
				LoWSReducedType tempRedType=lowsRedType.get(p);
				if(tempType==tempRedType.getTypeNumber())
				{
					serviceText[i]=tempRedType.decodeData(tempLows.getLowsServiceData());
					iconName[i]=tempRedType.getIconName();
					showAlarmSwitch[i]=tempRedType.showAlarmSwitch();
					showAlarmSearchField[i]=tempRedType.showSearchFieldSwitch();
					alarmInitialState[i]=tempRedType.getAlarmStartState();
					searchText[i]=tempRedType.getSearchFieldDescription();
					}
				}
				if(serviceText[i]==null)
				{
					serviceText[i]="Service not found, service data: "+tempLows.getLowsServiceData();
				}
			}
			if(tempFormatType==2)
			{
				  
				  for(int p=0; p<lowsExtType.size(); p++)
				  {
					  LoWSExtendedType tempExtType=lowsExtType.get(p);
					  if(tempType==tempExtType.getTypeNumber())
					  {
						  serviceText[i]=tempExtType.decodeData(tempLows.getLowsServiceData());
						  iconName[i]=tempExtType.getIconName();
						  showAlarmSwitch[i]=tempExtType.showAlarmSwitch();
						  showAlarmSearchField[i]=tempExtType.showSearchFieldSwitch();
						  searchText[i]=tempExtType.getSearchFieldDescription();
						  alarmInitialState[i]=tempExtType.getAlarmStartState();
					  }
				  }
				  if(serviceText[i]==null)
				  {
					  serviceText[i]="Service not found, service data: "+tempLows.getLowsServiceData();
				  }
			  }
		}
	
		  
 		dataAdapter.notifyDataSetChanged();
 		//Create new adapter and submit all the data generated out of the LoWS List to it
 		dataAdapter = new LowsAdapter(getActivity(), lows, typeText, serviceText, iconName);
 		//Set new adapter
 		listView.setAdapter(dataAdapter);
 		
 		//Generate on click listener which is called whenever the user taps on a single entry
 		listView.setOnItemClickListener(new OnItemClickListener() {
			  public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
				  
				  //Start the ClickActivity which shows the details about the specific LoWS the user tapped on.
				  
				  //New Intent with ClickActivity.class
				  Intent intent = new Intent(getActivity().getApplicationContext(), ClickActivity.class);
				  //Data which should be delivered to the ClickActivity
				  Bundle b = new Bundle();
				  //Position within the ListView
				  b.putInt("position", position);
				  //The corresponding type text e.g. BEPS
				  b.putString("type", typeText[position]);
				  //The corresponding serviceText
				  b.putString("data", serviceText[position]);
				  //Should the alarm switch be displayed in the ClickActivity
				  b.putBoolean("showAlarmSwitch",showAlarmSwitch[position]);
				  //Should the search field be displayed in the ClickActivity
				  b.putBoolean("showAlarmSearchField", showAlarmSearchField[position]);
				  //If yes how should the search field be tagged
				  b.putString("searchText", searchText[position]);
				  LoWS tempLows = lows.get(position);
				  //We also deliver all the data of the corresponding LoWS 
				  b.putString("serviceData",tempLows.getLowsServiceData());
				  b.putInt("serviceType", tempLows.getType());
				  b.putInt("formatType", tempLows.getFormatType());
				  AccessPoint apData = tempLows.getApData();
				  //And part of the corresponding AccessPoint Data
				  b.putString("mac", apData.getBssid());
				  b.putDouble("rssi", apData.getSignal());
				  if(tempLows.getType()==33 && tempLows.getFormatType()==1)//BEPS
				  {
					  b.putBoolean("AlarmInitialState", globalBepsEnabled);
				  }
				  else
				  {
					  b.putBoolean("AlarmInitialState", alarmInitialState[position]);
				  }
				  //Put all the data within the Intent
				  intent.putExtras(b); 
				  //start the new ClickActivity class
				  startActivityForResult(intent, 1);
			  }
		  });
 		dialog.cancel();
 	}
 	
 	
 	/**
 	 * This function is called when the user taps the return button for leaving the ClickActivity
 	 * It is used to process the settings the user did within the ClickActivity, e.g. setting search strings
 	 * 
 	 */
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		if (requestCode == 1) {
	        if(resultCode == -1){
	        	//debugText = debugText + "\n-" + "onActivityResult Frag RESULT_OK!!!";
	            boolean alarmSet = data.getBooleanExtra("alarmSet", false);
	            int position = data.getIntExtra("position", -1);
	            String searchTextReturn = data.getStringExtra("searchTextReturn");
	            //debugText = debugText + "\n-" + "onActivityResult Frag alarmSet= " +alarmSet + "position: " + position;
	            if(position >=0)
	            {
	            	LoWS tempLows = lows.get(position);
	            	
	            	/*This should be made somehow put into the Type class*/
	            	if(tempLows.getType()==33 && tempLows.getFormatType()==1)
	            	{
	            		addRemoveBepsSearchStrings(alarmSet, tempLows);
	            	}//
	            	if(tempLows.getType()==16 && tempLows.getFormatType()==2)
	            	{
	            		addRemoveWtnsSearchStrings(alarmSet, tempLows, searchTextReturn);
	            	}
	            }
	            
	        }
	            
	        
	        if (resultCode == 0) {

	        }
	    }
 		//dialog.cancel();
	    //((LowsActivity)getActivity()).appendDebugText(debugText);
	}//onActivityResult
 	
 	
 	/**
 	 * This function is called from onActivityResult() when BEPS notifications should be enabled or disabled, 
 	 * TODO: -this function should be improved when the search and display string management is moved to sqlite.
 	 * 
 	 * @param alarmSet, true if the BEPS notifications should be turned on
 	 * @param tempLows, the LoWS which caused the change
 	 */
 	void addRemoveBepsSearchStrings(boolean alarmSet, LoWS tempLows)
 	{
 		
 		for(int p=0; p<lowsRedType.size(); p++)
		  	{
			  LoWSReducedType tempRedType=lowsRedType.get(p);
			  if(tempLows.getType()==tempRedType.getTypeNumber())
			  {
				BEPSLoWSReducedType bepsType = (BEPSLoWSReducedType) tempRedType;
				if(alarmSet==true)
				{
					Log.i(TAG, "BEPS AlarmSet=true!");
					searchNCompareData= ((LowsActivity)getActivity()).getSearchNCompareDataArray();
					alarmMessagesData = ((LowsActivity)getActivity()).getAlarmMessagesDataArray();
					globalBepsEnabled=true;
    			
	    			if(alarmMessagesData==null || searchNCompareData==null || searchNCompareData.length==0 || alarmMessagesData.length==0)
	    			{
	    				Log.i(TAG, "Something is null or length==0");
	    				alarmMessagesData = new String[bepsType.getNumberOfBackgroundScannerItems()];
	    				String[] tempAlarmMessagesDataFromType=bepsType.getBackgroundScannerDisplayStrings();
	    				for(int i=0; i<bepsType.getNumberOfBackgroundScannerItems(); i++)
	    				{
	    					alarmMessagesData[i]=tempAlarmMessagesDataFromType[i];
	    				}
	    				searchNCompareData = new String[bepsType.getNumberOfBackgroundScannerItems()];
	    				String[] tempsearchNCompareDataFromType=bepsType.getBackgroundScannerSearchStrings();
	    				for(int i=0; i<bepsType.getNumberOfBackgroundScannerItems(); i++)
	    				{
	    					searchNCompareData[i]=tempsearchNCompareDataFromType[i];
	    				}
	    			}
	    			else
	    			{
	    				Log.i(TAG, "length>0 && !=null");
	    				int q=0;
	    				int z=0;
	    				int t=0;
	    				int newArraySize=0;
	    				String[] alarmMessagesDataTemp = new String[alarmMessagesData.length+bepsType.getNumberOfBackgroundScannerItems()];
	    				String[] searchNCompareDataTemp = new String[searchNCompareData.length+bepsType.getNumberOfBackgroundScannerItems()];
	    				String[] tempAlarmMessagesDataFromType=bepsType.getBackgroundScannerDisplayStrings();
	    				String[] tempsearchNCompareDataFromType=bepsType.getBackgroundScannerSearchStrings();
	    				for(q=0; q<alarmMessagesData.length; q++)
	    				{
	    					alarmMessagesDataTemp[q] = alarmMessagesData[q];
	    					searchNCompareDataTemp[q] = searchNCompareData[q];
	    				}
	    				z=0;
	    				for(q=q; q<(alarmMessagesData.length+bepsType.getNumberOfBackgroundScannerItems()); q++)
	    				{
	    					boolean currentEntryZisAlreadyStored=false;
	    					for(t=0;t<searchNCompareData.length;t++)
	    					{
	    						if(searchNCompareData[t].equals(tempsearchNCompareDataFromType[z]))
	    						{
	    							currentEntryZisAlreadyStored=true;
	    						}
	    					}
	    					if(!currentEntryZisAlreadyStored)
	    					{
	    						alarmMessagesDataTemp[q] = tempAlarmMessagesDataFromType[z];
	    						searchNCompareData[q] = tempsearchNCompareDataFromType[z];
	    						newArraySize++;
	    					}
	    					z++;
	    				}
	    				String[] alarmMessageDataCopyArray = new String[alarmMessagesData.length+newArraySize];
	    				String[] searchNCompareDataCopyArray = new String[alarmMessagesData.length+newArraySize];
	    				for(int x=0;x<(alarmMessagesData.length+newArraySize); x++)
	    				{
	    					alarmMessageDataCopyArray[x]=alarmMessagesDataTemp[x];
	    					searchNCompareDataCopyArray[x]=searchNCompareDataTemp[x];
	    				}
	    				alarmMessagesData=alarmMessageDataCopyArray;
	    				searchNCompareData=searchNCompareDataCopyArray;
	    			}
	    			
	    			((LowsActivity)getActivity()).setSearchNCompareDataArray(searchNCompareData,alarmMessagesData);
	    			Toast.makeText(getActivity(), "BEPS enabled!", Toast.LENGTH_SHORT).show();
    		
				}
				else if(alarmSet==false)
				{
					Log.i(TAG, "AlarmSet=false!");
					globalBepsEnabled=false;
	    			searchNCompareData= ((LowsActivity)getActivity()).getSearchNCompareDataArray();
	    	 		alarmMessagesData = ((LowsActivity)getActivity()).getAlarmMessagesDataArray();
	    	 		if((alarmMessagesData==null)||(alarmMessagesData.length==0))
	    			{
	    				alarmMessagesData = new String[0];
	    				//alarmMessagesData[0]="BEPS Alarm";
	    				Log.i(TAG, "alarmMessagesData==null");
	    			}
	    	 		else
	    	 		{
	    	 			Log.i(TAG, "alarmMessagesData!=null");
	    	 			int j,i,q;
	    	 			q=0;
	    	 			
	    	 			int doNotCopyThisEntry=0;
	    	 			String[] alarmMessagesDataTemp = new String[alarmMessagesData.length];
	    	 			String[] tempAlarmMessagesDataFromType=bepsType.getBackgroundScannerDisplayStrings();
	    	 			for(i=0; i<alarmMessagesData.length; i++)
	    	 			{
	    	 				for(j=0; j<tempAlarmMessagesDataFromType.length; j++)
	    	 				{
	    	 					if(alarmMessagesData[i].equals(tempAlarmMessagesDataFromType[j]))
	    	 					{
	    	 						//do not copy this entry in new array
	    	 						doNotCopyThisEntry=1;
	    	 						//Log.i(TAG, "doNotCopyThisEntry found for " + alarmMessagesData[i]);
	    	 					}
	    	 					
	    	 				}
	    	 				if(doNotCopyThisEntry!=1)
	    	 				{
	    	 					alarmMessagesDataTemp[q]=alarmMessagesData[i];
	    	 					q++;
	    	 				}
	    	 			}
	    	 			String[] newAlarmMessagesData = new String[q];
	    	 			for(i=0; i<q; i++)
	    	 			{
	    	 				newAlarmMessagesData[i]=alarmMessagesDataTemp[i];
	    	 			}
	    	 			alarmMessagesData = newAlarmMessagesData;
	    	 		}
	    	 		if((searchNCompareData==null)||(searchNCompareData.length==0))
	    			{
	    	 			searchNCompareData = new String[0];
	    				//alarmMessagesData[0]="BEPS Alarm";
	    			}
	    	 		else
	    	 		{
	    	 			int j,i,q;
	    	 			q=0;
	    	 			
	    	 			int doNotCopyThisEntry=0;
	    	 			String[] searchNCompareDataTemp = new String[searchNCompareData.length];
	    	 			String[] tempsearchNCompareDataFromType=bepsType.getBackgroundScannerSearchStrings();
	    	 			for(i=0; i<searchNCompareData.length; i++)
	    	 			{
	    	 				for(j=0; j<tempsearchNCompareDataFromType.length; j++)
	    	 				{
	    	 					if(searchNCompareData[i].equals(tempsearchNCompareDataFromType[j]))
	    	 					{
	    	 						//do not copy this entry in new array
	    	 						doNotCopyThisEntry=1;
	    	 					}
	    	 					
	    	 				}
	    	 				if(doNotCopyThisEntry!=1)
	    	 				{
	    	 					searchNCompareDataTemp[q]=searchNCompareData[i];
	    	 					q++;
	    	 				}
	    	 			}
	    	 			String[] newsearchNCompareData = new String[q];
	    	 			for(i=0; i<q; i++)
	    	 			{
	    	 				newsearchNCompareData[i]=searchNCompareDataTemp[i];
	    	 			}
	    	 			searchNCompareData = newsearchNCompareData;
	    	 		}
	    	 		((LowsActivity)getActivity()).setSearchNCompareDataArray(searchNCompareData,alarmMessagesData);
	    	 		Toast.makeText(getActivity(), "BEPS disabled!", Toast.LENGTH_SHORT).show();
				}
				
				
			  }//
		  	}//
 		
 	}
 	
 	/**
 	 * This function is called from onActivityResult() when WTN notifications should be enabled or disabled, 
 	 * TODO: -this function should be improved when the search and display string management is moved to sqlite.
 	 * 
 	 * @param alarmSet, true if a WTN notification should be turned on
 	 * @param tempLows, the corresponding WTN LoWS for the notification (needed for the room name)
 	 * @param searchTextReturn, the waiting ticket number for the notification
 	 */
 	void addRemoveWtnsSearchStrings(boolean alarmSet, LoWS tempLows, String searchTextReturn)
 	{
 		if(searchTextReturn == null)
 		{
 			//Toast.makeText(getActivity(), "Search String is null, Alarm not set!", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		else if (!searchTextReturn.matches("[0-9]+") && searchTextReturn.length() > 6) 
 		{
 			//Toast.makeText(getActivity(), "Search is not numeric or longer than 5 digits, Alarm not set!", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		int searchNumber;
 		try {
 			searchNumber = Integer.parseInt(searchTextReturn);
 		  } catch (NumberFormatException e) {
 			Toast.makeText(getActivity(), "Search String cannot converted to Number, wrong format, Alarm not set!", Toast.LENGTH_SHORT).show();
 		    return;
 		  }
 		if(searchNumber>65536 || searchNumber <0)
 		{
 			Toast.makeText(getActivity(), "Search String cannot converted to Number, wrong format, Alarm not set!", Toast.LENGTH_SHORT).show();
 		    return;
 		}
 		StringBuilder sb = new StringBuilder();
 		sb.append(Integer.toHexString(searchNumber));
 		if (sb.length() < 2) {
 		    sb.insert(0, '0'); // pad with leading zero if needed
 		   sb.insert(1, '0'); // pad with leading zero if needed
 		  sb.insert(2, '0'); // pad with leading zero if needed
 		}
 		if (sb.length() < 3) {
 		    sb.insert(0, '0'); // pad with leading zero if needed
 		   sb.insert(1, '0'); // pad with leading zero if needed
 		}
 		if (sb.length() < 4) {
 		    sb.insert(0, '0'); // pad with leading zero if needed
 		}
 		String hexSearchNumber = sb.toString();
 		String originalLowsData = tempLows.getLowsData();
 		String newLowsDataSearchString = originalLowsData.subSequence(0, 4).toString() + hexSearchNumber + originalLowsData.subSequence(8, originalLowsData.length()).toString();
 		Log.i(TAG, "originalLowsData: " + originalLowsData);
 		Log.i(TAG, "newLowsDataSearchString: " + newLowsDataSearchString + "hexSearchNumber: " + hexSearchNumber);
 		
 		
 		searchNCompareData= ((LowsActivity)getActivity()).getSearchNCompareDataArray();
		alarmMessagesData = ((LowsActivity)getActivity()).getAlarmMessagesDataArray();
		
		WTNLoWSExtendedType wtnsExtendedType = null;
		for(int p=0; p<lowsExtType.size(); p++)
	  	{
		  LoWSExtendedType tempExtType=lowsExtType.get(p);
		  if(tempLows.getType()==tempExtType.getTypeNumber())
		  {
			  wtnsExtendedType = (WTNLoWSExtendedType)tempExtType;
		  }
	  	}
		if(wtnsExtendedType==null)
		{
			Toast.makeText(getActivity(), "Type Object not found, Alarm not set!", Toast.LENGTH_SHORT).show();
			return;
		}
		String displayMessage = wtnsExtendedType.decodeRoom(tempLows.getLowsServiceData()) + " now reached waiting ticket number " + searchNumber +".";
 		if(alarmSet==true)
		{
			Log.i(TAG, "WTNS AlarmSet=true!");
			if(alarmMessagesData==null || searchNCompareData==null || searchNCompareData.length==0 || alarmMessagesData.length==0)
			{
				Log.i(TAG, "WTNS set new Search Something is null or length==0");
				alarmMessagesData = new String[1];
				searchNCompareData = new String[1];
				alarmMessagesData[0]=displayMessage;
				searchNCompareData[0]=newLowsDataSearchString;
			}
			else
			{
				int i=0;
			
				Log.i(TAG, "length>0 && !=null");
				for(i=0; i<alarmMessagesData.length; i++)
				{
					if(searchNCompareData[i].equals(newLowsDataSearchString))
					{
						Toast.makeText(getActivity(), "Search is already running for this Room and Number Combination, Alarm not set!", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				String[] alarmMessagesDataNew = new String[alarmMessagesData.length+1]; 
				String[] searchNCompareDataNew = new String[alarmMessagesData.length+1]; 
				
				for(i=0; i<(alarmMessagesData.length); i++)
				{
					alarmMessagesDataNew[i]=alarmMessagesData[i];
					searchNCompareDataNew[i]=searchNCompareData[i];
				}
				
				alarmMessagesDataNew[alarmMessagesData.length]=displayMessage;
				searchNCompareDataNew[searchNCompareData.length]=newLowsDataSearchString;
				alarmMessagesData=alarmMessagesDataNew;
				searchNCompareData=searchNCompareDataNew;
				
				
			}
			((LowsActivity)getActivity()).setSearchNCompareDataArray(searchNCompareData,alarmMessagesData);
			
			Toast.makeText(getActivity(), "New Alarm Set", Toast.LENGTH_SHORT).show();
		}
 		if(alarmSet==false)
		{
			Log.i(TAG, "WTNS AlarmSet=false!");
			if(alarmMessagesData==null || searchNCompareData==null || searchNCompareData.length==0 || alarmMessagesData.length==0)
			{
				Log.i(TAG, "Search array is already empty");
				return;
			}
			else
			{
				int i=0;
				int q=0;
				int entryToRemove=-1;
				Log.i(TAG, "length>0 && !=null");
				for(i=0; i<alarmMessagesData.length; i++)
				{
					if(searchNCompareData[i].equals(newLowsDataSearchString))
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
				((LowsActivity)getActivity()).setSearchNCompareDataArray(searchNCompareData,alarmMessagesData);
				Log.i(TAG, "Search request successfully removed");
				Toast.makeText(getActivity(), "Alarm cleared", Toast.LENGTH_SHORT).show();
			}
			
		}
			
			
 		
 		
 	}
}