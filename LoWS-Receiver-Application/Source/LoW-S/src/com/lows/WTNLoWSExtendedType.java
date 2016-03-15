package com.lows;

/**
 * This class implements the Waiting Ticket Number Service
 * LoWS type via the LoWSExtendedType interface class
 * 
 * 
 * @author Sven Zehl
 *
 */
public class WTNLoWSExtendedType implements LoWSExtendedType {

	@Override
	public int getTypeNumber() {
		return 16;
	}

	@Override
	public String getDisplayString() {
		return "Waiting Ticket Number Service (WTNS)";
	}

	@Override
	public String decodeData(String dataInHex) {
		
		int dataInHexLength=dataInHex.length();
		String paRoom = dataInHex.subSequence(4, dataInHexLength).toString();
		String paNumber = dataInHex.subSequence(0, 4).toString();
		int paNumberInt=Integer.parseInt(paNumber, 16); 
		
		StringBuilder paRoomAscii = new StringBuilder();
		for(int p = 0; p < paRoom.length(); p+=2)
		{
			String str = paRoom.substring(p, p+2);
			paRoomAscii.append((char)Integer.parseInt(str, 16));
		}
		return "Room: " + paRoomAscii + " / WT#: " + paNumberInt;
	}
	

	public String decodeRoom(String dataInHex) {
		
		int dataInHexLength=dataInHex.length();
		String paRoom = dataInHex.subSequence(4, dataInHexLength).toString();
		String paNumber = dataInHex.subSequence(0, 4).toString();
		int paNumberInt=Integer.parseInt(paNumber, 16); 
		
		StringBuilder paRoomAscii = new StringBuilder();
		for(int p = 0; p < paRoom.length(); p+=2)
		{
			String str = paRoom.substring(p, p+2);
			paRoomAscii.append((char)Integer.parseInt(str, 16));
		}
		return "Counter " + paRoomAscii;
	}

	@Override
	public String getIconName() {
		return "wtns";
	}

	@Override
	public boolean showAlarmSwitch() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean getAlarmStartState() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAlarmClickStandardText(String serviceData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEnhancedClickText(String serviceData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnhancedClickText(String serviceData,
			String enhancedClickText) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean showSearchFieldSwitch() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getSearchFieldDescription() {
		// TODO Auto-generated method stub
		return "Enter waiting ticket number, which should cause the alarm to go on, afterwards switch the alarm button on and press the \"Save\" button to save your alarm.";
	}
	

}
