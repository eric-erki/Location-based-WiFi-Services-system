package com.lows;

import java.util.ArrayList;
import java.util.List;
/**
 * The class that stores all Access Point parameters, an List of this class is used to store the 
 * current IEEE 802.11 scan results.
 * 
 * TODO: -add an timestamp field, when this entry was stored.
 * 
 * @author Sven Zehl
 *
 */
public class AccessPoint {
	/**
	 * The BSSID of the AP
	 */
	private String bssid;
	/**
	 * The SSID of the AP
	 */
	private String ssid;
	/**
	 * The Frequency where the AP sends its IEEE 802.11 beacon and probe response frames
	 */
	private int freq;
	/**
	 * Beacon Interval of the AP
	 */
	private int beaconInterval;
	/**
	 * Signal strength (RSSI) of the AP
	 */
	private double signal;
	/**
	 * Last seen field of the AP, this field is useless unless we provide the time when this entry was stored
	 * TODO: add an timestamp field
	 */
	private int lastSeen;
	/**
	 * This String list stores all IEEE 802.11 Information Elements the AP broadcasted
	 */
	private List<String> ies;
	
	
	public AccessPoint()
	{	
		ies = new ArrayList<String>();
	}	
	
	/**
	 * Add a new IEEE 802.11 IE to the IE list
	 * @param ieData
	 */
	public void addIE(String ieData)
	{
		this.ies.add(ieData);
	}
	/**
	 * Get a specific IE from the IE list
	 * @param index
	 * @return
	 */
	public String getIE(int index)
	{
		return this.ies.get(index);
	}
	/**
	 * Get current length of the IE list
	 * @return
	 */
	public int getIESize()
	{
		return this.ies.size();
	}
	
	/**
	 * Get the BSSID of the AP
	 * @return, String with BSSID
	 */
	public String getBssid()
	{
		return this.bssid;
	}
	
	/**
	 * Set the BSSID
	 * @param bssid
	 */
	public void setBssid(String bssid)
	{
		this.bssid = bssid;
	}
	
	/**
	 * get the SSID of the AP
	 * @return
	 */
	public String getSsid()
	{
		return this.ssid;
	}
	
	/**
	 * Set the SSID of the AP
	 * @param ssid
	 */
	public void setSsid(String ssid)
	{
		this.ssid = ssid;
	}
	
	/**
	 * Get the Frequency where the AP operates
	 * @return
	 */
	public int getFreq()
	{
		return this.freq;
	}
	
	/**
	 * Set the Frequency of the AP
	 * @param freq
	 */
	public void setFreq(int freq)
	{
		this.freq = freq;
	}

	/**
	 * Get the Beacon Interval of the AP
	 * @return
	 */
	public int getBeaconInterval() {
		return beaconInterval;
	}

	/**
	 * Set the beacon interval of the AP
	 * @param beaconInterval
	 */
	public void setBeaconInterval(int beaconInterval) 
	{
		this.beaconInterval = beaconInterval;
	}

	/**
	 * Get the current signal stength (RSSI) of the AP
	 * @return
	 */
	public double getSignal() {
		return this.signal;
	}

	/**
	 * Set the signal strength (RSSI) of the AP
	 * @param signal
	 */
	public void setSignal(double signal) 
	{
		this.signal = signal;
	}

	/**
	 * Get the last seen value
	 * @return
	 */
	public int getLastSeen() 
	{
		return lastSeen;
	}

	/**
	 * set the last seen value
	 * @param lastSeen
	 */
	public void setLastSeen(int lastSeen) 
	{
		this.lastSeen = lastSeen;
	}

}
