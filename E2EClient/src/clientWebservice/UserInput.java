package clientWebservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class UserInput {

	public String accountType; 
	private String actionType;
	private String updateType;
	private String envType;
	private String refCache;
	public static int handset_flag = 1;
	
	private  Scanner in = new Scanner(System.in);
	
	public UserInput() {
		super();
		this.accountType = "";
		this.actionType = "";
		this.updateType = "";
		this.envType = "";
		this.refCache = "";
	}



	public void setEnvType() {
		
		String msg = "Please select the environment you want to connect to.......";
		System.out.println(msg);
		
		 System.out.println("1. Dev");
		 System.out.println("2. IT");
		 System.out.println("3. ATBill");
		 System.out.println("4. IODIO");
		 System.out.println("5. ASTATO");
		
		 envType = in.nextLine();
		 while(!isInteger(envType) || Integer.parseInt(envType) <= 0 || Integer.parseInt(envType) > 5){
		        System.out.println("Please enter a valid number");
		        envType = in.nextLine();
		 }		
		 
	}
	
	/* Refresh Cache*/
	public void setRefCache() {
		
		 System.out.println("Do you want to perform Refresh Cache(Y/N)..Just Press ENTER for No: ");
		 refCache = in.nextLine();
		 while(!(refCache.equalsIgnoreCase("y") || refCache.equalsIgnoreCase("n") || refCache.equalsIgnoreCase("") )){
		        System.out.println("Please enter a valid value");
		        refCache = in.nextLine();
		  }
		 if(refCache.equals("")){
			 refCache = "N";
		 }

	}
	
	public void setAccountCreateType() {
		String msg = "Please select the type of request.......";
		System.out.println(msg);
		
		 System.out.println("1. Prepaid	Consumer");
		 System.out.println("2. Prepaid	Business");
		 System.out.println("3. Prepaid SOHOLP");
		 System.out.println("4. Postpaid Consumer");
		 System.out.println("5. Postpaid Business");
		 System.out.println("6. Postpaid SOHOLP");
		 System.out.println("7. Postpaid Handset (Ondemand)");
		 System.out.println("8. Adjustment");
		 System.out.println("13. Postpaid Consumer SinglePlay");  //added
		 //added for M2M
		 System.out.println("14. Postpaid Consumer M2M");
		 
		 accountType = in.nextLine();
		 //added   //added for M2M
		 while(!isInteger(accountType) || Integer.parseInt(accountType) <= 0 || (Integer.parseInt(accountType) > 9 && Integer.parseInt(accountType) != 13 && Integer.parseInt(accountType) != 14) ){
		        System.out.println("Please enter a valid number");
		        accountType = in.nextLine();
		 }		
		 
		 System.out.println("********** Process Started********** ");
		 System.out.println("");
	}

	public void setActionType() {
		
		if(accountType.equals("8") ){
			
			actionType = "0";
		}else{	 
		 String msg = "Please select the type of request.......";
		 
		 System.out.println(msg);
		 System.out.println("1. New");
		 System.out.println("2. Upgrade");
		 System.out.println("3. Option Delete");
		 System.out.println("4. Option Update");
		 		 
		 actionType = in.nextLine();
		 while(!isInteger(actionType) || Integer.parseInt(actionType) <= 0 || Integer.parseInt(actionType) > 4){
		        System.out.println("Please enter a valid number");
		        actionType = in.nextLine();
		    }
		 
		 if(actionType.equals("2") ){
			 System.out.println("Please select from where to start ..............");
			 System.out.println("1. Account create");
			 System.out.println("2. Base plan create");
			 System.out.println("3. Refill");
			 System.out.println("4. Option Create");
			 
			 updateType = in.nextLine();
			 while(!isInteger(updateType) || Integer.parseInt(updateType) <= 0 || Integer.parseInt(updateType) > 4){
			        System.out.println("Please enter a valid number");
			        updateType = in.nextLine();
			 }
		 }
	  }
	}
	
	
	
	public String getAccountCreateType() {
		return accountType;
	}
	
	public String getActionType() {
		return actionType;
	}

	public String getUpdateType() {
		return updateType;
	}
	
	public String getEnvType() {
		return envType;
	}

	public String getRefCache() {
		return refCache;
	}

	public static boolean isInteger(String s){
	
	    if(s.equals("") )return false;
	    for (int i = 0; i <s.length();++i){
	        char c = s.charAt(i);
	        if(!Character.isDigit(c) && c !='-')
	            return false;
	    }
	
	    return true;
	}

	public String getMsisdnNo(String autoMsisdn , DBConnection db) throws IOException, DBException {
		String inpVal;
		String retVal="";
		
		
		System.out.println("Please Enter MSISDN ... just enter for auto create ..............");
		inpVal = in.nextLine();
		if(inpVal.equals("")){
			inpVal = autoMsisdn;
			
			retVal = db.queryValidMsisdn(inpVal);
			if(retVal.equals("")){
				// HERE I AM GOING TO TRY ANOTHER 12 DATA 
				Long autoMsisdnIncr = Long.parseLong(autoMsisdn)+13;
				inpVal = autoMsisdnIncr.toString();
				retVal = db.queryValidMsisdn(inpVal);
			}
		}
		else{
			
			 while(!isInteger(inpVal) || inpVal.length() != 12 ){
			        System.out.println("Please enter a valid 12 digit number .." );
			        inpVal = in.nextLine();
			 }
			 
			retVal = db.queryValidMsisdn(inpVal);
			
			if(!inpVal.equals(retVal)){
				if(retVal.equals("")){
					System.out.println("Sorry the MSISDN u provided seems to exist ! so cant do much !! me exiting");
					System.exit(0);
				}
				else{
					System.out.println("Sorry the MSISDN u provided seems to exist ! so I will create using " + retVal);
				}
			}
		}
		return retVal;
	}


	public String getTipoMVNO() {
		
		 String msg = "PLEASE ENTER THE SIM TYPE(ESP/FULL/Fisso) .......";
		 String inpVal;
		 System.out.println(msg);
		 		 
		 inpVal = in.nextLine();
		 //added
		 while(!(inpVal.equalsIgnoreCase("ESP") || inpVal.equalsIgnoreCase("FULL") || inpVal.equalsIgnoreCase("Fisso") || inpVal.equalsIgnoreCase("") )){
		        System.out.println("Please enter a valid value");
		        inpVal = in.nextLine();
		    }
		
		return inpVal;
	}
	
	public String getBaseTariffCode() {
		// TODO Auto-generated method stub
		 String msg = "PLEASE ENTER TARIFF ID FOR BASE PLAN .......";
		 String inpVal;
		 System.out.println(msg);
		 		 
		 inpVal = in.nextLine();
		 while(!isInteger(inpVal)){
		        System.out.println("Please enter a valid number");
		        inpVal = in.nextLine();
		    }
		
		return inpVal;
	}
	
	//added
	public String getNumeroPortato() {
		// TODO Auto-generated method stub
		 String msg = "PLEASE ENTER Numero Portato .......";
		 String inpVal;
		 System.out.println(msg);
		 		 
		 inpVal = in.nextLine();
		 while(!isInteger(inpVal)){
		        System.out.println("Please enter a valid number");
		        inpVal = in.nextLine();
		    }
		
		return inpVal;
	}
	
	public String getNumeroTemporaneo() {
		// TODO Auto-generated method stub
		 String msg = "PLEASE ENTER Numero Temporaneo .......";
		 String inpVal;
		 System.out.println(msg);
		 		 
		 inpVal = in.nextLine();
		 while(!isInteger(inpVal)){
		        System.out.println("Please enter a valid number");
		        inpVal = in.nextLine();
		    }
		
		return inpVal;
	}
	
	public String getCodiceMigrazioneGNP() {
		// TODO Auto-generated method stub
		 String msg = "PLEASE ENTER Codice Migrazione GNP .......";
		 String inpVal;
		 System.out.println(msg);
		 		 
		 inpVal = in.nextLine();
		 while(inpVal.equalsIgnoreCase(null) || inpVal.equalsIgnoreCase("")){
		        System.out.println("Please enter  valid Codice Migrazione GNP");
		        inpVal = in.nextLine();
		    }
		
		return inpVal;
	}
	
	public String getDataAttivazioneLinea() {
		// TODO Auto-generated method stub
		 String msg = "PLEASE ENTER Data Attivazione Linea .......";
		 String inpVal;
		 System.out.println(msg);
		 		 
		 inpVal = in.nextLine();
		  while(inpVal.equalsIgnoreCase(null) || inpVal.equalsIgnoreCase("")){
	        System.out.println("Please enter  valid Data Attivazione Linea");
	        inpVal = in.nextLine();
	    }
		
		return inpVal;
	}
	
	public String getDataAttivazioneGNP(){
		// TODO Auto-generated method stub
		 String msg = "PLEASE ENTER Data Attivazione GNP .......";
		 String inpVal;
		 System.out.println(msg);
		 
		 inpVal = in.nextLine();
		 while(inpVal.equalsIgnoreCase(null) || inpVal.equalsIgnoreCase("")){
		        System.out.println("Please enter a valid Date");
		        inpVal = in.nextLine();
		    }
		
		return inpVal;
	}
	
	public String getTipologiaTerminale() {
		// TODO Auto-generated method stub
		 String msg = "PLEASE ENTER Tipologia Terminale .......";
		 String inpVal;
		 System.out.println(msg);
		 		 
		 inpVal = in.nextLine();
		 while(inpVal.equalsIgnoreCase(null) || inpVal.equalsIgnoreCase("")){
		        System.out.println("Please enter a valid Data");
		        inpVal = in.nextLine();
		    }
		
		return inpVal;
	}
	
	//added for 6080
	public String getDataAttivazioneCanone(){
		String msg = "PLEASE ENTER Data Activation canone .......";
		 String inpVal;
		 System.out.println(msg);
		 		 
		 inpVal = in.nextLine();
		 while(inpVal.equalsIgnoreCase(null) || inpVal.equalsIgnoreCase("")){
		        System.out.println("Please enter a valid Data");
		        inpVal = in.nextLine();
		    }
		
		return inpVal;
	}
	
	public String getDataDisattivazioneCanone(){
		String msg = "PLEASE ENTER Data Disattivazione canone .......";
		 String inpVal;
		 System.out.println(msg);
		 		 
		 inpVal = in.nextLine();
		while(inpVal.equalsIgnoreCase(null) || inpVal.equalsIgnoreCase("")){
		        System.out.println("Please enter a valid Data");
		        inpVal = in.nextLine();
		    }
		
		return inpVal;
	}
	
	public void get_user_input_refil(String target, String FileName , FileManager fil, MasterData md) {
		// TODO Auto-generated method stub
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =target +"/"+FileName;
		tmp_target_file = target +"/tmp_"+FileName;

		 System.out.println("PLEASE ENTER REFIL AMT .......");
		 md.setREF_AMT_CODE(in.nextLine());
		 
		 System.out.println("PLEASE ENTER REFIL CHANNEL (like BATCH / ATM_PI / ATM_BI ) .......");
		 md.setCHANNEL_CODE(in.nextLine());
		 		 
		 System.out.println("PLEASE ENTER REFIL PAYMENT METHOD (like 912 968 ) .......");
		 md.setPAYMETHOD_CODE(in.nextLine());

	//	 md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
	//	 System.out.println(">>>> in user inut"+md.getREF_TRANS_CODE());
		 
		 fil.replace(target_file,  tmp_target_file,"SW_REFAMT_XXXX", md.getREF_AMT_CODE());
		 fil.replace(target_file,  tmp_target_file,"SW_CHANNEL_XXXX", md.getCHANNEL_CODE());
		 fil.replace(target_file,  tmp_target_file,"SW_PAYMETHOD_XXXX", md.getPAYMETHOD_CODE());
		 fil.replace(target_file,  tmp_target_file,"SW_TRANSCODE_XXXX", md.getREF_TRANS_CODE());
		 fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		 fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
	}
	
    public void get_user_input_option(String target, String FileName , FileManager fil, MasterData md, DBConnection db) throws DBException {
		
		String getInp;
		int flg = 0;
		String inpValue;
		String overrideStr;
		String delimiter = "\\|";
		String[] arrEra;
		ArrayList<String> overrideData;
		ArrayList<InputPojoAttributes> loadedAttriData = null;
		ArrayList<InputPojoAttributes> loadedAttriData_accountlevel = null;
		boolean check_account_level_deal = false ;

		HashMap<Integer,InputPojoAttributes>  mAttriMap1 = new HashMap<Integer,InputPojoAttributes>();
		HashMap<Integer,InputPojoAttributes>  mAttriMap2 = new HashMap<Integer,InputPojoAttributes>();
		HashMap<Integer,InputPojoAttributes>  mAttriMap3 = new HashMap<Integer,InputPojoAttributes>();
		HashMap<Integer,InputPojoAttributes>  mAttriMap4 = new HashMap<Integer,InputPojoAttributes>();
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =target +"/"+FileName;
		tmp_target_file = target +"/tmp_"+FileName;

		System.out.println("PLEASE ENTER OPTION CATALOGUE ID ( like 4235 ) .......");
		md.setOPTION_CATALOGUE_CODE(in.nextLine());
		
		if(md.getOPTION_CATALOGUE_CODE().equalsIgnoreCase("6080"))
		{
			md.setData_Attivazione_Canone(getDataAttivazioneCanone());
				
		} 
		
		/* System.out.println("PLEASE ENTER OPTION TARIFF ID ( like 24 ) .......");
		 md.setOPTION_TARIFF_CODE(in.nextLine());*/
		 		 
		 //System.out.println("PLEASE ENTER REFIL PAYMENT METHOD (like 912 968 ) .......");
		 //lab1.PAYMETHOD_CODE = in.nextLine();
		
//		added for M2M
		if(Integer.parseInt(accountType) == 14){
			loadedAttriData = db.getAttributeData1(md.getOPTION_CATALOGUE_CODE(), md.getBASE_TARIFF_CODE());
			System.out.println("jjjjjjjjjjjjjjjM2M");
		}else
		

		loadedAttriData = db.getAttributeData(md.getOPTION_CATALOGUE_CODE(), md.getBASE_TARIFF_CODE());
		
		
		 
		Iterator<InputPojoAttributes> itr1 = loadedAttriData.iterator();
		InputPojoAttributes ip1 = new InputPojoAttributes();
		int flg_att1=0;
		int flg_att2=0;
		int flg_att3=0;
		int flg_att4=0;

		while(itr1.hasNext()){
			ip1 = (InputPojoAttributes) itr1.next();
			//System.out.println ("loop hash: " + ip1.getname() + "----" + ip1.getvalue() + "----" + ip1.getcolum() );
			if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG1") ){
				mAttriMap1.put(flg_att1++, ip1);
			}
			else if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG2") ){
				mAttriMap2.put(flg_att2++, ip1);
			}
			else if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG3") ){
				mAttriMap3.put(flg_att3++, ip1);
			} 
			else if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG4") ){
				mAttriMap4.put(flg_att4++, ip1);
			}
			else if(ip1.getcolum().equals("SBL_ERA_TAG") ){
				//PIPEEEEE SYM .. ip frm user
				
				arrEra = ip1.getname().split(delimiter);
				for(int i =0; i < arrEra.length ; i++){
				    System.out.println("Please enter the value for ERA name " +  arrEra[i]);
					inpValue = in.nextLine();
				  
					md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", arrEra[i]));
					md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_PARAMETER_TAG1().replace("SW_ATT_VAL", inpValue));
					
					fil.replace(target_file,  tmp_target_file,"SW_ERA_TAG_ATTR_XXXX", md.getATTRIBUTE_PARAMETER_TAG1()+"SW_ERA_TAG_ATTR_XXXX");
					
				}
				fil.replace(target_file,  tmp_target_file,"SW_ERA_TAG_ATTR_XXXX", "");
			} 			
			
		}		
		if(mAttriMap1.size() > 0){
			if(mAttriMap1.size() > 1){
				System.out.println("Choose the option SBL_BILL_PARAMETER_TAG1: " + mAttriMap1.size());
				for (int val=0; val < mAttriMap1.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[val];
					System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
				getInp = in.nextLine();
				while(!isInteger(getInp)){
			        System.out.println("Please enter a valid number");
			        getInp = in.nextLine();
			    }				
			}else{
				getInp = "0";
				ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[0];
				if(ip1.getvalue().equals("NOVALUE")){
					System.out.println("Please enter the value for " +ip1.getname() +" :" );
					ip1.setvalue(in.nextLine());
				}
			}
			
			ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_PARAMETER_TAG1().replace("SW_ATT_VAL", ip1.getvalue()));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 1;
			}
			
		}
		if(mAttriMap2.size() > 0){
			if(mAttriMap2.size() > 1){
				System.out.println("Choose the option SBL_BILL_PARAMETER_TAG2: " + mAttriMap2.size());
				for (int val=0; val < mAttriMap2.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap2.values().toArray()[val];
					System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
				getInp = in.nextLine();
				/*getInp = Integer.parseInt(in.nextLine());*/
				while(!isInteger(getInp)){
			        System.out.println("Please enter a valid number");
			        getInp = in.nextLine();
			    }
			}else{
				getInp = "0";
				ip1 =  (InputPojoAttributes) mAttriMap2.values().toArray()[0];
				if(ip1.getvalue().equals("NOVALUE")){
					System.out.println("Please enter the value for " +ip1.getname() +" :" );
					ip1.setvalue(in.nextLine());
				}
			}
			
			ip1 =  (InputPojoAttributes) mAttriMap2.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG2(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG2(md.getATTRIBUTE_PARAMETER_TAG2().replace("SW_ATT_VAL", ip1.getvalue()));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 2;
			}
		}
		if(mAttriMap3.size() > 0){
			if(mAttriMap3.size() > 1){
				System.out.println("Choose the option SBL_BILL_PARAMETER_TAG3: " + mAttriMap3.size());
				for (int val=0; val < mAttriMap3.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap3.values().toArray()[val];
					System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
				getInp = in.nextLine();
				/*getInp = Integer.parseInt(in.nextLine());*/
				while(!isInteger(getInp)){
			        System.out.println("Please enter a valid number");
			        getInp = in.nextLine();
			    }
			}else{
				getInp = "0";
				ip1 =  (InputPojoAttributes) mAttriMap3.values().toArray()[0];
				if(ip1.getvalue().equals("NOVALUE")){
					System.out.println("Please enter the value for " +ip1.getname() +" :" );
					ip1.setvalue(in.nextLine());
				}
			}
			
			ip1 =  (InputPojoAttributes) mAttriMap3.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG3(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG3(md.getATTRIBUTE_PARAMETER_TAG3().replace("SW_ATT_VAL", ip1.getvalue()));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 3;
			}			
		}
		if(mAttriMap4.size() > 0){
			if(mAttriMap4.size() > 1){
				System.out.println("Choose the option SBL_BILL_PARAMETER_TAG4: " + mAttriMap4.size());
				for (int val=0; val < mAttriMap4.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap4.values().toArray()[val];
					System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
				getInp = in.nextLine();
				/*getInp = Integer.parseInt(in.nextLine());*/
				while(!isInteger(getInp)){
			        System.out.println("Please enter a valid number");
			        getInp = in.nextLine();
			    }
			}else{
				getInp = "0";
				ip1 =  (InputPojoAttributes) mAttriMap4.values().toArray()[0];
				if(ip1.getvalue().equals("NOVALUE")){
					System.out.println("Please enter the value for " +ip1.getname() +" :" );
					ip1.setvalue(in.nextLine());
				}
			}
			
			ip1 =  (InputPojoAttributes) mAttriMap4.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG4(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG4(md.getATTRIBUTE_PARAMETER_TAG4().replace("SW_ATT_VAL", ip1.getvalue()));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 4;
			}			
		}	
		
	
		 
		if(flg > 0){
		    System.out.println("Please enter the PricingFlexibilityFlag OverrideStartDate value in (MM/DD/YYYY HH:MM:SS) 07/21/2013 04:17:39" );
			inpValue = in.nextLine();
			md.setATTRIBUTE_OVERRIDE_START(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", "OverrideStartDate"));  
			md.setATTRIBUTE_OVERRIDE_START(md.getATTRIBUTE_OVERRIDE_START().replace("SW_ATT_VAL", inpValue));
			
			
			System.out.println("Please enter the PricingFlexibilityFlag OverrideEndDate value in (MM/DD/YYYY HH:MM:SS) 07/21/2013 04:17:39" );
			inpValue = in.nextLine();
			md.setATTRIBUTE_OVERRIDE_END(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", "OverrideEndDate"));  
			md.setATTRIBUTE_OVERRIDE_END(md.getATTRIBUTE_OVERRIDE_END().replace("SW_ATT_VAL", inpValue));

			overrideData = db.getOverrideData(md.getOPTION_CATALOGUE_CODE(), md.getBASE_TARIFF_CODE());
			//System.out.println("TESTTTING" + overrideData.size());
			for (int val = 0; val < overrideData.size(); val++){
				overrideStr = (String) overrideData.get(val);
				System.out.println("Enter the value for override Data " + overrideStr);
				inpValue = in.nextLine();
				
				md.setATTRIBUTE_OVERRIDE_PF(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", overrideStr));  
				md.setATTRIBUTE_OVERRIDE_PF(md.getATTRIBUTE_OVERRIDE_PF().replace("SW_ATT_VAL", inpValue));	
				
			}
		}
		
		/////update on handset
		
		
		//added for M2M
		//System.out.println("((((((((((((((((((((((((");
		if(Integer.parseInt(accountType) == 14){
			check_account_level_deal = db.check_account_level_deal1(md.getOPTION_CATALOGUE_CODE());
			System.out.println("vvvvvvvvvvvvvvvvvvM2M");
			
		}else
		
		
		check_account_level_deal = db.check_account_level_deal(md.getOPTION_CATALOGUE_CODE());
		
		if(check_account_level_deal == true){
			//System.out.println("HERE root inst id" +md.getROOT_INST_CODE());
			
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc1 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc2 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc3 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc4 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc5 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc6 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc7 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc8 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc9 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc10 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc11 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc12 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc13 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc14 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc15 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc16 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc17 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc18 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc19 = new HashMap<Integer,InputPojoAttributes>();
			HashMap<Integer,InputPojoAttributes>  mAttriMap_acc20 = new HashMap<Integer,InputPojoAttributes>();
			
			loadedAttriData_accountlevel = db.getAttributeDataForAccountLevel(md.getOPTION_CATALOGUE_CODE(), md.getBASE_TARIFF_CODE());
			 
			Iterator<InputPojoAttributes> itr2 = loadedAttriData_accountlevel.iterator();
			InputPojoAttributes ip2 = new InputPojoAttributes();
			int flg_att_acc1=0;
			int flg_att_acc2=0;
			int flg_att_acc3=0;
			int flg_att_acc4=0;
			int flg_att_acc5=0;
			int flg_att_acc6=0;
			int flg_att_acc7=0;
			int flg_att_acc8=0;
			int flg_att_acc9=0;
			int flg_att_acc10=0;
			int flg_att_acc11=0;
			int flg_att_acc12=0;
			int flg_att_acc13=0;
			int flg_att_acc14=0;
			int flg_att_acc15=0;
			int flg_att_acc16=0;
			int flg_att_acc17=0;
			int flg_att_acc18=0;
			int flg_att_acc19=0;
			int flg_att_acc20=0;

			while(itr2.hasNext()){
				ip2 = (InputPojoAttributes) itr2.next();
				//System.out.println ("loop hash: " + ip1.getname() + "----" + ip1.getvalue() + "----" + ip1.getcolum() );
				if(ip2.getcolum().equals("EXT_PARAM_TAG_1") ){
					mAttriMap_acc1.put(flg_att_acc1++, ip2);
				}
				else if(ip2.getcolum().equals("OVERRIDE_PARAMETER_TAG_1") ){
					mAttriMap_acc2.put(flg_att_acc2++, ip2);
				}
				else if(ip2.getcolum().equals("OVERRIDE_PARAMETER_TAG_2") ){
					mAttriMap_acc3.put(flg_att_acc3++, ip2);
				} 
				else if(ip2.getcolum().equals("OVERRIDE_PARAMETER_TAG_3") ){
					mAttriMap_acc4.put(flg_att_acc4++, ip2);
				}
				else if(ip2.getcolum().equals("OVERRIDE_PARAMETER_TAG_4") ){
					mAttriMap_acc5.put(flg_att_acc5++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_2") ){
					mAttriMap_acc6.put(flg_att_acc6++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_3") ){
					mAttriMap_acc7.put(flg_att_acc7++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_4") ){
					mAttriMap_acc8.put(flg_att_acc8++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_5") ){
					mAttriMap_acc9.put(flg_att_acc9++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_6") ){
					mAttriMap_acc10.put(flg_att_acc10++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_7") ){
					mAttriMap_acc11.put(flg_att_acc11++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_8") ){
					mAttriMap_acc12.put(flg_att_acc12++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_9") ){
					mAttriMap_acc13.put(flg_att_acc13++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_10") ){
					mAttriMap_acc14.put(flg_att_acc14++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_11") ){
					mAttriMap_acc15.put(flg_att_acc15++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_12") ){
					mAttriMap_acc16.put(flg_att_acc16++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_13") ){
					mAttriMap_acc17.put(flg_att_acc17++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_14") ){
					mAttriMap_acc18.put(flg_att_acc18++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_15") ){
					mAttriMap_acc19.put(flg_att_acc19++, ip2);
				}
				else if(ip2.getcolum().equals("EXT_PARAM_TAG_16") ){
					mAttriMap_acc20.put(flg_att_acc20++, ip2);
				}	
				
			}		
			if(mAttriMap_acc1.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_1: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc1.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG1(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG1(md.getATTRIBUTE_PARAMETER_ACC_TAG1().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc2.size() > 0){
				System.out.println("Enter the value of OVERRIDE_PARAMETER_TAG_1: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc2.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG2(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG2(md.getATTRIBUTE_PARAMETER_ACC_TAG2().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc3.size() > 0){
				System.out.println("Enter the value of OVERRIDE_PARAMETER_TAG_2: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc3.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG3(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG3(md.getATTRIBUTE_PARAMETER_ACC_TAG3().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc4.size() > 0){
				System.out.println("Enter the value of OVERRIDE_PARAMETER_TAG_3: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc4.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG4(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG4(md.getATTRIBUTE_PARAMETER_ACC_TAG4().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc5.size() > 0){
				System.out.println("Enter the value of OVERRIDE_PARAMETER_TAG_4: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc5.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG5(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG5(md.getATTRIBUTE_PARAMETER_ACC_TAG5().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc6.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_2: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc6.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG6(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG6(md.getATTRIBUTE_PARAMETER_ACC_TAG6().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc7.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_3: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc7.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG7(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG7(md.getATTRIBUTE_PARAMETER_ACC_TAG7().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc8.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_4: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc8.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG8(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG8(md.getATTRIBUTE_PARAMETER_ACC_TAG8().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc9.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_5: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc9.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG9(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG9(md.getATTRIBUTE_PARAMETER_ACC_TAG9().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc10.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_6: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc10.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG10(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG10(md.getATTRIBUTE_PARAMETER_ACC_TAG10().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc11.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_7: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc11.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG11(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG11(md.getATTRIBUTE_PARAMETER_ACC_TAG11().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc12.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_8: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc12.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG12(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG12(md.getATTRIBUTE_PARAMETER_ACC_TAG12().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc13.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_9: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc13.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG13(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG13(md.getATTRIBUTE_PARAMETER_ACC_TAG13().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc14.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_10: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc14.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG14(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG14(md.getATTRIBUTE_PARAMETER_ACC_TAG14().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc15.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_11: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc15.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG15(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG15(md.getATTRIBUTE_PARAMETER_ACC_TAG15().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc16.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_12: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc16.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG16(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG16(md.getATTRIBUTE_PARAMETER_ACC_TAG16().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc17.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_13: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc17.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG17(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG17(md.getATTRIBUTE_PARAMETER_ACC_TAG17().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc18.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_14: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc18.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG18(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG18(md.getATTRIBUTE_PARAMETER_ACC_TAG18().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc19.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_15: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc19.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG19(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG19(md.getATTRIBUTE_PARAMETER_ACC_TAG19().replace("SW_ATT_VAL", getInp));
			}
			if(mAttriMap_acc20.size() > 0){
				System.out.println("Enter the value of EXT_PARAM_TAG_16: " +ip2.getname());
				getInp = in.nextLine();
				ip2 =  (InputPojoAttributes) mAttriMap_acc20.values().toArray()[0];
				md.setATTRIBUTE_PARAMETER_ACC_TAG20(md.getATTRIBUTE_TEMP().replace("SW_ATT_NAME", ip2.getname()));  
				md.setATTRIBUTE_PARAMETER_ACC_TAG20(md.getATTRIBUTE_PARAMETER_ACC_TAG20().replace("SW_ATT_VAL", getInp));
			}
		
			md.setROOT_INST_CODE_HS(md.getROOT_INST_CODE()+ "_HS" +handset_flag);
			md.setPROD_INST_CODE(md.getROOT_INST_CODE()+ "_HS" +handset_flag);
			++handset_flag;
			fil.replace(target_file,  tmp_target_file,md.getHANDSET_OLD(), md.getHANDSET_NEW());
			fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE_HS());
		}else{
			//System.out.println("In else root inst id" +md.getROOT_INST_CODE());
			fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE());
			md.setPROD_INST_CODE(md.getPROD_INST_CODE() + md.getPROP_TEMPLATE_ID()); 
		}
		
		//System.out.println("Outside root inst id" +md.getROOT_INST_CODE());
		/*md.setPROD_INST_CODE(md.getPROD_INST_CODE() + md.getPROP_TEMPLATE_ID()); */
		
		fil.replace(target_file,  tmp_target_file,"SW_ERA_TAG_ATTR_XXXX", "");
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR1_XXXX", md.getATTRIBUTE_PARAMETER_TAG1());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR2_XXXX", md.getATTRIBUTE_PARAMETER_TAG2());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR3_XXXX", md.getATTRIBUTE_PARAMETER_TAG3());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR4_XXXX", md.getATTRIBUTE_PARAMETER_TAG4());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERSTART_XXXX", md.getATTRIBUTE_OVERRIDE_START());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVEREND_XXXX", md.getATTRIBUTE_OVERRIDE_END());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERPF_XXXX", md.getATTRIBUTE_OVERRIDE_PF());
		fil.replace(target_file,  tmp_target_file,"SW_PRODINST_AUTO_XXXX", md.getPROD_INST_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_OPTIONCATALOG_ID_XXX", md.getOPTION_CATALOGUE_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_BASE_TARIFF_XXXX", md.getBASE_TARIFF_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		//fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE());		
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		//HANDSET
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG1_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG1());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERPTAG1_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG2());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERPTAG2_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG3());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERPTAG3_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG4());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERPTAG4_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG5());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG2_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG6());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG3_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG7());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG4_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG8());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG5_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG9());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG6_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG10());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG7_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG11());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG8_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG12());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG9_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG13());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG10_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG14());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG11_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG15());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG12_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG16());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG13_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG17());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG14_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG18());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG15_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG19());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_EXTPTAG16_XXXX", md.getATTRIBUTE_PARAMETER_ACC_TAG20());
	}
    
    public void getCreditClassInfo(String target, String FileName , FileManager fil, MasterData md, AcsLabels al) {
		// TODO Auto-generated method stub
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =target +"/"+FileName;
		tmp_target_file = target +"/tmp_"+FileName;

		if(md.getACCOUNT_CODE().contains("PRS")){
			
			System.out.println("PLEASE ENTER CREDIT CLASS (207/208) .......");
			md.setCREDIT_CLASS_CODE(in.nextLine());
			
		} else if(md.getACCOUNT_CODE().contains("PRB")){
			
			if(md.getMARKET_SEGMENT() == "GruppoPoste"){
				fil.replace(target_file,  tmp_target_file,"SW_ART_XXX", "0");
				fil.replace(target_file,  tmp_target_file,"SW_ARA_XXX", "0");
			}
			else{
				fil.replace(target_file,  tmp_target_file,"SW_ART_XXX", "1000");
				fil.replace(target_file,  tmp_target_file,"SW_ARA_XXX", "1500");
			}
			md.setCREDIT_CLASS_CODE(al.mapCreditClass_PRE.get(md.getMARKET_SEGMENT()));
		} else if(md.getACCOUNT_CODE().contains("POB") || md.getACCOUNT_CODE().contains("POH")){
			
			if(md.getMARKET_SEGMENT() == "GruppoPoste"){
				fil.replace(target_file,  tmp_target_file,"SW_ART_XXX", "0");
				fil.replace(target_file,  tmp_target_file,"SW_ARA_XXX", "0");
			}
			else{
				fil.replace(target_file,  tmp_target_file,"SW_ART_XXX", "1000");
				fil.replace(target_file,  tmp_target_file,"SW_ARA_XXX", "1500");
			}
			md.setCREDIT_CLASS_CODE(al.mapCreditClass_POST.get(md.getMARKET_SEGMENT()));
		}
		
		 fil.replace(target_file,  tmp_target_file,"SW_CC_XXX", md.getCREDIT_CLASS_CODE());
	}
    
    public void getMarketSegmentInfo(String target, String FileName , FileManager fil, MasterData md, AcsLabels al) {
		
    	 String marketSegment = ""; 
    	 String target_file = null;
		 String tmp_target_file = null;
			
		 target_file =target +"/"+FileName;
		 tmp_target_file = target +"/tmp_"+FileName;
		 
		String msg = "Please select the type of Market Segment.......";
		System.out.println(msg);
		
		 System.out.println("1. Small");
		 System.out.println("2. Large");
		 System.out.println("3. Top");
		 System.out.println("4. SME");
		 System.out.println("5. GruppoPoste");
		 
		 marketSegment = in.nextLine();
		 
		 while(!isInteger(marketSegment)){
		        System.out.println("Please enter a valid number");
		        marketSegment = in.nextLine();
		 }
		 
		 md.setMARKET_SEGMENT(al.markSeg.get(marketSegment));
		 
		 fil.replace(target_file,  tmp_target_file, "SW_MS_XXX", md.getMARKET_SEGMENT());
	}
    
    public void getCustomerCodeUpdate(MasterData md){
    	
    	String custCode = in.nextLine();
    	md.setCUSTOMER_CODE(custCode);
    }
    
    public void getAccountCodeUpdate(MasterData md){
    	
    	String actCode = in.nextLine();
    	md.setACCOUNT_CODE(actCode);
    }
    
    public void get_user_input_option_delete(String target, String FileName , FileManager fil, MasterData md, DBConnection db) throws DBException {
		
		// TODO Auto-generated method stub
		String getInp;
		int flg = 0;
		String overrideStr;
		String delimiter = "\\|";
		String[] arrEra;
		ArrayList<String> overrideData;
		ArrayList<InputPojoAttributes> loadedAttriData = null;

		HashMap<Integer,InputPojoAttributes>  mAttriMap1 = new HashMap<Integer,InputPojoAttributes>();
		HashMap<Integer,InputPojoAttributes>  mAttriMap2 = new HashMap<Integer,InputPojoAttributes>();
		HashMap<Integer,InputPojoAttributes>  mAttriMap3 = new HashMap<Integer,InputPojoAttributes>();
		HashMap<Integer,InputPojoAttributes>  mAttriMap4 = new HashMap<Integer,InputPojoAttributes>();
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =target +"/"+FileName;
		tmp_target_file = target +"/tmp_"+FileName;
		
		String msg = "Please enter the account number.......";
		System.out.println(msg);
		 
		md.setACCOUNT_CODE(in.nextLine());
		
		//Multiple SIM information
		get_input_option_upgrade(md, db);
		
		System.out.println("PLEASE ENTER OPTION CATALOGUE ID ( like 4235 ) .......");
		md.setOPTION_CATALOGUE_CODE(in.nextLine());
		
		//added
		if(md.getOPTION_CATALOGUE_CODE().equalsIgnoreCase("6080"))
		{
			md.setData_disattivazione_Canone(getDataDisattivazioneCanone());
				
		}
		
		/* System.out.println("PLEASE ENTER OPTION TARIFF ID ( like 24 ) .......");
		 md.setOPTION_TARIFF_CODE(in.nextLine());*/
		 		 
		 //System.out.println("PLEASE ENTER REFIL PAYMENT METHOD (like 912 968 ) .......");
		 //lab1.PAYMETHOD_CODE = in.nextLine();

		//GET account related data like customer code, product instance from db
		/*System.out.println(md.getROOT_INST_CODE());
		System.out.println(md.getOPTION_CATALOGUE_CODE());*/
		
//		added for 6080
	    fil.replace(target_file,tmp_target_file,"SW_Data_disattivazione_Canone_XXXX", md.getData_disattivazione_Canone());
	    
	    
	    //added for M2M
	    
	    if(Integer.parseInt(accountType) == 14){
	    	db.getOptionDetailsForDelUpdate1(md.getROOT_INST_CODE(), md.getOPTION_CATALOGUE_CODE(), md);
	    	
	    	loadedAttriData = db.getAttributeDataforDel1(md.getOPTION_CATALOGUE_CODE());
	    	System.out.println("%%%M2M");
	    }else{
	    	System.out.println("***E2E");
		db.getOptionDetailsForDelUpdate(md.getROOT_INST_CODE(), md.getOPTION_CATALOGUE_CODE(), md);
		loadedAttriData = db.getAttributeDataforDel(md.getOPTION_CATALOGUE_CODE());
	    }
		//System.out.println("!!!!!!!!!!!!!!!!!!!");
		 
		Iterator<InputPojoAttributes> itr1 = loadedAttriData.iterator();
		InputPojoAttributes ip1 = new InputPojoAttributes();
		int flg_att1=0;
		int flg_att2=0;
		int flg_att3=0;
		int flg_att4=0;

		while(itr1.hasNext()){
			ip1 = (InputPojoAttributes) itr1.next();
	//		System.out.println ("loop hash: " + ip1.getname() + "----" + ip1.getvalue() + "----" + ip1.getcolum() );
			if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG1") ){
				mAttriMap1.put(flg_att1++, ip1);
			}
			else if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG2") ){
				mAttriMap2.put(flg_att2++, ip1);
			}
			else if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG3") ){
				mAttriMap3.put(flg_att3++, ip1);
			} 
			else if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG4") ){
				mAttriMap4.put(flg_att4++, ip1);
			}
			else if(ip1.getcolum().equals("SBL_ERA_TAG") ){
				//PIPEEEEE SYM .. ip frm user
				
				arrEra = ip1.getname().split(delimiter);
				for(int i =0; i < arrEra.length ; i++){
				  //  System.out.println("Please enter the value for ERA name " +  arrEra[i]);
					//inpValue = in.nextLine();
				  
					md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_TEMP_DEL().replace("SW_ATT_NAME", arrEra[i]));
					md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_PARAMETER_TAG1().replace("SW_ATT_VAL", "0"));
					
					fil.replace(target_file,  tmp_target_file,"SW_ERA_TAG_ATTR_XXXX", md.getATTRIBUTE_PARAMETER_TAG1()+"SW_ERA_TAG_ATTR_XXXX");
					
				}
				fil.replace(target_file,  tmp_target_file,"SW_ERA_TAG_ATTR_XXXX", "");
			} 			
			
		}		
		if(mAttriMap1.size() > 0){
			/*System.out.println("Choose the option SBL_BILL_PARAMETER_TAG1: " + mAttriMap1.size());
			for (int val=0; val < mAttriMap1.size(); val++) {
				ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[val];
				System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
			}
			getInp = in.nextLine();
			getInp = Integer.parseInt(in.nextLine());
			while(!isInteger(getInp)){
		        System.out.println("Please enter a valid number");
		        getInp = in.nextLine();
		    }*/
			
			//if(mAttriMap1.size() > 1){
			//	System.out.println("Choose the option SBL_BILL_PARAMETER_TAG1: " + mAttriMap1.size());
				for (int val=0; val < mAttriMap1.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[val];
			//		System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
			//	getInp = in.nextLine();
			//	while(!isInteger(getInp)){
			//        System.out.println("Please enter a valid number");
			//        getInp = in.nextLine();
			//    }				
		///	}else{
				getInp = "0";
		//	}
			
			ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_TEMP_DEL().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_PARAMETER_TAG1().replace("SW_ATT_VAL", "0"));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 1;
			}
			
		}
		if(mAttriMap2.size() > 0){
			//if(mAttriMap2.size() > 1){
			//	System.out.println("Choose the option SBL_BILL_PARAMETER_TAG2: " + mAttriMap2.size());
				for (int val=0; val < mAttriMap2.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap2.values().toArray()[val];
			//		System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
			//	getInp = in.nextLine();
				/*getInp = Integer.parseInt(in.nextLine());*/
			//	while(!isInteger(getInp)){
			//        System.out.println("Please enter a valid number");
			 //       getInp = in.nextLine();
			//    }
			//}else{
				getInp = "0";
		//	}
			
			ip1 =  (InputPojoAttributes) mAttriMap2.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG2(md.getATTRIBUTE_TEMP_DEL().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG2(md.getATTRIBUTE_PARAMETER_TAG2().replace("SW_ATT_VAL", "0"));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 2;
			}
		}
		if(mAttriMap3.size() > 0){
		//	if(mAttriMap3.size() > 1){
			//	System.out.println("Choose the option SBL_BILL_PARAMETER_TAG3: " + mAttriMap3.size());
				for (int val=0; val < mAttriMap3.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap3.values().toArray()[val];
			//		System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
		//		getInp = in.nextLine();
				/*getInp = Integer.parseInt(in.nextLine());*/
			//	while(!isInteger(getInp)){
			//        System.out.println("Please enter a valid number");
			//        getInp = in.nextLine();
			//    }
		//	}else{
				getInp = "0";
		//	}
			
			ip1 =  (InputPojoAttributes) mAttriMap3.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG3(md.getATTRIBUTE_TEMP_DEL().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG3(md.getATTRIBUTE_PARAMETER_TAG3().replace("SW_ATT_VAL", "0"));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 3;
			}			
		}
		if(mAttriMap4.size() > 0){
		//	if(mAttriMap4.size() > 1){
		//		System.out.println("Choose the option SBL_BILL_PARAMETER_TAG4: " + mAttriMap4.size());
				for (int val=0; val < mAttriMap4.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap4.values().toArray()[val];
		//			System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
			//	getInp = in.nextLine();
				/*getInp = Integer.parseInt(in.nextLine());*/
			//	while(!isInteger(getInp)){
			//        System.out.println("Please enter a valid number");
			//        getInp = in.nextLine();
			//    }
		//	}else{
				getInp = "0";
		//	}
			
			ip1 =  (InputPojoAttributes) mAttriMap4.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG4(md.getATTRIBUTE_TEMP_DEL().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG4(md.getATTRIBUTE_PARAMETER_TAG4().replace("SW_ATT_VAL", "0"));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 4;
			}			
		}	
		
	
		 
		if(flg > 0){
		 //   System.out.println("Please enter the PricingFlexibilityFlag OverrideStartDate value in (MM/DD/YYYY HH:MM:SS) 07/21/2013 04:17:39" );
		//	inpValue = in.nextLine();
			md.setATTRIBUTE_OVERRIDE_START(md.getATTRIBUTE_TEMP_DEL().replace("SW_ATT_NAME", "OverrideStartDate"));  
			md.setATTRIBUTE_OVERRIDE_START(md.getATTRIBUTE_OVERRIDE_START().replace("SW_ATT_VAL", "07/21/2013 04:17:39"));
			
			
		//	System.out.println("Please enter the PricingFlexibilityFlag OverrideEndDate value in (MM/DD/YYYY HH:MM:SS) 07/21/2013 04:17:39" );
		//	inpValue = in.nextLine();
			md.setATTRIBUTE_OVERRIDE_END(md.getATTRIBUTE_TEMP_DEL().replace("SW_ATT_NAME", "OverrideEndDate"));  
			md.setATTRIBUTE_OVERRIDE_END(md.getATTRIBUTE_OVERRIDE_END().replace("SW_ATT_VAL", "07/21/2013 04:17:39"));

			overrideData = db.getOverrideData(md.getOPTION_CATALOGUE_CODE(), md.getBASE_TARIFF_CODE());
			//System.out.println("TESTTTING" + overrideData.size());
			for (int val = 0; val < overrideData.size(); val++){
				overrideStr = (String) overrideData.get(val);
			//	System.out.println("Enter the value for override Data " + overrideStr);
			//	inpValue = in.nextLine();
				
				md.setATTRIBUTE_OVERRIDE_PF(md.getATTRIBUTE_TEMP_DEL().replace("SW_ATT_NAME", overrideStr));  
				md.setATTRIBUTE_OVERRIDE_PF(md.getATTRIBUTE_OVERRIDE_PF().replace("SW_ATT_VAL", "qwerty"));	
				
			}
		}
		
		//md.setPROD_INST_CODE(md.getPROD_INST_CODE() + md.getPROP_TEMPLATE_ID()); 
		
		fil.replace(target_file,  tmp_target_file,"SW_ERA_TAG_ATTR_XXXX", "");
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR1_XXXX", md.getATTRIBUTE_PARAMETER_TAG1());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR2_XXXX", md.getATTRIBUTE_PARAMETER_TAG2());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR3_XXXX", md.getATTRIBUTE_PARAMETER_TAG3());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR4_XXXX", md.getATTRIBUTE_PARAMETER_TAG4());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERSTART_XXXX", md.getATTRIBUTE_OVERRIDE_START());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVEREND_XXXX", md.getATTRIBUTE_OVERRIDE_END());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERPF_XXXX", md.getATTRIBUTE_OVERRIDE_PF());
		fil.replace(target_file,  tmp_target_file,"SW_PRODINST_AUTO_XXXX", md.getPROD_INST_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_OPTIONCATALOG_ID_XXX", md.getOPTION_CATALOGUE_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_BASE_TARIFF_XXXX", md.getBASE_TARIFF_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE());		
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
	}
    
    public void get_input_base_upgrade(MasterData md, DBConnection db) throws DBException{
    	
    	HashMap<Integer,InputOptionUpgrade> hm_acc = new HashMap<Integer,InputOptionUpgrade>();
 
    		ArrayList<InputOptionUpgrade> optionUpgradeData = null;
			int loop = 0;
			String inp_acc = "";
			
			System.out.println("Please enter the customer code : ");
			
			getCustomerCodeUpdate(md);
		
			optionUpgradeData = db.getAccDetailsForUpgr(md.getCUSTOMER_CODE());
			Iterator<InputOptionUpgrade> itr1 = optionUpgradeData.iterator();
			InputOptionUpgrade io1 = new InputOptionUpgrade();

			System.out.println("Please Select the account on which you want to purchase base plan : ");
			while(itr1.hasNext()){
				io1 = (InputOptionUpgrade) itr1.next();
				System.out.println ( loop + "----" + io1.getAccount_code());
				if(!io1.getAccount_code().equals("") ){
					hm_acc.put(loop, io1);
				}
				loop++;
			}
			
			inp_acc = in.nextLine();
			 while(!isInteger(inp_acc)){
			        System.out.println("Please enter a valid number");
			        inp_acc = in.nextLine();
			 }
			
			 io1 =  (InputOptionUpgrade) hm_acc.values().toArray()[Integer.parseInt(inp_acc)];
			 md.setACCOUNT_CODE(io1.getAccount_code());		
    }
	
    
    public void get_input_option_upgrade(MasterData md, DBConnection db) throws DBException{
    	
    	HashMap<Integer,InputOptionUpgrade> hm_acc = new HashMap<Integer,InputOptionUpgrade>();
	
    		ArrayList<InputOptionUpgrade> optionUpgradeData = null;
			int loop = 0;
			String inp_acc = "";
		//	System.out.println(md.getACCOUNT_CODE());
			
			optionUpgradeData = db.getOptionDetailsForUpgr(md.getACCOUNT_CODE(), md);
			Iterator<InputOptionUpgrade> itr1 = optionUpgradeData.iterator();
			InputOptionUpgrade io1 = new InputOptionUpgrade();

			System.out.println("Please Select the MSISDN on which you want to perform action : ");
			//added for M2M
			/*if(Integer.parseInt(accountType) == 14){
				//io1.setAccount_code("SUB_"+md.getROOT_INST_CODE());
				System.out.println("hhhhhh"+md.getROOT_INST_CODE());
				System.out.println("ggggggg"+io1.getAccount_code());
			while(itr1.hasNext()){
				io1 = (InputOptionUpgrade) itr1.next();
				System.out.println ( loop + "----" + io1.getMsisdn() +" ("+io1.getAccount_code()+")");
				if(!io1.getAccount_code().equals("") ){
					hm_acc.put(loop, io1);
				}
				loop++;
			}*/
			//}else
			while(itr1.hasNext()){
				io1 = (InputOptionUpgrade) itr1.next();
				System.out.println ( loop + "----" + io1.getMsisdn() +" ("+io1.getAccount_code()+")");
				if(!io1.getAccount_code().equals("") ){
					hm_acc.put(loop, io1);
				}
				loop++;
			}
			
			inp_acc = in.nextLine();
			 while(!isInteger(inp_acc)){
			        System.out.println("Please enter a valid number");
			        actionType = in.nextLine();
			 }
			
			 io1 =  (InputOptionUpgrade) hm_acc.values().toArray()[Integer.parseInt(inp_acc)];
			 md.setACCOUNT_CODE(io1.getAccount_code());
			 md.setROOT_INST_CODE(io1.getRoot_inst_id());
			 
    }
    
    public void get_user_input_option_update(String target, String FileName , FileManager fil, MasterData md, DBConnection db) throws DBException {
		
		// TODO Auto-generated method stub
		String getInp;
		int flg = 0;
		String inpValue;
		String overrideStr;
		String delimiter = "\\|";
		String[] arrEra;
		ArrayList<String> overrideData;
		ArrayList<InputPojoAttributes> loadedAttriData = null;

		HashMap<Integer,InputPojoAttributes>  mAttriMap1 = new HashMap<Integer,InputPojoAttributes>();
		HashMap<Integer,InputPojoAttributes>  mAttriMap2 = new HashMap<Integer,InputPojoAttributes>();
		HashMap<Integer,InputPojoAttributes>  mAttriMap3 = new HashMap<Integer,InputPojoAttributes>();
		HashMap<Integer,InputPojoAttributes>  mAttriMap4 = new HashMap<Integer,InputPojoAttributes>();
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =target +"/"+FileName;
		tmp_target_file = target +"/tmp_"+FileName;
		
		String msg = "Please enter the account number.......";
		System.out.println(msg);
		 
		md.setACCOUNT_CODE(in.nextLine());
		//added
		System.out.println("llllllllllllllllllll"+md.getACCOUNT_CODE());
		
		//Multiple SIM information
		get_input_option_upgrade(md, db);

		System.out.println("PLEASE ENTER OPTION CATALOGUE ID ( like 4235 ) .......");
		md.setOPTION_CATALOGUE_CODE(in.nextLine());
		 
		 System.out.println("PLEASE ENTER OPTION TARIFF ID ( like 24 ) .......");
		 md.setBASE_TARIFF_CODE(in.nextLine());
		 		 
		 //System.out.println("PLEASE ENTER REFIL PAYMENT METHOD (like 912 968 ) .......");
		 //lab1.PAYMETHOD_CODE = in.nextLine();

		//GET account related data like customer code, product instance from db
		 
		 //added for M2M
		 if(Integer.parseInt(accountType) == 14){
			 db.getOptionDetailsForDelUpdate1(md.getROOT_INST_CODE(), md.getOPTION_CATALOGUE_CODE(), md);
		 }else{
		db.getOptionDetailsForDelUpdate(md.getROOT_INST_CODE(), md.getOPTION_CATALOGUE_CODE(), md);
		 }
		//added for M2M
		if(Integer.parseInt(accountType) == 14){
			loadedAttriData = db.getAttributeData1(md.getOPTION_CATALOGUE_CODE(), md.getBASE_TARIFF_CODE());
		}else{
		loadedAttriData = db.getAttributeData(md.getOPTION_CATALOGUE_CODE(), md.getBASE_TARIFF_CODE());
		}
		Iterator<InputPojoAttributes> itr1 = loadedAttriData.iterator();
		InputPojoAttributes ip1 = new InputPojoAttributes();
		int flg_att1=0;
		int flg_att2=0;
		int flg_att3=0;
		int flg_att4=0;

		while(itr1.hasNext()){
			ip1 = (InputPojoAttributes) itr1.next();
			//System.out.println ("loop hash: " + ip1.getname() + "----" + ip1.getvalue() + "----" + ip1.getcolum() );
			if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG1") ){
				mAttriMap1.put(flg_att1++, ip1);
			}
			else if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG2") ){
				mAttriMap2.put(flg_att2++, ip1);
			}
			else if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG3") ){
				mAttriMap3.put(flg_att3++, ip1);
			} 
			else if(ip1.getcolum().equals("SBL_BILL_PARAMETER_TAG4") ){
				mAttriMap4.put(flg_att4++, ip1);
			}
			else if(ip1.getcolum().equals("SBL_ERA_TAG") ){
				//PIPEEEEE SYM .. ip frm user
				
				arrEra = ip1.getname().split(delimiter);
				for(int i =0; i < arrEra.length ; i++){
				    System.out.println("Please enter the value for ERA name " +  arrEra[i]);
					inpValue = in.nextLine();
				  
					md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_TEMP_UPDATE().replace("SW_ATT_NAME", arrEra[i]));
					md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_PARAMETER_TAG1().replace("SW_ATT_VAL", inpValue));
					
					fil.replace(target_file,  tmp_target_file,"SW_ERA_TAG_ATTR_XXXX", md.getATTRIBUTE_PARAMETER_TAG1()+"SW_ERA_TAG_ATTR_XXXX");
					
				}
				fil.replace(target_file,  tmp_target_file,"SW_ERA_TAG_ATTR_XXXX", "");
			} 			
			
		}		
		if(mAttriMap1.size() > 0){
			/*System.out.println("Choose the option SBL_BILL_PARAMETER_TAG1: " + mAttriMap1.size());
			for (int val=0; val < mAttriMap1.size(); val++) {
				ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[val];
				System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
			}
			getInp = in.nextLine();
			getInp = Integer.parseInt(in.nextLine());
			while(!isInteger(getInp)){
		        System.out.println("Please enter a valid number");
		        getInp = in.nextLine();
		    }*/
			
			if(mAttriMap1.size() > 1){
				System.out.println("Choose the option SBL_BILL_PARAMETER_TAG1: " + mAttriMap1.size());
				for (int val=0; val < mAttriMap1.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[val];
					System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
				getInp = in.nextLine();
				while(!isInteger(getInp)){
			        System.out.println("Please enter a valid number");
			        getInp = in.nextLine();
			    }				
			}else{
				getInp = "0";
				ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[0];
				if(ip1.getvalue().equals("NOVALUE")){
					System.out.println("Please enter the value for " +ip1.getname() +" :" );
					ip1.setvalue(in.nextLine());
				}
			}
			
			ip1 =  (InputPojoAttributes) mAttriMap1.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_TEMP_UPDATE().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG1(md.getATTRIBUTE_PARAMETER_TAG1().replace("SW_ATT_VAL", ip1.getvalue()));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 1;
			}
			
		}
		if(mAttriMap2.size() > 0){
			if(mAttriMap2.size() > 1){
				System.out.println("Choose the option SBL_BILL_PARAMETER_TAG2: " + mAttriMap2.size());
				for (int val=0; val < mAttriMap2.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap2.values().toArray()[val];
					System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
				getInp = in.nextLine();
				/*getInp = Integer.parseInt(in.nextLine());*/
				while(!isInteger(getInp)){
			        System.out.println("Please enter a valid number");
			        getInp = in.nextLine();
			    }
			}else{
				getInp = "0";
				ip1 =  (InputPojoAttributes) mAttriMap2.values().toArray()[0];
				if(ip1.getvalue().equals("NOVALUE")){
					System.out.println("Please enter the value for " +ip1.getname() +" :" );
					ip1.setvalue(in.nextLine());
				}
			}
			
			ip1 =  (InputPojoAttributes) mAttriMap2.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG2(md.getATTRIBUTE_TEMP_UPDATE().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG2(md.getATTRIBUTE_PARAMETER_TAG2().replace("SW_ATT_VAL", ip1.getvalue()));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 2;
			}
		}
		if(mAttriMap3.size() > 0){
			if(mAttriMap3.size() > 1){
				System.out.println("Choose the option SBL_BILL_PARAMETER_TAG3: " + mAttriMap3.size());
				for (int val=0; val < mAttriMap3.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap3.values().toArray()[val];
					System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
				getInp = in.nextLine();
				/*getInp = Integer.parseInt(in.nextLine());*/
				while(!isInteger(getInp)){
			        System.out.println("Please enter a valid number");
			        getInp = in.nextLine();
			    }
			}else{
				getInp = "0";
				ip1 =  (InputPojoAttributes) mAttriMap3.values().toArray()[0];
				if(ip1.getvalue().equals("NOVALUE")){
					System.out.println("Please enter the value for " +ip1.getname() +" :" );
					ip1.setvalue(in.nextLine());
				}
			}
			
			ip1 =  (InputPojoAttributes) mAttriMap3.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG3(md.getATTRIBUTE_TEMP_UPDATE().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG3(md.getATTRIBUTE_PARAMETER_TAG3().replace("SW_ATT_VAL", ip1.getvalue()));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 3;
			}			
		}
		if(mAttriMap4.size() > 0){
			if(mAttriMap4.size() > 1){
				System.out.println("Choose the option SBL_BILL_PARAMETER_TAG4: " + mAttriMap4.size());
				for (int val=0; val < mAttriMap4.size(); val++) {
					ip1 =  (InputPojoAttributes) mAttriMap4.values().toArray()[val];
					System.out.println("   " + val + ". " + ip1.getname()   + "," + ip1.getvalue())   ;
				}
				getInp = in.nextLine();
				/*getInp = Integer.parseInt(in.nextLine());*/
				while(!isInteger(getInp)){
			        System.out.println("Please enter a valid number");
			        getInp = in.nextLine();
			    }
			}else{
				getInp = "0";
				ip1 =  (InputPojoAttributes) mAttriMap4.values().toArray()[0];
				if(ip1.getvalue().equals("NOVALUE")){
					System.out.println("Please enter the value for " +ip1.getname() +" :" );
					ip1.setvalue(in.nextLine());
				}
			}
			
			ip1 =  (InputPojoAttributes) mAttriMap4.values().toArray()[Integer.parseInt(getInp)];
			md.setATTRIBUTE_PARAMETER_TAG4(md.getATTRIBUTE_TEMP_UPDATE().replace("SW_ATT_NAME", ip1.getname()));  
			md.setATTRIBUTE_PARAMETER_TAG4(md.getATTRIBUTE_PARAMETER_TAG4().replace("SW_ATT_VAL", ip1.getvalue()));
			
			//System.out.println("VALLLL" + lab1.ATTRIBUTE_PARAMETER_TAG1 );
			if(ip1.getname().equalsIgnoreCase("PricingFlexibilityFlag") && ip1.getvalue().equalsIgnoreCase("Y") )
			{
				flg = 4;
			}			
		}	
		
	
		 
		if(flg > 0){
		    System.out.println("Please enter the PricingFlexibilityFlag OverrideStartDate value in (MM/DD/YYYY HH:MM:SS) 07/21/2013 04:17:39" );
			inpValue = in.nextLine();
			md.setATTRIBUTE_OVERRIDE_START(md.getATTRIBUTE_TEMP_UPDATE().replace("SW_ATT_NAME", "OverrideStartDate"));  
			md.setATTRIBUTE_OVERRIDE_START(md.getATTRIBUTE_OVERRIDE_START().replace("SW_ATT_VAL", inpValue));
			
			
			System.out.println("Please enter the PricingFlexibilityFlag OverrideEndDate value in (MM/DD/YYYY HH:MM:SS) 07/21/2013 04:17:39" );
			inpValue = in.nextLine();
			md.setATTRIBUTE_OVERRIDE_END(md.getATTRIBUTE_TEMP_UPDATE().replace("SW_ATT_NAME", "OverrideEndDate"));  
			md.setATTRIBUTE_OVERRIDE_END(md.getATTRIBUTE_OVERRIDE_END().replace("SW_ATT_VAL", inpValue));

			overrideData = db.getOverrideData(md.getOPTION_CATALOGUE_CODE(), md.getBASE_TARIFF_CODE());
			//System.out.println("TESTTTING" + overrideData.size());
			for (int val = 0; val < overrideData.size(); val++){
				overrideStr = (String) overrideData.get(val);
				System.out.println("Enter the value for override Data " + overrideStr);
				inpValue = in.nextLine();
				
				md.setATTRIBUTE_OVERRIDE_PF(md.getATTRIBUTE_TEMP_UPDATE().replace("SW_ATT_NAME", overrideStr));  
				md.setATTRIBUTE_OVERRIDE_PF(md.getATTRIBUTE_OVERRIDE_PF().replace("SW_ATT_VAL", inpValue));	
				
			}
		}
		
	//	md.setPROD_INST_CODE(md.getPROD_INST_CODE() + md.getPROP_TEMPLATE_ID()); 
		
		fil.replace(target_file,  tmp_target_file,"SW_ERA_TAG_ATTR_XXXX", "");
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR1_XXXX", md.getATTRIBUTE_PARAMETER_TAG1());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR2_XXXX", md.getATTRIBUTE_PARAMETER_TAG2());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR3_XXXX", md.getATTRIBUTE_PARAMETER_TAG3());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_ATTR4_XXXX", md.getATTRIBUTE_PARAMETER_TAG4());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERSTART_XXXX", md.getATTRIBUTE_OVERRIDE_START());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVEREND_XXXX", md.getATTRIBUTE_OVERRIDE_END());
		fil.replace(target_file,  tmp_target_file,"SW_LIST_OVERPF_XXXX", md.getATTRIBUTE_OVERRIDE_PF());
		fil.replace(target_file,  tmp_target_file,"SW_PRODINST_AUTO_XXXX", md.getPROD_INST_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_OPTIONCATALOG_ID_XXX", md.getOPTION_CATALOGUE_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_BASE_TARIFF_XXXX", md.getBASE_TARIFF_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE());		
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		
		
		System.out.println("---------------------"+md.getROOT_INST_CODE()+"------------------------------"+md.getPROD_INST_CODE());
	}
    
    public void get_user_input_handset_1(String target, String FileName , FileManager fil, MasterData md, DBConnection db) throws DBException {
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =target +"/"+FileName;
		tmp_target_file = target +"/tmp_"+FileName;

		/*System.out.println("Please enter the Tariff ID : ");
		md.setBASE_TARIFF_CODE(in.nextLine());
		*/
		System.out.println("Please enter the DeliveryDate value in (MM/DD/YYYY HH:MM:SS) 07/21/2013 04:17:39" );
		md.setDELIVERY_DATE(in.nextLine());
	//	md.setPROD_INST_CODE(md.getPROD_INST_CODE() + md.getPROP_TEMPLATE_ID()); 
		
	//	fil.replace(target_file,  tmp_target_file,"SW_PRODINST_AUTO_XXXX", md.getPROD_INST_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_BASE_TARIFF_XXXX", md.getBASE_TARIFF_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE());		
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_DELDATE_XXX", md.getDELIVERY_DATE());
	}
    
    public void get_user_input_handset_2(String target, String FileName , FileManager fil, MasterData md, DBConnection db) throws DBException {
		
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =target +"/"+FileName;
		tmp_target_file = target +"/tmp_"+FileName;
		
		System.out.println("Please enter the Entry Fee : " );
		md.setENTRY_FEE(in.nextLine());

		System.out.println("Please enter the Importo value : " );
		md.setIMPORTO_FEE(in.nextLine());
		
		System.out.println("Please enter the Numero value : " );
		md.setNUMERO(in.nextLine());

		md.setPROD_INST_CODE(md.getPROD_INST_CODE() + md.getPROP_TEMPLATE_ID()); 
		
		fil.replace(target_file,  tmp_target_file,"SW_PRODINST_AUTO_XXXX", md.getPROD_INST_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_BASE_TARIFF_XXXX", md.getBASE_TARIFF_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE());		
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ENTRYFEE_XXX", md.getENTRY_FEE());
		fil.replace(target_file,  tmp_target_file,"SW_ENTRYFEEIVA_XXX", String.valueOf(Integer.parseInt(md.getENTRY_FEE())*1.22));
		fil.replace(target_file,  tmp_target_file,"SW_IMPORTOFEE_XXX", md.getIMPORTO_FEE());		
		fil.replace(target_file,  tmp_target_file,"SW_IMPORTOFEEIVA_XXX", String.valueOf(Integer.parseInt(md.getIMPORTO_FEE())*1.22));
		fil.replace(target_file,  tmp_target_file,"SW_NUMERO_XXX", md.getNUMERO());
	}
    
    public void get_user_input_handset_3(String target, String FileName , FileManager fil, MasterData md, DBConnection db) throws DBException {
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =target +"/"+FileName;
		tmp_target_file = target +"/tmp_"+FileName;
		
		System.out.println("Please enter the Importo value for Shipment : " );
		md.setIMPORTO_SHIP(in.nextLine());
		
		md.setROOT_INST_CODE(md.getROOT_INST_CODE() + "_1"); 
		
		//fil.replace(target_file,  tmp_target_file,"SW_PRODINST_AUTO_XXXX", md.getPROD_INST_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_BASE_TARIFF_XXXX", md.getBASE_TARIFF_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE());		
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_DELDATE_XXX", md.getDELIVERY_DATE());
		fil.replace(target_file,  tmp_target_file,"SW_IMPORTOSHIP_XXX", md.getIMPORTO_SHIP());		
		fil.replace(target_file,  tmp_target_file,"SW_IMPORTOSHIPIVA_XXX", String.valueOf(Integer.parseInt(md.getIMPORTO_SHIP())*1.22));
	}
    
    public void get_user_input_adjustment(String target, String FileName , FileManager fil, MasterData md, DBConnection db){
    	
    	String target_file = null;
		String tmp_target_file = null;
		
		target_file =target +"/"+FileName;
		tmp_target_file = target +"/tmp_"+FileName;

		 System.out.println("PLEASE ENTER ADJUSTMENT AMOUNT .......");
		 md.setADJ_AMT(in.nextLine());
		 
		 System.out.println("PLEASE ENTER CURRENCY(EUR for Euro AND FU_EUR FOR FU_Euro) .......");
		 md.setCURR_CODE(in.nextLine());
		 
		 System.out.println("PLEASE ENTER ACCOUNT TYPE(Postpaid/Prepaid) .......");
		 md.setADJ_ACCT(in.nextLine());
		 
		 System.out.println("PLEASE ENTER TYPE OF ADJUSTMENT(Credit/Debit) .......");
		 md.setADJ_TYPE(in.nextLine());
		 
		 System.out.println("PLEASE ENTER REASON FOR REQUEST .......");
		 md.setREA_REQ(in.nextLine());
		 
		 System.out.println("PLEASE ENTER ACCOUNT NUMBER .......");
		 md.setACCOUNT_CODE(in.nextLine());
		 
		 if((md.getCURR_CODE()).equalsIgnoreCase("FU_EUR")){
			 System.out.println("PLEASE ENTER DAYS TO EXPIRE .......");
			 md.setDAYS_EXP(in.nextLine());
		 }else{
			 md.setDAYS_EXP("");
		 }

		 md.setADJ_ID(md.getAUTO_NUMBER() + md.getPROP_TEMPLATE_ID());
		 
		 fil.replace(target_file,  tmp_target_file,"SW_AMT_XXXX", md.getADJ_AMT());
		 fil.replace(target_file,  tmp_target_file,"SW_CURR_XXXX", md.getCURR_CODE());
		 fil.replace(target_file,  tmp_target_file,"SW_TYPE_XXXX", md.getADJ_TYPE());
		 fil.replace(target_file,  tmp_target_file,"SW_ADJ_ACCT_XXXX", md.getADJ_ACCT());
		 fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		 fil.replace(target_file,  tmp_target_file,"SW_ADJ_ID_XXXX", md.getADJ_ID());
		 fil.replace(target_file,  tmp_target_file,"SW_RFREQ_XXXX", md.getREA_REQ());
		 fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		 fil.replace(target_file,  tmp_target_file,"SW_DAYS_EXP_XXXX", md.getDAYS_EXP());
    }
    
}
