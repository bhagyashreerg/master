package clientWebservice;

import java.util.HashMap;

public class AcsLabels {

	//INFRANET PROPERTIES
	public static String PROP_CONTEXT_PATH ="";
	
	//DATABASE PROPERTIES
	public static final String PROP_DATA_PATH = "config/database.properties";
	public static final String PROP_DATE_DRIVER = "driverClassName";
	public static String PROP_DATA_URL = "";
	public static final String PROP_DATA_UNAME = "userName";
	public static final String PROP_DATA_PWD = "password";
	
	
	// REQUEST AND RESPONSE PATH
	public String PROP_REQUEST_PATH = "request";
	public String PROP_RESPONSE_PATH = "response";
	
	public static String WEB_SERVICE_URL  = "";
	
	public HashMap<String, String>  hm_DBEnv = new HashMap<String, String>();
	public HashMap<String, String>  hm_PortalEnv = new HashMap<String, String>();
	public HashMap<String, String>  hm = new HashMap<String, String>();
	public HashMap<String, String>  xmlTemplate = new HashMap<String, String>();
	public HashMap<String, String>  mapCreditClass_PRE = new HashMap<String, String>();
	public HashMap<String, String>  mapCreditClass_POST = new HashMap<String, String>();
	public HashMap<String, String>  markSeg = new HashMap<String, String>();

	public AcsLabels() {
		
		hm_DBEnv.put("1", "dev_url");
		hm_DBEnv.put("2", "it_url");
		hm_DBEnv.put("3", "atbill_url");
		hm_DBEnv.put("4", "iodio_url");
		hm_DBEnv.put("5", "astato_url");
		
		hm_PortalEnv.put("1", "config/infranet_dev.properties");
		hm_PortalEnv.put("2", "config/infranet_it.properties");
		hm_PortalEnv.put("3", "config/infranet_atbill.properties");
		hm_PortalEnv.put("4", "config/infranet_iodio.properties");
		hm_PortalEnv.put("5", "config/infranet_astato.properties");
		
		hm.put("1", "PRC");
		hm.put("2", "PRB");
		hm.put("3", "PRS");
		hm.put("4", "POC");
		hm.put("5", "POB");
		hm.put("6", "POS");
		hm.put("7", "POH");
		hm.put("8", "PRD");
		hm.put("9", "POD");
		hm.put("10", "OD");
		hm.put("11", "OU");
		hm.put("12", "AD");
		hm.put("13", "POCSP");  //added
		hm.put("14", "POM2M"); //added for M2M
		
		
		xmlTemplate.put("1", "./xml_template/PRC");
		xmlTemplate.put("2", "./xml_template/PRB");
		xmlTemplate.put("3", "./xml_template/PRS");
		xmlTemplate.put("4", "./xml_template/POC");
		xmlTemplate.put("5", "./xml_template/POB");
		xmlTemplate.put("6", "./xml_template/POS");
		xmlTemplate.put("7", "./xml_template/POH");
		xmlTemplate.put("8", "./xml_template/PRD");
		xmlTemplate.put("9", "./xml_template/POD");
		xmlTemplate.put("10", "./xml_template/OD");
		xmlTemplate.put("11", "./xml_template/OU");
		xmlTemplate.put("12", "./xml_template/AD");
		xmlTemplate.put("13", "./xml_template/POCSP");  //added
		xmlTemplate.put("14", "./xml_template/POM2M"); //added for M2M
		
		
		markSeg.put("5", "GruppoPoste");
		markSeg.put("4", "SME");
		markSeg.put("3", "Top");
		markSeg.put("2", "Large");
		markSeg.put("1", "Small");
		
		mapCreditClass_PRE.put("GruppoPoste", "200");
		mapCreditClass_PRE.put("SME", "201");
		mapCreditClass_PRE.put("Top", "203");
		mapCreditClass_PRE.put("Large", "204");
	    mapCreditClass_PRE.put("Small", "205");
	    
	    mapCreditClass_POST.put("GruppoPoste", "404");
		mapCreditClass_POST.put("SME", "407");
		mapCreditClass_POST.put("Top", "405");
		mapCreditClass_POST.put("Large", "406");
	    mapCreditClass_POST.put("Small", "408");
	    
	}

	
}
