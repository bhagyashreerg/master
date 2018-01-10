package ContractStampDuty;

import java.util.Date;

public class ContractStampDutyPojo {


	private	String	strAcsBillingAccount;
	private	String	strTaxTreatmentId;	
	private	String	strCustomerCode;
	private	String	strStatus;
	private	String	strTaxOnContract;
	private	Date	dtStartDate;
	private	Date	dtInsertionDate;
	private	Date	dtModifiedDate;
	private	Date	dtEndDate;
	private long 	creationT;
	private	long	accountPoid;
	private String  strTreatmentType;
	private String  strTreatmentTypeCode;
	private Date currentDate;	
	private Date nextBillTPlusOffset;
	private Date nextBillT;	
	private Date cutOffDate;	
	private Date monthStartDate;
	private	long	billInfoPoid;
	private Date billStartDate;
	private String msisdn;		 /*Added : for ISC00010084*/	private Long servicePoid;
	
	public long getAccountPoid() {
		return accountPoid;
	}
	public void setAccountPoid(long accountPoid) {
		this.accountPoid = accountPoid;
	}
	public long getBillInfoPoid() {
		return billInfoPoid;
	}
	public void setBillInfoPoid(long billInfoPoid) {
		this.billInfoPoid = billInfoPoid;
	}
	public long getCreationT() {
		return creationT;
	}
	public void setCreationT(long creationT) {
		this.creationT = creationT;
	}
	public Date getCurrentDate() {
		return currentDate;
	}
	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}
	public Date getCutOffDate() {
		return cutOffDate;
	}
	public void setCutOffDate(Date cutOffDate) {
		this.cutOffDate = cutOffDate;
	}
	public Date getDtEndDate() {
		return dtEndDate;
	}
	public void setDtEndDate(Date dtEndDate) {
		this.dtEndDate = dtEndDate;
	}
	public Date getDtInsertionDate() {
		return dtInsertionDate;
	}
	public void setDtInsertionDate(Date dtInsertionDate) {
		this.dtInsertionDate = dtInsertionDate;
	}
	public Date getDtModifiedDate() {
		return dtModifiedDate;
	}
	public void setDtModifiedDate(Date dtModifiedDate) {
		this.dtModifiedDate = dtModifiedDate;
	}
	public Date getDtStartDate() {
		return dtStartDate;
	}
	public void setDtStartDate(Date dtStartDate) {
		this.dtStartDate = dtStartDate;
	}
	public Date getMonthStartDate() {
		return monthStartDate;
	}
	public void setMonthStartDate(Date monthStartDate) {
		this.monthStartDate = monthStartDate;
	}
	public Date getNextBillT() {
		return nextBillT;
	}
	public void setNextBillT(Date nextBillT) {
		this.nextBillT = nextBillT;
	}
	public Date getNextBillTPlusOffset() {
		return nextBillTPlusOffset;
	}
	public void setNextBillTPlusOffset(Date nextBillTPlusOffset) {
		this.nextBillTPlusOffset = nextBillTPlusOffset;
	}
	public String getStrAcsBillingAccount() {
		return strAcsBillingAccount;
	}
	public void setStrAcsBillingAccount(String strAcsBillingAccount) {
		this.strAcsBillingAccount = strAcsBillingAccount;
	}
	public String getStrCustomerCode() {
		return strCustomerCode;
	}
	public void setStrCustomerCode(String strCustomerCode) {
		this.strCustomerCode = strCustomerCode;
	}
	public String getStrStatus() {
		return strStatus;
	}
	public void setStrStatus(String strStatus) {
		this.strStatus = strStatus;
	}
	public String getStrTaxOnContract() {
		return strTaxOnContract;
	}
	public void setStrTaxOnContract(String strTaxOnContract) {
		this.strTaxOnContract = strTaxOnContract;
	}
	public String getStrTaxTreatmentId() {
		return strTaxTreatmentId;
	}
	public void setStrTaxTreatmentId(String strTaxTreatmentId) {
		this.strTaxTreatmentId = strTaxTreatmentId;
	}
	public String getStrTreatmentType() {
		return strTreatmentType;
	}
	public void setStrTreatmentType(String strTreatmentType) {
		this.strTreatmentType = strTreatmentType;
	}
	public String getStrTreatmentTypeCode() {
		return strTreatmentTypeCode;
	}
	public void setStrTreatmentTypeCode(String strTreatmentTypeCode) {
		this.strTreatmentTypeCode = strTreatmentTypeCode;
	}
	public Date getBillStartDate() {
		return billStartDate;
	}
	public void setBillStartDate(Date billStartDate) {
		this.billStartDate = billStartDate;
	}
	
	//added by Mayur for X3-2012 business hierarchy on 18-01-2012
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}	public Long getServicePoid() {		return servicePoid;	}	public void setServicePoid(Long servicePoid) {		this.servicePoid = servicePoid;	}
	
	
	
	}
