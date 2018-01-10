package ContractStampDuty;

import java.util.HashMap;

/**
 * @author   Kamal.thangavel
 * 
 */

public class AcsTaxOnContractConfigLines {
    
    /** Creates a new instance of AcsTaxOnInvoiceLines */
    public AcsTaxOnContractConfigLines() {}
    
    private HashMap <String,AcsTaxOnContractLinePojo> AcsTaxOnContractLines = new HashMap<String,AcsTaxOnContractLinePojo>();

	/**
	 * Add a line to the cache
	 * @param line line to be added to the cache
	 */ 
	public void addLine(AcsTaxOnContractLinePojo line){
		String key=line.getBusinessType()+"-"+line.getExemptionType();
		AcsTaxOnContractLines.put(key, line);
	}

	/**
	 * Get a line from the cache
	 * @param businessType
	 */
	public AcsTaxOnContractLinePojo getLine(String businessType, String exemptionType)throws NullPointerException{
		String key= businessType+"-"+exemptionType;
		return AcsTaxOnContractLines.get(key);
	}
}

