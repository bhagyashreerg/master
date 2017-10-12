package clientWebservice;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.soap.SOAPException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.portal.pcm.Poid;

public class JobRouter {

	private String action = "";
	private String updateStartAction = "";
	public MasterData md; 
	private UserInput ui;
	private DBConnection db;
	webManager web = new webManager();
	private String templatePath;
	FileManager fil = new FileManager();
	public AcsLabels al = new AcsLabels();
	public String flag_re = "";
	public String flag_op = "";
	public String flag_dual = "";
	public Scanner inf = new Scanner(System.in);
	String[] listFilesRef = new String[1];
	
	public JobRouter(UserInput ui, DBConnection db) throws IOException {
		// TODO Auto-generated constructor stub
		// GET USER DATA
		this.ui=ui;
		action = ui.getAccountCreateType() + ui.getActionType() ;
		updateStartAction = ui.getUpdateType();
		md = new MasterData(ui.getAccountCreateType(), al);
		System.out.println("-------------------"+action);
		this.db = db;
	
		// CHECK AND CLEAR RESPONSE AND REQUEST FOLDER
		fil.confirmDir(al.PROP_REQUEST_PATH);
		fil.confirmDir(al.PROP_RESPONSE_PATH);
		fil.deleteFiles(al.PROP_REQUEST_PATH);
		fil.deleteFiles(al.PROP_RESPONSE_PATH);		
	
		templatePath=al.xmlTemplate.get(ui.getAccountCreateType());	

		///Sync
		File destination_file_req = fil.createWorkDir(al.PROP_REQUEST_PATH, md, al);
		al.PROP_REQUEST_PATH = al.PROP_REQUEST_PATH+"//"+destination_file_req.getName();
		//System.out.println(al.PROP_REQUEST_PATH);
		
		
		File destination_file_res = fil.createWorkDir(al.PROP_RESPONSE_PATH, md, al);
		al.PROP_RESPONSE_PATH = al.PROP_RESPONSE_PATH+"//"+destination_file_res.getName();
		//System.out.println(al.PROP_RESPONSE_PATH);
		
		System.out.println("Please Note Request and Response folders: " +md.getAUTO_EPOCHTIME());
	}

	public void workerSelect() throws IOException, DBException{
		
	
		// TODO Auto-generated method stub
		
		switch (Integer.parseInt(action)) {
		
		
        case 11:  
        		prepaidConsumerAdd();
                break;        
        case 21: 
        	    prepaidBusinessAdd();
                break;
        case 31:
        		prepaidSoholpAdd();
                break;     
        case 41:
        		postpaidConsumerAdd();
                break;
        case 51:
        		postpaidBusinessAdd();
                break;
        case 61:
        		postpaidSoholpAdd();
                break;        
        case 71:
    			postpaidHandsetAdd();
    			break;            
        case 131:
        	    postpaidConsumerSinglePlayAdd();  //added
        	    break;
        case 141:
        	    postpaidConsumerM2MAdd();//added for M2M
        	    break;
        case 12:  
        		prepaidConsumerUpgrade(updateStartAction);
    			break;
	    case 22:
	    		prepaidBusinessUpgrade(updateStartAction);
	            break;
	    case 32:
	    	    prepaidSoholpUpgrade(updateStartAction);
	            break;
	    case 42:
	    		postpaidConsumerUpgrade(updateStartAction);
	            break;
	    case 52:
	    		postpaidBusinessUpgrade(updateStartAction);
	            break;
	    case 62:
	    		postpaidSoholpUpgrade(updateStartAction);
	            break;   
	    case 132:
	    	    postpaidConsumerSinglePlayUpgrade(updateStartAction);   //added
	    	    break;
	    case 142:
	    		postpaidConsumerM2MUpgrade(updateStartAction); //added for M2M
	    		break;
	    case 13:case 23:case 33:case 43: case 53:case 63:case 133:case 143: //added //added for M2M
    			optionDelete();
    			break;    
	    case 14:case 24:case 34:case 44: case 54:case 64:case 134:case 144: //added //added for M2M
    			optionUpdate();
    			break;
	    case 80:
    			adjustment();
    			break;
        default: 
        		System.out.println("Sorry Boss.... No matching entry by you");
        		break;
		}         
	}

	private void prepaidConsumerAdd() throws IOException, DBException {
		// TODO Auto-generated method stub
		String[] listFiles =null;
		
		// SETTING VARIABLES VALUE
		//md.setPROP_TEMPLATE_ID("PRC");
		
		// GET INPUT FROM USER FR 1st THREE PROCESS
		md.setTipo_MVNO(ui.getTipoMVNO());
		md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

		/*Update IMSI for Full MVNO*/
		if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
			md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
		}else{
			md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
		}
		 
		md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
		md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
		
		System.out.println(templatePath);
		
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		listFiles = fil.listFiles(templatePath);

		System.out.println(listFiles);
		
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

		//---------------  CALLING WEB SERVICE :-) HOPE IT WORKS !!!!! ---------------// 
		//---------------  I HAVE SPLIT THE TOTAL FILES CALL TO 3 AND REST SO THAT I DON'T HAVE TO  ---------------// 
		//---------------  GET INPUT UNNECESSARILY WHEN THE 1ST WEB SERVICE ITSELF CAN FAIL. ---------------//  
		/*
		for(int i=0;i<3;i++){
			System.out.println("...... Calling webservice for " + listFiles[i]);
			web.callWebservice(lab1.WEB_SERVICE_URL, lab1.PROP_REQUEST_PATH + "//" + listFiles[i] , lab1.PROP_RESPONSE_PATH + "//" + listFiles[i]);
			File response_file = new File(al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
			
			//---------------  IF WEBSERVICE FAILS . ---------------//
			
			if(fil.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
				System.exit(0);
			}
			else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
				if(listFiles[i].equals("01_customer_create.xml")){
					System.out.println("............ Customer Created: " + md.getCUSTOMER_CODE()  );
				}
				else if(listFiles[i].equals("02_account_create.xml")){
					System.out.println("............ Account Created: " + md.getACCOUNT_CODE()  );
				}
				else if(listFiles[i].equals("03_base_create.xml")){
					System.out.println("............ RootInstId Created: " + md.getROOT_INST_CODE() );
				}
			}
		}//END FOR
		*/
	/*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}		
		
		int i = 1;
			createCustomer(listFiles[0]);
			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();
			while(!(flag_re.equalsIgnoreCase("y") || flag_re.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_re = inf.nextLine();
			}
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();
			
			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
			
	}

	//PRINCY//
	private void prepaidSoholpAdd() throws IOException, DBException {
		// TODO Auto-generated method stub
		String[] listFiles =null;
		
		// SETTING VARIABLES VALUE
		//md.setPROP_TEMPLATE_ID("PRC");
		
		// GET INPUT FROM USER FR 1st THREE PROCESS
		md.setTipo_MVNO(ui.getTipoMVNO());
		md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

		/*Update IMSI for Full MVNO*/
		if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
			md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
		}else{
			md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
		}
		 
		md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
		md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
		
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		listFiles = fil.listFiles(templatePath);
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

		//---------------  CALLING WEB SERVICE :-) HOPE IT WORKS !!!!! ---------------// 
		//---------------  I HAVE SPLIT THE TOTAL FILES CALL TO 3 AND REST SO THAT I DON'T HAVE TO  ---------------// 
		//---------------  GET INPUT UNNECESSARILY WHEN THE 1ST WEB SERVICE ITSELF CAN FAIL. ---------------//  
		/*
		for(int i=0;i<3;i++){
			System.out.println("...... Calling webservice for " + listFiles[i]);
			web.callWebservice(lab1.WEB_SERVICE_URL, lab1.PROP_REQUEST_PATH + "//" + listFiles[i] , lab1.PROP_RESPONSE_PATH + "//" + listFiles[i]);
			File response_file = new File(al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
			
			//---------------  IF WEBSERVICE FAILS . ---------------//
			
			if(fil.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
				System.exit(0);
			}
			else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
				if(listFiles[i].equals("01_customer_create.xml")){
					System.out.println("............ Customer Created: " + md.getCUSTOMER_CODE()  );
				}
				else if(listFiles[i].equals("02_account_create.xml")){
					System.out.println("............ Account Created: " + md.getACCOUNT_CODE()  );
				}
				else if(listFiles[i].equals("03_base_create.xml")){
					System.out.println("............ RootInstId Created: " + md.getROOT_INST_CODE() );
				}
			}
		}//END FOR
		*/
		
		/*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}	
		int i = 1;
			createCustomer(listFiles[0]);
			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();
			
			while(!(flag_re.equalsIgnoreCase("y") || flag_re.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_re = inf.nextLine();
			}
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
	}
	
	
	private void prepaidBusinessAdd() throws IOException, DBException {
		// TODO Auto-generated method stub
		String[] listFiles =null;
		
		// SETTING VARIABLES VALUE
		//md.setPROP_TEMPLATE_ID("PRC");
		
		// GET INPUT FROM USER FR 1st THREE PROCESS
		md.setTipo_MVNO(ui.getTipoMVNO());
		md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

		/*Update IMSI for Full MVNO*/
		if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
			md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
		}else{
			md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
		}
		
		md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
		md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
		
		
		//AsK if user is intrested in dual billing or not
		System.out.println("Do you want to create a prepaid Business account for Dual Billing(Y/N): ");
		flag_dual = inf.nextLine();
		while(!(flag_dual.equalsIgnoreCase("y") || flag_dual.equalsIgnoreCase("n"))){
	        System.out.println("Please enter a valid value");
	        flag_dual = inf.nextLine();
		}
		if(flag_dual.equalsIgnoreCase("N")){
			
			//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
			listFiles = fil.listFiles(templatePath);
			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
			
			int i = 1;
				ui.getMarketSegmentInfo(al.PROP_REQUEST_PATH, listFiles[0], fil, md, al);
				
				/*	Refresh Cache */
				if(ui.getRefCache().equalsIgnoreCase("Y")){
					doRefreshCache("refresh_cache.xml");
				}	
				
				createCustomer(listFiles[0]);
				
				ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
				createAccount(listFiles[1]);
				createBaseDeal(listFiles[2]);
				
				System.out.println("");
				System.out.println("Do you want to do a refill(Y/N): ");
				flag_re = inf.nextLine();

				while(!(flag_re.equalsIgnoreCase("y") || flag_re.equalsIgnoreCase("n"))){
			        System.out.println("Please enter a valid value");
			        flag_re = inf.nextLine();
				}
				
				while(flag_re.equalsIgnoreCase("Y")){
					doRefill(listFiles[3]);
					
					System.out.println("Do you want to do another refill(Y/N): ");
					flag_re = inf.nextLine();
					if(flag_re.equalsIgnoreCase("Y")){
						listFilesRef[0] = listFiles[3];
					//	System.out.println(listFilesRef[0]);
						fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
						md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
						++i;
					}
				}
				
				i = 1;
				
				System.out.println("Do you want to purchase an option(Y/N): ");
				flag_op = inf.nextLine();

				while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
			        System.out.println("Please enter a valid value");
			        flag_op = inf.nextLine();
				}
				
				while(flag_op.equalsIgnoreCase("Y")){
					createOption(listFiles[4]);
					
					System.out.println("Do you want to purchase another option(Y/N): ");
					flag_op = inf.nextLine();
					if(flag_op.equalsIgnoreCase("Y")){
						listFilesRef[0] = listFiles[4];
					//	System.out.println(listFilesRef[0]);
						fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
						++i;
						md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
					}
				
				}			
		}else if(flag_dual.equalsIgnoreCase("Y")){

			templatePath = al.xmlTemplate.get("8");
			//System.out.println(templatePath);
			//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
			listFiles = fil.listFiles(templatePath);
			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

			int i = 1;
			ui.getMarketSegmentInfo(al.PROP_REQUEST_PATH, listFiles[0], fil, md, al);
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			createCustomer(listFiles[0]);
			
			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			System.out.println("");
			System.out.println("Do you want to do a refill on main account(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				++i;
				md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
				}
			
			}		
			createCustomerDual(listFiles[5]);
			createAccountDual(listFiles[6]);
			createOptionDual(listFiles[7]);
			
			i = 1;
			System.out.println("");
			System.out.println("Do you want to do a refill on personal account(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				md.setACCOUNT_CODE(md.getACCOUNT_CODE_DUAL());
				md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
				listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
				fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
				}
			}
			
		}
		
	}
	
	private void postpaidConsumerAdd() throws IOException, DBException {
		// TODO Auto-generated method stub
		String[] listFiles =null;
		
		// SETTING VARIABLES VALUE
		//md.setPROP_TEMPLATE_ID("PRC");
		
		// GET INPUT FROM USER FR 1st THREE PROCESS
		md.setTipo_MVNO(ui.getTipoMVNO());
		md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

		/*Update IMSI for Full MVNO*/
		if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
			md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
		}else{
			md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
		}
		
		md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
		md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
		
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		listFiles = fil.listFiles(templatePath);
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

		//---------------  CALLING WEB SERVICE :-) HOPE IT WORKS !!!!! ---------------// 
		//---------------  I HAVE SPLIT THE TOTAL FILES CALL TO 3 AND REST SO THAT I DON'T HAVE TO  ---------------// 
		//---------------  GET INPUT UNNECESSARILY WHEN THE 1ST WEB SERVICE ITSELF CAN FAIL. ---------------//  
		/*
		for(int i=0;i<3;i++){
			System.out.println("...... Calling webservice for " + listFiles[i]);
			web.callWebservice(lab1.WEB_SERVICE_URL, lab1.PROP_REQUEST_PATH + "//" + listFiles[i] , lab1.PROP_RESPONSE_PATH + "//" + listFiles[i]);
			File response_file = new File(al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
			
			//---------------  IF WEBSERVICE FAILS . ---------------//
			
			if(fil.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
				System.exit(0);
			}
			else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
				if(listFiles[i].equals("01_customer_create.xml")){
					System.out.println("............ Customer Created: " + md.getCUSTOMER_CODE()  );
				}
				else if(listFiles[i].equals("02_account_create.xml")){
					System.out.println("............ Account Created: " + md.getACCOUNT_CODE()  );
				}
				else if(listFiles[i].equals("03_base_create.xml")){
					System.out.println("............ RootInstId Created: " + md.getROOT_INST_CODE() );
				}
			}
		}//END FOR
		*/
		
		/*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}
		
		//int i = 0;
			createCustomer(listFiles[0]);
			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			int i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
		
	}
	
	private void postpaidBusinessAdd() throws IOException, DBException {
		// TODO Auto-generated method stub
		String[] listFiles =null;
		
		// SETTING VARIABLES VALUE
		//md.setPROP_TEMPLATE_ID("PRC");
		
		// GET INPUT FROM USER FR 1st THREE PROCESS
		md.setTipo_MVNO(ui.getTipoMVNO());
		md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db));
		
		/*Update IMSI for Full MVNO*/
		if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
			md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
		}else{
			md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
		}
		
		md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
		md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
		
		System.out.println("Do you want to create a postpaid Business account for Dual Billing(Y/N): ");
		flag_dual = inf.nextLine();
		flag_dual = inf.nextLine();
		while(!(flag_dual.equalsIgnoreCase("y") || flag_dual.equalsIgnoreCase("n"))){
	        System.out.println("Please enter a valid value");
	        flag_dual = inf.nextLine();
		}
		
		if(flag_dual.equalsIgnoreCase("N")){
			
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		listFiles = fil.listFiles(templatePath);
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

		ui.getMarketSegmentInfo(al.PROP_REQUEST_PATH, listFiles[0], fil, md, al);
			
		/*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}	
		
			createCustomer(listFiles[0]);
			
			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
		
			int i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
		}else if(flag_dual.equalsIgnoreCase("Y")){

			templatePath = al.xmlTemplate.get("9");
			//System.out.println(templatePath);
			//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
			listFiles = fil.listFiles(templatePath);
			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

			
			int i = 1;
			ui.getMarketSegmentInfo(al.PROP_REQUEST_PATH, listFiles[0], fil, md, al);
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			createCustomer(listFiles[0]);
			
			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				++i;
				md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
				}
			}
			
			createCustomerDual(listFiles[5]);
			createAccountDual(listFiles[6]);
			createOptionDual(listFiles[7]);
			
			i = 1;
			System.out.println("");
			System.out.println("Do you want to do a refill on personal account(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				md.setACCOUNT_CODE(md.getACCOUNT_CODE_DUAL());
				md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
				listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
				fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
				}
			}
			
		}

	}
	
	private void postpaidSoholpAdd() throws IOException, DBException {
		// TODO Auto-generated method stub
		String[] listFiles =null;
		
		// SETTING VARIABLES VALUE
		//md.setPROP_TEMPLATE_ID("PRC");
		
		// GET INPUT FROM USER FR 1st THREE PROCESS
		md.setTipo_MVNO(ui.getTipoMVNO());
		md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db));
		
		/*Update IMSI for Full MVNO*/
		if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
			md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
		}else{
			md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
		}
		
		md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
		md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
		
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		listFiles = fil.listFiles(templatePath);
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

		//---------------  CALLING WEB SERVICE :-) HOPE IT WORKS !!!!! ---------------// 
		//---------------  I HAVE SPLIT THE TOTAL FILES CALL TO 3 AND REST SO THAT I DON'T HAVE TO  ---------------// 
		//---------------  GET INPUT UNNECESSARILY WHEN THE 1ST WEB SERVICE ITSELF CAN FAIL. ---------------//  
		/*
		for(int i=0;i<3;i++){
			System.out.println("...... Calling webservice for " + listFiles[i]);
			web.callWebservice(lab1.WEB_SERVICE_URL, lab1.PROP_REQUEST_PATH + "//" + listFiles[i] , lab1.PROP_RESPONSE_PATH + "//" + listFiles[i]);
			File response_file = new File(al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
			
			//---------------  IF WEBSERVICE FAILS . ---------------//
			
			if(fil.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
				System.exit(0);
			}
			else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
				if(listFiles[i].equals("01_customer_create.xml")){
					System.out.println("............ Customer Created: " + md.getCUSTOMER_CODE()  );
				}
				else if(listFiles[i].equals("02_account_create.xml")){
					System.out.println("............ Account Created: " + md.getACCOUNT_CODE()  );
				}
				else if(listFiles[i].equals("03_base_create.xml")){
					System.out.println("............ RootInstId Created: " + md.getROOT_INST_CODE() );
				}
			}
		}//END FOR
		*/

		/*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}	
		
			createCustomer(listFiles[0]);
			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			int i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
	}
	//added
	private void postpaidConsumerSinglePlayAdd() throws IOException, DBException{
		// TODO Auto-generated method stub
		//templatePath = al.xmlTemplate.get("13");
          String[] listFiles =null;
		
          md.setTipo_MVNO(ui.getTipoMVNO());
  		md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

  		/*Update IMSI for Full MVNO*/
  		if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
  			md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
  		}else{
  			md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
  		}
  		
  		md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
  		//added
  		md.setNumero_Portato(ui.getNumeroPortato());
  		md.setNumero_Temporaneo(ui.getNumeroTemporaneo());
  		md.setCodice_Migrazione_GNP(ui.getCodiceMigrazioneGNP());
  		md.setData_Attivazione_Linea(ui.getDataAttivazioneLinea());
  		md.setData_Attivazione_GNP(ui.getDataAttivazioneGNP());
  		md.setTipologia_Terminale(ui.getTipologiaTerminale());
  		
  		
  		md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
  		
  		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
  		
  		//System.out.println("---------------------"+templatePath);
  		
  		listFiles = fil.listFiles(templatePath);
  		//System.out.println("--------------------*********"+listFiles.length);
  		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

  		//---------------  CALLING WEB SERVICE :-) HOPE IT WORKS !!!!! ---------------// 
  		//---------------  I HAVE SPLIT THE TOTAL FILES CALL TO 3 AND REST SO THAT I DON'T HAVE TO  ---------------// 
  		//---------------  GET INPUT UNNECESSARILY WHEN THE 1ST WEB SERVICE ITSELF CAN FAIL. ---------------//  
  		
  		
  		/*	Refresh Cache */
  		if(ui.getRefCache().equalsIgnoreCase("Y")){
  			doRefreshCache("refresh_cache.xml");
  		}
  		
  		//int i = 0;
  			createCustomer(listFiles[0]);
  			createAccount(listFiles[1]);
  			createBaseDeal(listFiles[2]);
  			
  			int i = 1;
  			
  			System.out.println("Do you want to purchase an option(Y/N): ");
  			flag_op = inf.nextLine();

  			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
  		        System.out.println("Please enter a valid value");
  		        flag_op = inf.nextLine();
  			}
  			
  			while(flag_op.equalsIgnoreCase("Y")){
  				
  				
  				createOption(listFiles[3]);
  				
  				
  				System.out.println("Do you want to purchase another option(Y/N): ");
  				flag_op = inf.nextLine();
  				if(flag_op.equalsIgnoreCase("Y")){
  					listFilesRef[0] = listFiles[3];
  				//	System.out.println(listFilesRef[0]);
  					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
  					++i;
  					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
  				}
  			
  			}
  		
  	}
	
	//added for M2M
	private void postpaidConsumerM2MAdd() throws IOException, DBException{
		// TODO Auto-generated method stub
		String[] listFiles =null;
		
        md.setTipo_MVNO(ui.getTipoMVNO());
		md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

		/*Update IMSI for Full MVNO*/
		if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
			md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
		}else{
			md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
		}
		
		md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
	
		md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
		
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		
		listFiles = fil.listFiles(templatePath);
		
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

		//---------------  CALLING WEB SERVICE :-) HOPE IT WORKS !!!!! ---------------// 
		//---------------  I HAVE SPLIT THE TOTAL FILES CALL TO 3 AND REST SO THAT I DON'T HAVE TO  ---------------// 
		//---------------  GET INPUT UNNECESSARILY WHEN THE 1ST WEB SERVICE ITSELF CAN FAIL. ---------------//  
		
		
		/*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}
		
		//int i = 0;
			createCustomer(listFiles[0]);
			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			int i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				
				createOption(listFiles[3]);
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
  		
  	}
	
	private boolean createCustomer(String file){
			
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =al.PROP_REQUEST_PATH +"/"+file;
		tmp_target_file =al.PROP_REQUEST_PATH +"/tmp_"+file;
			
		// Replace data into temp xml files
			
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
			
		System.out.println("...... Calling webservice for " + file);
		/*System.out.println("REQUEST PATH  :::"+al.PROP_REQUEST_PATH + "//" + listFiles[i]);
		System.out.println("RESPONSE PATH  :::"+al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
		System.out.println("PORTAL URL  :::"+al.WEB_SERVICE_URL);
		*/try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(file);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(file.equals("01_customer_create.xml")){
				System.out.println("____________ Customer Created: " + md.getCUSTOMER_CODE()  );
				return true;
			}
		}
		return false;
	}
	
	private void createAccount(String file){
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =al.PROP_REQUEST_PATH +"/"+file;
		tmp_target_file =al.PROP_REQUEST_PATH +"/tmp_"+file;
			
		// Replace data into temp xml files
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		//added for M2M
		/*if(Integer.parseInt(ui.accountType) == 14){
			//System.out.println("###"+md.getACCOUNT_CODE());
			//fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", "SUB_"+md.getROOT_INST_CODE());
		}else*/
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		
		System.out.println("...... Calling webservice for " + file);
		/*System.out.println("REQUEST PATH  :::"+al.PROP_REQUEST_PATH + "//" + listFiles[i]);
		System.out.println("RESPONSE PATH  :::"+al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
		System.out.println("PORTAL URL  :::"+al.WEB_SERVICE_URL);
		*/try {
			
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(file);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			 if(file.equals("02_account_create.xml")){
				 //added for M2M
				 if(Integer.parseInt(ui.accountType) == 14){
					 //System.out.println("____________ Account Created: " +"SUB_"+md.getROOT_INST_CODE()  );
					 System.out.println("____________ Account Created: " + md.getACCOUNT_CODE()  );
				 }else
				System.out.println("____________ Account Created: " + md.getACCOUNT_CODE()  );
			}
		}
	}
	
	private void createBaseDeal(String file){
		
		String target_file = null;
		String tmp_target_file = null;
		target_file =al.PROP_REQUEST_PATH +"/"+file;
		tmp_target_file =al.PROP_REQUEST_PATH +"/tmp_"+file;
		// Replace data into temp xml files
			//System.out.println("dddddddddddddddd");
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		//added for M2M
		/*if(Integer.parseInt(ui.accountType) == 14){
			//fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", "SUB_"+md.getROOT_INST_CODE());
			System.out.println("@@@@@@"+md.getACCOUNT_CODE());
		}else*/
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE());			
		fil.replace(target_file,  tmp_target_file,"SW_MSISDN_XXXX", md.getMSISDN_CODE());
		
		fil.replace(target_file,  tmp_target_file,"SW_IMSI_XXXX", md.getIMSI_CODE());
		
		fil.replace(target_file,  tmp_target_file,"SW_BASE_TARIFF_XXXX", md.getBASE_TARIFF_CODE());
			
		fil.replace(target_file,  tmp_target_file,"SW_MVNO_XXXX", md.getTipo_MVNO());
		
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		
		//added
		if(Integer.parseInt(ui.accountType) == 13){
		fil.replace(target_file,  tmp_target_file,"SW_Numero_Portato_XXXX", md.getNumero_Portato());
		fil.replace(target_file,  tmp_target_file,"SW_Numero_Temporaneo_XXXX", md.getNumero_Temporaneo());
		fil.replace(target_file,  tmp_target_file,"SW_Codice_Migrazione_GNP_XXXX", md.getCodice_Migrazione_GNP());
		fil.replace(target_file,  tmp_target_file,"SW_Data_Attivazione_Linea_XXXX", md.getData_Attivazione_Linea());
		fil.replace(target_file,  tmp_target_file,"SW_Data_Attivazione_GNP_XXXX", md.getData_Attivazione_GNP());
		fil.replace(target_file,  tmp_target_file,"SW_Tipologia_Terminale_XXXX", md.getTipologia_Terminale());
		}
		
		
		System.out.println("...... Calling webservice for " + file);
		/*System.out.println("REQUEST PATH  :::"+al.PROP_REQUEST_PATH + "//" + listFiles[i]);
		System.out.println("RESPONSE PATH  :::"+al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
		System.out.println("PORTAL URL  :::"+al.WEB_SERVICE_URL);
		*/try {
			//System.out.println("zzzzzzzzzzz");
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
			//System.out.println("ggggggggggggg");
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
		
		//System.out.println("***"+al.PROP_RESPONSE_PATH);
		
		
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(file);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(file.equals("03_base_create.xml")){
				System.out.println("Base Deal created with : ");
				System.out.println("____________ RootInstId: " + md.getROOT_INST_CODE() );
				System.out.println("____________ MSISDN: " + md.getMSISDN_CODE());
				System.out.println("____________ IMSI: " + md.getIMSI_CODE());
			}
		}
	}
	
	
	private void doRefill(String file){
		
		if(file.contains("refill.xml")){
			ui.get_user_input_refil(al.PROP_REQUEST_PATH, file, fil, md);
		}
		
		
		System.out.println("...... Calling webservice for " + file);
		try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(file);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(file.equals("04_refill.xml")){
				System.out.println("____________ Refil done for " + md.getREF_AMT_CODE() + " with TransID: " + md.getREF_TRANS_CODE());
			}
		}
	}
	
 private void createOption(String file) throws DBException{
	 String target_file = null;
		String tmp_target_file = null;
		
		target_file =al.PROP_REQUEST_PATH +"/"+file;
		tmp_target_file =al.PROP_REQUEST_PATH +"/tmp_"+file;
		
	    if(file.contains("option_create.xml")){
			ui.get_user_input_option(al.PROP_REQUEST_PATH, file, fil, md, db);	
			
		}
	    
	   //added
	    if(Integer.parseInt(ui.accountType) == 13){
	    fil.replace(target_file,  tmp_target_file,"SW_Data_Attivazione_Canone_XXXX", md.getData_Attivazione_Canone());
	    }
	    //added for M2M
	    if(Integer.parseInt(ui.accountType) == 14){
	    	/*//md.setTipo_MVNO(ui.getTipoMVNO());
	    	//fil.replace(target_file,  tmp_target_file,"SW_MVNO_XXXX", md.getTipo_MVNO());
	    	//System.out.println("***"+md.getTipo_MVNO());*/
	    	fil.replace(target_file,  tmp_target_file,"SW_MSISDN_XXXX", md.getMSISDN_CODE());
	    }	    
	    System.out.println("-------------------");
	    
		System.out.println("...... Calling webservice for " + file);
		try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(file);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(file.equals("05_option_create.xml")){
				System.out.println("____________ Option Created for " + md.getOPTION_CATALOGUE_CODE() +" for Product Instance Id : "+ md.getPROD_INST_CODE());
			}
		}
	}
 
  private boolean createCustomerDual(String file){
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =al.PROP_REQUEST_PATH +"/"+file;
		tmp_target_file =al.PROP_REQUEST_PATH +"/tmp_"+file;
			
		// Replace data into temp xml files
			
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_D_XXXX", md.getCUSTOMER_CODE_DUAL());
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
			
		System.out.println("...... Calling webservice for " + file);
		/*System.out.println("REQUEST PATH  :::"+al.PROP_REQUEST_PATH + "//" + listFiles[i]);
		System.out.println("RESPONSE PATH  :::"+al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
		System.out.println("PORTAL URL  :::"+AcsLabels.WEB_SERVICE_URL);
		*/try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(file);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(file.equals("06_customer_create_dual.xml")){
				System.out.println("____________ Customer Created for personal line: " + md.getCUSTOMER_CODE_DUAL() );
				return true;
			}
		}
		return false;
	}
 
 	private void createAccountDual(String file){
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =al.PROP_REQUEST_PATH +"/"+file;
		tmp_target_file =al.PROP_REQUEST_PATH +"/tmp_"+file;
			
		// Replace data into temp xml files
			
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_D_XXXX", md.getCUSTOMER_CODE_DUAL());
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_D_XXXX", md.getACCOUNT_CODE_DUAL());
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		
		System.out.println("...... Calling webservice for " + file);
		/*System.out.println("REQUEST PATH  :::"+al.PROP_REQUEST_PATH + "//" + listFiles[i]);
		System.out.println("RESPONSE PATH  :::"+al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
		System.out.println("PORTAL URL  :::"+al.WEB_SERVICE_URL);
		*/try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(file);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			 if(file.equals("07_account_create_dual.xml")){
				System.out.println("____________ Account Created: " + md.getACCOUNT_CODE_DUAL() );
			}
		}
	}
 	
 	private void createOptionDual(String file){
		
		String target_file = null;
		String tmp_target_file = null;
		
		target_file =al.PROP_REQUEST_PATH +"/"+file;
		tmp_target_file =al.PROP_REQUEST_PATH +"/tmp_"+file;
			
		// Replace data into temp xml files
			
		fil.replace(target_file,  tmp_target_file,"SW_CUST_AUTO_XXXX", md.getCUSTOMER_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_XXXX", md.getACCOUNT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ROOTINST_AUTO_XXXX", md.getROOT_INST_CODE());			
		/*fil.replace(target_file,  tmp_target_file,"SW_MSISDN_XXXX", md.getMSISDN_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_IMSI_XXXX", md.getIMSI_CODE());*/
		fil.replace(target_file,  tmp_target_file,"SW_BASE_TARIFF_XXXX", md.getBASE_TARIFF_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_PVT_XXX", md.getPVT_CODE());
		fil.replace(target_file,  tmp_target_file,"SW_ACCT_AUTO_D_XXXX", md.getACCOUNT_CODE_DUAL());
		fil.replace(target_file,  tmp_target_file,"SW_PRODINST_AUTO_XXXX", md.getPROD_INST_CODE());
		
		System.out.println("...... Calling webservice for " + file);
		/*System.out.println("REQUEST PATH  :::"+al.PROP_REQUEST_PATH + "//" + listFiles[i]);
		System.out.println("RESPONSE PATH  :::"+al.PROP_RESPONSE_PATH + "//" + listFiles[i]);
		System.out.println("PORTAL URL  :::"+al.WEB_SERVICE_URL);
		*/try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(file);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(file.equals("08_option_create_dual.xml")){
				System.out.println("____________ Option purchased on personal line with Product Instanc Id : " + md.getPROD_INST_CODE());
			}
		}
	}
 	
 	
 	private void prepaidConsumerUpgrade(String updateType) throws IOException, DBException{
 		
 		String[] listFiles =null;
 		int i = 1;
 		
 		if(updateType.equals("1")){
 			
 			System.out.println("Please enter the customer code : ");
 			
 			ui.getCustomerCodeUpdate(md);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();

			while(!(flag_re.equalsIgnoreCase("y") || flag_re.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_re = inf.nextLine();
			}
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("2")){
 			
 			ui.get_input_base_upgrade(md, db);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			createBaseDeal(listFiles[2]);
			
			System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();

			while(!(flag_re.equalsIgnoreCase("y") || flag_re.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_re = inf.nextLine();
			}
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("3")){
 			
 			System.out.println("Please enter the account code : ");
 			
 			ui.getAccountCodeUpdate(md);
 			
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			flag_re = "Y";
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			
			while(flag_op.equalsIgnoreCase("Y")){
				md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
				ui.get_input_option_upgrade(md, db);
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("4")){
 			
 			System.out.println("Please enter the account code : ");
			ui.getAccountCodeUpdate(md);
	
 			ui.get_input_option_upgrade(md, db);
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			//md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			i = 1;
			
			flag_op = "Y";
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}
 	}
 	
 	private void prepaidSoholpUpgrade(String updateType) throws IOException, DBException{
 		
 		String[] listFiles =null;
 		int i = 1;
 		
 		if(updateType.equals("1")){
 			
 			System.out.println("Please enter the customer code : ");
 			
 			ui.getCustomerCodeUpdate(md);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
 			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();

			while(!(flag_re.equalsIgnoreCase("y") || flag_re.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_re = inf.nextLine();
			}
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("2")){
 			
 			ui.get_input_base_upgrade(md, db);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			 
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			createBaseDeal(listFiles[2]);
			
			System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();

			while(!(flag_re.equalsIgnoreCase("y") || flag_re.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_re = inf.nextLine();
			}
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("3")){
 			
 			System.out.println("Please enter the account code : ");
 			
 			ui.getAccountCodeUpdate(md);
 			
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

			flag_re = "Y";
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			
			while(flag_op.equalsIgnoreCase("Y")){
				md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
				ui.get_input_option_upgrade(md, db);
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("4")){
	
 			System.out.println("Please enter the account code : ");
			ui.getAccountCodeUpdate(md);
 			
 			ui.get_input_option_upgrade(md, db);
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			//md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			i = 1;
			
			flag_op = "Y";
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}
 	}

 	
 	private void prepaidBusinessUpgrade(String updateType) throws IOException, DBException{
 		
 		String[] listFiles =null;
 		int i = 1;
 		
 		if(updateType.equals("1")){
 			
 			System.out.println("Please enter the customer code : ");
 			
 			ui.getCustomerCodeUpdate(md);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			db.getMarketSegment(md.getCUSTOMER_CODE(), md, al);
 			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
 			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();

			while(!(flag_re.equalsIgnoreCase("y") || flag_re.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_re = inf.nextLine();
			}
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("2")){
 			
 			ui.get_input_base_upgrade(md, db);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			createBaseDeal(listFiles[2]);
			
			System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();

			while(!(flag_re.equalsIgnoreCase("y") || flag_re.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_re = inf.nextLine();
			}
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("3")){
 			
 			System.out.println("Please enter the account code : ");
 			
 			ui.getAccountCodeUpdate(md);
 			
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

			flag_re = "Y";
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			
			while(flag_op.equalsIgnoreCase("Y")){
				md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
				ui.get_input_option_upgrade(md, db);
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("4")){
	
 			System.out.println("Please enter the account code : ");
			ui.getAccountCodeUpdate(md);
 			
 			ui.get_input_option_upgrade(md, db);
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			//md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
			i = 1;
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			flag_op = "Y";
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}
 	}
 	
 	
 	private void postpaidConsumerUpgrade(String updateType) throws IOException, DBException{
 		
 		String[] listFiles =null;
 		int i = 1;
 		
 		if(updateType.equals("1")){
 			
 			System.out.println("Please enter the customer code : ");
 			
 			ui.getCustomerCodeUpdate(md);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			 
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
 			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			/*System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}*/
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("2")){
 			
 			ui.get_input_base_upgrade(md, db);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			createBaseDeal(listFiles[2]);
			
			/*System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}*/
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("3")){
 			
 			System.out.println("Sorry.. Refill cannot be done on a postpaid account..");
 			
 			/*System.out.println("Please enter the account code : ");
 			
 			ui.getAccountCodeUpdate(md);
 			
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

			flag_re = "Y";
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_EPOCHTIME() + "R_" +i);
				}
			
			}*/
 		}else if(updateType.equals("4")){
 			
 			System.out.println("Please enter the account code : ");
			ui.getAccountCodeUpdate(md);
	
 			ui.get_input_option_upgrade(md, db);
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			//md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
			i = 1;
			
			flag_op = "Y";
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}
 	}
 	
 	
 	private void postpaidSoholpUpgrade(String updateType) throws IOException, DBException{
 		
 		String[] listFiles =null;
 		int i = 1;
 		
 		if(updateType.equals("1")){
 			
 			System.out.println("Please enter the customer code : ");
 			
 			ui.getCustomerCodeUpdate(md);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
 			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
		/*	System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}*/
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("2")){
 			
 			ui.get_input_base_upgrade(md, db);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			createBaseDeal(listFiles[2]);
			
			/*System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}*/
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("3")){
 			
 			System.out.println("Sorry.. Refill cannot be done on a postpaid account..");
 			
 			/*System.out.println("Please enter the account code : ");
 			
 			ui.getAccountCodeUpdate(md);
 			
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

			flag_re = "Y";
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_EPOCHTIME() + "R_" +i);
				}
			
			}*/
 		}else if(updateType.equals("4")){
	
 			System.out.println("Please enter the account code : ");
			ui.getAccountCodeUpdate(md);
 			
 			ui.get_input_option_upgrade(md, db);
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			//md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
			i = 1;
			
			flag_op = "Y";
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}
 	}
 	
 	
 	private void postpaidBusinessUpgrade(String updateType) throws IOException, DBException{
 		
 		String[] listFiles =null;
 		int i = 1;
 		
 		if(updateType.equals("1")){
 			
 			System.out.println("Please enter the customer code : ");
 			
 			ui.getCustomerCodeUpdate(md);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			 
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			db.getMarketSegment(md.getCUSTOMER_CODE(), md, al);
 			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
 			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
		/*	
			System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}*/
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("2")){
 			
 			ui.get_input_base_upgrade(md, db);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			createBaseDeal(listFiles[2]);
			
			/*System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			*/
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("3")){
 			
 			System.out.println("Sorry.. Refill cannot be done on a postpaid account..");
 			
 			/*System.out.println("Please enter the account code : ");
 			
 			ui.getAccountCodeUpdate(md);
 			
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

			flag_re = "Y";
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_EPOCHTIME() + "R_" +i);
				}
			
			}*/
 		}else if(updateType.equals("4")){
	
 			System.out.println("Please enter the account code : ");
			ui.getAccountCodeUpdate(md);
		
 			ui.get_input_option_upgrade(md, db);
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			//md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
			i = 1;
			
			flag_op = "Y";
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}
 	}
 	//added
private void postpaidConsumerSinglePlayUpgrade(String updateType) throws IOException, DBException{
 		
 		String[] listFiles =null;
 		int i = 1;
 		
 		//templatePath = al.xmlTemplate.get("13");
          if(updateType.equals("1")){
 			
 			System.out.println("Please enter the customer code : ");
 			
 			ui.getCustomerCodeUpdate(md);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			 
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			//added
 			md.setNumero_Portato(ui.getNumeroPortato());
 	  		md.setNumero_Temporaneo(ui.getNumeroTemporaneo());
 	  		md.setCodice_Migrazione_GNP(ui.getCodiceMigrazioneGNP());
 	  		md.setData_Attivazione_Linea(ui.getDataAttivazioneLinea());
 	  		md.setData_Attivazione_GNP(ui.getDataAttivazioneGNP());
 	  		md.setTipologia_Terminale(ui.getTipologiaTerminale());
 	  		
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
 			createAccount(listFiles[1]);
			createBaseDeal(listFiles[2]);
			
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("2")){
 			
 			ui.get_input_base_upgrade(md, db);
 			
 			md.setTipo_MVNO(ui.getTipoMVNO());
 			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

 			/*Update IMSI for Full MVNO*/
 			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
 				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
 			}else{
 				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
 			}
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			
 			//added
 			md.setNumero_Portato(ui.getNumeroPortato());
 	  		md.setNumero_Temporaneo(ui.getNumeroTemporaneo());
 	  		md.setCodice_Migrazione_GNP(ui.getCodiceMigrazioneGNP());
 	  		md.setData_Attivazione_Linea(ui.getDataAttivazioneLinea());
 	  		md.setData_Attivazione_GNP(ui.getDataAttivazioneGNP());
 	  		md.setTipologia_Terminale(ui.getTipologiaTerminale());
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			/*	Refresh Cache */
 			if(ui.getRefCache().equalsIgnoreCase("Y")){
 				doRefreshCache("refresh_cache.xml");
 			}	
 			
			createBaseDeal(listFiles[2]);
			
			/*System.out.println("");
			System.out.println("Do you want to do a refill(Y/N): ");
			flag_re = inf.nextLine();
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}*/
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();

			while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
		        System.out.println("Please enter a valid value");
		        flag_op = inf.nextLine();
			}
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}else if(updateType.equals("3")){
 			
 			System.out.println("Sorry.. Refill cannot be done on a postpaid account..");
 			
 			/*System.out.println("Please enter the account code : ");
 			
 			ui.getAccountCodeUpdate(md);
 			
 			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

			flag_re = "Y";
			
			while(flag_re.equalsIgnoreCase("Y")){
				doRefill(listFiles[3]);
				
				System.out.println("Do you want to do another refill(Y/N): ");
				flag_re = inf.nextLine();
				if(flag_re.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID()+"_"+i);
					++i;
				}
			}
			
			i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[4]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[4];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_EPOCHTIME() + "R_" +i);
				}
			
			}*/
 		}else if(updateType.equals("4")){
 			
 			System.out.println("Please enter the account code : ");
			ui.getAccountCodeUpdate(md);
	
 			ui.get_input_option_upgrade(md, db);
 			
 			listFiles = fil.listFiles(templatePath);
 			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 			
 			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
 			//md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
 			
			i = 1;
			
			flag_op = "Y";
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
				}
			
			}
 		}
 	}


//added for M2M
private void postpaidConsumerM2MUpgrade(String updateType) throws IOException, DBException{
		
		String[] listFiles =null;
		int i = 1;
		
      if(updateType.equals("1")){
			
			System.out.println("Please enter the customer code : ");
			
			ui.getCustomerCodeUpdate(md);
			
			md.setTipo_MVNO(ui.getTipoMVNO());
			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

			/*Update IMSI for Full MVNO*/
			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
			}else{
				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
			}
			 
			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
	  		
			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
			
			listFiles = fil.listFiles(templatePath);
			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			createAccount(listFiles[1]);
		createBaseDeal(listFiles[2]);
		i = 1;
		
		System.out.println("Do you want to purchase an option(Y/N): ");
		flag_op = inf.nextLine();

		while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
	        System.out.println("Please enter a valid value");
	        flag_op = inf.nextLine();
		}
		
		while(flag_op.equalsIgnoreCase("Y")){
			createOption(listFiles[3]);
			
			System.out.println("Do you want to purchase another option(Y/N): ");
			flag_op = inf.nextLine();
			if(flag_op.equalsIgnoreCase("Y")){
				listFilesRef[0] = listFiles[3];
			//	System.out.println(listFilesRef[0]);
				fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
				++i;
				md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
			}
		
		}
		}else if(updateType.equals("2")){
			
			ui.get_input_base_upgrade(md, db);
			
			md.setTipo_MVNO(ui.getTipoMVNO());
			md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 

			/*Update IMSI for Full MVNO*/
			if(md.getTipo_MVNO().equalsIgnoreCase("ESP") || md.getTipo_MVNO().equalsIgnoreCase("")){
				md.setIMSI_CODE("22210" + (md.getMSISDN_CODE()).substring(2));
			}else{
				md.setIMSI_CODE("22211" + (md.getMSISDN_CODE()).substring(2));	
			}
			
			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
			
			md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
			
			listFiles = fil.listFiles(templatePath);
			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
		createBaseDeal(listFiles[2]);
		
		i = 1;
		
		System.out.println("Do you want to purchase an option(Y/N): ");
		flag_op = inf.nextLine();

		while(!(flag_op.equalsIgnoreCase("y") || flag_op.equalsIgnoreCase("n"))){
	        System.out.println("Please enter a valid value");
	        flag_op = inf.nextLine();
		}
		
		while(flag_op.equalsIgnoreCase("Y")){
			createOption(listFiles[3]);
			
			System.out.println("Do you want to purchase another option(Y/N): ");
			flag_op = inf.nextLine();
			if(flag_op.equalsIgnoreCase("Y")){
				listFilesRef[0] = listFiles[3];
			//	System.out.println(listFilesRef[0]);
				fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
				++i;
				md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
			}
		
		}
		}else if(updateType.equals("3")){
			
			System.out.println("Sorry.. Refill cannot be done on a postpaid account..");
			
			
		}else if(updateType.equals("4")){
			
			System.out.println("Please enter the account code : ");
		ui.getAccountCodeUpdate(md);

			ui.get_input_option_upgrade(md, db);
			
			listFiles = fil.listFiles(templatePath);
			fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
			
			md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
			
			//md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
		i = 1;
		flag_op = "Y";
		/*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}	
		while(flag_op.equalsIgnoreCase("Y")){
			createOption(listFiles[3]);
			System.out.println("Do you want to purchase another option(Y/N): ");
			flag_op = inf.nextLine();
			if(flag_op.equalsIgnoreCase("Y")){
				listFilesRef[0] = listFiles[3];
			//	System.out.println(listFilesRef[0]);
				fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
				++i;
				md.setPROD_INST_CODE(md.getAUTO_NUMBER() + "R_" +i);
			}
		
		}
		}
	}

 	private void optionDelete() throws DBException, IOException{
		
 		String[] listFiles =null;
		
 		templatePath = al.xmlTemplate.get("10");
 		
 		//System.out.println(templatePath);
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		listFiles = fil.listFiles(templatePath);
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

	    if(listFiles[0].contains("option_delete.xml")){
		ui.get_user_input_option_delete(al.PROP_REQUEST_PATH, listFiles[0], fil, md, db);				
		}
	    
	    /*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}	
		
		System.out.println("...... Calling webservice for " + listFiles[0]);
		try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + listFiles[0] , al.PROP_RESPONSE_PATH + "//" + listFiles[0]);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + listFiles[0]);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(listFiles[0]);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(listFiles[0].equals("01_option_delete.xml")){
				System.out.println("____________ Option Deleted for " + md.getOPTION_CATALOGUE_CODE() +" for Product Instance Id : "+ md.getPROD_INST_CODE());
			}
		}
	}
 	
 	
 	private void postpaidHandsetAdd() throws IOException, DBException {
		// TODO Auto-generated method stub
		String[] listFiles =null;
		
		// SETTING VARIABLES VALUE
		//md.setPROP_TEMPLATE_ID("PRC");
		
		// GET INPUT FROM USER FR 1st THREE PROCESS
	//	md.setMSISDN_CODE(ui.getMsisdnNo("39" + md.getAUTO_EPOCHTIME(), db)); 
	//	md.setIMSI_CODE("222" + md.getMSISDN_CODE()); 
		md.setBASE_TARIFF_CODE(ui.getBaseTariffCode());
	//	md.setREF_TRANS_CODE(md.getAUTO_EPOCHTIME() + "_TR_"  + md.getPROP_TEMPLATE_ID());
		
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		listFiles = fil.listFiles(templatePath);
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);

			ui.getMarketSegmentInfo(al.PROP_REQUEST_PATH, listFiles[0], fil, md, al);
			
			/*	Refresh Cache */
			if(ui.getRefCache().equalsIgnoreCase("Y")){
				doRefreshCache("refresh_cache.xml");
			}	
			
			createCustomer(listFiles[0]);
			
			ui.getCreditClassInfo(al.PROP_REQUEST_PATH, listFiles[1], fil, md, al);
			createAccount(listFiles[1]);
			purchaseOption1(listFiles[2]);
			purchaseOption2(listFiles[3]);
			purchaseOption3(listFiles[4]);
		/*
			int i = 1;
			
			System.out.println("Do you want to purchase an option(Y/N): ");
			flag_op = inf.nextLine();
			
			while(flag_op.equalsIgnoreCase("Y")){
				createOption(listFiles[3]);
				
				System.out.println("Do you want to purchase another option(Y/N): ");
				flag_op = inf.nextLine();
				if(flag_op.equalsIgnoreCase("Y")){
					listFilesRef[0] = listFiles[3];
				//	System.out.println(listFilesRef[0]);
					fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFilesRef, md);
					++i;
					md.setPROD_INST_CODE(md.getAUTO_EPOCHTIME() + "R_" +i);
				}
			
			}*/
	}
 	
 	
 	private void optionUpdate() throws DBException, IOException{
		
 		String[] listFiles =null;
		
 		System.out.println("--------------------------------------------------------------------");
 		templatePath = al.xmlTemplate.get("11");
 		//System.out.println(templatePath);
 		//added1
 		String target_file = null;
		String tmp_target_file = null;
		String file = null;
		target_file =al.PROP_REQUEST_PATH +"/"+file;
		tmp_target_file =al.PROP_REQUEST_PATH +"/tmp_"+file;
 		
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		listFiles = fil.listFiles(templatePath);
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
		//added1
		/*if(Integer.parseInt(ui.accountType) == 14){
			System.out.println("kkkkkkkkkkkkkkkkkkkkkkk");
			fil.replace(target_file,  tmp_target_file,"SW_MSISDN_XXXX", md.getMSISDN_CODE());
		}*/
	    if(listFiles[0].contains("option_update.xml")){
			ui.get_user_input_option_update(al.PROP_REQUEST_PATH, listFiles[0], fil, md, db);				
		}
		
	    /*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}	
		System.out.println("--***********************************");
		System.out.println("...... Calling webservice for " + listFiles[0]);
		try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + listFiles[0] , al.PROP_RESPONSE_PATH + "//" + listFiles[0]);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + listFiles[0]);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(listFiles[0]);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(listFiles[0].equals("01_option_update.xml")){
				System.out.println("____________ Option Updated for " + md.getOPTION_CATALOGUE_CODE() +" for Product Instance Id : "+ md.getPROD_INST_CODE());
			}
		}
	}
 	
 	 private void purchaseOption1(String file) throws DBException{
 		
 	    if(file.contains("base_create_1.xml")){
 			ui.get_user_input_handset_1(al.PROP_REQUEST_PATH, file, fil, md, db);				
 		}
 		
 		
 		System.out.println("...... Calling webservice for " + file);
 		try {
 			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
 			
 		} catch (SOAPException e) {
 			System.out.println("Error calling Web Service..");
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
 		
 		//---------------  IF WEBSERVICE FAILS . ---------------//
 		
 		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
 			System.out.println("............. Error: "); 
			getErrorDescription(file);
 			System.exit(0);
 		}
 		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
 			if(file.equals("03_base_create_1.xml")){
 				System.out.println("____________ Option Created for " + md.getOPTION_CATALOGUE_CODE() +" for Product Instance Id : "+ md.getPROD_INST_CODE());
 			}
 		}
 	}
 	 
 	 private void purchaseOption2(String file) throws DBException{
 		
 	    if(file.contains("base_create_2.xml")){
 			ui.get_user_input_handset_2(al.PROP_REQUEST_PATH, file, fil, md, db);				
 		}
 		
 		
 		System.out.println("...... Calling webservice for " + file);
 		try {
 			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
 			
 		} catch (SOAPException e) {
 			System.out.println("Error calling Web Service..");
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
 		
 		//---------------  IF WEBSERVICE FAILS . ---------------//
 		
 		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
 			System.out.println("............. Error: "); 
			getErrorDescription(file);
 			System.exit(0);
 		}
 		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
 			if(file.equals("04_base_create_2.xml")){
 				System.out.println("____________ Option Created for " + md.getOPTION_CATALOGUE_CODE() +" for Product Instance Id : "+ md.getPROD_INST_CODE());
 			}
 		}
 	}
 	 
 	 private void purchaseOption3(String file) throws DBException{
 		
 	    if(file.contains("base_create_3.xml")){
 			ui.get_user_input_handset_3(al.PROP_REQUEST_PATH, file, fil, md, db);				
 		}
 		
 		
 		System.out.println("...... Calling webservice for " + file);
 		try {
 			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
 			
 		} catch (SOAPException e) {
 			System.out.println("Error calling Web Service..");
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
 		
 		//---------------  IF WEBSERVICE FAILS . ---------------//
 		
 		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
 			System.out.println("............. Error: "); 
			getErrorDescription(file);
 			System.exit(0);
 		}
 		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
 			if(file.equals("05_base_create_3.xml")){
 				System.out.println("____________ Option Created for " + md.getOPTION_CATALOGUE_CODE() +" for Product Instance Id : "+ md.getPROD_INST_CODE());
 			}
 		}
 	}
 	 
 	 public void getErrorDescription(String file){ 		

 		/*System.out.println("Mike testing 123 " + file );
 		String response_path = al.PROP_RESPONSE_PATH + "//" + file;
 		
 		response_path = "response//1381147717//03_base_create.xml";
 		System.out.println("Mike testing 1234 " + al.PROP_RESPONSE_PATH + "//" + file );
 		 
 	//	System.out.println(response_path);
        Document document = null;
        SAXReader reader = new SAXReader();
        reader.setMergeAdjacentText(true);
        try {
        	
			document = reader.read(response_path);
			
		} catch (DocumentException e) {
			System.out.println("ERROR BOSSSSS ");
			e.printStackTrace();
		}

        System.out.println("STARTING 1");
        String xpathVal="//soapenv:Body";
        XPath xpath = DocumentHelper.createXPath(xpathVal);
      System.out.println("xpath" + xpath);
        List<Element> elements = xpath.selectNodes(document);

      
        for (Element e : elements)
        {
        	 System.out.println("God hel plsp");
               // System.out.println("..... Error " + e.getText());
        	// System.out.println("..... " + 
        }
        
       //Node node = xpath.selectSingleNode(document);
       //System.out.println(node);
       //System.out.println("....Error" + node.getStringValue());
*/ 		
 		
 		/*FileReader fr;
		try {
			fr = new FileReader(respFile);
			BufferedReader br = new BufferedReader(fr); 
			String s; 
			while((s = br.readLine()) != null) { 
				if( (s.startsWith("<java:ErrorDescription>")) || (s.startsWith("<java:ErrorCode>")))
				System.out.println(s); 
			} 
			fr.close(); 
 		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}  catch (IOException e) {
			e.printStackTrace();
		} 
*/ 	
 		
 		try {
 			 
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser saxParser = factory.newSAXParser();
 		 
 			DefaultHandler handler = new DefaultHandler() {
 		 
 			boolean eCode = false;
 			boolean eDescr = false;
 		 
 			public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
 		 
 				//System.out.println("Start Element :" + qName);
 		 
 				if (qName.equalsIgnoreCase("java:ErrorCode")) {
 					eCode = true;
 				}
 		 
 				if (qName.equalsIgnoreCase("java:ErrorDescription")) {
 					eDescr = true;
 				}
 		 
 		 
 			}
 		 
 			public void endElement(String uri, String localName,
 				String qName) throws SAXException {
 		 
 				//System.out.println("End Element :" + qName);
 				if (qName.equalsIgnoreCase("java:ErrorDescription")) {
 					System.exit(0);
 				}

 		 
 			}
 		 
 			public void characters(char ch[], int start, int length) throws SAXException {
 		 
 				//System.out.println("TESTINGGGGGGGG " + new String(ch, start, length));

 				if (eCode) {
 					System.out.println("ERROR CODE IS : " + new String(ch, start, length));
 					eCode = false;
 				}
 		 
 				if (eDescr) {
 					System.out.println("ERROR DESC IS : " + new String(ch, start, length));
 					eDescr = false;
 				}
 		 
 			}
 		 
 		     };
 		 
 		       saxParser.parse(al.PROP_RESPONSE_PATH + "//" + file, handler);
 		 
 		     } catch (Exception e) {
 		       e.printStackTrace();
 		     }
 		 
 	}
 	
 	private void doRefreshCache(String file){
		
		System.out.println("...... Calling webservice for " + file);
		try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + file , al.PROP_RESPONSE_PATH + "//" + file);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + file);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(file);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(file.equals("refresh_cache.xml")){
				System.out.println("____________ Refresh Cache done!!");
			}
		}
	}
 	
 	
 	private void adjustment() throws DBException, IOException{
		
 		String[] listFiles =null;
		
 		templatePath = al.xmlTemplate.get("12");
 		
		//GET LIST OF FILES AND COPY TEMPLATE FILES TO REQUEST FOLDER
		listFiles = fil.listFiles(templatePath);
		fil.copyFile(templatePath, al.PROP_REQUEST_PATH, listFiles, md);
 		
	    if(listFiles[0].contains("adjustment.xml")){
			ui.get_user_input_adjustment(al.PROP_REQUEST_PATH, listFiles[0], fil, md, db);				
		}
		
	    /*	Refresh Cache */
		if(ui.getRefCache().equalsIgnoreCase("Y")){
			doRefreshCache("refresh_cache.xml");
		}	
		
		System.out.println("...... Calling webservice for " + listFiles[0]);
		try {
			web.callWebservice(AcsLabels.WEB_SERVICE_URL, al.PROP_REQUEST_PATH + "//" + listFiles[0] , al.PROP_RESPONSE_PATH + "//" + listFiles[0]);
			
		} catch (SOAPException e) {
			System.out.println("Error calling Web Service..");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File response_file = new File(al.PROP_RESPONSE_PATH + "//" + listFiles[0]);
		
		//---------------  IF WEBSERVICE FAILS . ---------------//
		
		if(FileManager.find(response_file, "<java:StatusCode>0</java:StatusCode>") == false ){
			System.out.println("............. Error: "); 
			getErrorDescription(listFiles[0]);
			System.exit(0);
		}
		else{ //---------------  IF WEBSERVICE SUCCEEDS . ---------------//
			if(listFiles[0].equals("01_adjustment.xml")){
				System.out.println("____________ Adjustment done for reason " + md.getREA_REQ() + " on account " + md.getACCOUNT_CODE());
			}
		}
	}
 	
 	
}
