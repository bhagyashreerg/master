package ContractStampDuty;


public class AcsLabels {
	
	
	
	//Portal Interface Values
	public static final String PROP_CONTEXT_PATH ="config/infranet.properties";
	public static final String PROP_PROMO_ID = "Promoid";
	public static final String PROP_EVENT_TYPE = "Event_type";
	public static final String CLASS_SERVICE = "/service/telco/Postpaid";		
	public static final String CLASS_ACCOUNT = "/account";
	public static final String PROG_NAME = "STAMP_ON_CONTRACT";
	public static final int CUR_EURO = 978;
	public static final int INSUFFICIENT_FUND = 3;
	public static final int NO_FUND = 4;
	public static final String PROP_RESOURCE_ID = "Resourceid";
	
	//Logger Class
	public static final String LOG_FILE = "Log_FileName";
	public static final String LOG_LEVEL = "Log_Level";
	public static final String LOG_FILE_FORMAT = "yyyyMMddHHmmss";
	
	//DBManager Class
	public static final String PROP_DATA_PATH = "config/database.properties";
	public static final String PROP_DATE_DRIVER = "driverClassName";
	public static final String PROP_DATA_URL = "url";
	public static final String PROP_DATA_UNAME = "userName";
	public static final String PROP_DATA_PWD = "password";
	public static final String IN_STATUS = "NEW";
	public static final String DATE_PATTERN = "MM/dd/yyyy HH:mm:ss";
	public static final String YEAR_PATTERN = "yyyy";
	public static final String DATE_PATTERN_SQL = "MM/dd/yyyy HH24:MI:SS";
	public static final String REN_DATE_PATTERN = "MM/dd/yyyy";
	public static final String DATE_PATTERN_DATE = "MM/dd/yyyy";
	public static final String DATE_PATTERN_TIME = "HH:mm:ss";
	public static final String MMDDYYYY = "MMddyyyy";
	
	public static final int INACTIVE = 2;
	public static final int ACTIVE = 1;
	public static final int STATUS_FLAGS = 0;
	public static final int UNKNOWN = 0;
	public static final int ERROR = -1;
	public static final int STATUS_MISMATCH = 19;
	
	public static final String TRAFFIC_TYPE = "monthly_forward";
	public static final String CONNECTION_ERROR1 = "ORA-00028";
	public static final String CONNECTION_ERROR2 = "ORA-01012";
	
			
	public static final String Process_Name = "Contract Stamp Duty";
	public static final String Thread_Process_Name = "ContractStampDuty";
	
	public static final int CALC_ONLY = 0;
	public static final String YEAR_FORMAT = "yyyy";
	public static final String DEFAULT_YEAR = "1970";
	public static final int BUSINESS_CUSTOMER=2;
	public static final int CONSUMER_CUSTOMER=1;
	
	public static final String DELIMITER = ",";
	public static final String STR_ACTIVE = "ACTIVE";
	public static final String TABLE_ACS_TREATMENT_IVA_CONFIG = "ACS_TREATMENT_IVA_CONFIG";
	public static final String TABLE_ACS_TAX_ON_INVOICE_CONFIG  = "ACS_TAX_ON_INVOICE_CONFIG";
	public static final String TABLE_ACS_TAX_TREATMENT_POSTPAID  = "ACS_TAX_TREATMENT_POSTPAID";
	public static final String TABLE_ACS_COD_CONVERSION  = "ACS_COD_CONVERSION";
	public static final String TABLE_ACS_TAX_ON_INVOICE_POSTPAID  = "ACS_TAX_ON_INVOICE_POSTPAID";
	public static final String TABLE_ACS_TAX_ON_CONTRACT_CONFIG  = "ACS_TAX_ON_CONTRACT_CONFIG";
	public static final String TABLE_ACS_TAX_ON_CONTRACT_POSTPAID  = "ACS_TAX_ON_CONTRACT_POSTPAID";
	public static final String EXEMPTION_YES = "YES";
	public static final String STR_CONSUMER = "Consumer";
	public static final String STR_BUSINESS = "Business";
	public static final String CLASS_SEARCH = "/search";
	public static final int ADJ_RES_FAIL = 0;
	public static final String YES = "YES";
	public static final String NO = "NO";
	public static final String ET_ADJ_GL_STR_ID  = "ADJ_GL_STR_ID";
	public static final String ET_ADJ_GL_STR_VER = "ADJ_GL_STR_VER";
	public static final int EUR = 978;
	public static final String CREDIT = "Credit";
	public static final String DEBIT = "Debit";
	public static final String RES_ID_NONE = "NONE";
	public static final String ET_CONSUMER_MS = "CONSUMER_MS";
	public static final String ET_SOHOLP_MS = "SOHOLP_MS";
	public static final String SYSTEM = "CC";
	public static final String XVALUE = "X";
	public static final String DIR_IMPORT = "IMPORT";
	public static final String DIR_EXPORT = "EXPORT";
	public static final String CONSUMER_MS = "Consumer";
	public static final String SOHOLP_MS = "SOHOLP";
	public static final String POSTPAID = "Postpaid";
	
	public static final String DEFAULT_TAX_TREATMENT_ID = "TF000";

	//Narasimha: X12 
	public static final String ET_BUSINESS_MS = "BUSINESS_MS";
	public static final String BUSINESS_MS = "Business";
	
	public static final String SOHO_LP_MULTISIM = "409";
	
	public static final String TABLE_ACS_M2M_ACCOUNT  = "PIN.ACS_M2M_ACCOUNT_T";
	
	public static final String M2M_CREDIT_CLASS  = "608";
	
	/* Neethu Chnages start : X10 2016 4193 (4291) - Domestic Home (Telefono Casa) */
	public static final String TABLE_ACS_SINGLEPLAY_ACCOUNT_T  = "ACS_SINGLEPLAY_ACCOUNT_T";
	public static final String TABLE_ACS_SINGLEPLAY_STAMP_T  = "ACS_SINGLEPLAY_STAMP_T";
	public static final String SP_INST_REASONCODE = "771";
	public static final String RETURN_STAMP_REASONCODE = "772";
	public static final String TABLE_ACS_ADJUSTMENT = "ACS_ADJUSTMENT";
	
}
