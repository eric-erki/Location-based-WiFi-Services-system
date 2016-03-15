package com.lows;


/**
 * This is the interface class for all extended (flexible) LoWS types.
 * All new flexible (extended) LoWS types must implement this interface.
 * See the STLoWSExtendedType class as an example how this is done.
 * 
 * @author Sven Zehl
 *
 */
public interface LoWSExtendedType {
	/**
	 * This function should return the LoWS type number that identifies this specific LoWS type
	 * @return
	 */
	int getTypeNumber();
	/**
	 * This function should return the String that is displayed within the Scan Result ListView
	 * if this specific service with the specific type number was found.
	 * @return
	 */
	String getDisplayString();
	/**
	 * This function takes the payload of the LoWS message and decodes it.
	 * It returns the decoded LoWS data that can afterwards be displayed to the user.
	 * 
	 * @param dataInHex, raw LoWS message paylod in hex as String
	 * @return, decoded LoWS message payload as String
	 */
	String decodeData(String dataInHex);
	/**
	 * Get the name of the icon which should be displayed together with the Display String
	 * @return, the name of the icon as String
	 */
	String getIconName();
	/**
	 * Should the alarm switch be displayed in the ClickActivity? If yes, return true.
	 * @return
	 */
	boolean showAlarmSwitch();
	/**
	 * Should the Search Field be displayed within the ClickActivity? If yes return true.
	 * @return
	 */
	boolean showSearchFieldSwitch();
	/**
	 * If the Search field should be displayed what should be listed above the search field.
	 * @return
	 */
	String getSearchFieldDescription();
	/**
	 * If the Alarm switch should be displayed, should it initially turned off or on?
	 * Return true if initially enabled, false if initially disabled
	 * @return
	 */
	boolean getAlarmStartState(); //true = alarm enabled when recognized, false = not enabled initially
	/**
	 * This function takes the raw payload of the LoWS message and returns the text that should
	 * be displayed within the AlarmClickActivity.
	 * !!!Currently we do not use this function, we currently use the getDisplay String for all 
	 * purposes, this may be changed in future.!!!
	 * 
	 * @param serviceData
	 * @return
	 */
	String getAlarmClickStandardText(String serviceData);
	/**
	 * This function should return a more advanced description of the LoWS payload.
	 * !!!Currently this function is also not used!!!
	 * @param serviceData
	 * @return
	 */
	String getEnhancedClickText(String serviceData);
	/**
	 * Enables setting of the enhanced Click text, currently not used.
	 * @param serviceData
	 * @param enhancedClickText
	 */
	void setEnhancedClickText(String serviceData, String enhancedClickText);
}
