package ContractStampDuty;

import java.util.HashMap;


/**
 * @author   Kamal.thangavel
 * 
 */
public class AcsCodConversionConfigLines {
    
	 /** Creates a new instance of AcsCodConversionLines */
    public AcsCodConversionConfigLines() {}
    
    private HashMap <String,String> AcsCodConversionLines = new HashMap<String,String>();

	/**
	 * Add a line to the cache
	 * @param line line to be added to the cache
	 */ 
	public void addLine(AcsCodConversionLinePojo line){
		String key=line.getSystem()+"-"+line.getEntityType()+"-"+line.getOrganizationId()+"-"
		+line.getDirection()+"-"+line.getInputValue()+"-"+line.getKey1()+"-"+line.getKey2()+"-"+line.getKey3();
		AcsCodConversionLines.put(key, line.getOutputValue());
	}

	/**
	 * Get a line from the cache
	 * @param entityType
	 * @param reasonForRequest
	 * @param adjustmentType
	 */
	public String getOutputValue(String s, 
			String e, 
			String o, 
			String di, 
			String in, 
			String key1,
			String key2,
			String key3)  throws NullPointerException{
		
		String newKey1 = key1;
		String newKey2 = key2;
		String newKey3 = key3;
		
		if ( key1 == null ) newKey1 = "X";
		
		if ( key2 == null ) newKey2 = "X";
		
		if ( key3 == null ) newKey3 = "X";
		
		String key=s+"-"+e+"-"+o+"-"+di+"-"+in+"-"+newKey1+"-"+newKey2+"-"+newKey3;
		return AcsCodConversionLines.get(key);
	}
}

