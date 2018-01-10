package ContractStampDuty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import com.portal.pcm.EBufException;
import com.portal.pcm.FList;
import com.portal.pcm.Poid;
import com.portal.pcm.PortalContext;
import com.portal.pcm.PortalOp;
import com.portal.pcm.SparseArray;
import com.portal.pcm.fields.FldAccountNo;
import com.portal.pcm.fields.FldAccountObj;
import com.portal.pcm.fields.FldAliasList;
import com.portal.pcm.fields.FldAmount;
import com.portal.pcm.fields.FldArgs;
import com.portal.pcm.fields.FldBalGrpObj;
import com.portal.pcm.fields.FldBalImpacts;
import com.portal.pcm.fields.FldBalOperand;
import com.portal.pcm.fields.FldBalances;
import com.portal.pcm.fields.FldBusinessType;
import com.portal.pcm.fields.FldCurrency;
import com.portal.pcm.fields.FldCurrentBal;
import com.portal.pcm.fields.FldCustomerSegmentList;
import com.portal.pcm.fields.FldDebit;
import com.portal.pcm.fields.FldDescr;
import com.portal.pcm.fields.FldEndT;
import com.portal.pcm.fields.FldFlags;
import com.portal.pcm.fields.FldLogin;
import com.portal.pcm.fields.FldName;
import com.portal.pcm.fields.FldPoid;
import com.portal.pcm.fields.FldProgramName;
import com.portal.pcm.fields.FldReadBalgrpMode;
import com.portal.pcm.fields.FldResult;
import com.portal.pcm.fields.FldResults;
import com.portal.pcm.fields.FldServiceObj;
import com.portal.pcm.fields.FldStartT;
import com.portal.pcm.fields.FldStrVersion;
import com.portal.pcm.fields.FldStringId;
import com.portal.pcm.fields.FldSubBalances;
import com.portal.pcm.fields.FldTaxCode;
import com.portal.pcm.fields.FldTemplate;
import com.portal.pcm.fields.FldTypeStr;
import com.portal.pcm.fields.FldValidTo;
import com.portal.pcm.fields.FldVirtualT;



public class PortalInterface  {

	private PortalContext context = null;
	/**
	 * @uml.property  name="ldbNumber"
	 */
	private long ldbNumber ;
	/**
	 * @uml.property  name="prop"
	 */
	/**
	 * @uml.property  name="prop"
	 */
	private Properties prop ;
	/**
	 * @uml.property  name="strPromoIDs"
	 */
	String strPromoIDs = null;
	/**
	 * @uml.property  name="strInFList"
	 */
	String strInFList = null;

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws EBufException
	 */
	public PortalInterface() throws FileNotFoundException, IOException,
	EBufException {
		openPortalContext();
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws EBufException
	 */
	public PortalContext openPortalContext() throws FileNotFoundException,
	IOException, EBufException {

		prop = new Properties();
		File file = new File(AcsLabels.PROP_CONTEXT_PATH);

		FileInputStream fis;

		fis = new FileInputStream(file);
		prop.load((InputStream) fis);
		context = new PortalContext(prop);
		context.connect();
		ldbNumber = context.getCurrentDB();
		return context;
	}


	public Poid[] getServiceFromLogin(PortalContext context, String serviceClass, String login, LoggerManager logger) throws EBufException	{

		FList inFList = new FList();
		FList outFList = null;

		inFList.set(FldPoid.getInst(), new Poid(context.getCurrentDB(), -1, serviceClass));
		inFList.set(FldLogin.getInst(), login);

		strInFList = inFList.asString();
		logger.log("","Call OPCODE ACT_FIND\n",strInFList);

		outFList = context.opcode(PortalOp.ACT_FIND, 0, inFList);
		strInFList = outFList.asString();
		logger.log("","Output OPCODE ACT_FIND\n",strInFList);

		Poid[] result = new Poid[2];
		result[1] = outFList.get(FldPoid.getInst());
		result[0] = outFList.get(FldAccountObj.getInst());

		return result;

	}

	/*
	 * @param context
	 * @param logger
	 * @return pin virtual time is returned as date object
	 * @throws EBufException if an error came during opcode execution
	 * @author niranjan.narayana
	 */
	public Date getPinVirtualTime(PortalContext context, LoggerManager logger)
	throws EBufException {
		FList inFList = new FList();
		FList outFList;
		inFList.set(FldPoid.getInst(), new Poid( ldbNumber , -1, "/account" ));
		//logger.log("","","Call OPCODE GET_PIN_VIRTUAL_TIME :\n");
		strInFList = inFList.asString();
		//logger.log("","Input Flist :\n",strInFList);
		outFList = context.opcode( PortalOp.GET_PIN_VIRTUAL_TIME, inFList);
		strInFList = outFList.asString();
		logger.log("","pin_virtual_time :",outFList.get(FldVirtualT.getInst()).toString());

		return outFList.get(FldVirtualT.getInst());
	}

	public Integer getBusinessType(PortalContext context, Poid acctPoid , LoggerManager logger) throws EBufException	{

		FList inFList = new FList();
		FList outFList = null;
		Integer businessType = null;

			inFList.set(FldBusinessType.getInst());
			outFList=opReadFlds(context,acctPoid,inFList , logger);

			businessType = outFList.get(FldBusinessType.getInst());
			return businessType;

	}


	private static FList opReadFlds(PortalContext context, Poid obj,
			FList fields , LoggerManager logger) throws EBufException {

		FList inFList, outFList;
		inFList = new FList();

		inFList = fields.deepClone();
		inFList.set(FldPoid.getInst(), obj);
		logger.log("","Call OPCODE READ_FLDS\n", inFList.asString());

		outFList = context.opcode(PortalOp.READ_FLDS, inFList);
		logger.log("","OutFlist OPCODE READ_FLDS\n",outFList.asString());

		return outFList;
	}

	public Poid findCustomer(PortalContext context, String accountNumber, LoggerManager logger)
			throws EBufException { //Search for customer POID
		FList inFList = new FList();
		FList outFList = null;
		inFList.set(FldPoid.getInst(), new Poid(context.getCurrentDB(), -1,
				"/account"));
		FList args = new FList();
		args.set(FldAccountNo.getInst(), accountNumber);
		inFList.setElement(FldArgs.getInst(), 1, args);
		logger.log("","Call OPCODE CUST_FIND\n", inFList.asString());

		outFList = context.opcode(PortalOp.CUST_FIND, inFList);

		logger.log("","OutFlist OPCODE CUST_FIND\n", outFList.asString());
		FList results = outFList.get(FldResults.getInst()).getAnyElement();
		return results.get(FldPoid.getInst());

	}



	/**
	 * @param context
	 * @param accountNumber
	 * @return
	 * @throws EBufException
	 */
	public HashMap<Long,String> getServiceDataUsingAccountPoid(PortalContext context,
				Poid acctPoid, String creditClass, boolean singlePlayFlag, LoggerManager logger) throws EBufException, java.text.ParseException {
			FList inFList = new FList();
			FList outFList;
			FList arg1 = new FList();
			FList arg2 = new FList();
			
			/*Added forISC00010084*/
			HashMap<Long,String> serviceData = new  HashMap<Long,String>();
			String eventSource = null;
			String eventSourceTemp = null;
			Long serviceId = null;
			
			

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			ContractStampDutyPojo line = new ContractStampDutyPojo();

				inFList.set(FldPoid.getInst(), new Poid(context.getCurrentDB(), -1,
						AcsLabels.CLASS_SEARCH));
				/*inFList
						.set(FldTemplate.getInst(),
								"select X from /service 1, /account 2 where ( 1.F1 = 2.F2 and 2.F3 = V3 )");*/
				inFList
				.set(FldTemplate.getInst(),
						"select X from /service  where ( F1 = V1 and F2 = V2 )");
				inFList.set(FldFlags.getInst(), 0);
				inFList.setElement(FldResults.getInst(), 0);

				//Naman : Start ISC00008234

				/*arg1.set(FldAccountObj.getInst(), stringToPoid("0.0.0.1 /account 0 0"));
				inFList.setElement(FldArgs.getInst(), 1, arg1);
				arg2.set(FldPoid.getInst(), stringToPoid("0.0.0.1 "+AcsLabels.CLASS_SERVICE+" 0 0"));
				inFList.setElement(FldArgs.getInst(), 2, arg2);
				arg3.set(FldAccountNo.getInst(), accountNumber);
				inFList.setElement(FldArgs.getInst(), 3, arg3);*/

				arg1.set(FldAccountObj.getInst(), acctPoid);
				inFList.setElement(FldArgs.getInst(), 1, arg1);
				arg2.set(FldPoid.getInst(), stringToPoid("0.0.0.1 "+AcsLabels.CLASS_SERVICE+" -1 0"));
				inFList.setElement(FldArgs.getInst(), 2, arg2);

				//Naman : End ISC00008234

				logger.log("","Call OPCODE SEARCH\n", inFList.asString());

				outFList = context.opcode(PortalOp.SEARCH, 0, inFList);

				logger.log("","OutFlist OPCODE SEARCH\n", outFList.asString());

				if(outFList.hasField(FldResults.getInst())){
					SparseArray resultList = outFList.get(FldResults.getInst());
				    Enumeration resultEnum = resultList.getKeyEnumerator();
				    while(resultEnum.hasMoreElements()){
				    	eventSourceTemp = null;
				    	Integer resIndex = (Integer) resultEnum.nextElement();
					    FList result = resultList.elementAt(resIndex);
					    serviceId = result.get(FldPoid.getInst()).getId();
					    if(result.hasField(FldAliasList.getInst())){
						    SparseArray aliasList = result.get(FldAliasList.getInst());
						    Enumeration aliasIndexEnum = aliasList.getKeyEnumerator();
	
						    while (aliasIndexEnum.hasMoreElements()) {
						     Integer aliasIndex = (Integer) aliasIndexEnum.nextElement();
						     eventSourceTemp = aliasList.elementAt(aliasIndex).get(
								       FldName.getInst());
	
						     // Check for MSISDN which is not terminated
	
						     //Naman : Start ISC00008234
						     /*Added:start forISC00010084*/
							    	 //if ( eventSourceTemp.startsWith("39") && eventSourceTemp != "39")
						     if(!eventSourceTemp.startsWith("222") && eventSourceTemp != "39")
						     {
	
								    	 if(eventSourceTemp.contains("_TX-")){ 
								    		/*BSS01084227 -don't apply stamp duty for terminated singlePlay -ashutosh*/
								    		 if(singlePlayFlag == false)								    		 
								    			 eventSource= eventSourceTemp.substring(0, 12);
								    		 else
								    			 eventSource = eventSourceTemp;
								    	 } else {
								    		 eventSource=eventSourceTemp;
								    	 }
								    	 if(eventSource != null)
								    		 serviceData.put(serviceId,eventSource);
								    	 break; // stops the loop search and returns eventSource
								     }
						     //Naman : End ISC00008234
						    }
						    if(!creditClass.equals(AcsLabels.SOHO_LP_MULTISIM)){
						    	 break; // stops the loop search and returns HashMap
						    }
					    }
				    }
				   }
				return serviceData;
		}

	public Poid stringToPoid(String poidString)
	{
		//In prod mapping table the poid is a string with this form: "0.0.0.db type id rev"
		StringTokenizer st = new StringTokenizer(poidString, ". ");
		st.nextToken();//first zero
		st.nextToken();//second zero
		st.nextToken();//third zero
		long db = Long.parseLong(st.nextToken());
		String type = st.nextToken();
		long id = Long.parseLong(st.nextToken());

		return new Poid(db, id, type);
	}

	public BigDecimal getResourceBalance(PortalContext context, String resourceId, Poid accountPoid,
			Date startDate, Date endDate, LoggerManager logger)
		throws EBufException	{

			FList inFList = new FList();
			FList outFList;
			BigDecimal currentBal= new BigDecimal(0);
			String year=null;

			inFList.set(FldPoid.getInst(), accountPoid);
			inFList.set(FldFlags.getInst(), 1);
			inFList.set(FldStartT.getInst(), startDate);
			inFList.set(FldEndT.getInst(), endDate);
			inFList.set(FldReadBalgrpMode.getInst(),AcsLabels.CALC_ONLY);
			inFList.setElement(FldBalances.getInst(),0);


			strInFList = inFList.asString();
			logger.log("","Call OPCODE BAL_GET_BALANCES\n",strInFList);

			outFList = context.opcode(PortalOp.BAL_GET_BALANCES, inFList);
			strInFList = outFList.asString();
			logger.log("","Output OPCODE BAL_GET_BALANCES\n",strInFList);

			SparseArray balances = outFList.get(FldBalances.getInst());
			Enumeration indexEnum =  balances.getKeyEnumerator();

			// Retrieving Current Balance
			while (indexEnum.hasMoreElements())
			{
				Integer index = (Integer)indexEnum.nextElement();

					if(resourceId.equals(index.toString())){

						if (!balances.elementAt(index).hasField(FldSubBalances.getInst())){
							logger.log("","","No resource is availble for this month");
							continue;
						}

						SparseArray subBal = balances.elementAt(index).get(FldSubBalances.getInst());
						Enumeration subIndexEnum = subBal.getKeyEnumerator();
						while(subIndexEnum.hasMoreElements())
						{
							Integer subIndex = (Integer)subIndexEnum.nextElement();
							Date validTo = subBal.elementAt(subIndex).get(FldValidTo.getInst());
							logger.log("","Valid To: ",validTo.toString());
							year = new SimpleDateFormat("yyyy").format(validTo);
							if(year.equals(AcsLabels.DEFAULT_YEAR))
							{
								currentBal = subBal.elementAt(subIndex).get(FldCurrentBal.getInst());
							}

						}

				}
			}
			return currentBal;
		}

	public String[] callApiAccAdjustment(Date adjustDate, AcsTaxOnContractLinePojo acsTaxOnContractConfigLine, Poid subscrPoid, String glStringId, String glStringVer, String reasonCode,String adjustmentType,Double stampShare, PortalContext context,DBInterface dbInf,LoggerManager logger)
	throws EBufException
	{
	
		boolean adjIdExists=false;
		Calendar cal=Calendar.getInstance();
		FList inFList = new FList();
		FList outFList = null;
		String strDescr = null;
		String strAdjId = null;
		String strCreatBy = null;
		String strAccType = null;
		String strType = null;
		String strAdjObjId = null;
		String strReasonCode = null;
		BigDecimal amount =null;
		String[] output = null;

		String randomNumber = dbInf.getDbManager().randomNumberGenerator();
		
		String strBuff = "                                                                                                                                                      ";
		strDescr ="Adj_Stamp_Contract" + strBuff;
		strAdjId =cal.getTimeInMillis() + randomNumber + strBuff;
		strAdjObjId =cal.getTimeInMillis() + strBuff;
		strCreatBy ="BatchProcess" + strBuff;
		if(reasonCode != null)
			strReasonCode = reasonCode + strBuff;
		else
			strReasonCode = acsTaxOnContractConfigLine.getReasonForRequest() + strBuff;
		
		strDescr = strDescr.substring(0,144);
		strAdjId = strAdjId.substring(0,38);
		strAdjObjId =strAdjObjId.substring(0,23);
		strCreatBy = strCreatBy.substring(0,28);
		strReasonCode = strReasonCode.substring(0,5);
		
		
		
			if(stampShare == 0 ){
				amount = BigDecimal.valueOf(0.0001);
			} else {
				amount = BigDecimal.valueOf(stampShare);
			}
		
		
			/*if(acsTaxOnContractConfigLine.getTaxOnContract()==0){
				amount = BigDecimal.valueOf(0.0001);
			} else {
				amount = BigDecimal.valueOf(acsTaxOnContractConfigLine.getTaxOnContract());
			}*/
		
			
				
				if(adjustmentType.equalsIgnoreCase(AcsLabels.CREDIT)){
					amount= amount.multiply(new BigDecimal(-1));
					strType="1";
				} else {
					strType="2";
				}
				
				
			
			
		/*if(acsTaxOnContractConfigLine.getAdjustmentType().equals(AcsLabels.CREDIT)){
			amount= amount.multiply(new BigDecimal(-1));
			strType="1";
		} else {
			strType="2";
		}*/

		strAccType ="1";//Postpaid

		strDescr= strDescr + "|" + strAdjId + "|" + strCreatBy + "|" + strAccType + "|" + strType + "|" + strAdjObjId +"|" + strReasonCode;
		strDescr=strDescr.replaceAll(" {2,}", " ");
		strDescr=strDescr.replace(" |", "|");

		inFList.set(FldPoid.getInst(), subscrPoid);
		inFList.set(FldProgramName.getInst(), AcsLabels.PROG_NAME);
		inFList.set(FldEndT.getInst(), adjustDate );
		inFList.set(FldAmount.getInst(), amount);
		inFList.set(FldCurrency.getInst(), AcsLabels.EUR);
		inFList.set(FldDescr.getInst(), strDescr);
		inFList.set(FldStringId.getInst(), Integer.parseInt(glStringId));
		inFList.set(FldStrVersion.getInst(), Integer.parseInt(glStringVer));

		//NN: Added for Roll-up patch
		Poid balGrpPoid = getBalanceGroup(context,subscrPoid,logger);
		inFList.set(FldBalGrpObj.getInst(),balGrpPoid);
		inFList.set(FldFlags.getInst(),1);
		//NN: End: Added for Roll-up patch

		logger.log("","Call OPCODE AR_ACCOUNT_ADJUSTMENT\n", inFList.asString());

		outFList = context.opcode(PortalOp.AR_ACCOUNT_ADJUSTMENT, 0, inFList);
		logger.log("","OutFlist OPCODE AR_ACCOUNT_ADJUSTMENT\n", outFList.asString());

		if ( outFList.hasField(FldResults.getInst()) )
		{
			FList results = outFList.get(FldResults.getInst()).getAnyElement();

			Poid eventPoidd = results.get(FldPoid.getInst());
			FList balImpacts =results.get(FldBalImpacts.getInst()).getAnyElement();

			String taxCode =balImpacts.get(FldTaxCode.getInst());
			output = new String[4];
			output[0] = String.valueOf(eventPoidd.getId());
			output[1] = taxCode;
			output[2] = strAdjId.trim();
			output[3] = strAdjObjId.trim();
		}

		if ( outFList.hasField(FldResult.getInst()) )
		{
			int result = outFList.get(FldResult.getInst()).intValue();

			if ( result == AcsLabels.ADJ_RES_FAIL )
			{

				String descr = outFList.get(FldDescr.getInst());
				logger.log("","Adjustment could not be done for the reason: ", descr);
				return null;
			}


		}

		return output;

	}

	private Poid getBalanceGroup(PortalContext context, Poid servPoid, LoggerManager logger) throws EBufException	{

		FList inFList = new FList();
		FList outFList = null;


			inFList.set(FldBalGrpObj.getInst());
			logger.log("","Call OPCODE PCM_OP_READ_FLD\n", inFList.asString());
			outFList=opReadFlds(context,servPoid,inFList,logger);
			logger.log("","Output OPCODE PCM_OP_READ_FLD\n", outFList.asString());

			Poid balanceGroup = outFList.get(FldBalGrpObj.getInst());
			return balanceGroup;

	}


	public void callApiBillDebit(PortalContext context, Poid acctPoid, Poid servicePoid, Date startDate,
			String  resourceId, AcsTaxOnContractLinePojo acsTaxOnContractConfigLine, String glStringId, String glStringVer, LoggerManager logger) throws EBufException{

		FList inFList = new FList();
		FList outFList = null;

		BigDecimal amount = BigDecimal.valueOf(acsTaxOnContractConfigLine.getTaxOnContract());
		if(acsTaxOnContractConfigLine.getAdjustmentType().equals(AcsLabels.CREDIT)){
			amount= amount.multiply(new BigDecimal(-1));
		}

		inFList.set(FldPoid.getInst(), acctPoid);
		inFList.set(FldProgramName.getInst(), AcsLabels.PROG_NAME);
		inFList.set(FldServiceObj.getInst(), servicePoid);
		inFList.set(FldStartT.getInst(), startDate);

		FList setLimit = new FList();
		setLimit.set(FldBalOperand.getInst(),amount);
		inFList.setElement(FldDebit.getInst(),Integer.parseInt(resourceId),setLimit);

		inFList.set(FldStringId.getInst(), Integer.parseInt(glStringId));
		inFList.set(FldStrVersion.getInst(), Integer.parseInt(glStringVer));

		strInFList=	inFList.asString();
		logger.log("","Call OPCODE BILL_DEBIT:\n",strInFList);

		outFList = context.opcode(PortalOp.BILL_DEBIT, 0, inFList);
		strInFList = outFList.asString();
		logger.log("", "Output OPCODE BILL_DEBIT:\n", strInFList);
	}

	/**
	 * @param context
	 * @param acctPoid
	 * @return
	 * @throws AcsException
	 */
	public String getCreditClassInfo(PortalContext context, Poid acctPoid, LoggerManager logger)
			throws EBufException {
		FList inFList, outFList;
		inFList = new FList();
		inFList.set(FldPoid.getInst(), acctPoid);
		String creditClass = null;

		strInFList=	inFList.asString();
		logger.log("","Call OPCODE READ_OBJ Credit Class Info:\n",strInFList);

		outFList = context.opcode(PortalOp.READ_OBJ, inFList);
		strInFList = outFList.asString();
		logger.log("", "Output OPCODE READ_OBJ Credit Class Info:\n", strInFList);

		creditClass = String.valueOf(outFList.get(FldCustomerSegmentList.getInst()));

		return creditClass;
	}

	/**
	 * @param creditClass
	 * @return
	 * @throws AcsException
	 */
	public String getMarketSegmentStr(AcsCodConversionConfigLines codConConfigLines, String creditClass, LoggerManager logger) throws Exception{

		String marketSegment = null;
		String consumerMS = null;

		consumerMS = codConConfigLines.getOutputValue(AcsLabels.SYSTEM,AcsLabels.ET_CONSUMER_MS,
				AcsLabels.XVALUE,AcsLabels.DIR_IMPORT,AcsLabels.CONSUMER_MS,AcsLabels.XVALUE, AcsLabels.XVALUE,
				AcsLabels.XVALUE);

		if(consumerMS ==null){
			logger.log("", "Consumer is not configured in ACS_COD_CONVERSION table","");
			return null;
		}

		String soholpMS = codConConfigLines.getOutputValue(AcsLabels.SYSTEM,AcsLabels.ET_SOHOLP_MS,
				AcsLabels.XVALUE,AcsLabels.DIR_IMPORT,AcsLabels.SOHOLP_MS,AcsLabels.XVALUE, AcsLabels.XVALUE,
				AcsLabels.XVALUE);

		if(soholpMS ==null){
			logger.log("", "SOHOLP is not configured in ACS_COD_CONVERSION table","");
			return null;
		}

		String businessMS = null;

		businessMS = codConConfigLines.getOutputValue(AcsLabels.SYSTEM,AcsLabels.ET_BUSINESS_MS,
				AcsLabels.XVALUE,AcsLabels.DIR_IMPORT,AcsLabels.BUSINESS_MS,AcsLabels.XVALUE, AcsLabels.XVALUE,
				AcsLabels.XVALUE);

		if(businessMS ==null){
			logger.log("", "Business is not configured in ACS_COD_CONVERSION table","");
			return null;
		}

		if(consumerMS != null){
			if(consumerMS.contains(creditClass)){
				marketSegment = AcsLabels.CONSUMER_MS;
			}
		}

		if(soholpMS != null && marketSegment==null){
			if(soholpMS.contains(creditClass)){
				marketSegment = AcsLabels.SOHOLP_MS;
			}
		}

		if(businessMS != null && marketSegment == null){
			if(businessMS.contains(creditClass)){
				marketSegment = AcsLabels.BUSINESS_MS;
			}
		}

		return marketSegment;
	}
	
	/**
	 * @param context
	 * @param acctPoid
	 * @param logger
	 * @param profileType
	 * @return
	 * @throws Exception
	 */
	public FList getProfileData(PortalContext context, Poid acctPoid, LoggerManager logger, String profileType) throws Exception {
		FList inFList = new FList();
		FList outFList = null;
		inFList.set(FldPoid.getInst(), new Poid(context.getCurrentDB()));
		inFList.set(FldAccountObj.getInst(), acctPoid);
		inFList.set(FldTypeStr.getInst(), profileType);

		try {
			
			logger.log("","Call OPCODE CUST_FIND_PROFILE:\n",inFList.asString());
			
			outFList = context.opcode(PortalOp.CUST_FIND_PROFILE, PortalContext.OPFLG_READ_RESULT, inFList);
			
			logger.log("","Output CUST_FIND_PROFILE:\n",outFList.asString());
		} catch (EBufException e) {
			throw new Exception(
					"Error during CUST_FIND_PROFILE opcode processing:\n" + e);
		}
		try {
			return outFList.get(FldResults.getInst()).getAnyElement();
		} catch (EBufException e) {
			throw new Exception( 
					"Profile info not found in portal DB");
		}
	}
}