package ContractStampDuty;

/**
 * @author   Kamal.thangavel
 * 
 */

public class AcsTaxOnContractLinePojo {

	private String businessType;
	
	
	private Double taxOnContract;

	private String exemptionType;

	private String reasonForRequest;

	private String adjustmentType;

	private String rateTag;

	private String acctLevelResId;

	private String serviceLevelId1;

	private String serviceLevelId2;

	/**
	 * @return Returns the acctLevelResId.
	 */
	public String getAcctLevelResId() {
		return acctLevelResId;
	}

	/**
	 * @param acctLevelResId The acctLevelResId to set.
	 */
	public void setAcctLevelResId(String acctLevelResId) {
		this.acctLevelResId = acctLevelResId;
	}

	/**
	 * @return Returns the adjustmentType.
	 */
	public String getAdjustmentType() {
		return adjustmentType;
	}

	/**
	 * @param adjustmentType The adjustmentType to set.
	 */
	public void setAdjustmentType(String adjustmentType) {
		this.adjustmentType = adjustmentType;
	}

	/**
	 * @return Returns the exemptionType.
	 */
	public String getExemptionType() {
		return exemptionType;
	}

	/**
	 * @param exemptionType The exemptionType to set.
	 */
	public void setExemptionType(String exemptionType) {
		this.exemptionType = exemptionType;
	}

	
	/**
	 * @return Returns the rateTag.
	 */
	public String getRateTag() {
		return rateTag;
	}

	/**
	 * @param rateTag The rateTag to set.
	 */
	public void setRateTag(String rateTag) {
		this.rateTag = rateTag;
	}

	/**
	 * @return Returns the reasonForRequest.
	 */
	public String getReasonForRequest() {
		return reasonForRequest;
	}

	/**
	 * @param reasonForRequest The reasonForRequest to set.
	 */
	public void setReasonForRequest(String reasonForRequest) {
		this.reasonForRequest = reasonForRequest;
	}

	/**
	 * @return Returns the serviceLevelId1.
	 */
	public String getServiceLevelId1() {
		return serviceLevelId1;
	}

	/**
	 * @param serviceLevelId1 The serviceLevelId1 to set.
	 */
	public void setServiceLevelId1(String serviceLevelId1) {
		this.serviceLevelId1 = serviceLevelId1;
	}

	/**
	 * @return Returns the serviceLevelId2.
	 */
	public String getServiceLevelId2() {
		return serviceLevelId2;
	}

	/**
	 * @param serviceLevelId2 The serviceLevelId2 to set.
	 */
	public void setServiceLevelId2(String serviceLevelId2) {
		this.serviceLevelId2 = serviceLevelId2;
	}

	
	public Double getTaxOnContract() {
		return taxOnContract;
	}

	public void setTaxOnContract(Double taxOnContract) {
		this.taxOnContract = taxOnContract;
	}

	/**
	 * @return Returns the businessType.
	 */
	public String getBusinessType() {
		return businessType;
	}

	/**
	 * @param businessType The businessType to set.
	 */
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	

	
}
