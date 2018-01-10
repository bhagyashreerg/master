package ContractStampDuty;

import java.util.HashMap;

/**
 * @author   Kamal.thangavel
 * 
 */

public class AcsTaxTreatmentConfigLines {
    
	  /** Creates a new instance of AcsTaxLines */
    public AcsTaxTreatmentConfigLines() {}
    
    private HashMap <String,AcsTaxTreatmentLinePojo> AcsTaxLines = new HashMap<String,AcsTaxTreatmentLinePojo>();

	/**
	 * Add a line to the cache
	 * @param line line to be added to the cache
	 */ 
	public void addLine(AcsTaxTreatmentLinePojo line){
		String key=line.getStrTreatmentTypeCode()+"-"+line.getMarketSegment()+"-"+line.getOfferType();
		AcsTaxLines.put(key, line);
	}

	/**
	 * Get a line from the cache
	 * @param treatmentTypeCode
	 */
	public AcsTaxTreatmentLinePojo getLine(String treatmentTypeCode, String marketSegment, String offerType)throws NullPointerException{
		String key=treatmentTypeCode+"-"+marketSegment+"-"+offerType;
		return AcsTaxLines.get(key);
	}
}

