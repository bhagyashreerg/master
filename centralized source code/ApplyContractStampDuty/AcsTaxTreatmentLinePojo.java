package ContractStampDuty;

/**
 * @author   Kamal.thangavel
 * 
 */

public class AcsTaxTreatmentLinePojo {

	private String strTreatmentType;
	
	private String strTreatmentTypeCode;

	private String vatExemption;

	private Double percentageVatExemption;

	private String overrideVatCode;

	private String overrideTaxCodeId;

	private String overrideVatDesc;

	private String stampOnInvoice;

	private String stampOnContract;

	private String tcgExemption;
	
	private String marketSegment;
	
	private String offerType;

	public String getOverrideTaxCodeId() {
		return overrideTaxCodeId;
	}

	public void setOverrideTaxCodeId(String overrideTaxCodeId) {
		this.overrideTaxCodeId = overrideTaxCodeId;
	}

	public String getOverrideVatCode() {
		return overrideVatCode;
	}

	public void setOverrideVatCode(String overrideVatCode) {
		this.overrideVatCode = overrideVatCode;
	}

	public String getOverrideVatDesc() {
		return overrideVatDesc;
	}

	public void setOverrideVatDesc(String overrideVatDesc) {
		this.overrideVatDesc = overrideVatDesc;
	}

	public Double getPercentageVatExemption() {
		return percentageVatExemption;
	}

	public void setPercentageVatExemption(Double percentageVatExemption) {
		this.percentageVatExemption = percentageVatExemption;
	}

	public String getStampOnContract() {
		return stampOnContract;
	}

	public void setStampOnContract(String stampOnContract) {
		this.stampOnContract = stampOnContract;
	}

	public String getStampOnInvoice() {
		return stampOnInvoice;
	}

	public void setStampOnInvoice(String stampOnInvoice) {
		this.stampOnInvoice = stampOnInvoice;
	}

	
	/**
	 * @return Returns the strTreatmentTypeCode.
	 */
	public String getStrTreatmentTypeCode() {
		return strTreatmentTypeCode;
	}

	/**
	 * @param strTreatmentTypeCode The strTreatmentTypeCode to set.
	 */
	public void setStrTreatmentTypeCode(String strTreatmentTypeCode) {
		this.strTreatmentTypeCode = strTreatmentTypeCode;
	}

	public String getTcgExemption() {
		return tcgExemption;
	}

	public void setTcgExemption(String tcgExemption) {
		this.tcgExemption = tcgExemption;
	}

	public String getVatExemption() {
		return vatExemption;
	}

	public void setVatExemption(String vatExemption) {
		this.vatExemption = vatExemption;
	}

	/**
	 * @return Returns the strTreatmentType.
	 */
	public String getStrTreatmentType() {
		return strTreatmentType;
	}

	/**
	 * @param strTreatmentType The strTreatmentType to set.
	 */
	public void setStrTreatmentType(String strTreatmentType) {
		this.strTreatmentType = strTreatmentType;
	}

	public String getMarketSegment() {
		return marketSegment;
	}

	public void setMarketSegment(String marketSegment) {
		this.marketSegment = marketSegment;
	}

	public String getOfferType() {
		return offerType;
	}

	public void setOfferType(String offerType) {
		this.offerType = offerType;
	}

	
}
