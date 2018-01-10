package ContractStampDuty;

public class AcsSinglePlayStampPojo {

	String accountNo;
	Long accountPoid;
	Double chargedStamp;
	Double stampValue;
	
	
	public AcsSinglePlayStampPojo(String accountNo, Long accountPoid, Double chargedStamp, Double stampValue){ 
	
		this.accountNo = accountNo;
		this.accountPoid = accountPoid;
		this.chargedStamp = chargedStamp;
		this.stampValue = stampValue;

	}
	
	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public Long getAccountPoid() {
		return accountPoid;
	}
	public void setAccountPoid(Long accountPoid) {
		this.accountPoid = accountPoid;
	}
	public Double getChargedStamp() {
		return chargedStamp;
	}
	public void setChargedStamp(Double chargedStamp) {
		this.chargedStamp = chargedStamp;
	}
	public Double getStampValue() {
		return stampValue;
	}
	public void setStampValue(Double stampValue) {
		this.stampValue = stampValue;
	}
	
}
