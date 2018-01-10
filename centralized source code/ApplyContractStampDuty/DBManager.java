package ContractStampDuty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;



/**
 * This class is the Oracle DB Manager: it creates a connection to an oracle database
 * @author Kamalanathan
 */
public class DBManager { 
	
	
	/**
	 * @uml.property  name="dbConn"
	 */
	public Connection dbConn;
	

	/**
	 * Constructor Method:this constructor creates a connection to the Database by calling the connection method.
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public DBManager() throws FileNotFoundException, ClassNotFoundException, SQLException, IOException	{
		getConnection();		
	}

	/**
	 * This method will perform the connection to the database and will create a new Connection object
	 * that will be used by the other methods.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void getConnection() throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(new File(AcsLabels.PROP_DATA_PATH))) ;
		String strDriverClassName = props.getProperty(AcsLabels.PROP_DATE_DRIVER);
		String strUrl = props.getProperty(AcsLabels.PROP_DATA_URL);
		String strUserName = props.getProperty(AcsLabels.PROP_DATA_UNAME);
		String strPassword = props.getProperty(AcsLabels.PROP_DATA_PWD);
		Class.forName(strDriverClassName);
		dbConn = DriverManager.getConnection(strUrl,strUserName,strPassword);
		dbConn.setAutoCommit(false);
	}

	/**
	 * This method will commits the Connection object.
	 * @throws SQLException
	 */
	public void commitConnection() throws SQLException	{
		dbConn.commit();
	}

	/**
	 * This Method will relese the DataBase Connection Object.
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException	{
		if(dbConn!=null)	{
			dbConn.close();
		}
	}
	/**
	 * This Method will rollbacks the Connection object.
	 * @throws SQLException
	 */
	public void rollbackConnection() throws SQLException	{
		dbConn.rollback();
	}

	/**
	 *
	 * @param promoIdList
	 * @return
	 * @throws SQLException
	 * @throws ParseException 
	 */
	public ContractStampDutyPojo[] getContractStampDutyList(String accountNo, String creditClass, Date currentDate,Date cutOffDate,
			LoggerManager logger
	) 
	throws SQLException, ParseException 
	{
		ArrayList <ContractStampDutyPojo> processpojoArrayList;
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				AcsLabels.DATE_PATTERN);
		
		SimpleDateFormat formato = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateOffSet=formato.format(currentDate);
		long offsetValue = Utilities.deltaDate(dateOffSet, formato) /1000/60/60;
		
		Date chekDate= dateFormat.parse("01/01/2012 00:00:00");
		
		System.out.println("===>"+chekDate);
		
		String nextBillTWithOffset = "(TO_DATE ('01-01-1970 00:00:00', 'MM/DD/YYYY hh24:mi:ss') " +
		"+ (BT.NEXT_BILL_T / 60 / 60 / 24) + "+offsetValue+" / 24)";

		String nextBillT = "(TO_DATE ('01-01-1970 00:00:00', 'MM/DD/YYYY hh24:mi:ss') " +
				"+ (BT.NEXT_BILL_T / 60 / 60 / 24) + ("+offsetValue+" - 0.001) / 24)";
		
		String pinvirtualTime = "(TO_DATE('"+dateFormat.format(currentDate)+"','" + AcsLabels.DATE_PATTERN_SQL + "'))";
		
		String checkDateStr = "(TO_DATE('"+dateFormat.format(chekDate)+"','" + AcsLabels.DATE_PATTERN_SQL + "'))";
		
		//String cutOffDateStr = "TRUNC(TO_DATE('"+dateFormat.format(cutOffDate)+"','" + AcsLabels.DATE_PATTERN_SQL + "'))";
		
		//String effectiveT ="(TO_DATE ('01-01-1970 00:00:00', 'MM/DD/YYYY hh24:mi:ss')+ (ATTP.ACCT_EFFECTIVE_T / 60 / 60 / 24)+ ("+offsetValue+" - 1) / 24)";
		
		//String effectiveTStr ="TO_CHAR ("+effectiveT+", 'MMYYYY')";
		
		//String nextBillTStr ="TO_CHAR ("+nextBillT+", 'MMYYYY')";
		
		String billStartDate = "(TO_DATE ('01-01-1970 00:00:00', 'MM/DD/YYYY hh24:mi:ss') " +
		"+ (ATTP.ACCT_EFFECTIVE_T / 60 / 60 / 24) + ("+offsetValue+" - 0.001) / 24)";
		
		String query = "SELECT ATTP.*, "+nextBillTWithOffset+" as NEXT_BILL_T_OFFSET, " +
						" "+nextBillT+" as NEXT_BILL_T, "+billStartDate+" as BILL_START_DATE" +
						" FROM ACSCONFIG.ACS_TAX_TREATMENT_POSTPAID ATTP, PIN.BILLINFO_T BT, PIN.ACCOUNT_T ACC " +
						" WHERE ATTP.INSERTION_DATE >= "+checkDateStr+" " +
						" AND ATTP.BILLING_ACC_POID = BT.ACCOUNT_OBJ_ID0 AND ACC.POID_ID0= ATTP.BILLING_ACC_POID "+
						//" AND ATTP.TREATMENT_TYPE_CODE NOT IN ('"+AcsLabels.DEFAULT_TAX_TREATMENT_ID+"') "+
						//" AND MONTHS_BETWEEN( "+effectiveT+", "+nextBillT+" ) <= 2 " +
						//" AND "+effectiveTStr+" = "+nextBillTStr+" "+
						" AND ( ("+nextBillTWithOffset+" >= attp.start_date  AND "+nextBillTWithOffset+" <= attp.end_date) "+
						" OR ( "+nextBillTWithOffset+" >= attp.start_date AND attp.end_date IS NULL)) " +
						/*ISC00009993 - Fix*/
						/*" AND ATTP.TAX_ON_CONTRACT_APPLIED= '"+AcsLabels.NO+"' AND "+nextBillT+" < "+pinvirtualTime+" " +*/ 
						" AND "+nextBillT+" < "+pinvirtualTime+" " +
						" AND ((ATTP.BILLING_ACC_CODE NOT IN ( SELECT DISTINCT ACP.BILL_ACCOUNT_CODE FROM ACSCONFIG.ACS_TAX_ON_CONTRACT_POSTPAID ACP " ;
						//" WHERE  TRUNC(ACP.insertion_date) = "+cutOffDateStr+" ";
	
		
		if(accountNo!=null && !accountNo.equals("0") && !accountNo.equals("ALL")){	
			
			/*ISC00009993 - Fix*/
			/*query =query+" WHERE ACP.BILL_ACCOUNT_CODE IN ('"+accountNo+"')) ";
			query = query+" AND ATTP.BILLING_ACC_CODE IN ('"+accountNo+"')";*/
			
			query =query+" WHERE ACP.BILL_ACCOUNT_CODE IN ('"+accountNo+"')) AND ACC.CUST_SEG_LIST <> '409' AND ATTP.TAX_ON_CONTRACT_APPLIED= '"+AcsLabels.NO+"') OR (ACC.CUST_SEG_LIST ='409'))";
			query = query+" AND ATTP.BILLING_ACC_CODE IN ('"+accountNo+"') ";
		} else {
			
			/*ISC00009993 - Fix*/
			query =query+" ) AND ACC.CUST_SEG_LIST <> '409' AND ATTP.TAX_ON_CONTRACT_APPLIED= '"+AcsLabels.NO+"') OR (ACC.CUST_SEG_LIST ='409'))";
		}
        
	
		if(creditClass!=null && !creditClass.equals("0") && !creditClass.equals("ALL")){			
			query = query+" AND ACC.CUST_SEG_LIST = '"+creditClass+"'";
		}
		
		//query = query+" AND ROWNUM < 5 ";
		//query = query+" AND acc.account_no='ACCT_ND2503' ";
		 
		
		logger.log("","","Query for Getting the getContractStampDutyList : "+query);

		if	(ContractStampDutyProcessor.debugMode)
			System.out.println("Query getContractStampDutyList: " + query + "!!");

		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			stmt = dbConn.createStatement();
			rs = stmt.executeQuery(query);
			processpojoArrayList = createContractStampDutyLine(rs);
			
		}
		catch (SQLException e){ 
			throw e;
		}
		finally {
			if (stmt != null)
				stmt.close();
			
			if (rs != null)
				rs.close();			
		}

		return (ContractStampDutyPojo[])processpojoArrayList.toArray( new ContractStampDutyPojo[processpojoArrayList.size()]);
	}
	
	
	private ArrayList<ContractStampDutyPojo> createContractStampDutyLine(ResultSet rs) throws SQLException {
		
		ArrayList <ContractStampDutyPojo> list = new ArrayList<ContractStampDutyPojo>();
		while(rs.next())  
		{
			int i = 1;
			ContractStampDutyPojo line = new ContractStampDutyPojo();
			
			line.setStrCustomerCode(rs.getString(i++));			
			line.setStrAcsBillingAccount(rs.getString(i++));
			line.setAccountPoid(rs.getLong(i++));
			line.setBillInfoPoid(rs.getLong(i++));
			line.setStrTreatmentTypeCode(rs.getString(i++));
			line.setStrTreatmentType(rs.getString(i++));
			line.setStrTaxTreatmentId(rs.getString(i++));
			line.setCreationT(rs.getLong(i++));	
			line.setDtStartDate(rs.getTimestamp(i++));			
			line.setDtInsertionDate(rs.getTimestamp(i++));	
			line.setDtModifiedDate(rs.getTimestamp(i++));	
			line.setDtEndDate(rs.getTimestamp(i++));	
			line.setStrStatus(rs.getString(i++));
			line.setStrTaxOnContract(rs.getString(i++));			
			line.setNextBillTPlusOffset(rs.getTimestamp(i++));	
			line.setNextBillT(rs.getTimestamp(i++));	
			line.setBillStartDate(rs.getTimestamp(i++));	
			list.add(line);
		}
		
		return list;
	}
	
	
	
	public AcsTaxTreatmentConfigLines readCacheTax(LoggerManager logger)
	throws SQLException {
		AcsTaxTreatmentLinePojo acsTaxLineInst = null;
		AcsTaxTreatmentConfigLines acsTaxLines = new AcsTaxTreatmentConfigLines();
		Statement stmt = null;
		ResultSet rs = null;
		
		String strQuery = "Select * from "+ AcsLabels.TABLE_ACS_TREATMENT_IVA_CONFIG;
		logger.log("", "", (new StringBuilder()).append("Query for Getting the Records from ACS_TREATMENT_IVA_CONFIG : ").append(strQuery).toString());
		try {
			stmt = dbConn.createStatement();
			rs = stmt.executeQuery(strQuery);
			
			while(rs.next()) {
				int i=1;
				acsTaxLineInst = new AcsTaxTreatmentLinePojo();
				
				acsTaxLineInst.setStrTreatmentType(rs.getString(i++));
				acsTaxLineInst.setStrTreatmentTypeCode(rs.getString(i++));
				acsTaxLineInst.setVatExemption(rs.getString(i++));
				acsTaxLineInst.setPercentageVatExemption(rs.getDouble(i++));
				acsTaxLineInst.setOverrideVatCode(rs.getString(i++));
				acsTaxLineInst.setOverrideTaxCodeId(rs.getString(i++));
				acsTaxLineInst.setOverrideVatDesc(rs.getString(i++));
				acsTaxLineInst.setStampOnInvoice(rs.getString(i++));
				acsTaxLineInst.setStampOnContract(rs.getString(i++));
				acsTaxLineInst.setTcgExemption(rs.getString(i++));
				acsTaxLineInst.setMarketSegment(rs.getString(i++));
				acsTaxLineInst.setOfferType(rs.getString(i++));
				
				acsTaxLines.addLine(acsTaxLineInst);
			}
			
		} finally {
			if (rs!=null)
				rs.close();
			if (stmt!=null)
				stmt.close();
		}
		return acsTaxLines;
	}
	
	public AcsTaxOnContractConfigLines readCacheTaxOnContract(LoggerManager logger)
	throws SQLException {
		AcsTaxOnContractLinePojo acsTaxOnContractLineInst = null;
		AcsTaxOnContractConfigLines acsTaxContractLines = new AcsTaxOnContractConfigLines();
		Statement stmt = null;
		ResultSet rs = null;
		
		String strQuery = "Select * from "+ AcsLabels.TABLE_ACS_TAX_ON_CONTRACT_CONFIG;
		
		logger.log("", "", (new StringBuilder()).append("Query for Getting the Records from ACS_TAX_ON_CONTRACT_CONFIG : ").append(strQuery).toString());
		try {
			stmt = dbConn.createStatement();
			rs = stmt.executeQuery(strQuery);
			
			while(rs.next()) {
				int i=1;
				
				acsTaxOnContractLineInst = new AcsTaxOnContractLinePojo();
				acsTaxOnContractLineInst.setBusinessType(rs.getString(i++));
				acsTaxOnContractLineInst.setTaxOnContract(rs.getDouble(i++));
				acsTaxOnContractLineInst.setExemptionType(rs.getString(i++));
				acsTaxOnContractLineInst.setReasonForRequest(rs.getString(i++));
				acsTaxOnContractLineInst.setAdjustmentType(rs.getString(i++));
				acsTaxOnContractLineInst.setRateTag(rs.getString(i++));
				acsTaxOnContractLineInst.setAcctLevelResId(rs.getString(i++));
				acsTaxOnContractLineInst.setServiceLevelId1(rs.getString(i++));
				
				acsTaxContractLines.addLine(acsTaxOnContractLineInst);
			}
			
		} finally {
			if (rs!=null)
				rs.close();
			if (stmt!=null)
				stmt.close();
		}
		return acsTaxContractLines;
	}
	
	/**
	 * To update ACS_TAX_TREATMENT_POSTPAID
	 * @param billingAccountCode
	 * @param logger
	 * @throws SQLException
	 * @throws java.text.ParseException
	 */
	public void updateAcsTaxTreatmentPostPaid(
			ContractStampDutyPojo stampDutyPojo,
						LoggerManager logger
	) 
	throws SQLException {
		
		String query = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				AcsLabels.DATE_PATTERN);
		
		query = "UPDATE "+AcsLabels.TABLE_ACS_TAX_TREATMENT_POSTPAID+" SET TAX_ON_CONTRACT_APPLIED =? " +
		" WHERE TREATMENT_TYPE_CODE = ? " +
		" AND BILLING_ACC_CODE = ? AND TRUNC(START_DATE)=TRUNC(TO_DATE(?,?)) AND TAX_ON_CONTRACT_APPLIED=?";
						
		PreparedStatement pStmt = null;

		logger.log("", "", "Query for updating ACS_TAX_TREATMENT_POSTPAID : "
				+ query);
		try{
			
			int i = 1;
			pStmt = dbConn.prepareStatement(query);		
			pStmt.setString(i++, AcsLabels.YES);
			pStmt.setString(i++, stampDutyPojo.getStrTreatmentTypeCode());			
			pStmt.setString(i++, stampDutyPojo.getStrAcsBillingAccount());
			pStmt.setString(i++, dateFormat.format(stampDutyPojo.getDtStartDate()));
			pStmt.setString(i++, AcsLabels.DATE_PATTERN_SQL);
			pStmt.setString(i++, AcsLabels.NO);
			pStmt.executeQuery();
		}
		catch (SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
		}
	}
	
	
	public AcsCodConversionConfigLines readCacheCodConversion(LoggerManager logger)
	throws SQLException {
		AcsCodConversionLinePojo acsCodConversionLineInst = null;
		AcsCodConversionConfigLines acsCodConversionLines = new AcsCodConversionConfigLines();
		Statement stmt = null;
		ResultSet rs = null;
		
		String strQuery = "Select * from "+ AcsLabels.TABLE_ACS_COD_CONVERSION +" ";
		
		logger.log("", "", (new StringBuilder()).append("Query for Getting the Records from ACS_COD_CONVERSION : ").append(strQuery).toString());
		try {
			stmt = dbConn.createStatement();
			rs = stmt.executeQuery(strQuery);
			
			while(rs.next()) {
				int i=1;
				acsCodConversionLineInst = new AcsCodConversionLinePojo();
				
				acsCodConversionLineInst.setSystem(rs.getString(i++));
				acsCodConversionLineInst.setEntityType(rs.getString(i++));
				acsCodConversionLineInst.setOrganizationId(rs.getString(i++));
				acsCodConversionLineInst.setDirection(rs.getString(i++));
				acsCodConversionLineInst.setDescription(rs.getString(i++));
				acsCodConversionLineInst.setInputValue(rs.getString(i++));
				acsCodConversionLineInst.setOutputValue(rs.getString(i++));
				acsCodConversionLineInst.setKey1(rs.getString(i++));
				acsCodConversionLineInst.setKey2(rs.getString(i++));
				acsCodConversionLineInst.setKey3(rs.getString(i++));
				
				acsCodConversionLines.addLine(acsCodConversionLineInst);
			}
			
		} finally {
			if (rs!=null)
				rs.close();
			if (stmt!=null)
				stmt.close();
		}
		return acsCodConversionLines;
	}
	
	
	/**
	 * To insert ACS_TAX_ON_CONTRACT_POSTPAID
	 * @param stampDutyPojo
	 * @param logger
	 * @throws SQLException
	 * @throws java.text.ParseException
	 */
	public void insertAcsTaxOnContractPostPaid(
			ContractStampDutyPojo stampDutyPojo,AcsTaxOnContractLinePojo taxOnContractPojo,
			Date cutoffDate,Date eventCreateDt, long eventPoidId, String taxcode,
			String creditClass, Date billStartDate, String reasonCode,
						LoggerManager logger
	) 
	throws SQLException {
		
		String query = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				AcsLabels.DATE_PATTERN);
		
		query = "INSERT INTO "+AcsLabels.TABLE_ACS_TAX_ON_CONTRACT_POSTPAID+" (customer_code, bill_account_code, " +
				" external_invoice_number, treatment_type_code, insertion_date, invoice_printed_date, invoice_due_date," +
				" tax_on_contract_amt, tax_code, rate_tag, reason_for_request, event_poid, event_created_t, credit_class, bill_cycle_start_date,bill_cycle_end_date,sim_msisdn,SERVICE_OBJ_ID0) " +
				" VALUES (?,?,?,?,TO_DATE(?,'"+ AcsLabels.DATE_PATTERN_SQL + "'),?,?,?,?,?,?,?,?,?,TO_DATE(?,'"+ AcsLabels.DATE_PATTERN_SQL + "'),TO_DATE(?,'"+ AcsLabels.DATE_PATTERN_SQL + "'),?,?)";
					
		PreparedStatement pStmt = null;

		logger.log("", "", "Query for inserting into ACS_TAX_ON_CONTRACT_POSTPAID : "
				+ query);
		try{
			
			int i = 1;
			pStmt = dbConn.prepareStatement(query);		
			pStmt.setString(i++, stampDutyPojo.getStrCustomerCode());
			pStmt.setString(i++, stampDutyPojo.getStrAcsBillingAccount());
			pStmt.setString(i++, null);
			pStmt.setString(i++, stampDutyPojo.getStrTreatmentTypeCode());
			pStmt.setString(i++, dateFormat.format(cutoffDate));
			pStmt.setString(i++, null);
			pStmt.setString(i++, null);
			pStmt.setDouble(i++, taxOnContractPojo.getTaxOnContract());
			pStmt.setString(i++, taxcode);
			pStmt.setString(i++, taxOnContractPojo.getRateTag());
			if(reasonCode != null)
				pStmt.setString(i++, reasonCode);
			else
				pStmt.setString(i++, taxOnContractPojo.getReasonForRequest());
			pStmt.setLong(i++, eventPoidId);
			pStmt.setLong(i++, eventCreateDt.getTime()/1000);
			pStmt.setString(i++, creditClass);
			pStmt.setString(i++, dateFormat.format(billStartDate));
			pStmt.setString(i++, dateFormat.format(stampDutyPojo.getNextBillTPlusOffset()));
			//System.out.println("taxoncontractpojo is"+taxOnContractPojo.getBusinessType());
			//added by Mayur for X3-2012 Business hierarchy on 18-01-2012
			//if(taxOnContractPojo.getBusinessType().equals((AcsLabels.STR_CONSUMER)) || taxOnContractPojo.getBusinessType().equals(AcsLabels.SOHOLP_MS)){
			if(creditClass.equals(AcsLabels.SOHO_LP_MULTISIM)){	
			//Soholp Multisim Customers
				pStmt.setString(i++,stampDutyPojo.getMsisdn());
				 /*Added :for ISC00010084*/
				pStmt.setLong(i++,stampDutyPojo.getServicePoid());
			} else {
//				Business Customers
				pStmt.setString(i++,null);
				 /*Added :  for ISC00010084*/
				pStmt.setNull(i++, java.sql.Types.INTEGER); 
				}
			pStmt.executeQuery();
		}
		catch (SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
		}
	}
	

	//Naman: X03: 15/03/2012 Added for MULTISIM
	public boolean entryExistAcsTaxOnContractPostPaid(Long serviceId, LoggerManager logger) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement pStmt = null;
		boolean flag = false;
		 /*Added : for ISC00010084*/
		String query = "";
		query = "SELECT * FROM "+AcsLabels.TABLE_ACS_TAX_ON_CONTRACT_POSTPAID+" WHERE SERVICE_OBJ_ID0 = ? ";
		
		logger.log("", "", "Query for selecting from ACS_TAX_ON_CONTRACT_POSTPAID : "
				+ "SELECT * FROM "+AcsLabels.TABLE_ACS_TAX_ON_CONTRACT_POSTPAID+" WHERE SERVICE_OBJ_ID0 = '"+serviceId+"' ");
		
		try{
			pStmt = dbConn.prepareStatement(query);
			pStmt.setLong(1, serviceId);
			rs = pStmt.executeQuery();
			
			if(rs.next()){
				flag = true;
			}
		}
		catch(SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
			if(rs != null){
				rs.close();
			}
		}
		return flag;
	}
	
	/**
	 * @param logger
	 * @return
	 * @throws SQLException
	 */
	public int verifyM2MCustomer(LoggerManager logger, Long custPoid) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement pStmt = null;
		
		String query = "";
		query = "SELECT count(1) FROM "+AcsLabels.TABLE_ACS_M2M_ACCOUNT+ " where ACS_FLD_CUSTOMER_OBJ_ID0 = ? and ACS_FLD_TIPO_SERVIZIO = 'M2M'";
		
		logger.log("", "", "Query for selecting from ACS_M2M_ACCOUNT_T : "
				+ "SELECT count(1) FROM "
				+ AcsLabels.TABLE_ACS_M2M_ACCOUNT
				+ " where ACS_FLD_CUSTOMER_OBJ_ID0 = " + custPoid
				+ "  and ACS_FLD_TIPO_SERVIZIO = 'M2M'");
		
		try{
			pStmt = dbConn.prepareStatement(query);
			pStmt.setLong(1, custPoid);
			rs = pStmt.executeQuery();
			
			if(rs.next()){
					return rs.getInt(1);
			}
		}
		catch(SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
			if(rs != null){
				rs.close();
			}
		}
		return -1;
	}
	
	public int verifySMSM2MAccount(LoggerManager logger, Long acctPoid) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement pStmt = null;
		
		String query = "";
		query = "SELECT count(1) FROM "+AcsLabels.TABLE_ACS_M2M_ACCOUNT+ " where ACCOUNT_OBJ_ID0 = ? and ACS_FLD_TIPO_SERVIZIO = 'SMS Bulk'";
		
		logger.log("", "", "Query for selecting from ACS_M2M_ACCOUNT_T : "
				+ "SELECT count(1) FROM "
				+ AcsLabels.TABLE_ACS_M2M_ACCOUNT
				+ " where ACCOUNT_OBJ_ID0 = " + acctPoid
				+ "  and ACS_FLD_TIPO_SERVIZIO = 'SMS Bulk'");
		
		try{
			pStmt = dbConn.prepareStatement(query);
			pStmt.setLong(1, acctPoid);
			rs = pStmt.executeQuery();
			
			if(rs.next()){
				
					return rs.getInt(1);
			}
		}
		catch(SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
			if(rs != null){
				rs.close();
			}
		}
		return -1;
	}
	
	public int verifyRootM2MAccount(LoggerManager logger, Long acctPoid) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement pStmt = null;
		
		String query = "";
		query = "SELECT count(1) FROM "+AcsLabels.TABLE_ACS_M2M_ACCOUNT+ " where ACS_FLD_ROOT_ACCOUNT_OBJ_ID0 = ?";
		
		logger.log("", "", "Query for selecting from ACS_M2M_ACCOUNT_T : "
				+ "SELECT count(1) FROM "
				+ AcsLabels.TABLE_ACS_M2M_ACCOUNT
				+ " where ACS_FLD_ROOT_ACCOUNT_OBJ_ID0 = " + acctPoid);
		
		try{
			pStmt = dbConn.prepareStatement(query);
			pStmt.setLong(1, acctPoid);
			rs = pStmt.executeQuery();
			
			if(rs.next()){
				
					return rs.getInt(1);
			}
		}
		catch(SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
			if(rs != null){
				rs.close();
			}
		}
		return -1;
	}
	
public boolean rootAcctEntryExistAcsTaxOnContractPostPaid(String acctCode, LoggerManager logger) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement pStmt = null;
		boolean flag = false;
		 /*Added : for ISC00010084*/
		String query = "";
		query = "SELECT * FROM "+AcsLabels.TABLE_ACS_TAX_ON_CONTRACT_POSTPAID+" WHERE BILL_ACCOUNT_CODE = ? ";
		
		logger.log("", "", "Query for selecting from ACS_TAX_ON_CONTRACT_POSTPAID : "
				+ "SELECT * FROM "+AcsLabels.TABLE_ACS_TAX_ON_CONTRACT_POSTPAID+" WHERE BILL_ACCOUNT_CODE = '"+acctCode+"' ");
		
		try{
			pStmt = dbConn.prepareStatement(query);
			pStmt.setString(1, acctCode);
			rs = pStmt.executeQuery();
			
			if(rs.next()){
				flag = true;
			}
		}
		catch(SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
			if(rs != null){
				rs.close();
			}
		}
		return flag;
	}

	/* Neethu Changes start : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
	
	/**
	 * @param acctCode
	 * @param logger
	 * @return
	 * @throws SQLException
	 */
	public boolean checkIfSinglePlayAccount (String acctCode, LoggerManager logger) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement pStmt = null;
		boolean flag = false;
	
		String query = "";
		query = " SELECT * FROM "+AcsLabels.TABLE_ACS_SINGLEPLAY_ACCOUNT_T+" WHERE ACCOUNT_NO = ? ";
		
		logger.log("", "", "Query for selecting from ACS_SINGLEPLAY_ACCOUNT_T : "
				+ "SELECT * FROM "+AcsLabels.TABLE_ACS_SINGLEPLAY_ACCOUNT_T+" WHERE ACCOUNT_NO = '"+acctCode+"' ");
		
		try{
			pStmt = dbConn.prepareStatement(query);
			pStmt.setString(1, acctCode);
			rs = pStmt.executeQuery();
			
			if(rs.next()){
				flag = true;
			}
		}
		catch(SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
			if(rs != null){
				rs.close();
			}
		}
		return flag;
	}
	
	/**
	 * @param acctCode
	 * @param actPoid
	 * @param chargeStamp
	 * @param stampValue
	 * @param lastUpdateDate
	 * @param logger
	 * @throws SQLException
	 */
	public void insertSinglePlayStampT (String acctCode, long actPoid, Double chargeStamp, 
			Double stampValue, String lastUpdateDate, LoggerManager logger )throws SQLException, ParseException{
		
		String query = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat(AcsLabels.DATE_PATTERN);
		
		Date chekDate= dateFormat.parse(lastUpdateDate);
		
	
		
		query = "INSERT INTO " + AcsLabels.TABLE_ACS_SINGLEPLAY_STAMP_T + 
				"(ACCOUNT_NO, ACCOUNT_OBJ_ID0, CHARGED_STAMP, STAMP_VALUE, LAST_UPDATE_DATE) " +
				"VALUES (?,?,?,?,?)";
		
		PreparedStatement pStmt = null;
		logger.log("", "", "Query for inserting into ACS_SINGLEPLAY_STAMP_T : " + query);
		
		try{
			
			int i = 1;
			pStmt = dbConn.prepareStatement(query);		
			pStmt.setString(i++, acctCode);
			pStmt.setLong(i++, actPoid);
			pStmt.setDouble(i++, chargeStamp);
			pStmt.setDouble(i++, stampValue);
			pStmt.setDate(i++, new java.sql.Date(chekDate.getTime()));
			
			pStmt.executeQuery();
		}
		catch (SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
		}
	}
	
	/**
	 * @param acctCode
	 * @param logger
	 * @return
	 * @throws SQLException
	 */
	public AcsSinglePlayStampPojo getSinglePlayStampInfo (String acctCode, LoggerManager logger) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement pStmt = null;
		AcsSinglePlayStampPojo acsSinglePlayStampPojo = null;
	
		String query = "";
		
		query = "SELECT * FROM " + AcsLabels.TABLE_ACS_SINGLEPLAY_STAMP_T + " WHERE ACCOUNT_NO = ? ";
		
		logger.log("", "", "Query for selecting from ACS_SINGLEPLAY_STAMP_T : "
				+ "SELECT * FROM "+AcsLabels.TABLE_ACS_SINGLEPLAY_STAMP_T+" WHERE ACCOUNT_NO = '"+acctCode+"' ");
		
		try{
			pStmt = dbConn.prepareStatement(query);
			pStmt.setString(1, acctCode);
			rs = pStmt.executeQuery();
			
			if(rs.next()){
				acsSinglePlayStampPojo = new AcsSinglePlayStampPojo(rs.getString(1), rs.getLong(2), rs.getDouble(3), rs.getDouble(4));
				
				return acsSinglePlayStampPojo;
			}
		}
		catch(SQLException e){ 
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
			if(rs != null){
				rs.close();
			}
		}
		return null;
	}
	
	/**
	 * @param logger
	 * @return
	 * @throws SQLException
	 */
	public Double fetchStampShareVal(LoggerManager logger) throws SQLException {
		
	
		String query = "";
		
		query = "SELECT STAMP_SHARE FROM ACSCONFIG.ACS_SINGLEPLAY_STAMP_SHARE_T";
		
		Statement stmt = null;
		ResultSet rs = null;
		logger.log("", "", "Query for selecting STAMP_SHARE FROM ACS_SINGLEPLAY_STAMP_T : " + query);
		
		try{
			stmt = dbConn.createStatement();
			rs = stmt.executeQuery(query);
			if(rs.next()){
				return rs.getDouble("STAMP_SHARE");
			}
			
		}
		catch (SQLException e){ 
			throw e;
		}
		finally {
			if (stmt != null)
				stmt.close();
		}
		return null;
	}
	
	/**
	 * @param adjAmt
	 * @param accountNo
	 * @param logger
	 * @throws SQLException
	 */
	public void updateAcsSinglePlayStamp(Double adjAmt, String accountNo,
			LoggerManager logger) throws SQLException {

		String query = "";
		query = "UPDATE " + AcsLabels.TABLE_ACS_SINGLEPLAY_STAMP_T
				+ " SET CHARGED_STAMP = (CHARGED_STAMP + ?) "
				+ " WHERE ACCOUNT_NO = ?";

		PreparedStatement pStmt = null;

		logger.log("", "", "Query for updating ACS_SINGLEPLAY_STAMP_T : "
				+ query);
		try {

			pStmt = dbConn.prepareStatement(query);
			pStmt.setDouble(1, adjAmt);
			pStmt.setString(2, accountNo);
			pStmt.executeQuery();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (pStmt != null)
				pStmt.close();
		}
	}
	
	public void insertAdjustment(String acctCode, Date adjustmentDate,
			String adjustmentAmount, String strAdjId, String strAdjObjId,
			String reasonCode, String adjType, LoggerManager logger)
	throws SQLException {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				AcsLabels.DATE_PATTERN);
		
		String query= "";
		
		query= "Insert into " + AcsLabels.TABLE_ACS_ADJUSTMENT + " "+
		"(BILL_ACCOUNT_CODE, CREATED_DATE, ADJUSTMENT_DATE, ADJUSTMENT_AMOUNT, CURRENCY, " +
		"ACCOUNT_TYPE, ADJUSTMENT_TYPE, ADJUSTMENT_ID, ADJUSTMENT_OBJECT_ID, REASON_FOR_REQUEST)" +
		" Values (?, TO_DATE(?, '"+ AcsLabels.DATE_PATTERN_SQL + "'), TO_DATE(?, '"+ AcsLabels.DATE_PATTERN_SQL + "'), ?, ?, ?, ?, ?, ?, ?)";
						
		PreparedStatement pStmt = null;
		
		try{
			
			int i = 1;
			pStmt = dbConn.prepareStatement(query);		
			pStmt.setString(i++, acctCode);
			pStmt.setString(i++, dateFormat.format(adjustmentDate));
			pStmt.setString(i++, dateFormat.format(adjustmentDate));
			pStmt.setString(i++, adjustmentAmount);
			pStmt.setString(i++, AcsLabels.EUR+"");			
			pStmt.setString(i++, AcsLabels.POSTPAID);
			pStmt.setString(i++, adjType);
			pStmt.setString(i++, strAdjId);
			pStmt.setString(i++, strAdjObjId);		
			pStmt.setString(i++,reasonCode);		
			pStmt.executeQuery();
			
			logger.log("", "","Record was Inserted in to the Table: "+AcsLabels.TABLE_ACS_ADJUSTMENT + " "
					+ query);
		
		}
		catch (SQLException e){ 
			String queryPrint = "";
			queryPrint = "Insert into " + AcsLabels.TABLE_ACS_ADJUSTMENT + " "+
			"(BILL_ACCOUNT_CODE, CREATED_DATE, ADJUSTMENT_DATE, ADJUSTMENT_AMOUNT, CURRENCY, " +
			"ACCOUNT_TYPE, ADJUSTMENT_TYPE, ADJUSTMENT_ID, ADJUSTMENT_OBJECT_ID, REASON_FOR_REQUEST)" +
			" Values ('"+acctCode+"', TO_DATE('"+dateFormat.format(adjustmentDate)+"', '"+ AcsLabels.DATE_PATTERN_SQL + "'), " +
					"TO_DATE('"+dateFormat.format(adjustmentDate)+"', '"+ AcsLabels.DATE_PATTERN_SQL + "'), '"+adjustmentAmount+"', '"+AcsLabels.EUR+""+"', " +
							"'"+AcsLabels.POSTPAID+"', '"+AcsLabels.CREDIT+"', '"+strAdjId+"', '"+strAdjObjId+"', '"+reasonCode+"')";
			
			logger.log("", "","Record was Inserted in to the Table: "+AcsLabels.TABLE_ACS_ADJUSTMENT + " "
					+ queryPrint);
			
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
		}
	}
	
	/* Neethu Changes end : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
	
	
	
	/*public boolean getAdjustmentId(String adjustId,LoggerManager logger) throws SQLException{
		
		String query= "";
		boolean flag = false;
		
		query="SELECT ADJUSTMENT_ID FROM "+AcsLabels.TABLE_ACS_ADJUSTMENT+" WHERE ADJUSTMENT_ID = ?";
			
				
			
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		
		pStmt = dbConn.prepareStatement(query);	
		pStmt.setString(1, adjustId);
		
		logger.log("", "","Query to check if adjustment id is present in "+AcsLabels.TABLE_ACS_ADJUSTMENT + " "
				+ query);
		try{
				
				rs=pStmt.executeQuery();		
				if(rs.next()){
					flag = true;
				}
		
		}catch (SQLException e){ 
			String queryPrint = "";
			queryPrint = "select ADJUSTMENT_ID from AcsLabels.TABLE_ACS_ADJUSTMENT where ADJUSTMENT_ID '"+adjustId+"'";
			
			logger.log("", "","failure of select from "+AcsLabels.TABLE_ACS_ADJUSTMENT + " "
					+ queryPrint);
			
			throw e;
		}
		finally {
			if (pStmt != null)
				pStmt.close();
		}
		return flag;
	}*/
	
	public String randomNumberGenerator(){
		
		int Max=99999;
		int Min=10000;
		long n3 =  Math.round(Math.random() * (Max - Min) + Min);
				
		return Long.toString(n3);
	}
	
/*public boolean checkIfSinglePlayAccountTerminated (String acctCode,Date runDate, LoggerManager logger) throws SQLException {
		
	ResultSet rs = null;
	PreparedStatement pStmt = null;
	boolean flag = false;

	SimpleDateFormat dateFormat = new SimpleDateFormat(
			AcsLabels.DATE_PATTERN);
	String query = "";
	query = " SELECT * FROM "+AcsLabels.TABLE_ACS_SINGLEPLAY_ACCOUNT_T+" WHERE ACCOUNT_NO = ? and (TO_DATE(?, '"+ AcsLabels.DATE_PATTERN_SQL + "') > VDF_DEACTIVATION_CONFIRM AND VDF_DEACTIVATION_CONFIRM IS NOT NULL)";
	
	logger.log("", "", "Query for selecting from ACS_SINGLEPLAY_ACCOUNT_T : "
			+ "SELECT * FROM "+AcsLabels.TABLE_ACS_SINGLEPLAY_ACCOUNT_T+" WHERE ACCOUNT_NO = '"+acctCode+"' and (TO_DATE('"+dateFormat.format(runDate)+"', '"+ AcsLabels.DATE_PATTERN_SQL + "') > VDF_DEACTIVATION_CONFIRM AND VDF_DEACTIVATION_CONFIRM IS NOT NULL)");
	
	
	
	//Date chekDate= dateFormat.parse(runDate);
	try{
		pStmt = dbConn.prepareStatement(query);
		pStmt.setString(1, acctCode);
		pStmt.setString(2, dateFormat.format(runDate));
		rs = pStmt.executeQuery();
		
		if(rs.next()){
			flag = true;
		}
	}
	catch(SQLException e){ 
		throw e;
	}
	finally {
		if (pStmt != null)
			pStmt.close();
		if(rs != null){
			rs.close();
		}
	}
	return flag;
}*/

public boolean CheckContarctStampDuty (long acctpoid, LoggerManager logger) throws SQLException {
	
	ResultSet rs = null;
	PreparedStatement pStmt = null;

	String query = "";
	query = " select 1 from pin.event_t et,pin.event_bal_impacts_t ebit " +
			"where et.poid_id0=ebit.obj_id0  and et.program_name='STAMP_ON_CONTRACT' " +
			"and ebit.rate_tag = 'Storno Bollo Contratto' and et.account_obj_id0= ? ";
	
	logger.log("",  "Query for checking if single play account is terminated and contract stamp duty is applied once",
			 "select 1 from pin.event_t et,pin.event_bal_impacts_t ebit where et.poid_id0=ebit.obj_id0  and et.program_name='STAMP_ON_CONTRACT' and ebit.rate_tag = 'Storno Bollo Contratto' and et.account_obj_id0= ? "+acctpoid);
	
	
	
	//Date chekDate= dateFormat.parse(runDate);
	try{
		pStmt = dbConn.prepareStatement(query);
		pStmt.setLong(1, acctpoid);
		
		rs = pStmt.executeQuery();
		
		if(rs.next()){
			return true;
		}else{
			return false;
		}
	}
	catch(SQLException e){ 
		throw e;
	}
	finally {
		if (pStmt != null)
			pStmt.close();
		if(rs != null){
			rs.close();
		}
	}
	
}

}