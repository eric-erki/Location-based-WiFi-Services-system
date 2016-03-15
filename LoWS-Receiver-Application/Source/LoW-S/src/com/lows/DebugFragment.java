package com.lows;

import java.util.List;


import android.app.Fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * This is the debug fragment that can be accessed from the ResultListFragment when swiping to the right.
 * It allows to view some Lists of the application and to view the debugText output.
 * 
 * @author sz
 *
 */
public class DebugFragment extends Fragment {

	private static final String ARG_SECTION_NUMBER = "section_number";
	private Button mstart_button;
	private Button mread_button;
	private Button mlows_button;
	private Button msearch_button;
	private List<AccessPoint> aps;
	private List<LoWS> lows;


	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static DebugFragment newInstance(int sectionNumber) {
		DebugFragment fragment = new DebugFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public DebugFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.debug_fragment_lows, container,
				false);
		mstart_button = (Button) rootView.findViewById(R.id.sb);
		mread_button = (Button) rootView.findViewById(R.id.rb);
		mlows_button = (Button) rootView.findViewById(R.id.lb);
		msearch_button = (Button) rootView.findViewById(R.id.search_button);
		final TextView outputText = (TextView) rootView.findViewById(R.id.output_text);
		final TextView headLine = (TextView) rootView.findViewById(R.id.stdo_text);
		outputText.setText(((LowsActivity)getActivity()).getDebugText());
		headLine.setText("Debug-Messages");
		mstart_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			/**
			 * This is the function that is initially called and it displays the current debugText
			 */
			public void onClick(View v) {
				//((LowsActivity)getActivity()).appendDebugText("Test\n");
				headLine.setText("Debug-Messages");
				outputText.setText(((LowsActivity)getActivity()).getDebugText());
			
				
			}
		});
		
		/**
		 * If the st button is pressed, this function displays all current search strings the 
		 * Background Scanner currently uses, as this can be a lot, this function may take a while.
		 */
		msearch_button.setOnClickListener(new View.OnClickListener() {
			// Setting the action to perform when the start button is pressed.
			@Override
			public void onClick(View v) {
				String[] searchNCompareData= ((LowsActivity)getActivity()).getSearchNCompareDataArray();
			    String[] alarmMessagesData = ((LowsActivity)getActivity()).getAlarmMessagesDataArray();
			    int i=0;
			    int numberSearchStrings = searchNCompareData.length; 
				headLine.setText("Current Background Scanner Searchstrings");
				if(searchNCompareData==null || alarmMessagesData ==null)
				{
					outputText.setText("Database is empty.");
				}
				else
				{
					String modifyText;
					modifyText = "Currently, " + numberSearchStrings + " Search Strings used by LoWS Background Scanner, printing...\n";
					outputText.setText(modifyText);
				    modifyText = modifyText + "*******************\n" + "*******************\n";
				    for(i=0; i<searchNCompareData.length; i++)
				    {
				    	modifyText = modifyText + "searchNCompareData["+i+"]: " +  searchNCompareData[i] + "\n";
				    	modifyText = modifyText + "alarmMessagesData["+i+"]: " +  alarmMessagesData[i] + "\n";
				    }
				    outputText.setText(modifyText);
				}
				
			}
			
		});
		
		/**
		 * This function is executed if the user presses the aps button
		 * It displays the contents of the AccessPoint List.
		 */
		mread_button.setOnClickListener(new View.OnClickListener() {
			// Setting the action to perform when the start button is pressed.
			@Override
			public void onClick(View v) {
				aps=(((LowsActivity)getActivity()).getApArray());
				headLine.setText("Current AccessPoint Database");
				if(aps==null)
				{
					outputText.setText("Database is empty.");
				}
				else
				{
					String modifyText;
					int numberAps = aps.size();
					modifyText = "Currently, " + numberAps + " Access Points in Database, printing...\n";
					outputText.setText(modifyText);
					int i,j;
					int numberIEs;
					AccessPoint tempReadAp;
					for(i=0; i<numberAps; i++)
					{
						modifyText = modifyText + "*******************\n" + "Access Point #" + (i+1) +"\n" + "*******************\n";
						tempReadAp = aps.get(i);
						numberIEs = tempReadAp.getIESize();
						modifyText = modifyText + "SSID: " +tempReadAp.getSsid() + "\n";
						modifyText = modifyText + "BSSID: " +tempReadAp.getBssid() + "\n";
						modifyText = modifyText + "Frequency: " +tempReadAp.getFreq() + "\n";
						modifyText = modifyText + "Beacon Interval: " + tempReadAp.getBeaconInterval() + "\n";
						modifyText = modifyText + "Signal Level: " + tempReadAp.getSignal() + "\n";
						modifyText = modifyText + "Last seen: " + tempReadAp.getLastSeen() + "\n";
						modifyText = modifyText + "Information Elements:\n";
						for(j=0; j<numberIEs; j++)
						{
							modifyText = modifyText + "\tIE (#" + (j+1) + "): " + tempReadAp.getIE(j) + "\n";
						}
						outputText.setText(modifyText);
	
					}
				    outputText.setText(modifyText);
				}
				
			}
			
		});
		
		/**
		 * This function is executed if the user taps on the lows button
		 * It displays the current contents of the LoWS List
		 */
		mlows_button.setOnClickListener(new View.OnClickListener() {
			// Setting the action to perform when the start button is pressed.
			@Override
			public void onClick(View v) {
				lows=(((LowsActivity)getActivity()).getLowsArray());
				headLine.setText("Current LoWS Database");
				if(lows==null)
				{
					outputText.setText("Database is empty.");
				}
				else
				{
					String modifyText;
					int numberLows = lows.size();
					modifyText = "Currently, " + numberLows + " Location-based Wifi Services in Database, printing...\n";
					outputText.setText(modifyText);
					int i;
					LoWS tempReadLows;
					for(i=0; i<numberLows; i++)
					{
						modifyText = modifyText + "*******************\n" + "LoWS #" + (i+1) +"\n" + "*******************\n";
						tempReadLows = lows.get(i);
						modifyText = modifyText + "SSID: " +tempReadLows.getSsid() + "\n";
						modifyText = modifyText + "BSSID: " +tempReadLows.getBssid() + "\n";
						modifyText = modifyText + "LoWS-Data-Raw(hex): " +tempReadLows.getLowsData() + "\n";
						if(tempReadLows.getFormatType()!=0)
						{
							modifyText = modifyText + "LoWS-Format-Type: " +tempReadLows.getFormatType() + "\n";
						}
						if(tempReadLows.getType()!=0)
						{
							modifyText = modifyText + "LoWS-Type(int): " +tempReadLows.getType() + "\n";
						}
						if(tempReadLows.getLowsServiceData()!=null)
						{
							modifyText = modifyText + "LoWS-Service-Data: " +tempReadLows.getLowsServiceData() + "\n";
						}
						outputText.setText(modifyText);
	
					}

				}
				
			}
			
		});
		
		
		return rootView;
	}
}



