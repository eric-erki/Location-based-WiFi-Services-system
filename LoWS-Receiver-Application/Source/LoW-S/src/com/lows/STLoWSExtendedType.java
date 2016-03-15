package com.lows;

/**
 * This class implements the String Type
 * LoWS type via the LoWSExtendedType interface class
 * 
 * TODO: 	-Enable searching for String Types
 * 
 * @author Sven Zehl
 *
 */
public class STLoWSExtendedType implements LoWSExtendedType {

	@Override
	public int getTypeNumber() {
		return 32;
	}

	@Override
	public String getDisplayString() {
		return "String Type (ST)";
	}

	@Override
	public String decodeData(String dataInHex) {
		
		String stringTypeHex = dataInHex;
		StringBuilder stringTypeAscii = new StringBuilder();
		
		for(int p = 0; p < stringTypeHex.length(); p+=2)
		{
			String str = stringTypeHex.substring(p, p+2);
			stringTypeAscii.append((char)Integer.parseInt(str, 16));
		}
		return stringTypeAscii.toString();
	}
	
	@Override
	public String getIconName() {
		return "st";
	}

	@Override
	public boolean showAlarmSwitch() {
		// TODO Auto-generated method stub
		return false;
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
		return false;
	}

	@Override
	public String getSearchFieldDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
