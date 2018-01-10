package ContractStampDuty;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.portal.pcm.EBufException;
import com.portal.pcm.Poid;
import com.portal.pcm.PortalContext;




/**
 * @author Kamal.thangavel
 *
 */
public class ContractStampDutyManagement {
	
	//private static boolean debugMode = ContractStampDutyProcessor.debugMode;
		
	/**
	 * @param dbInf
	 * @param portal
	 * @param context
	 * @param processPojoArray
	 * @param contractStampDutyPojo
	 * @param acsTaxTreatmentLines
	 * @param logger
	 * @throws Exception 
	 */
	public static void contractStampDuty(
			DBInterface dbInf,
			PortalInterface portal, 
			PortalContext context,
			ArrayList<ContractStampDutyPojo> processPojoArray,
			ContractStampDutyPojo contractStampDutyPojo,
			AcsTaxTreatmentConfigLines acsTaxTreatmentConfigLines,
			AcsTaxOnContractConfigLines acsTaxOnContractConfigLines,
			AcsCodConversionConfigLines acsCodConversionConfigLines,
			LoggerManager logger)	
	{
								
		
		AcsTaxTreatmentLinePojo acsTaxTreatmentConfigLine = null;
		AcsTaxOnContractLinePojo acsTaxOnContractConfigLine = null;
		ContractStampDutyPojo processLine = null;
		boolean technicalError;	
		boolean insertError;
		Integer businessType = 0;
		String businessTypeStr = null;
		
		String glStringId=null;
		String glStringVer=null;
		
		/* Neethu Changes start : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
		String glStringIdSP=null;
		String glStringVerSP=null;
		String glStringIdInsSP=null;
		String glStringVerInsSP=null;
		
		Poid acctPoid = null;
		Poid servPoid = null;
		String eventSource = null;
		HashMap<Long,String> serviceData = null;
		String resourceId =null;
		String creditClass =null;
		String marketSegment =null;
		Set<?> set= null;
		
		int eventSourceCount = 0;
		int insertCount = 0;
		
		int verifyM2MCustomerFlag = -1;
		int verifySMSBulkAcctFlag = -1;
		int verifyM2MRootAcctFlag = -1;
		boolean applyStampForM2M = false;
		Poid custPoid = null;
		
		/* Neethu Change start : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
		boolean singlePlayFlag = false; 
		String outputSinglePlay[] = null;
		String outputSinglePlayInst[] = null;
		boolean insertSinglePlayInfo = false;
		boolean singlePlayTerminated = false;
		String reasonCode = null;
		/* Neethu Change end: X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */

		
		logger.log("", "", (new StringBuilder()).append("\n").toString());
		logger.log("STARTING CONTRACT STAMP DUTY PROCESS ","","");

		for(int Index = 0; Index < processPojoArray.size(); Index++)
		{
			acsTaxOnContractConfigLine = null;
			technicalError=false;	
			insertError=true;
			processLine = processPojoArray.get(Index);			
			glStringId=null;
			glStringVer=null;
			eventSource = null;
			acctPoid = null;
			resourceId=null;
			creditClass =null;
			marketSegment =null;
			
			verifySMSBulkAcctFlag = -1;
			verifyM2MCustomerFlag = -1;
			verifyM2MRootAcctFlag = -1;
			applyStampForM2M = false;
			custPoid = null;
			
			/* Neethu Changes start : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
			insertSinglePlayInfo = false;
			singlePlayTerminated = false;
			glStringIdSP=null;
			glStringVerSP=null;
			
			glStringIdInsSP=null;
			glStringVerInsSP=null;
			singlePlayFlag = false; 
			
			boolean flag = false;
			
			logger.log("", "", (new StringBuilder())
					.append("\nStarting elaboration of row related to Treatment Fiscal Code: ")
					.append(processLine.getStrTreatmentTypeCode())
					.append(" and Billing Account: ")
					.append(processLine.getStrAcsBillingAccount())
					.append("\n").toString());
			
							
			try{
				
				//Naman : Start ISC00008234
				//To Fetch the eventsource list
				String acctPoidStr = String.valueOf(processLine.getAccountPoid());
				acctPoid = portal.stringToPoid("0.0.0.1 "+AcsLabels.CLASS_ACCOUNT+" "+acctPoidStr);
				
				/* Neethu Changes start : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
				singlePlayFlag = dbInf.getDbManager().checkIfSinglePlayAccount(processLine.getStrAcsBillingAccount(), logger);
				
				custPoid = portal.findCustomer(context,processLine.getStrCustomerCode(),logger);
				if(custPoid == null){
					logger.log("", "", (new StringBuilder())
							.append("\nCustomer: ")
							.append(processLine.getStrCustomerCode())
							.append(" not found on the DB. ")
							.toString());
					continue;
				}
				
				//To Fetch the Credit Class
				creditClass = portal.getCreditClassInfo(context,acctPoid, logger);
				
				if(AcsLabels.M2M_CREDIT_CLASS.equals(creditClass)){
					verifySMSBulkAcctFlag = dbInf.getDbManager().verifySMSM2MAccount(logger,acctPoid.getId());
					verifyM2MCustomerFlag = dbInf.getDbManager().verifyM2MCustomer(logger, custPoid.getId());
					verifyM2MRootAcctFlag = dbInf.getDbManager().verifyRootM2MAccount(logger,acctPoid.getId());
					
					 if(verifyM2MRootAcctFlag > 0){
						applyStampForM2M = true;
						logger.log("", "", (new StringBuilder())
									.append("Root Account Apply Stamp Duty").toString());
					} else if(verifySMSBulkAcctFlag > 0 && verifyM2MCustomerFlag > 0) {
						applyStampForM2M = true;
						logger.log("", "", (new StringBuilder())
								.append("SMS Bulk Account with M2M associated Customer Apply Stamp Duty").toString());
						
					} else if(verifySMSBulkAcctFlag == 0 && verifyM2MCustomerFlag > 0) {
						applyStampForM2M = false;
						logger.log("", "", (new StringBuilder())
								.append("Do not Apply Stamp Duty as it is a Child Account").toString());
						continue;
					
					} else if(verifySMSBulkAcctFlag == 0 && verifyM2MCustomerFlag == 0) {
						applyStampForM2M = true;
						logger.log("", "", (new StringBuilder())
								.append("Non M2M Account or Only Generic Account Apply Stamp Duty").toString());
						
					}else if(verifySMSBulkAcctFlag > 0 && verifyM2MCustomerFlag == 0) {
						applyStampForM2M = true;
						logger.log("", "", (new StringBuilder())
								.append("Only SMS Bulk Account Apply Stamp Duty").toString());
						
					} 
				}
				
				serviceData = portal.getServiceDataUsingAccountPoid(context, acctPoid, creditClass, singlePlayFlag, logger);
				//Naman : End ISC00008234
				
			
				
				if(serviceData.size()>0 || (AcsLabels.M2M_CREDIT_CLASS.equals(creditClass) && applyStampForM2M)) {
											
					// Naman : Start ISC00008234  
					//To Fetch the account and service poid
					/*Poid[] poids = portal.getServiceFromLogin(context,AcsLabels.CLASS_SERVICE,eventSource.get(0), logger);
					acctPoid = poids[0];
					servPoid = poids[1];*/
					//Naman : End ISC00008234  
					
					//	To Fetch the business type
					businessType = portal.getBusinessType(context,acctPoid,logger);								
					
					if(businessType==null){
						logger.log("","","business type not available...");
						continue;
					}
					
					if(businessType == AcsLabels.BUSINESS_CUSTOMER){
						
						businessTypeStr = AcsLabels.STR_BUSINESS;
					}else if(businessType == AcsLabels.CONSUMER_CUSTOMER){
						
						businessTypeStr = AcsLabels.STR_CONSUMER;
							
					}
					
					
					
					logger.log("","","Credit Class: "+creditClass);
						
					//To Fetch the MarketSegment
					marketSegment = portal.getMarketSegmentStr(acsCodConversionConfigLines, creditClass, logger);
					
					logger.log("","","Market Segment: "+marketSegment);
					
					if(marketSegment==null){
						continue;
					}					
					
					logger.log("","","TreatmentType: "+processLine.getStrTreatmentTypeCode());
					//	To Fetch the tax details
					acsTaxTreatmentConfigLine = acsTaxTreatmentConfigLines.getLine(processLine.getStrTreatmentTypeCode(), marketSegment, AcsLabels.POSTPAID);
					
					if(acsTaxTreatmentConfigLine==null){
						logger.log("","","Tax Treatment not configured in ACS_TREATMENT_IVA_CONFIG table...");
						continue;
					}
					
	//				If the account has Tax Exemption for Stamp on Contract process further
					/*if (acsTaxTreatmentConfigLine.getStampOnContract() != null
							&& acsTaxTreatmentConfigLine.getStampOnContract()
									.equalsIgnoreCase(AcsLabels.EXEMPTION_YES)) {*/
						
					//	Fetch the Tax on Contract details
					if(businessTypeStr.equals(AcsLabels.STR_CONSUMER) && marketSegment.equals(AcsLabels.SOHOLP_MS)){
						
						businessTypeStr=marketSegment;
					}
					logger.log("","","businessTypeStr: "+businessTypeStr);
					
					acsTaxOnContractConfigLine = acsTaxOnContractConfigLines.getLine(businessTypeStr,acsTaxTreatmentConfigLine.getStampOnContract());
					
					
					if(acsTaxOnContractConfigLine==null){
						logger.log("","","Tax contract details not configured in ACS_TAX_ON_CONTRACT_CONFIG table...");
						continue;
					}
						
						
					glStringId = acsCodConversionConfigLines.getOutputValue(AcsLabels.SYSTEM,AcsLabels.ET_ADJ_GL_STR_ID,
							AcsLabels.XVALUE,AcsLabels.DIR_IMPORT,acsTaxOnContractConfigLine.getReasonForRequest(),acsTaxOnContractConfigLine.getAdjustmentType(), AcsLabels.XVALUE, 
							AcsLabels.XVALUE);
											
					if(glStringId==null){
						
						logger.log("","","Adjustment details for ADJ_GL_STR_ID not configured in ACS_COD_CONVERSION table...");
						continue;
					}
					
					glStringVer = acsCodConversionConfigLines.getOutputValue(AcsLabels.SYSTEM,AcsLabels.ET_ADJ_GL_STR_VER,
							AcsLabels.XVALUE,AcsLabels.DIR_IMPORT,acsTaxOnContractConfigLine.getReasonForRequest(),acsTaxOnContractConfigLine.getAdjustmentType(), AcsLabels.XVALUE, 
							AcsLabels.XVALUE);
					
					if(glStringVer==null){
						
						logger.log("","","Adjustment details for ADJ_GL_STR_VER not configured in ACS_COD_CONVERSION table...");
						continue;
					}
					
					/* Neethu Changes end : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
					glStringIdSP = acsCodConversionConfigLines.getOutputValue(AcsLabels.SYSTEM,AcsLabels.ET_ADJ_GL_STR_ID,
							AcsLabels.XVALUE,AcsLabels.DIR_IMPORT,AcsLabels.RETURN_STAMP_REASONCODE,AcsLabels.CREDIT, AcsLabels.XVALUE, 
							AcsLabels.XVALUE);
											
					if(glStringIdSP==null){
						
						logger.log("","","Adjustment details for ADJ_GL_STR_ID not configured in ACS_COD_CONVERSION table...");
						continue;
					}
					
					glStringVerSP = acsCodConversionConfigLines.getOutputValue(AcsLabels.SYSTEM,AcsLabels.ET_ADJ_GL_STR_VER,
							AcsLabels.XVALUE,AcsLabels.DIR_IMPORT,AcsLabels.RETURN_STAMP_REASONCODE,AcsLabels.CREDIT, AcsLabels.XVALUE, 
							AcsLabels.XVALUE);
					
					if(glStringVerSP==null){
																		
						logger.log("","","Adjustment details for ADJ_GL_STR_VER not configured in ACS_COD_CONVERSION table...");
						continue;
					}
					
					glStringIdInsSP = acsCodConversionConfigLines.getOutputValue(AcsLabels.SYSTEM,AcsLabels.ET_ADJ_GL_STR_ID,
							AcsLabels.XVALUE,AcsLabels.DIR_IMPORT,AcsLabels.SP_INST_REASONCODE,AcsLabels.DEBIT, AcsLabels.XVALUE, 
							AcsLabels.XVALUE);
											
					if(glStringIdInsSP==null){
						
						logger.log("","","Adjustment details for ADJ_GL_STR_ID not configured in ACS_COD_CONVERSION table...");
						continue;
					}
					
					glStringVerInsSP = acsCodConversionConfigLines.getOutputValue(AcsLabels.SYSTEM,AcsLabels.ET_ADJ_GL_STR_VER,
							AcsLabels.XVALUE,AcsLabels.DIR_IMPORT,AcsLabels.SP_INST_REASONCODE,AcsLabels.DEBIT, AcsLabels.XVALUE, 
							AcsLabels.XVALUE);
					
					if(glStringVerInsSP==null){
						
						logger.log("","","Adjustment details for ADJ_GL_STR_VER not configured in ACS_COD_CONVERSION table...");
						continue;
					}
					/* Neethu Changes end : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
				}
				
				//	Naman: X03: 15/03/2012 Updated for MULTISIM
				
				context.transactionOpen(PortalContext.TRAN_OPEN_READWRITE);
				
				 /*Added :  for ISC00010084 :*/
				set = serviceData.entrySet(); 
				Iterator<?> itr = set.iterator();
				while(itr.hasNext() || (AcsLabels.M2M_CREDIT_CLASS.equals(creditClass) && applyStampForM2M)) {
					
					Map.Entry me = null;
					if(serviceData.size() > 0){
						
						me = (Map.Entry)itr.next(); 
						Long serviceKey = (Long) me.getKey();
						processLine.setServicePoid(serviceKey);
					}
					
					if (creditClass.equals(AcsLabels.SOHO_LP_MULTISIM)){
						flag = dbInf.getDbManager().entryExistAcsTaxOnContractPostPaid(processLine.getServicePoid(), logger);
					}
					
					if((AcsLabels.M2M_CREDIT_CLASS.equals(creditClass) && applyStampForM2M))
						flag = dbInf.getDbManager().rootAcctEntryExistAcsTaxOnContractPostPaid(processLine.getStrAcsBillingAccount(), logger);
					
					
				if (flag == false){
					
					/*if(singlePlayFlag && dbInf.getDbManager().checkIfSinglePlayAccountTerminated(processLine.getStrAcsBillingAccount(),contractStampDutyPojo.getCutOffDate(),logger)){
						logger.log("","","SinglePlay Account already Deactivated, And the account_no is : "+processLine.getStrAcsBillingAccount());
						continue;
					}*/
					if(serviceData.size() > 0){
						eventSource =(String) me.getValue();
						
						if(singlePlayFlag && eventSource.contains("_TX-")) {
							
							eventSource= eventSource.substring(0, 12);
							singlePlayTerminated = true;
						}
						
					}
							
					eventSourceCount++;
					//added by mayur for X3-2012 business hierarchy on 18-01-2012
					processLine.setMsisdn(eventSource);	
					
					Date eventCreateDt = portal.getPinVirtualTime(context,logger);
					/*ISC00012569 start*/
					if(singlePlayTerminated && dbInf.getDbManager().CheckContarctStampDuty(processLine.getAccountPoid(),logger)){
						
						try{
							dbInf.getDbManager().updateAcsTaxTreatmentPostPaid(
									processLine,
									logger);
						}catch(SQLException e){
							logger.log("", "",
									"Error updating table ACS_TAX_TREATMENT_POSTPAID"
									+ e.fillInStackTrace());					
							technicalError=true;
						}
						
						logger.log("","","SinglePlay account is terminated and Contract stamp duty charges are already applied once for account no:"+processLine.getStrAcsBillingAccount());
						continue;
					}
					/*ISC00012569 end*/
					
					AcsSinglePlayStampPojo acsSinglePlayStampPojo = dbInf.getDbManager().getSinglePlayStampInfo(processLine.getStrAcsBillingAccount(), logger);
					String output[] = null;
					
					//Call opcode for adjustment
					if(!singlePlayFlag || acsSinglePlayStampPojo == null)
						output = portal.callApiAccAdjustment(processLine.getNextBillT(),
							acsTaxOnContractConfigLine,acctPoid, glStringId, glStringVer, null,acsTaxOnContractConfigLine.getAdjustmentType(),acsTaxOnContractConfigLine.getTaxOnContract(), context,dbInf,logger);
								
					
					/* Neethu Changes end : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
					if(singlePlayFlag){
						
						SimpleDateFormat sdf1 = new SimpleDateFormat(AcsLabels.DATE_PATTERN);
						Calendar cal = Calendar.getInstance();
						String lastUpdateDate = sdf1.format(cal.getTime());






						/*if (singlePlayFlag
									&& dbInf
											.getDbManager()
											.checkIfSinglePlayAccountTerminated(
													processLine
															.getStrAcsBillingAccount(),
													contractStampDutyPojo
															.getCutOffDate(),
													logger) && acsSinglePlayStampPojo != null) {
								logger
										.log(
												"",
												"",
												"SinglePlay Account already Deactivated, and the account_no is : "
														+ processLine
																.getStrAcsBillingAccount());
								continue;
							}*/










						
						/*If for the first run then refund*/
						if(acsSinglePlayStampPojo == null){
							
						/*	String tempAdjustType = acsTaxOnContractConfigLine.getAdjustmentType();
							logger.log("","","------tempAdjustType-------"+tempAdjustType);
							
							
							if (tempAdjustType.equalsIgnoreCase(AcsLabels.CREDIT)){
								acsTaxOnContractConfigLine.setAdjustmentType(AcsLabels.DEBIT);

							}else {
								acsTaxOnContractConfigLine.setAdjustmentType(AcsLabels.CREDIT);
							}*/
							String adjustmentType = null;
							if (acsTaxOnContractConfigLine.getAdjustmentType().equalsIgnoreCase(AcsLabels.CREDIT)){
								adjustmentType = AcsLabels.DEBIT;

							}else {
								adjustmentType=AcsLabels.CREDIT;
							}
							
							//logger.log("","","-----adjustement55555------"+acsTaxOnContractConfigLine.getAdjustmentType());
							
							outputSinglePlay = portal.callApiAccAdjustment(processLine.getNextBillT(),
									acsTaxOnContractConfigLine,acctPoid, glStringIdSP, glStringVerSP, AcsLabels.RETURN_STAMP_REASONCODE,adjustmentType,acsTaxOnContractConfigLine.getTaxOnContract(), context,dbInf,logger);
							
							dbInf.getDbManager().insertAdjustment(
										processLine.getStrAcsBillingAccount(),
										sdf1.parse(lastUpdateDate),
										(acsTaxOnContractConfigLine.getTaxOnContract() * -1)+ "", 
										outputSinglePlay[2],outputSinglePlay[3], AcsLabels.RETURN_STAMP_REASONCODE,
										AcsLabels.CREDIT, logger);
							
						
							/*acsTaxOnContractConfigLine.setAdjustmentType(tempAdjustType);
							logger.log("","","-----after resetting------"+acsTaxOnContractConfigLine.getAdjustmentType());*/
						}

						Double stampShare = 0D;
						String tablename = null;
						try{
							
							
							if(!singlePlayTerminated){
								
								stampShare = dbInf.getDbManager().fetchStampShareVal(logger);
								
								if(stampShare == null){
									
									logger.log("", "", "Error fetching stamp value from DB");					
									technicalError=true; 
								}
								



								if(acsSinglePlayStampPojo == null){
									
									/*Double tempContractFee = acsTaxOnContractConfigLine.getTaxOnContract();
									
									logger.log("","","------tempContractFee-------"+tempContractFee);
									acsTaxOnContractConfigLine.setTaxOnContract(stampShare);
	
									logger.log("","","------getTaxOnContract11111-------"+acsTaxOnContractConfigLine.getTaxOnContract());*/
								
									
									
									outputSinglePlayInst = portal.callApiAccAdjustment(processLine.getNextBillT(),


											acsTaxOnContractConfigLine,acctPoid, glStringIdInsSP, glStringVerInsSP,  AcsLabels.SP_INST_REASONCODE,acsTaxOnContractConfigLine.getAdjustmentType(),stampShare, context,dbInf,logger);
									





								/*	acsTaxOnContractConfigLine.setTaxOnContract(tempContractFee);
									logger.log("","","------getTaxOnContract2222-------"+acsTaxOnContractConfigLine.getTaxOnContract());*/
									
									tablename="ACS_SINGLEPLAY_STAMP_T";
									/*insert details but skipping update of the charged share as insert done with correct value*/
									dbInf.getDbManager().insertSinglePlayStampT(processLine.getStrAcsBillingAccount(),
											processLine.getAccountPoid(), stampShare, acsTaxOnContractConfigLine.getTaxOnContract(), lastUpdateDate, logger);
									
									tablename="ACS_ADJUSTMENT";
									
									dbInf.getDbManager().insertAdjustment(processLine.getStrAcsBillingAccount(),
											sdf1.parse(lastUpdateDate), stampShare+"", outputSinglePlayInst[2], outputSinglePlayInst[3], AcsLabels.SP_INST_REASONCODE, AcsLabels.DEBIT, logger );
									
									/*remaining amount calculated for adj that is value of X*/
									Double adjDiffAmt = acsTaxOnContractConfigLine.getTaxOnContract() - stampShare;
									
									/*To decide for SP to insert into ACS_CONTRACT_POSTPAID table*/
									Double lastRunAdjAmt = adjDiffAmt;
										
									/*If value <= 0 then decision made to insert*/
									if(lastRunAdjAmt.compareTo(0D) <= 0) {
										
										insertSinglePlayInfo = true;
										reasonCode = AcsLabels.SP_INST_REASONCODE;
										output = outputSinglePlayInst;
									}
									
								
								} else {
									
									//System.out.println("-----------came to else ----------");
									
									/*remaining amount calculated for adj that is value of X*/
									Double adjDiffAmt = acsSinglePlayStampPojo.getStampValue() - acsSinglePlayStampPojo.getChargedStamp();
									
									/*To decide for SP to insert into ACS_CONTRACT_POSTPAID table*/
									Double lastRunAdjAmt = adjDiffAmt - stampShare;
									
									if(adjDiffAmt.compareTo(0D) > 0){
								
										Double currAdjAmt = stampShare;
										
										/*If its the last installment*/
										if(lastRunAdjAmt.compareTo(stampShare) < 0 
												&& adjDiffAmt.compareTo(stampShare) < 0)
											currAdjAmt = adjDiffAmt;
										
									/*	Double tempContractFee = acsTaxOnContractConfigLine.getTaxOnContract();
										acsTaxOnContractConfigLine.setTaxOnContract(currAdjAmt);
										*/



										
										outputSinglePlayInst = portal.callApiAccAdjustment(processLine.getNextBillT(),
												acsTaxOnContractConfigLine,acctPoid, glStringIdInsSP, glStringVerInsSP, AcsLabels.SP_INST_REASONCODE,acsTaxOnContractConfigLine.getAdjustmentType(),currAdjAmt, context,dbInf,logger);
										
										//acsTaxOnContractConfigLine.setTaxOnContract(tempContractFee);
										
										tablename="ACS_SINGLEPLAY_STAMP_T";
										dbInf.getDbManager().updateAcsSinglePlayStamp(currAdjAmt, processLine.getStrAcsBillingAccount(), logger);
										//logger.log("","","----------update ofupdateAcsSinglePlayStamp done ------");
										
										tablename="ACS_ADJUSTMENT";
										dbInf.getDbManager().insertAdjustment(processLine.getStrAcsBillingAccount(),
												sdf1.parse(lastUpdateDate), currAdjAmt+"", outputSinglePlayInst[2], outputSinglePlayInst[3], AcsLabels.SP_INST_REASONCODE, AcsLabels.DEBIT, logger);
										
										/*If value <= 0 then decision made to insert*/
										if(lastRunAdjAmt.compareTo(0D) <= 0) {
											
											insertSinglePlayInfo = true;
											reasonCode = AcsLabels.SP_INST_REASONCODE;
											output = outputSinglePlayInst;
										}
									} 
								}
							}	
						}catch(SQLException e){
							logger.log("", "",
									"Error inserting table "+tablename+" "
									+ e.fillInStackTrace());					
							technicalError=true;
						}
					}
					/* Neethu Changes end : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
					
					if(singlePlayFlag == false || (singlePlayFlag == true && insertSinglePlayInfo == true)){
						
						/*if(businessType != null && businessType == AcsLabels.BUSINESS_CUSTOMER){//Business Customer
							
							resourceId = acsTaxOnContractConfigLine.getAcctLevelResId();
						} else if(businessType == AcsLabels.CONSUMER_CUSTOMER){//Consumer Customer
							
							resourceId = acsTaxOnContractConfigLine.getServiceLevelId1();							
						}
						
						if(resourceId!= null && !resourceId.equalsIgnoreCase(AcsLabels.RES_ID_NONE) ){
						
							//To update the counter
							portal.callApiBillDebit(
									context, acctPoid, servPoid,
									processLine.getNextBillT(),
									resourceId,
									acsTaxOnContractConfigLine,
									glStringId, glStringVer,
									logger
									);
						}*/	
						
						/* Neethu Changes end : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
							
						Date billStartDate = Utilities.fetchLatestDate(processLine.getBillStartDate(), processLine.getNextBillTPlusOffset());
						
						// To insert in the ACS_TAX_ON_CONTRACT_POSTPAID table
						try{
							dbInf.getDbManager().insertAcsTaxOnContractPostPaid(
									processLine,
									acsTaxOnContractConfigLine,
									contractStampDutyPojo.getCutOffDate(),
									eventCreateDt,
									Long.parseLong(output[0]),
									output[1],
									creditClass,
									billStartDate, reasonCode, 
									logger);
							
							insertCount++;
							
						}catch(SQLException e){
							logger.log("", "",
									"Error inserting table ACS_TAX_ON_CONTRACT_POSTPAID"
									+ e.fillInStackTrace());					
							technicalError=true;
						}
					}
					/*} else {
						
						logger.log("","","Exemption is not activated, verify in ACS_TREATMENT_IVA_CONFIG table ...");
						continue;
					}*/
					
					// X03 : Start For ISC00008009 - Multiple stamp on contract for business account
					if(!creditClass.equals(AcsLabels.SOHO_LP_MULTISIM)){
						
						break;
					}
					// X03 : End  For ISC00008009 - Multiple stamp on contract for business account
				
						}else {
						
							logger.log("","","Already Stamp Duty Calculated for this Service");
						}
				}	
				
				if(serviceData.size()==0 && !(AcsLabels.M2M_CREDIT_CLASS.equals(creditClass))){
					
					logger.log("","","Base Plan not purchased for this account...");
					technicalError=true;
				}
				
				if(technicalError == false && eventSourceCount == insertCount){
					
					/* Neethu Changes end : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
					if(singlePlayFlag == false || (singlePlayFlag == true && insertSinglePlayInfo == true)) {
						
						// To update in the ACS_TAX_TREATMENT_POSTPAID table
						try{
							dbInf.getDbManager().updateAcsTaxTreatmentPostPaid(
									processLine,
									logger);
						}catch(SQLException e){
							logger.log("", "",
									"Error updating table ACS_TAX_TREATMENT_POSTPAID"
									+ e.fillInStackTrace());					
							technicalError=true;
						}
					}
				}
					
			} catch (EBufException e){
				logger.log("", "","Error: "+ e.fillInStackTrace());
				technicalError=true;
			} catch (SQLException e){
				logger.log("", "","Error: "+ e.fillInStackTrace());
				technicalError=true;
			}
			catch (Exception e){
				logger.log("", "","Error: "+ e.fillInStackTrace());
				technicalError=true;
			}
			
			
			//Portal and Database Commit/Abort
			if(technicalError==false){
				
				try{
					context.transactionCommit();					
					logger.log("","","Transaction committed successfully");
					
					dbInf.getDbManager().commitConnection();
					logger.log("","","Database committed successfully");
					
				}catch (EBufException e){
					logger.log("","","Issue with transaction commit "+e.fillInStackTrace());	
					try{
						dbInf.getDbManager().rollbackConnection();
						logger.log("","","Database Rollbacked");
						context.transactionAbort();
						logger.log("","","Transaction Aborted");
						
					}
					catch(SQLException e1){
						logger.log("","","Issue with Database rollback "+e1.fillInStackTrace());
					}
					catch (EBufException e1){
						logger.log("","","Issue with transaction abort "+e1.fillInStackTrace());	
					}
					
				}
				catch (SQLException ex)
				{
					logger.log("", "", "Issue with DB commit"+ ex.fillInStackTrace());
				}
				
//				try {
//					dbInf.getDbManager().commitConnection();
//					logger.log("","","Database committed successfully");
//					
//				} catch (Exception ex) {
//					logger.log("", "", "Issue with DB commit"+ ex.fillInStackTrace());
//				}
				
			} else {
				
				try {
					context.transactionAbort();
					logger.log("","","Transaction Aborted");
					
				} catch (EBufException ex) {
					logger.log("", "", "Issue with transaction aborting"+ ex.fillInStackTrace());
				}
				
				try {
					dbInf.getDbManager().rollbackConnection();
					logger.log("","","Database Rollbacked");
					
				} catch (Exception e) {
					logger.log("","","Issue with DB rollback"+e.fillInStackTrace());
				}
			}
			//End
		}
		

		logger.log("END OF CONTRACT STAMP DUTY PROCESS","","");
	}
	
	

	
}
