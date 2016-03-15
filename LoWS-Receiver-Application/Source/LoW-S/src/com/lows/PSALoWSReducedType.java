package com.lows;

/**
 * This class implements the Physical Service Announcement
 * LoWS type via the LoWSReducedType interface class
 * 
 * TODO: 	-Enable searching for PSA types
 * 
 * @author Sven Zehl
 *
 */
public class PSALoWSReducedType implements LoWSReducedType{
	
	@Override
	public int getTypeNumber() {
		// TODO Auto-generated method stub
		return 63; //0x3f
	}

	@Override
	public String getDisplayString() {
		// TODO Auto-generated method stub
		return "Physical Service Announcement (PSA)";
	}

	@Override
	public String decodeData(String dataInHex) {
		
		String returnText;
		StringBuilder bepsID = new StringBuilder();
		for(int p = 0; p < dataInHex.length(); p+=2)
		{
			String str = dataInHex.substring(p, p+2);
			bepsID.append((char)Integer.parseInt(str, 16));
		}
		if(bepsID.charAt(0)=='%')
		{
			returnText = "Cinema";
		}
		else if(bepsID.charAt(0)=='/')
		{
			returnText = "DIY Warehouse";
		}
		else if(bepsID.charAt(0)==';')
		{
			returnText = "Bank";
		}
		else if(bepsID.charAt(0)=='<')
		{
			returnText = "Beverage Store";
		}
		else if(bepsID.charAt(0)=='>')
		{
			returnText = "Bakery";
		}
		else if(bepsID.charAt(0)=='@')
		{
			returnText = "Cyber Cafe";
		}
		else if(bepsID.charAt(0)=='A')
		{
			returnText = "Airport";
		}
		else if(bepsID.charAt(0)=='B')
		{
			returnText = "Baby-Care-Room";
		}
		else if(bepsID.charAt(0)=='C')
		{
			returnText = "Coffee Shop";
		}
		else if(bepsID.charAt(0)=='D')
		{
			returnText = "Disabled Toilet";
		}
		else if(bepsID.charAt(0)=='E')
		{
			returnText = "Elevator";
		}
		else if(bepsID.charAt(0)=='F')
		{
			returnText = "Fast Food Restaurant";
		}
		else if(bepsID.charAt(0)=='G')
		{
			returnText = "Airport Gate";
		}
		else if(bepsID.charAt(0)=='H')
		{
			returnText = "Hospital";
		}
		else if(bepsID.charAt(0)=='I')
		{
			returnText = "Information Point";
		}
		else if(bepsID.charAt(0)=='K')
		{
			returnText = "Kiosk";
		}
		else if(bepsID.charAt(0)=='M')
		{
			returnText = "Meat Market, Butchers Shop";
		}
		else if(bepsID.charAt(0)=='N')
		{
			returnText = "Tourist Information";
		}
		else if(bepsID.charAt(0)=='P')
		{
			returnText = "Pizzeria";
		}
		else if(bepsID.charAt(0)=='R')
		{
			returnText = "Restaurant";
		}
		else if(bepsID.charAt(0)=='T')
		{
			returnText = "Toilet";
		}
		else if(bepsID.charAt(0)=='U')
		{
			returnText = "Underground (Metro, Subway)";
		}
		else if(bepsID.charAt(0)=='X')
		{
			returnText = "Clothing Store";
		}
		else if(bepsID.charAt(0)=='a')
		{
			returnText = "Apothecary, Pharmacy";
		}
		else if(bepsID.charAt(0)=='b')
		{
			returnText = "Bus Station";
		}
		else if(bepsID.charAt(0)=='c')
		{
			returnText = "Copyshop";
		}
		else if(bepsID.charAt(0)=='d')
		{
			returnText = "Drugstore";
		}
		else if(bepsID.charAt(0)=='f')
		{
			returnText = "Free Wifi";
		}
		else if(bepsID.charAt(0)=='g')
		{
			returnText = "Garage";
		}
		else if(bepsID.charAt(0)=='m')
		{
			returnText = "Doctor of Medicine";
		}
		else if(bepsID.charAt(0)=='o')
		{
			returnText = "Police Station";
		}
		else if(bepsID.charAt(0)=='p')
		{
			returnText = "Parking Garage";
		}
		else if(bepsID.charAt(0)=='s')
		{
			returnText = "Supermarket";
		}
		else if(bepsID.charAt(0)=='t')
		{
			returnText = "Train Station";
		}
		else
		{
			returnText = "Not supported PSA ID";
		}
		return returnText;
	}

	@Override
	public String getIconName() {
		return "psa";
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

	
	public String[] getBackgroundScannerSearchStrings() {
		//auto generated with python script
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getNumberOfBackgroundScannerItems()
	{
		return 0;
	}

	@Override
	public String getAlarmClickStandardText(String serviceData) {		
		return null;
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
