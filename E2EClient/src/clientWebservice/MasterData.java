package clientWebservice;


public class MasterData {
	private String CUSTOMER_CODE;
	private String ACCOUNT_CODE ;
	private String ROOT_INST_CODE ;
	private String PROD_INST_CODE ;
	private String OPTION_CATALOGUE_CODE ;
	/*private String OPTION_TARIFF_CODE ;*/
	
	
	
	private String PVT_CODE ;
	private String Tipo_MVNO;
	private String MSISDN_CODE ;
	private String BASE_TARIFF_CODE ;
	private String IMSI_CODE ;
	private String CHANNEL_CODE ;
	private String REF_TRANS_CODE ;
	private String REF_AMT_CODE ;
	private String PAYMETHOD_CODE ;
	private String CREDIT_CLASS_CODE ;
	private String MARKET_SEGMENT ;
	//added
	private String Numero_Portato;
	private String Numero_Temporaneo;
	private String Codice_Migrazione_GNP;
	private String Data_Attivazione_Linea;
	private String Data_Attivazione_GNP;
	private String Tipologia_Terminale;
	//added for 6080
	private String Data_Attivazione_Canone;
	private String Data_disattivazione_Canone;
	
	
	
	
	//Handset
	private String ROOT_INST_CODE_HS ;
	private String DELIVERY_DATE;
	private String ENTRY_FEE;
	private String IMPORTO_FEE;
	private String NUMERO;
	private String IMPORTO_SHIP;
	private String ATTRIBUTE_PARAMETER_ACC_TAG1 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG2 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG3 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG4 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG5 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG6 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG7 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG8 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG9 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG10 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG11 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG12 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG13 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG14 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG15 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG16 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG17 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG18 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG19 = "";
	private String ATTRIBUTE_PARAMETER_ACC_TAG20 = "";	
	
	/*private String Auto_Refill_Threshold = "0" ;
	private String Auto_Refill_Amount = "0" ;*/

	private String ATTRIBUTE_PARAMETER_TAG1 = "";
	private String ATTRIBUTE_PARAMETER_TAG2 = "";
	private String ATTRIBUTE_PARAMETER_TAG3 = "";
	private String ATTRIBUTE_PARAMETER_TAG4 = "";
	private String ATTRIBUTE_OVERRIDE_START = "";
	private String ATTRIBUTE_OVERRIDE_END = "";
	private String ATTRIBUTE_OVERRIDE_PF = "";
	
	//For Dual Billing
	private String CUSTOMER_CODE_DUAL;
	private String ACCOUNT_CODE_DUAL ;	
	
	//ADJUSTMENT
	private String ADJ_AMT;
	private String CURR_CODE ;
	private String ADJ_TYPE ;
	private String REA_REQ ;
	private String DAYS_EXP ;
	private String ADJ_ID ;
	private String ADJ_ACCT ;
	
	
	private String ATTRIBUTE_TEMP = 	"<m1:Attribute>" +
			"<m1:ActionCode>Add</m1:ActionCode>" +
			"<m1:Name>SW_ATT_NAME</m1:Name>" +
			"<m1:Value>SW_ATT_VAL</m1:Value>" +
			"</m1:Attribute>" ;
	
	private  String ATTRIBUTE_TEMP_DEL = 	"<m1:Attribute>" +
			"<m1:ActionCode>Delete</m1:ActionCode>" +
			"<m1:Name>SW_ATT_NAME</m1:Name>" +
			"<m1:Value>SW_ATT_VAL</m1:Value>" +
			"</m1:Attribute>" ;
	
	private String ATTRIBUTE_TEMP_UPDATE = 	"<m1:Attribute>" +
			"<m1:ActionCode>Update</m1:ActionCode>" +
			"<m1:Name>SW_ATT_NAME</m1:Name>" +
			"<m1:Value>SW_ATT_VAL</m1:Value>" +
			"</m1:Attribute>" ;
	
	private String HANDSET_OLD = "<m1:ParentInstanceId>SW_ROOTINST_AUTO_XXXX</m1:ParentInstanceId>" ;
	/* +"\n"+
     "<m1:ProductInstanceId>SW_PRODINST_AUTO_XXXX</m1:ProductInstanceId>"*/
	private String HANDSET_NEW =/* "<m1:RootInstanceId>SW_ROOTINST_AUTO_XXXX</m1:RootInstanceId>" +"\n"+*/
	          "<m1:ParentInstanceId/>"; /*+"\n"+
	          "<m1:ProductInstanceId>SW_ROOTINST_AUTO_XXXX</m1:ProductInstanceId>" ;*/

	// THESE NOT USED IN XML TEMPLATE
	private String PROP_TEMPLATE_ID;
	private long AUTO_NUMBER;
	private long AUTO_EPOCHTIME;
	//public AcsLabels al = new AcsLabels();
	
	
	
	public String getCUSTOMER_CODE() {
		return CUSTOMER_CODE;
	}

	public void setCUSTOMER_CODE(String cUSTOMER_CODE) {
		CUSTOMER_CODE = cUSTOMER_CODE;
	}

	public String getPROP_TEMPLATE_ID() {
		return PROP_TEMPLATE_ID;
	}

	public void setPROP_TEMPLATE_ID(String pROP_TEMPLATE_ID) {
		PROP_TEMPLATE_ID = pROP_TEMPLATE_ID;
	}

	public long getAUTO_NUMBER() {
		return AUTO_NUMBER;
	}

	public long getAUTO_EPOCHTIME() {
		return AUTO_EPOCHTIME;
	}
	
	public void setAUTO_NUMBER(long aUTO_NUMBER) {
		AUTO_NUMBER = aUTO_NUMBER;
	}

	public String getACCOUNT_CODE() {
		return ACCOUNT_CODE;
	}

	public void setACCOUNT_CODE(String aCCOUNT_CODE) {
		ACCOUNT_CODE = aCCOUNT_CODE;
	}

	public String getROOT_INST_CODE() {
		return ROOT_INST_CODE;
	}

	public void setROOT_INST_CODE(String rOOT_INST_CODE) {
		ROOT_INST_CODE = rOOT_INST_CODE;
	}

	public String getPROD_INST_CODE() {
		return PROD_INST_CODE;
	}

	public void setPROD_INST_CODE(String pROD_INST_CODE) {
		PROD_INST_CODE = pROD_INST_CODE;
	}

	public String getOPTION_CATALOGUE_CODE() {
		return OPTION_CATALOGUE_CODE;
	}

	public void setOPTION_CATALOGUE_CODE(String oPTION_CATALOGUE_CODE) {
		OPTION_CATALOGUE_CODE = oPTION_CATALOGUE_CODE;
	}

	/*public String getOPTION_TARIFF_CODE() {
		return OPTION_TARIFF_CODE;
	}

	public void setOPTION_TARIFF_CODE(String oPTION_TARIFF_CODE) {
		OPTION_TARIFF_CODE = oPTION_TARIFF_CODE;
	}*/

	public String getPVT_CODE() {
		return PVT_CODE;
	}

	public void setPVT_CODE(String pVT_CODE) {
		PVT_CODE = pVT_CODE;
	}

	public String getMSISDN_CODE() {
		return MSISDN_CODE;
	}

	public void setMSISDN_CODE(String mSISDN_CODE) {
		MSISDN_CODE = mSISDN_CODE;
	}
	
	public String getTipo_MVNO() {
		return Tipo_MVNO;
	}

	public void setTipo_MVNO(String tipo_MVNO) {
		Tipo_MVNO = tipo_MVNO;
	}

	public String getBASE_TARIFF_CODE() {
		return BASE_TARIFF_CODE;
	}

	public void setBASE_TARIFF_CODE(String bASE_TARIFF_CODE) {
		BASE_TARIFF_CODE = bASE_TARIFF_CODE;
	}

	public String getIMSI_CODE() {
		return IMSI_CODE;
	}

	public void setIMSI_CODE(String iMSI_CODE) {
		IMSI_CODE = iMSI_CODE;
	}

	public String getCHANNEL_CODE() {
		return CHANNEL_CODE;
	}

	public void setCHANNEL_CODE(String cHANNEL_CODE) {
		CHANNEL_CODE = cHANNEL_CODE;
	}

	public String getREF_TRANS_CODE() {
		return REF_TRANS_CODE;
	}

	public void setREF_TRANS_CODE(String rEF_TRANS_CODE) {
		REF_TRANS_CODE = rEF_TRANS_CODE;
	}

	public String getREF_AMT_CODE() {
		return REF_AMT_CODE;
	}

	public void setREF_AMT_CODE(String rEF_AMT_CODE) {
		REF_AMT_CODE = rEF_AMT_CODE;
	}

	public String getPAYMETHOD_CODE() {
		return PAYMETHOD_CODE;
	}

	public void setPAYMETHOD_CODE(String pAYMETHOD_CODE) {
		PAYMETHOD_CODE = pAYMETHOD_CODE;
	}
	
	public String getATTRIBUTE_PARAMETER_TAG1() {
		return ATTRIBUTE_PARAMETER_TAG1;
	}

	public void setATTRIBUTE_PARAMETER_TAG1(String aTTRIBUTE_PARAMETER_TAG1) {
		ATTRIBUTE_PARAMETER_TAG1 = aTTRIBUTE_PARAMETER_TAG1;
	}
	
	public String getATTRIBUTE_PARAMETER_TAG2() {
		return ATTRIBUTE_PARAMETER_TAG2;
	}

	public void setATTRIBUTE_PARAMETER_TAG2(String aTTRIBUTE_PARAMETER_TAG2) {
		ATTRIBUTE_PARAMETER_TAG2 = aTTRIBUTE_PARAMETER_TAG2;
	}
	
	public String getATTRIBUTE_PARAMETER_TAG3() {
		return ATTRIBUTE_PARAMETER_TAG3;
	}

	public void setATTRIBUTE_PARAMETER_TAG3(String aTTRIBUTE_PARAMETER_TAG3) {
		ATTRIBUTE_PARAMETER_TAG3 = aTTRIBUTE_PARAMETER_TAG3;
	}

	public String getATTRIBUTE_PARAMETER_TAG4() {
		return ATTRIBUTE_PARAMETER_TAG4;
	}

	public void setATTRIBUTE_PARAMETER_TAG4(String aTTRIBUTE_PARAMETER_TAG4) {
		ATTRIBUTE_PARAMETER_TAG4 = aTTRIBUTE_PARAMETER_TAG4;
	}

	public String getATTRIBUTE_OVERRIDE_START() {
		return ATTRIBUTE_OVERRIDE_START;
	}

	public void setATTRIBUTE_OVERRIDE_START(String aTTRIBUTE_OVERRIDE_START) {
		ATTRIBUTE_OVERRIDE_START = aTTRIBUTE_OVERRIDE_START;
	}

	public String getATTRIBUTE_OVERRIDE_END() {
		return ATTRIBUTE_OVERRIDE_END;
	}

	public void setATTRIBUTE_OVERRIDE_END(String aTTRIBUTE_OVERRIDE_END) {
		ATTRIBUTE_OVERRIDE_END = aTTRIBUTE_OVERRIDE_END;
	}

	public String getATTRIBUTE_OVERRIDE_PF() {
		return ATTRIBUTE_OVERRIDE_PF;
	}

	public void setATTRIBUTE_OVERRIDE_PF(String aTTRIBUTE_OVERRIDE_PF) {
		ATTRIBUTE_OVERRIDE_PF = aTTRIBUTE_OVERRIDE_PF;
	}
	
	public String getCREDIT_CLASS_CODE() {
		return CREDIT_CLASS_CODE;
	}

	public void setCREDIT_CLASS_CODE(String cREDIT_CLASS_CODE) {
		CREDIT_CLASS_CODE = cREDIT_CLASS_CODE;
	}

	public String getMARKET_SEGMENT() {
		return MARKET_SEGMENT;
	}

	public void setMARKET_SEGMENT(String mARKET_SEGMENT) {
		MARKET_SEGMENT = mARKET_SEGMENT;
	}
	
	public String getATTRIBUTE_TEMP() {
		return ATTRIBUTE_TEMP;
	}
	
	/*public String getAuto_Refill_Threshold() {
		return Auto_Refill_Threshold;
	}

	public String getAuto_Refill_Amount() {
		return Auto_Refill_Amount;
	}*/
	
	public String getCUSTOMER_CODE_DUAL() {
		return CUSTOMER_CODE_DUAL;
	}

	public void setCUSTOMER_CODE_DUAL(String cUSTOMER_CODE_DUAL) {
		CUSTOMER_CODE_DUAL = cUSTOMER_CODE_DUAL;
	}

	public String getACCOUNT_CODE_DUAL() {
		return ACCOUNT_CODE_DUAL;
	}

	public void setACCOUNT_CODE_DUAL(String aCCOUNT_CODE_DUAL) {
		ACCOUNT_CODE_DUAL = aCCOUNT_CODE_DUAL;
	}
	
	public String getDELIVERY_DATE() {
		return DELIVERY_DATE;
	}

	public void setDELIVERY_DATE(String dELIVERY_DATE) {
		DELIVERY_DATE = dELIVERY_DATE;
	}

	public String getENTRY_FEE() {
		return ENTRY_FEE;
	}

	public void setENTRY_FEE(String eNTRY_FEE) {
		ENTRY_FEE = eNTRY_FEE;
	}

	public String getIMPORTO_FEE() {
		return IMPORTO_FEE;
	}

	public void setIMPORTO_FEE(String iMPORTO_FEE) {
		IMPORTO_FEE = iMPORTO_FEE;
	}

	public String getNUMERO() {
		return NUMERO;
	}

	public void setNUMERO(String nUMERO) {
		NUMERO = nUMERO;
	}

	public String getIMPORTO_SHIP() {
		return IMPORTO_SHIP;
	}

	public void setIMPORTO_SHIP(String iMPORTO_SHIP) {
		IMPORTO_SHIP = iMPORTO_SHIP;
	}
	
	public String getATTRIBUTE_TEMP_DEL() {
		return ATTRIBUTE_TEMP_DEL;
	}

	public String getATTRIBUTE_TEMP_UPDATE() {
		return ATTRIBUTE_TEMP_UPDATE;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG1() {
		return ATTRIBUTE_PARAMETER_ACC_TAG1;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG1(String aTTRIBUTE_PARAMETER_ACC_TAG1) {
		ATTRIBUTE_PARAMETER_ACC_TAG1 = aTTRIBUTE_PARAMETER_ACC_TAG1;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG2() {
		return ATTRIBUTE_PARAMETER_ACC_TAG2;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG2(String aTTRIBUTE_PARAMETER_ACC_TAG2) {
		ATTRIBUTE_PARAMETER_ACC_TAG2 = aTTRIBUTE_PARAMETER_ACC_TAG2;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG3() {
		return ATTRIBUTE_PARAMETER_ACC_TAG3;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG3(String aTTRIBUTE_PARAMETER_ACC_TAG3) {
		ATTRIBUTE_PARAMETER_ACC_TAG3 = aTTRIBUTE_PARAMETER_ACC_TAG3;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG4() {
		return ATTRIBUTE_PARAMETER_ACC_TAG4;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG4(String aTTRIBUTE_PARAMETER_ACC_TAG4) {
		ATTRIBUTE_PARAMETER_ACC_TAG4 = aTTRIBUTE_PARAMETER_ACC_TAG4;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG5() {
		return ATTRIBUTE_PARAMETER_ACC_TAG5;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG5(String aTTRIBUTE_PARAMETER_ACC_TAG5) {
		ATTRIBUTE_PARAMETER_ACC_TAG5 = aTTRIBUTE_PARAMETER_ACC_TAG5;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG6() {
		return ATTRIBUTE_PARAMETER_ACC_TAG6;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG6(String aTTRIBUTE_PARAMETER_ACC_TAG6) {
		ATTRIBUTE_PARAMETER_ACC_TAG6 = aTTRIBUTE_PARAMETER_ACC_TAG6;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG7() {
		return ATTRIBUTE_PARAMETER_ACC_TAG7;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG7(String aTTRIBUTE_PARAMETER_ACC_TAG7) {
		ATTRIBUTE_PARAMETER_ACC_TAG7 = aTTRIBUTE_PARAMETER_ACC_TAG7;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG8() {
		return ATTRIBUTE_PARAMETER_ACC_TAG8;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG8(String aTTRIBUTE_PARAMETER_ACC_TAG8) {
		ATTRIBUTE_PARAMETER_ACC_TAG8 = aTTRIBUTE_PARAMETER_ACC_TAG8;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG9() {
		return ATTRIBUTE_PARAMETER_ACC_TAG9;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG9(String aTTRIBUTE_PARAMETER_ACC_TAG9) {
		ATTRIBUTE_PARAMETER_ACC_TAG9 = aTTRIBUTE_PARAMETER_ACC_TAG9;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG10() {
		return ATTRIBUTE_PARAMETER_ACC_TAG10;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG10(
			String aTTRIBUTE_PARAMETER_ACC_TAG10) {
		ATTRIBUTE_PARAMETER_ACC_TAG10 = aTTRIBUTE_PARAMETER_ACC_TAG10;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG11() {
		return ATTRIBUTE_PARAMETER_ACC_TAG11;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG11(
			String aTTRIBUTE_PARAMETER_ACC_TAG11) {
		ATTRIBUTE_PARAMETER_ACC_TAG11 = aTTRIBUTE_PARAMETER_ACC_TAG11;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG12() {
		return ATTRIBUTE_PARAMETER_ACC_TAG12;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG12(
			String aTTRIBUTE_PARAMETER_ACC_TAG12) {
		ATTRIBUTE_PARAMETER_ACC_TAG12 = aTTRIBUTE_PARAMETER_ACC_TAG12;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG13() {
		return ATTRIBUTE_PARAMETER_ACC_TAG13;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG13(
			String aTTRIBUTE_PARAMETER_ACC_TAG13) {
		ATTRIBUTE_PARAMETER_ACC_TAG13 = aTTRIBUTE_PARAMETER_ACC_TAG13;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG14() {
		return ATTRIBUTE_PARAMETER_ACC_TAG14;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG14(
			String aTTRIBUTE_PARAMETER_ACC_TAG14) {
		ATTRIBUTE_PARAMETER_ACC_TAG14 = aTTRIBUTE_PARAMETER_ACC_TAG14;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG15() {
		return ATTRIBUTE_PARAMETER_ACC_TAG15;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG15(
			String aTTRIBUTE_PARAMETER_ACC_TAG15) {
		ATTRIBUTE_PARAMETER_ACC_TAG15 = aTTRIBUTE_PARAMETER_ACC_TAG15;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG16() {
		return ATTRIBUTE_PARAMETER_ACC_TAG16;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG16(
			String aTTRIBUTE_PARAMETER_ACC_TAG16) {
		ATTRIBUTE_PARAMETER_ACC_TAG16 = aTTRIBUTE_PARAMETER_ACC_TAG16;
	}
	
	

	public String getATTRIBUTE_PARAMETER_ACC_TAG17() {
		return ATTRIBUTE_PARAMETER_ACC_TAG17;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG17(
			String aTTRIBUTE_PARAMETER_ACC_TAG17) {
		ATTRIBUTE_PARAMETER_ACC_TAG17 = aTTRIBUTE_PARAMETER_ACC_TAG17;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG18() {
		return ATTRIBUTE_PARAMETER_ACC_TAG18;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG18(
			String aTTRIBUTE_PARAMETER_ACC_TAG18) {
		ATTRIBUTE_PARAMETER_ACC_TAG18 = aTTRIBUTE_PARAMETER_ACC_TAG18;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG19() {
		return ATTRIBUTE_PARAMETER_ACC_TAG19;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG19(
			String aTTRIBUTE_PARAMETER_ACC_TAG19) {
		ATTRIBUTE_PARAMETER_ACC_TAG19 = aTTRIBUTE_PARAMETER_ACC_TAG19;
	}

	public String getATTRIBUTE_PARAMETER_ACC_TAG20() {
		return ATTRIBUTE_PARAMETER_ACC_TAG20;
	}

	public void setATTRIBUTE_PARAMETER_ACC_TAG20(
			String aTTRIBUTE_PARAMETER_ACC_TAG20) {
		ATTRIBUTE_PARAMETER_ACC_TAG20 = aTTRIBUTE_PARAMETER_ACC_TAG20;
	}

	public String getHANDSET_OLD() {
		return HANDSET_OLD;
	}

	public String getHANDSET_NEW() {
		return HANDSET_NEW;
	}

	public String getROOT_INST_CODE_HS() {
		return ROOT_INST_CODE_HS;
	}

	public void setROOT_INST_CODE_HS(String rOOT_INST_CODE_HS) {
		ROOT_INST_CODE_HS = rOOT_INST_CODE_HS;
	}

	public String getADJ_AMT() {
		return ADJ_AMT;
	}

	public void setADJ_AMT(String aDJ_AMT) {
		ADJ_AMT = aDJ_AMT;
	}

	public String getCURR_CODE() {
		return CURR_CODE;
	}

	public void setCURR_CODE(String cURR_CODE) {
		CURR_CODE = cURR_CODE;
	}

	public String getADJ_TYPE() {
		return ADJ_TYPE;
	}

	public void setADJ_TYPE(String aDJ_TYPE) {
		ADJ_TYPE = aDJ_TYPE;
	}

	public String getREA_REQ() {
		return REA_REQ;
	}

	public void setREA_REQ(String rEA_REQ) {
		REA_REQ = rEA_REQ;
	}

	public String getDAYS_EXP() {
		return DAYS_EXP;
	}

	public void setDAYS_EXP(String dAYS_EXP) {
		DAYS_EXP = dAYS_EXP;
	}

	public String getADJ_ID() {
		return ADJ_ID;
	}

	public void setADJ_ID(String aDJ_ID) {
		ADJ_ID = aDJ_ID;
	}

	public String getADJ_ACCT() {
		return ADJ_ACCT;
	}

	public void setADJ_ACCT(String aDJ_ACCT) {
		ADJ_ACCT = aDJ_ACCT;
	}

	public MasterData(String accountType, AcsLabels al) {
		
		getAutoNumber();
		this.PROP_TEMPLATE_ID= (String) al.hm.get(accountType); 
		this.CUSTOMER_CODE= AUTO_NUMBER + "C_" + PROP_TEMPLATE_ID;
		//added for m2m
		if(Integer.parseInt(accountType) == 14){
			this.ACCOUNT_CODE=	"SUB_"+AUTO_NUMBER + "R_" + PROP_TEMPLATE_ID;
		}else{
		this.ACCOUNT_CODE=	AUTO_NUMBER + "A_" + PROP_TEMPLATE_ID; 
		}
		
		this.ROOT_INST_CODE=AUTO_NUMBER + "R_" + PROP_TEMPLATE_ID; 
		this.PROD_INST_CODE=AUTO_NUMBER + "R_01" ;
		this.OPTION_CATALOGUE_CODE=AUTO_NUMBER + "_TR_" + PROP_TEMPLATE_ID;
		this.CUSTOMER_CODE_DUAL= AUTO_NUMBER + "C_DUAL";
		this.ACCOUNT_CODE_DUAL= AUTO_NUMBER + "A_DUAL";
		/*this.OPTION_TARIFF_CODE="" ;*/
		
		
		this.PVT_CODE="" ;
		this.MSISDN_CODE="" ;
		this.BASE_TARIFF_CODE="" ;
		
		this.IMSI_CODE="" ;
		this.CHANNEL_CODE="" ;
		this.REF_TRANS_CODE="" ;
		this.REF_AMT_CODE="" ;
		this.PAYMETHOD_CODE="" ;

	}
	
	private synchronized void getAutoNumber() {
		// TODO Auto-generated method stub
		//autoNumber = System.nanoTime();
		AUTO_NUMBER = System.currentTimeMillis(); 
		//System.out.println("MARK 1"+ System.currentTimeMillis());
		//System.out.println("MARK 2"+ System.nanoTime());		
		AUTO_EPOCHTIME =  (System.currentTimeMillis() / 1000L );
	}

	public String getCodice_Migrazione_GNP() {
		return Codice_Migrazione_GNP;
	}

	public void setCodice_Migrazione_GNP(String codice_Migrazione_GNP) {
		Codice_Migrazione_GNP = codice_Migrazione_GNP;
	}

	public String getData_Attivazione_GNP() {
		return Data_Attivazione_GNP;
	}

	public void setData_Attivazione_GNP(String data_Attivazione_GNP) {
		Data_Attivazione_GNP = data_Attivazione_GNP;
	}

	public String getData_Attivazione_Linea() {
		return Data_Attivazione_Linea;
	}

	public void setData_Attivazione_Linea(String data_Attivazione_Linea) {
		Data_Attivazione_Linea = data_Attivazione_Linea;
	}

	public String getNumero_Portato() {
		return Numero_Portato;
	}

	public void setNumero_Portato(String numero_Portato) {
		Numero_Portato = numero_Portato;
	}

	public String getNumero_Temporaneo() {
		return Numero_Temporaneo;
	}

	public void setNumero_Temporaneo(String numero_Temporaneo) {
		Numero_Temporaneo = numero_Temporaneo;
	}

	public String getTipologia_Terminale() {
		return Tipologia_Terminale;
	}

	public void setTipologia_Terminale(String tipologia_Terminale) {
		Tipologia_Terminale = tipologia_Terminale;
	}

	public String getData_Attivazione_Canone() {
		return Data_Attivazione_Canone;
	}

	public void setData_Attivazione_Canone(String data_Attivazione_Canone) {
		Data_Attivazione_Canone = data_Attivazione_Canone;
	}

	public String getData_disattivazione_Canone() {
		return Data_disattivazione_Canone;
	}

	public void setData_disattivazione_Canone(String data_disattivazione_Canone) {
		Data_disattivazione_Canone = data_disattivazione_Canone;
	}
    
}
