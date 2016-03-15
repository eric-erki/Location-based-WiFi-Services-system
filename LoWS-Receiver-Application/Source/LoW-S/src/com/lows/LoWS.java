package com.lows;

/**
 * This is the general LoWS class, a list of this class objects is maintained to manage all
 * the LoWS message processing.
 * 
 * @author Sven Zehl
 *
 */
public class LoWS {
	private AccessPoint apData;
	private String lowsData; //Raw Data with type, format identifier...
	private int lowsDataLength; //Length in Byte
	private int formatType; //0 if not set, 1 if reduced format(codebook), 2 if extended format(flexible)
	private int type; 	//Currently only 32bit type is possible, if more types are needed, integer should be 
						//replaced with something bigger, 0 if not set
	private boolean seqNumberIsPresent; //
	private boolean typeIndexIsPresent; //
	private boolean EncryptionIsPresent; //
	private boolean SignatureIsPresent; //
	private int endOfType; //Number of Byte in lowsData, where the type ends
	private int beginOfServiceData;
	private String lowsServiceData; //The real data, already extracted
	private String lowsDisplayString; //String that should be displayed in Scan Result Fragment
	
	public LoWS()
	{
		this.formatType=0;
		this.type=0;
		this.lowsServiceData=null;
		this.lowsDisplayString="Unknown Type";
	}
	
	public LoWS(AccessPoint apData, String lowsData, int lowsDataLength)
	{
		this.apData=apData;
		this.lowsData=lowsData;
		this.lowsDataLength=lowsDataLength;
		this.formatType=0;
		this.type=0;
		this.lowsServiceData=null;
		this.lowsDisplayString="Unknown Type";
	}
	
	public void setFormatType(int formatType)
	{
		this.formatType=formatType;
	}
	
	public int getFormatType()
	{
		return this.formatType;
	}
	
	
	public String getSsid()
	{
		return this.apData.getSsid();
	}
	
	public String getBssid()
	{
		return this.apData.getBssid();
	}
	
	public double getSignalStrength()
	{
		return this.apData.getSignal();
	}
	
	public int getFrequency()
	{
		return this.apData.getFreq();
	}
	
	public String getLowsData()
	{
		return this.lowsData;
	}
	
	public void setLowsData(String lowsData)
	{
		this.lowsData=lowsData;
	}
	
	public int getLowsDataLength()
	{
		return this.lowsDataLength;
	}
	
	public void setLowsDataLength(int lowsDataLength)
	{
		this.lowsDataLength=lowsDataLength;
	}
	
	public AccessPoint getApData()
	{
		return this.apData;
	}
	public void setApData(AccessPoint apData)
	{
		this.apData=apData;
	}

	public String getLowsServiceData() {
		return lowsServiceData;
	}

	public void setLowsServiceData(String lowsServiceData) {
		this.lowsServiceData = lowsServiceData;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getLowsDisplayString() {
		return lowsDisplayString;
	}

	public void setLowsDisplayString(String lowsDisplayString) {
		this.lowsDisplayString = lowsDisplayString;
	}

	public boolean isSeqNumberIsPresent() {
		return seqNumberIsPresent;
	}

	public void setSeqNumberIsPresent(boolean seqNumberIsPresent) {
		this.seqNumberIsPresent = seqNumberIsPresent;
	}

	public boolean isTypeIndexIsPresent() {
		return typeIndexIsPresent;
	}

	public void setTypeIndexIsPresent(boolean typeIndexIsPresent) {
		this.typeIndexIsPresent = typeIndexIsPresent;
	}

	public boolean isEncryptionIsPresent() {
		return EncryptionIsPresent;
	}

	public void setEncryptionIsPresent(boolean encryptionIsPresent) {
		EncryptionIsPresent = encryptionIsPresent;
	}

	public boolean isSignatureIsPresent() {
		return SignatureIsPresent;
	}

	public void setSignatureIsPresent(boolean signatureIsPresent) {
		SignatureIsPresent = signatureIsPresent;
	}

	public int getEndOfType() {
		return endOfType;
	}

	public void setEndOfType(int endOfType) {
		this.endOfType = endOfType;
	}

	public int getBeginOfServiceData() {
		return beginOfServiceData;
	}

	public void setBeginOfServiceData(int beginOfServiceData) {
		this.beginOfServiceData = beginOfServiceData;
	}
}
